package com.moneysnap.presentation.report

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
import java.util.Calendar
import java.util.Locale

enum class ReportType { MONTHLY, YEARLY }

data class WeeklyFlow(
    val weekName: String,
    val income: Double,
    val expense: Double
)

data class CategorySpending(
    val category: Category?,
    val amount: Double,
    val percentage: Float
)

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?
)

data class ReportUiState(
    val reportType: ReportType = ReportType.MONTHLY,
    val currentMonthYearString: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val weeklyFlows: List<WeeklyFlow> = emptyList(),
    val categorySpendings: List<CategorySpending> = emptyList(),
    val significantOutflows: List<TransactionWithCategory> = emptyList(),
    val maxWeeklyAmount: Double = 0.0
)

class ReportViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val currentCalendar = MutableStateFlow(Calendar.getInstance())
    private val currentReportType = MutableStateFlow(ReportType.MONTHLY)

    val uiState: StateFlow<ReportUiState> = combine(
        currentReportType,
        currentCalendar,
        transactionRepository.getTransactions(),
        categoryRepository.getCategories()
    ) { type, calendar, transactions, categories ->
        mapToUiState(type, calendar, transactions, categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun setReportType(type: ReportType) {
        currentReportType.value = type
    }

    fun nextMonth() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentCalendar.value.timeInMillis
        if (currentReportType.value == ReportType.YEARLY) {
            cal.add(Calendar.YEAR, 1)
        } else {
            cal.add(Calendar.MONTH, 1)
        }
        currentCalendar.value = cal
    }

    fun previousMonth() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentCalendar.value.timeInMillis
        if (currentReportType.value == ReportType.YEARLY) {
            cal.add(Calendar.YEAR, -1)
        } else {
            cal.add(Calendar.MONTH, -1)
        }
        currentCalendar.value = cal
    }

    private fun mapToUiState(type: ReportType, calendar: Calendar, allTransactions: List<Transaction>, categories: List<Category>): ReportUiState {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)
        
        val dateString = if (type == ReportType.YEARLY) "$year" else "$monthName $year"
        
        val targetTransactions = allTransactions.filter { 
            val txCal = Calendar.getInstance().apply { timeInMillis = it.date }
            if (type == ReportType.YEARLY) {
                txCal.get(Calendar.YEAR) == year
            } else {
                txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month
            }
        }

        var totalIncome = 0.0
        var totalExpense = 0.0
        
        targetTransactions.forEach {
            if (it.type == TransactionType.INCOME) totalIncome += it.amount
            else totalExpense += it.amount
        }
        val netBalance = totalIncome - totalExpense

        // Calculate Flow (Weekly for Monthly view, Monthly for Yearly view)
        val flowNames = if (type == ReportType.YEARLY) {
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        } else {
            listOf("W1", "W2", "W3", "W4")
        }
        
        val incomes = DoubleArray(flowNames.size)
        val expenses = DoubleArray(flowNames.size)
        
        targetTransactions.forEach {
            val txCal = Calendar.getInstance().apply { timeInMillis = it.date }
            val index = if (type == ReportType.YEARLY) {
                txCal.get(Calendar.MONTH)
            } else {
                val day = txCal.get(Calendar.DAY_OF_MONTH)
                when (day) {
                    in 1..7 -> 0
                    in 8..14 -> 1
                    in 15..21 -> 2
                    else -> 3
                }
            }
            if (it.type == TransactionType.INCOME) {
                incomes[index] += it.amount
            } else {
                expenses[index] += it.amount
            }
        }
        
        val flowItems = flowNames.mapIndexed { index, name ->
            WeeklyFlow(name, incomes[index], expenses[index])
        }
        
        val maxWeeklyAmount = (incomes.maxOrNull() ?: 0.0).coerceAtLeast(expenses.maxOrNull() ?: 0.0)

        // Calculate Category Spending
        val expensesList = targetTransactions.filter { it.type == TransactionType.EXPENSE }
        val categoryMap = expensesList.groupBy { it.categoryId }
        
        val categorySpendings = categoryMap.mapNotNull { (catId, txs) ->
            val cat = categories.find { it.id == catId }
            if (cat != null) {
                val amount = txs.sumOf { it.amount }
                val percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                CategorySpending(cat, amount, percentage)
            } else null
        }.sortedByDescending { it.amount }

        // Significant Outflows (Top 5 expenses)
        val significantOutflows = expensesList
            .sortedByDescending { it.amount }
            .take(5)
            .map { tx ->
                TransactionWithCategory(tx, categories.find { it.id == tx.categoryId })
            }

        return ReportUiState(
            reportType = type,
            currentMonthYearString = dateString,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            weeklyFlows = flowItems,
            categorySpendings = categorySpendings,
            significantOutflows = significantOutflows,
            maxWeeklyAmount = maxWeeklyAmount
        )
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val transactionRepo = TransactionRepositoryImpl(db.transactionDao(), firestoreService)
                val categoryRepo = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                
                return ReportViewModel(transactionRepo, categoryRepo) as T
            }
        }
    }
}
