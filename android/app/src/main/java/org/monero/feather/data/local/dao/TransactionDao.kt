package org.monero.feather.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.monero.feather.data.local.entity.TransactionEntity

/**
 * DAO для работы с транзакциями в Room Database
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY timestamp DESC")
    fun getTransactionsByWalletId(walletId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE txId = :txId")
    suspend fun getTransactionById(txId: String): TransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE walletId = :walletId")
    suspend fun deleteAllTransactionsForWallet(walletId: String)
    
    @Query("SELECT COUNT(*) FROM transactions WHERE walletId = :walletId AND isConfirmed = 0")
    suspend fun getPendingTransactionCount(walletId: String): Int
    
    @Query("SELECT * FROM transactions WHERE walletId = :walletId AND isConfirmed = 0 ORDER BY timestamp DESC")
    fun getPendingTransactions(walletId: String): Flow<List<TransactionEntity>>
}
