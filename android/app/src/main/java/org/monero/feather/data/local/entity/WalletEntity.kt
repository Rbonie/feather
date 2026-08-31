package org.monero.feather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения информации о кошельке в локальной БД
 */
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val viewKey: String, // Зашифрованный
    val spendKey: String, // Зашифрованный
    val creationHeight: Long,
    val isTestnet: Boolean,
    val lastSyncHeight: Long,
    val createdAt: Long,
    val updatedAt: Long
)
