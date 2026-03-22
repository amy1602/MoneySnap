package com.moneysnap.domain.model

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: TransactionType = TransactionType.EXPENSE,
    val userId: String = "",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
