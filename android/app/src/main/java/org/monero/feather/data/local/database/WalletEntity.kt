package org.monero.feather.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a wallet in the local database
 */
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val path: String,
    val networkType: Int, // 0=mainnet, 1=testnet, 2=stagenet
    val createdAt: Long,
    val lastUsed: Long,
    val isWatchOnly: Boolean,
    val hasSeed: Boolean
)
