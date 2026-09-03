package org.monero.feather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения транзакций в локальной БД
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val txId: String,
    val walletId: String,
    val amount: Long,
    val fee: Long,
    val height: Long,
    val timestamp: Long,
    val isConfirmed: Boolean,
    val confirmations: Int,
    val paymentId: String?,
    val isIncoming: Boolean,
    val address: String?
)
