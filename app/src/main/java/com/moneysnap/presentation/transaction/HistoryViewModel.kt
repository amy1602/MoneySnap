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
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.repository.CategoryRepository
import com.moneysnap.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItem(
    val transaction: Transaction,
    val category: Category? // Contains icon and color info
)

data class HistoryUiState(
    val groupedTransactions: Map<String, List<HistoryItem>> = emptyMap(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categoryFilter: String? = null,
    val categories: List<Category> = emptyList()
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)
    private val _categoryFilter = MutableStateFlow<String?>(null)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        transactionRepository.getTransactions(),
        categoryRepository.getCategories(),
        _startDate,
        _endDate,
        _categoryFilter
    ) { transactions, categories, startDate, endDate, categoryFilter ->
        val catMap = categories.associateBy { it.id }
        
        var filtered = transactions

        // 1. Category Filter
        if (categoryFilter != null) {
            filtered = filtered.filter { it.categoryId == categoryFilter }
        }

        // 2. Date Range Filter
        if (startDate != null && endDate != null) {
            // Adjust end date to the end of the day (23:59:59.999) to be inclusive
            val cal = Calendar.getInstance()
            cal.timeInMillis = endDate
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val adjustedEnd = cal.timeInMillis
            
            filtered = filtered.filter { it.date in startDate..adjustedEnd }
        }

        val enriched = filtered.sortedByDescending { it.date }.map { tx ->
            HistoryItem(tx, catMap[tx.categoryId])
        }

        val grouped = enriched.groupBy { formatGroupDate(it.transaction.date) }
        
        HistoryUiState(
            groupedTransactions = grouped, 
            startDate = startDate,
            endDate = endDate,
            categoryFilter = categoryFilter,
            categories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun setCategoryFilter(categoryId: String?) {
        _categoryFilter.value = categoryId
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
        }
    }

    private fun formatGroupDate(timestamp: Long): String {
        val cal = Calendar.getInstance()
        val today = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 86400000L

        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val txDate = txCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return when (txDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(timestamp))
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), firestoreService)
                val categoryRepo = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                return HistoryViewModel(transactionRepo, categoryRepo) as T
            }
        }
    }
}
