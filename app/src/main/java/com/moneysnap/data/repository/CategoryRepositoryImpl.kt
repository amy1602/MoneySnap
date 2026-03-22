package com.moneysnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.moneysnap.data.local.dao.CategoryDao
import com.moneysnap.data.local.entity.toDomainModel
import com.moneysnap.data.local.entity.toEntity
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : CategoryRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun getCategories(): Flow<List<Category>> = flow {
        val userId = currentUserId ?: return@flow
        emitAll(categoryDao.getAllCategories(userId).map { list ->
            list.map { it.toDomainModel() }
        })
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        val userId = currentUserId ?: return null
        return categoryDao.getCategoryById(categoryId, userId)?.toDomainModel()
    }

    override suspend fun saveCategory(category: Category) {
        val userId = currentUserId ?: return
        val categoryToSave = category.copy(userId = userId, updatedAt = System.currentTimeMillis())
        
        // 1. Save locally
        categoryDao.insertCategory(categoryToSave.toEntity())
        
        // 2. Sync to remote asynchronously
        scope.launch {
            try {
                firestoreService.syncCategoryToRemote(categoryToSave, userId)
            } catch (e: Exception) {
                // Sync failed, will be caught by next sync pass
            }
        }
    }

    override suspend fun deleteCategory(categoryId: String) {
        val userId = currentUserId ?: return
        
        // 1. Soft delete locally
        val updatedAt = System.currentTimeMillis()
        categoryDao.deleteCategory(categoryId, userId, updatedAt)
        
        // 2. Sync to remote asynchronously
        scope.launch {
            try {
                val deletedCategory = categoryDao.getCategoryById(categoryId, userId)
                if (deletedCategory != null) {
                    firestoreService.syncCategoryToRemote(deletedCategory.toDomainModel(), userId)
                }
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun syncCategories() {
        val userId = currentUserId ?: return
        try {
            // 1. Get from remote
            val remoteCategories = firestoreService.getCategoriesFromRemote(userId)
            
            // 2. Get local including deleted
            val localCategories = categoryDao.getAllCategoriesIncludingDeleted(userId).associateBy { it.id }
            
            // 3. Merge strategy: Use latest timestamp
            remoteCategories.forEach { remote ->
                val local = localCategories[remote.id]
                if (local == null || remote.updatedAt > local.updatedAt) {
                    // Remote is newer or doesn't exist locally -> save to local
                    categoryDao.insertCategory(remote.toEntity())
                } else if (local.updatedAt > remote.updatedAt) {
                    // Local is newer -> push to remote
                    firestoreService.syncCategoryToRemote(local.toDomainModel(), userId)
                }
            }
            
            // 4. Push local changes that don't exist in remote
            val remoteIds = remoteCategories.map { it.id }.toSet()
            localCategories.values.forEach { local ->
                if (!remoteIds.contains(local.id)) {
                    firestoreService.syncCategoryToRemote(local.toDomainModel(), userId)
                }
            }

        } catch (e: Exception) {
            // Log sync error
        }
    }
}
