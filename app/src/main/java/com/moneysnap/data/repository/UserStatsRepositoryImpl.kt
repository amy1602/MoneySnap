package com.moneysnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.moneysnap.data.local.dao.UserStatsDao
import com.moneysnap.data.local.entity.toDomainModel
import com.moneysnap.data.local.entity.toEntity
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.domain.model.UserStats
import com.moneysnap.domain.repository.UserStatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserStatsRepositoryImpl(
    private val userStatsDao: UserStatsDao,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserStatsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun getUserStatsStream(): Flow<UserStats?> = flow {
        val userId = currentUserId ?: return@flow
        emitAll(userStatsDao.getUserStatsStream(userId).map { it?.toDomainModel() })
    }

    override suspend fun getUserStats(): UserStats? {
        val userId = currentUserId ?: return null
        return userStatsDao.getUserStats(userId)?.toDomainModel()
    }

    override suspend fun updateUserStats(stats: UserStats) {
        val userId = currentUserId ?: return
        val statsToSave = stats.copy(userId = userId, lastUpdated = System.currentTimeMillis())
        
        userStatsDao.insertUserStats(statsToSave.toEntity())
        
        scope.launch {
            try {
                firestoreService.syncUserStatsToRemote(statsToSave, userId)
            } catch (e: Exception) { }
        }
    }

    override suspend fun syncUserStats() {
        val userId = currentUserId ?: return
        try {
            val remoteStats = firestoreService.getUserStatsFromRemote(userId)
            val localStats = userStatsDao.getUserStats(userId)
            
            if (remoteStats != null) {
                if (localStats == null || remoteStats.lastUpdated > localStats.lastUpdated) {
                    userStatsDao.insertUserStats(remoteStats.toEntity())
                } else if (localStats.lastUpdated > remoteStats.lastUpdated) {
                    firestoreService.syncUserStatsToRemote(localStats.toDomainModel(), userId)
                }
            } else if (localStats != null) {
                firestoreService.syncUserStatsToRemote(localStats.toDomainModel(), userId)
            }
        } catch (e: Exception) { }
    }
}
