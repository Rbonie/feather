package org.monero.feather.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a cached transaction in the local database
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val txHash: String,
    val amount: Long,
    val fee: Long,
    val blockHeight: Long,
    val timestamp: Long,
    val isCoinbase: Boolean,
    val isPending: Boolean,
    val direction: String, // "in" or "out"
    val address: String?,
    val paymentId: String?,
    val confirmations: Int,
    val description: String?
)
