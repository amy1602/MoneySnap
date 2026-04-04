package com.moneysnap.presentation.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneysnap.data.local.MoneyDatabase
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.data.repository.CategoryRepositoryImpl
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.CategoryConstants
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddCategoryState(
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val iconName: String = CategoryConstants.ICON_MAP.keys.first(),
    val colorHex: String = CategoryConstants.DEFAULT_COLORS.first(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddCategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state: StateFlow<AddCategoryState> = _state.asStateFlow()

    fun onNameChange(newName: String) {
        _state.update { it.copy(name = newName, errorMessage = null) }
    }

    fun onTypeChange(newType: TransactionType) {
        _state.update { it.copy(type = newType) }
    }

    fun onIconSelected(newIconName: String) {
        _state.update { it.copy(iconName = newIconName) }
    }

    fun onColorSelected(newColorHex: String) {
        _state.update { it.copy(colorHex = newColorHex) }
    }

    fun saveCategory() {
        val currentState = _state.value
        if (currentState.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter a category name") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val newCategory = Category(
                    id = UUID.randomUUID().toString(),
                    name = currentState.name,
                    type = currentState.type,
                    icon = currentState.iconName,
                    color = currentState.colorHex,
                    updatedAt = System.currentTimeMillis()
                )
                categoryRepository.saveCategory(newCategory)
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "Failed to save category: ${e.message}") }
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MoneyDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                val repository = CategoryRepositoryImpl(db.categoryDao(), firestoreService)
                return AddCategoryViewModel(repository) as T
            }
        }
    }
}
