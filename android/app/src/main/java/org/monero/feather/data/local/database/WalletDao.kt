package org.monero.feather.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for wallet database operations
 */
@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY lastUsed DESC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE path = :path")
    suspend fun getWalletByPath(path: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET lastUsed = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM wallets")
    fun getWalletCount(): Flow<Int>

    @Query("SELECT * FROM wallets WHERE networkType = :networkType")
    fun getWalletsByNetworkType(networkType: Int): Flow<List<WalletEntity>>
}
