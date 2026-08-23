package org.monero.feather.domain.model

/**
 * Represents a Monero wallet with its basic properties
 */
data class Wallet(
    val id: String,
    val name: String,
    val path: String,
    val networkType: NetworkType,
    val isViewOnly: Boolean = false,
    val isHardwareBacked: Boolean = false,
    val creationHeight: Long = 0L,
    val lastSyncHeight: Long = 0L,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Represents the balance of a wallet or account
 */
data class Balance(
    val total: Long,          // Total balance in piconero
    val unlocked: Long,       // Unlocked balance in piconero
    val locked: Long          // Locked balance in piconero
) {
    val totalXMR: Double
        get() = total / 1e12
    
    val unlockedXMR: Double
        get() = unlocked / 1e12
    
    val lockedXMR: Double
        get() = locked / 1e12
}

/**
 * Represents a transaction in the wallet history
 */
data class Transaction(
    val txId: String,
    val height: Long,
    val timestamp: Long,
    val amount: Long,         // Positive for received, negative for sent
    val fee: Long = 0L,
    val confirmations: Int = 0,
    val isCoinbase: Boolean = false,
    val isPending: Boolean = false,
    val description: String = "",
    val address: String? = null,
    val paymentId: String? = null,
    val subaddressIndex: SubaddressIndex? = null
) {
    val amountXMR: Double
        get() = amount / 1e12
    
    val feeXMR: Double
        get() = fee / 1e12
}

/**
 * Represents a subaddress index (account, address)
 */
data class SubaddressIndex(
    val accountIndex: Int,
    val addressIndex: Int
) {
    val isPrimary: Boolean
        get() = accountIndex == 0 && addressIndex == 0
    
    val isChange: Boolean
        get() = addressIndex == 0
}

/**
 * Represents a subaddress account
 */
data class SubaddressAccount(
    val index: Int,
    val label: String,
    val address: String,
    val balance: Balance
)

/**
 * Represents a subaddress
 */
data class Subaddress(
    val accountIndex: Int,
    val addressIndex: Int,
    val address: String,
    val label: String,
    val used: Boolean = false
)

/**
 * Represents an unspent output (key image)
 */
data class Output(
    val keyImage: String,
    val amount: Long,
    val blockHeight: Long = 0L,
    val confirmations: Int,
    val isFrozen: Boolean = false,
    val isSpent: Boolean = false,
    val subaddressIndex: SubaddressIndex? = null
) {
    val amountXMR: Double
        get() = amount / 1e12
}

/**
 * Represents a contact in the address book
 */
data class Contact(
    val id: String,
    val name: String,
    val address: String,
    val description: String = ""
)

/**
 * Network type enumeration
 */
enum class NetworkType(val value: Int) {
    MAINNET(0),
    TESTNET(1),
    STAGENET(2);
    
    companion object {
        fun fromValue(value: Int): NetworkType {
            return when (value) {
                0 -> MAINNET
                1 -> TESTNET
                2 -> STAGENET
                else -> MAINNET
            }
        }
    }
}

/**
 * Connection status of the wallet
 */
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    SYNCHRONIZING,
    SYNCHRONIZED,
    WRONG_VERSION
}

/**
 * Result of a wallet operation
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
}
