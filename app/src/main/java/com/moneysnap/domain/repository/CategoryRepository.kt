package com.moneysnap.domain.repository

import com.moneysnap.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: String): Category?
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
    suspend fun syncCategories() // Triggers sync from Firestore to Room
}
