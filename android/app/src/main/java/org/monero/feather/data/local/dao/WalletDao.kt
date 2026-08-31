package org.monero.feather.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.monero.feather.data.local.entity.TransactionEntity
import org.monero.feather.data.local.entity.WalletEntity

/**
 * DAO для работы с кошельками в Room Database
 */
@Dao
interface WalletDao {
    
    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>
    
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: String): WalletEntity?
    
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    fun getWalletByIdFlow(walletId: String): Flow<WalletEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)
    
    @Update
    suspend fun updateWallet(wallet: WalletEntity)
    
    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)
    
    @Query("UPDATE wallets SET lastSyncHeight = :height, updatedAt = :timestamp WHERE id = :walletId")
    suspend fun updateSyncHeight(walletId: String, height: Long, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int
}
