package com.moneysnap.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.CategoryRepositoryImpl
import com.moneysnap.data.repository.TransactionRepositoryImpl
import com.moneysnap.data.repository.UserStatsRepositoryImpl
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.model.UserStats
import com.moneysnap.domain.repository.CategoryRepository
import com.moneysnap.domain.repository.TransactionRepository
import com.moneysnap.domain.repository.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.moneysnap.domain.model.Category
import com.moneysnap.presentation.report.TransactionWithCategory

data class DailySpending(
    val dayOfWeek: String, // MON, TUE, etc.
    val ratio: Float, // 0.0 to 1.0 for the bar chart
    val isToday: Boolean
)

data class HomeUiState(
    val totalBalance: String = "$0.00",
    val income: String = "$0.00",
    val expenses: String = "$0.00",
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val weeklySpending: List<DailySpending> = emptyList()
)

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    val uiState: StateFlow<HomeUiState> = combine(
        userStatsRepository.getUserStatsStream(),
        transactionRepository.getTransactions(),
        categoryRepository.getCategories()
    ) { stats, transactions, categories ->
        mapToUiState(stats, transactions, categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState() // Default empty state
    )

    init {
        // Trigger initial sync
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            userStatsRepository.syncUserStats()
            transactionRepository.syncTransactions()
            categoryRepository.syncCategories()
        }
    }

    private fun mapToUiState(stats: UserStats?, transactions: List<Transaction>, categories: List<Category>): HomeUiState {
        val incomeValue = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenseValue = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balanceValue = incomeValue - expenseValue

        val recentTxs = transactions.sortedByDescending { it.date }.take(5).map { tx ->
            TransactionWithCategory(tx, categories.find { it.id == tx.categoryId })
        }

        val weekly = calculateWeeklySpending(transactions)

        return HomeUiState(
            totalBalance = currencyFormatter.format(balanceValue),
            income = currencyFormatter.format(incomeValue),
            expenses = currencyFormatter.format(expenseValue),
            recentTransactions = recentTxs,
            weeklySpending = weekly
        )
    }

    private fun calculateWeeklySpending(transactions: List<Transaction>): List<DailySpending> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<DailySpending>()

        // Get limits
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // We want last 7 days ending today
        val todayMs = calendar.timeInMillis
        val sevenDaysAgoMs = todayMs - (6 * 24 * 60 * 60 * 1000L)

        // Filter valid expenses
        val weekExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date in sevenDaysAgoMs..(todayMs + 24 * 60 * 60 * 1000L)
        }

        // Group by day of week
        val amountsByDay = mutableMapOf<Int, Double>()
        for (tx in weekExpenses) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            val dayOfWeek = txCal.get(Calendar.DAY_OF_WEEK)
            amountsByDay[dayOfWeek] = (amountsByDay[dayOfWeek] ?: 0.0) + tx.amount
        }

        val maxAmount = amountsByDay.values.maxOrNull() ?: 1.0 // Avoid divide by zero
        val safeMax = if (maxAmount <= 0.0) 1.0 else maxAmount

        // Build list backwards from today
        val dayNames = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        
        // Let's populate the 7 days chronologically ending today
        val iterationCal = Calendar.getInstance()
        iterationCal.timeInMillis = sevenDaysAgoMs

        for (i in 0..6) {
            val dayOfWeek = iterationCal.get(Calendar.DAY_OF_WEEK)
            val name = dayNames[dayOfWeek - 1]
            val amount = amountsByDay[dayOfWeek] ?: 0.0
            val ratio = (amount / safeMax).toFloat().coerceIn(0.1f, 1f) // Give it a tiny base height visually
            
            days.add(
                DailySpending(
                    dayOfWeek = name,
                    ratio = ratio,
                    isToday = i == 6
                )
            )
            iterationCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), firestoreService)
                val userStatsRepo = UserStatsRepositoryImpl(db.userStatsDao(), firestoreService)
                val categoryRepo = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                
                return HomeViewModel(
                    transactionRepo,
                    userStatsRepo,
                    categoryRepo
                ) as T
            }
        }
    }
}
