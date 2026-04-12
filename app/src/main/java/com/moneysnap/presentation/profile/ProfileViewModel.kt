package com.moneysnap.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.AuthRepositoryImpl
import com.moneysnap.data.repository.TransactionRepositoryImpl
import com.moneysnap.data.repository.UserStatsRepositoryImpl
import com.moneysnap.domain.model.UserStats
import com.moneysnap.domain.repository.AuthRepository
import com.moneysnap.domain.repository.TransactionRepository
import com.moneysnap.domain.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val avatarId: String? = null,
    val totalSavings: String = "$0.00",
    val transactionCount: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val importResultMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userStatsRepository: UserStatsRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: com.moneysnap.domain.repository.CategoryRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "alex.thompson@example.com"
        val name = user?.displayName?.ifEmpty { "Alex Thompson" } ?: "Alex Thompson"
        val avatarId = user?.photoUrl?.toString()

        _uiState.update { it.copy(email = email, name = name, avatarId = avatarId) }

        viewModelScope.launch {
            userStatsRepository.getUserStatsStream().collect { stats ->
                val statsObj = stats ?: UserStats()
                val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
                    maximumFractionDigits = 0
                }
                val savings = formatter.format(statsObj.totalBalance)
                _uiState.update { it.copy(totalSavings = savings) }
            }
        }

        viewModelScope.launch {
            transactionRepository.getTransactions().collect { list ->
                _uiState.update { it.copy(transactionCount = list.size) }
            }
        }
    }

    fun updateAvatar(avatarId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        
        val request = UserProfileChangeRequest.Builder()
            .setPhotoUri(android.net.Uri.parse(avatarId))
            .build()
            
        user.updateProfile(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.update { it.copy(avatarId = avatarId) }
                // Persist to Firestore
                val userId = user.uid
                viewModelScope.launch {
                    try {
                        firestoreService.updateUserAvatar(userId, avatarId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun updateName(newName: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
            
        user.updateProfile(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.update { it.copy(name = newName) }
                // Persist to Firestore
                val userId = user.uid
                viewModelScope.launch {
                    try {
                        firestoreService.updateUserName(userId, newName)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun exportToExcel(context: Context, onExportReady: (java.io.File) -> Unit) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val transactions = transactionRepository.getTransactions().first()
                val categories = categoryRepository.getCategories().first()
                val file = com.moneysnap.util.ExcelExporter.exportTransactionsToExcel(context, transactions, categories)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onExportReady(file)
                }
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun importFromExcel(context: Context, uri: android.net.Uri) {
        _uiState.update { it.copy(isImporting = true, importResultMessage = null) }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val categories = categoryRepository.getCategories().first()
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val (transactions, result) = com.moneysnap.util.ExcelImporter.importTransactionsFromExcel(
                    context, uri, categories, userId
                )

                // Save all imported transactions
                for (transaction in transactions) {
                    transactionRepository.saveTransaction(transaction)
                }

                // Recalculate user stats after import
                if (transactions.isNotEmpty()) {
                    val allTransactions = transactionRepository.getTransactions().first()
                    val totalIncome = allTransactions
                        .filter { it.type == com.moneysnap.domain.model.TransactionType.INCOME }
                        .sumOf { it.amount }
                    val totalExpense = allTransactions
                        .filter { it.type == com.moneysnap.domain.model.TransactionType.EXPENSE }
                        .sumOf { it.amount }
                    val stats = com.moneysnap.domain.model.UserStats(
                        userId = userId,
                        totalBalance = totalIncome - totalExpense,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        lastUpdated = System.currentTimeMillis()
                    )
                    userStatsRepository.updateUserStats(stats)
                }

                val message = if (result.errors.isEmpty()) {
                    "Imported ${result.importedCount} transactions successfully"
                } else {
                    "Imported ${result.importedCount}, skipped ${result.skippedCount}"
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.update { it.copy(isImporting = false, importResultMessage = message) }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.update { it.copy(isImporting = false, importResultMessage = "Import failed: ${e.message}") }
                }
            }
        }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResultMessage = null) }
    }

    fun clearAllHistory() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                transactionRepository.deleteAllTransactions()

                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val stats = com.moneysnap.domain.model.UserStats(
                    userId = userId,
                    totalBalance = 0.0,
                    totalIncome = 0.0,
                    totalExpense = 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
                userStatsRepository.updateUserStats(stats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val authRepo = AuthRepositoryImpl()
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val userStatsRepo = UserStatsRepositoryImpl(db.userStatsDao(), firestoreService)
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), firestoreService)
                val categoryRepo = com.moneysnap.data.repository.CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                return ProfileViewModel(authRepo, userStatsRepo, transactionRepo, categoryRepo, firestoreService) as T
            }
        }
    }
}
