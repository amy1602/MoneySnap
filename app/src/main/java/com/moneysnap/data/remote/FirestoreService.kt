package com.moneysnap.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.domain.model.UserStats
import kotlinx.coroutines.tasks.await

class FirestoreService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // CATEGORY
    suspend fun syncCategoryToRemote(category: Category, userId: String) {
        val data = mapOf(
            "id" to category.id,
            "name" to category.name,
            "type" to category.type.name,
            "icon" to category.icon,
            "color" to category.color,
            "userId" to category.userId,
            "isDeleted" to category.isDeleted,
            "updatedAt" to category.updatedAt
        )
        firestore.collection("users").document(userId)
            .collection("categories").document(category.id)
            .set(data).await()
    }

    suspend fun getCategoriesFromRemote(userId: String): List<Category> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("categories").get().await()
        
        return snapshot.documents.mapNotNull { doc ->
            try {
                Category(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "",
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                    icon = doc.getString("icon") ?: "",
                    color = doc.getString("color") ?: "",
                    userId = doc.getString("userId") ?: "",
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    }

    // TRANSACTION
    suspend fun syncTransactionToRemote(transaction: Transaction, userId: String) {
        val data = mapOf(
            "id" to transaction.id,
            "amount" to transaction.amount,
            "categoryId" to transaction.categoryId,
            "note" to transaction.note,
            "date" to transaction.date,
            "type" to transaction.type.name,
            "userId" to transaction.userId,
            "isDeleted" to transaction.isDeleted,
            "updatedAt" to transaction.updatedAt
        )
        firestore.collection("users").document(userId)
            .collection("transactions").document(transaction.id)
            .set(data).await()
    }

    suspend fun getTransactionsFromRemote(userId: String): List<Transaction> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("transactions").get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Transaction(
                    id = doc.getString("id") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    categoryId = doc.getString("categoryId") ?: "",
                    note = doc.getString("note") ?: "",
                    date = doc.getLong("date") ?: 0L,
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                    userId = doc.getString("userId") ?: "",
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    }

    // USER STATS
    suspend fun syncUserStatsToRemote(stats: UserStats, userId: String) {
        val data = mapOf(
            "userId" to stats.userId,
            "totalBalance" to stats.totalBalance,
            "totalIncome" to stats.totalIncome,
            "totalExpense" to stats.totalExpense,
            "lastUpdated" to stats.lastUpdated
        )
        firestore.collection("users").document(userId)
            .collection("stats").document("main")
            .set(data).await()
    }

    suspend fun getUserStatsFromRemote(userId: String): UserStats? {
        val doc = firestore.collection("users").document(userId)
            .collection("stats").document("main").get().await()

        return if (doc.exists()) {
            try {
                UserStats(
                    userId = doc.getString("userId") ?: "",
                    totalBalance = doc.getDouble("totalBalance") ?: 0.0,
                    totalIncome = doc.getDouble("totalIncome") ?: 0.0,
                    totalExpense = doc.getDouble("totalExpense") ?: 0.0,
                    lastUpdated = doc.getLong("lastUpdated") ?: 0L
                )
            } catch (e: Exception) { null }
        } else null
    }

    suspend fun updateUserAvatar(userId: String, avatarId: String) {
        val data = mapOf("avatarId" to avatarId)
        firestore.collection("users").document(userId)
            .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}
