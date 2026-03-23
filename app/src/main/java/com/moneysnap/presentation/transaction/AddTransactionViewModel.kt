package com.moneysnap.presentation.transaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.TransactionRepositoryImpl
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class AddTransactionState(
    val amount: String = "0",
    val categoryId: String? = null,
    val categoryName: String = "Select category",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state = _state.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun onAmountChange(newAmount: String) {
        var processed = newAmount
        
        // Handle leading zeros: "09" -> "9", "00" -> "0"
        if (processed.length > 1 && processed.startsWith("0") && !processed.startsWith("0.")) {
            processed = processed.dropWhile { it == '0' }
            if (processed.isEmpty() || processed.startsWith(".")) {
                processed = "0$processed"
            }
        }

        if (processed.isEmpty() || processed.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
            _state.update { it.copy(amount = processed) }
        }
    }

    fun onCategorySelect(id: String, name: String) {
        _state.update { it.copy(categoryId = id, categoryName = name) }
    }

    fun onTypeChange(newType: TransactionType) {
        _state.update { it.copy(type = newType) }
    }

    fun onDateChange(newDate: Long) {
        _state.update { it.copy(date = newDate) }
    }

    fun onNoteChange(newNote: String) {
        _state.update { it.copy(note = newNote) }
    }

    fun saveTransaction() {
        val currentState = state.value
        
        // Validation
        val amountValue = currentState.amount.toDoubleOrNull() ?: 0.0
        if (amountValue <= 0) {
            viewModelScope.launch { _toastMessage.emit("Please enter an amount more than 0") }
            return
        }
        
        if (currentState.categoryId == null) {
            viewModelScope.launch { _toastMessage.emit("Please select a category") }
            return
        }

        if (currentState.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = amountValue,
                    categoryId = currentState.categoryId,
                    note = currentState.note.trim(),
                    date = currentState.date,
                    type = currentState.type,
                    userId = "" // Repository handles this
                )
                transactionRepository.saveTransaction(transaction)
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
                _toastMessage.emit("Error saving transaction: ${e.message}")
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val repository = TransactionRepositoryImpl(db.transactionDao(), FirestoreService())
                return AddTransactionViewModel(repository) as T
            }
        }
    }
}
