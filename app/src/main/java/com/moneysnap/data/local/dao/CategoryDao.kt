package com.moneysnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneysnap.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0")
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId AND userId = :userId")
    suspend fun getCategoryById(categoryId: String, userId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getAllCategoriesIncludingDeleted(userId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :categoryId AND userId = :userId")
    suspend fun deleteCategory(categoryId: String, userId: String, updatedAt: Long = System.currentTimeMillis())
}
