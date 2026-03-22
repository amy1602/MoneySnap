package com.moneysnap.domain.model

enum class TransactionType {
    INCOME, EXPENSE
}

data class Category(
    val id: String = "",
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val icon: String = "", // Could store Android Icon name or resource string
    val color: String = "", // Hex color
    val userId: String = "",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
