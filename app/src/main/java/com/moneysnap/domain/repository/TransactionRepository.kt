package com.moneysnap.domain.repository

import com.moneysnap.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(transactionId: String): Transaction?
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    suspend fun deleteAllTransactions()
    suspend fun syncTransactions() // Triggers sync from Firestore to Room
}
