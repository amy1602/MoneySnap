package com.moneysnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneysnap.domain.model.UserStats

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val userId: String,
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val lastUpdated: Long
)

fun UserStatsEntity.toDomainModel(): UserStats {
    return UserStats(
        userId = userId,
        totalBalance = totalBalance,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        lastUpdated = lastUpdated
    )
}

fun UserStats.toEntity(): UserStatsEntity {
    return UserStatsEntity(
        userId = userId,
        totalBalance = totalBalance,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        lastUpdated = lastUpdated
    )
}
