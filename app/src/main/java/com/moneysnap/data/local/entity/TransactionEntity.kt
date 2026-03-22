package com.moneysnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val categoryId: String,
    val note: String,
    val date: Long,
    val type: String,
    val userId: String,
    val isDeleted: Boolean,
    val updatedAt: Long
)

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        categoryId = categoryId,
        note = note,
        date = date,
        type = TransactionType.valueOf(type),
        userId = userId,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        note = note,
        date = date,
        type = type.name,
        userId = userId,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}
