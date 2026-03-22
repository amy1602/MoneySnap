package com.moneysnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneysnap.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId AND userId = :userId")
    suspend fun getTransactionById(transactionId: String, userId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsIncludingDeleted(userId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :transactionId AND userId = :userId")
    suspend fun deleteTransaction(transactionId: String, userId: String, updatedAt: Long = System.currentTimeMillis())
}
