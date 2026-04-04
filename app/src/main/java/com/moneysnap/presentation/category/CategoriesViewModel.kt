package com.moneysnap.presentation.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.CategoryRepositoryImpl
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val incomeCategories: List<Category> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategories()
        }
    }

    val uiState: StateFlow<CategoriesUiState> = categoryRepository.getCategories().map { categories ->
        CategoriesUiState(
            incomeCategories = categories.filter { it.type == TransactionType.INCOME && !it.isDeleted },
            expenseCategories = categories.filter { it.type == TransactionType.EXPENSE && !it.isDeleted },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState(isLoading = true)
    )

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val categoryRepository = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                return CategoriesViewModel(categoryRepository) as T
            }
        }
    }
}
