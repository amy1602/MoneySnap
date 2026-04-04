package com.moneysnap.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val totalSavings: String = "$0.00",
    val transactionCount: Int = 0
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userStatsRepository: UserStatsRepository,
    private val transactionRepository: TransactionRepository
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

        _uiState.update { it.copy(email = email, name = name) }

        viewModelScope.launch {
            userStatsRepository.getUserStatsStream().collect { stats ->
                val statsObj = stats ?: UserStats()
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
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

    fun logout() {
        authRepository.logout()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val authRepo = AuthRepositoryImpl()
                val db = MoneyDatabase.getDatabase(context)
                val userStatsRepo = UserStatsRepositoryImpl(db.userStatsDao(), FirestoreService())
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), FirestoreService())
                return ProfileViewModel(authRepo, userStatsRepo, transactionRepo) as T
            }
        }
    }
}
