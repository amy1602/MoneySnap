package com.moneysnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // Storing enum as String
    val icon: String,
    val color: String,
    val userId: String,
    val isDeleted: Boolean,
    val updatedAt: Long
)

fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = TransactionType.valueOf(type),
        icon = icon,
        color = color,
        userId = userId,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.name,
        icon = icon,
        color = color,
        userId = userId,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}
