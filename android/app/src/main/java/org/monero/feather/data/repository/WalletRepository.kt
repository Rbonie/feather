package org.monero.feather.data.repository

import kotlinx.coroutines.flow.Flow
import org.monero.feather.domain.model.*

/**
 * Repository interface for wallet operations.
 * Defines the contract for wallet data access and manipulation.
 */
interface WalletRepository {
    
    /**
     * Flow of current wallet state (null if no wallet is open)
     */
    val currentWallet: Flow<Wallet?>
    
    /**
     * Flow of current balance
     */
    val balance: Flow<Balance>
    
    /**
     * Flow of connection status
     */
    val connectionStatus: Flow<ConnectionStatus>
    
    /**
     * Flow of sync progress (current height, target height)
     */
    val syncProgress: Flow<Pair<Long, Long>>
    
    /**
     * Check if a wallet exists at the given path
     */
    suspend fun walletExists(path: String): Boolean
    
    /**
     * Create a new wallet from mnemonic seed
     */
    suspend fun createWalletFromSeed(
        path: String,
        password: String,
        seed: String,
        language: String = "English",
        networkType: NetworkType = NetworkType.MAINNET
    ): Result<Wallet>
    
    /**
     * Create a new wallet with random keys
     */
    suspend fun createWalletWithKeys(
        path: String,
        password: String,
        networkType: NetworkType = NetworkType.MAINNET
    ): Result<Wallet>
    
    /**
     * Open an existing wallet
     */
    suspend fun openWallet(
        path: String,
        password: String,
        networkType: NetworkType = NetworkType.MAINNET
    ): Result<Wallet>
    
    /**
     * Close the currently open wallet
     */
    suspend fun closeWallet(): Result<Unit>
    
    /**
     * Get wallet balance
     */
    suspend fun getBalance(): Result<Balance>
    
    /**
     * Get wallet address
     */
    suspend fun getAddress(accountIndex: Int = 0, addressIndex: Int = 0): Result<String>
    
    /**
     * Get mnemonic seed (only for non-view-only wallets)
     */
    suspend fun getSeed(): Result<String>
    
    /**
     * Start wallet refresh/synchronization
     */
    suspend fun startRefresh(): Result<Unit>
    
    /**
     * Pause wallet refresh
     */
    suspend fun pauseRefresh(): Result<Unit>
    
    /**
     * Set daemon connection
     */
    suspend fun setDaemon(
        address: String,
        useSSL: Boolean = false,
        username: String? = null,
        password: String? = null
    ): Result<Unit>
    
    /**
     * Store wallet to disk
     */
    suspend fun storeWallet(): Result<Unit>
    
    /**
     * Change wallet password
     */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit>
    
    /**
     * Get transaction history
     */
    suspend fun getTransactions(): Result<List<Transaction>>
    
    /**
     * Get subaddress accounts
     */
    suspend fun getSubaddressAccounts(): Result<List<SubaddressAccount>>
    
    /**
     * Create new subaddress account
     */
    suspend fun createSubaddressAccount(label: String): Result<SubaddressAccount>
    
    /**
     * Get subaddresses for an account
     */
    suspend fun getSubaddresses(accountIndex: Int): Result<List<Subaddress>>
    
    /**
     * Create new subaddress
     */
    suspend fun createSubaddress(accountIndex: Int, label: String): Result<Subaddress>
    
    /**
     * Get unspent outputs
     */
    suspend fun getOutputs(): Result<List<Output>>
    
    /**
     * Create a transaction
     */
    suspend fun createTransaction(
        address: String,
        amount: Long,
        feeLevel: Int = 0,
        description: String = "",
        subtractFeeFromAmount: Boolean = false
    ): Result<PendingTransactionInfo>
    
    /**
     * Send transaction
     */
    suspend fun sendTransaction(txInfo: PendingTransactionInfo): Result<String>
    
    /**
     * Sweep all outputs to address
     */
    suspend fun sweepAll(address: String, feeLevel: Int = 0): Result<String>
    
    /**
     * Export key images
     */
    suspend fun exportKeyImages(path: String): Result<String>
    
    /**
     * Import key images
     */
    suspend fun importKeyImages(path: String): Result<Int>
}

/**
 * Information about a pending transaction before sending
 */
data class PendingTransactionInfo(
    val txId: String,
    val amount: Long,
    val fee: Long,
    val ringSize: Int,
    val recipientAddress: String,
    val description: String = "",
    val txHex: String? = null
) {
    val amountXMR: Double
        get() = amount / 1e12
    
    val feeXMR: Double
        get() = fee / 1e12
}
