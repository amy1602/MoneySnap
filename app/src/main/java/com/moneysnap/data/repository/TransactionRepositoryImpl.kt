package com.moneysnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.moneysnap.data.local.dao.TransactionDao
import com.moneysnap.data.local.entity.toDomainModel
import com.moneysnap.data.local.entity.toEntity
import com.moneysnap.data.remote.FirestoreService
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TransactionRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun getTransactions(): Flow<List<Transaction>> = flow {
        val userId = currentUserId ?: return@flow
        emitAll(transactionDao.getAllTransactions(userId).map { list ->
            list.map { it.toDomainModel() }
        })
    }

    override suspend fun getTransactionById(transactionId: String): Transaction? {
        val userId = currentUserId ?: return null
        return transactionDao.getTransactionById(transactionId, userId)?.toDomainModel()
    }

    override suspend fun saveTransaction(transaction: Transaction) {
        val userId = currentUserId ?: return
        val txToSave = transaction.copy(userId = userId, updatedAt = System.currentTimeMillis())
        
        transactionDao.insertTransaction(txToSave.toEntity())
        
        scope.launch {
            try {
                firestoreService.syncTransactionToRemote(txToSave, userId)
            } catch (e: Exception) { }
        }
    }

    override suspend fun deleteTransaction(transactionId: String) {
        val userId = currentUserId ?: return
        
        val updatedAt = System.currentTimeMillis()
        transactionDao.deleteTransaction(transactionId, userId, updatedAt)
        
        scope.launch {
            try {
                val deletedTx = transactionDao.getTransactionById(transactionId, userId)
                if (deletedTx != null) {
                    firestoreService.syncTransactionToRemote(deletedTx.toDomainModel(), userId)
                }
            } catch (e: Exception) { }
        }
    }

    override suspend fun syncTransactions() {
        val userId = currentUserId ?: return
        try {
            val remoteTxs = firestoreService.getTransactionsFromRemote(userId)
            val localTxs = transactionDao.getAllTransactionsIncludingDeleted(userId).associateBy { it.id }
            
            remoteTxs.forEach { remote ->
                val local = localTxs[remote.id]
                if (local == null || remote.updatedAt > local.updatedAt) {
                    transactionDao.insertTransaction(remote.toEntity())
                } else if (local.updatedAt > remote.updatedAt) {
                    firestoreService.syncTransactionToRemote(local.toDomainModel(), userId)
                }
            }
            
            val remoteIds = remoteTxs.map { it.id }.toSet()
            localTxs.values.forEach { local ->
                if (!remoteIds.contains(local.id)) {
                    firestoreService.syncTransactionToRemote(local.toDomainModel(), userId)
                }
            }
        } catch (e: Exception) { }
    }
}
