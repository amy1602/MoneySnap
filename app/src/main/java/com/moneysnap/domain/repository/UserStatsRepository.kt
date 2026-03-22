package com.moneysnap.domain.repository

import com.moneysnap.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {
    fun getUserStatsStream(): Flow<UserStats?>
    suspend fun getUserStats(): UserStats?
    suspend fun updateUserStats(stats: UserStats)
    suspend fun syncUserStats() // Triggers sync from Firestore to Room
}
