package com.moneysnap.presentation.transaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.CategoryRepositoryImpl
import com.moneysnap.data.repository.TransactionRepositoryImpl
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.repository.CategoryRepository
import com.moneysnap.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val category: Category? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

class TransactionDetailViewModel(
    private val transactionId: String,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            combine(
                transactionRepository.getTransactions(),
                categoryRepository.getCategories()
            ) { transactions, categories ->
                val transaction = transactions.find { it.id == transactionId }
                val category = categories.find { it.id == transaction?.categoryId }
                Pair(transaction, category)
            }.collect { (transaction, category) ->
                _uiState.value = _uiState.value.copy(
                    transaction = transaction,
                    category = category,
                    isLoading = false
                )
            }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    companion object {
        fun provideFactory(context: Context, transactionId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), firestoreService)
                val categoryRepo = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                return TransactionDetailViewModel(transactionId, transactionRepo, categoryRepo) as T
            }
        }
    }
}
