package org.monero.feather.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JNI wrapper for Monero wallet C++ library.
 * This class provides the interface between Kotlin/Java and the native C++ wallet implementation.
 */
@Singleton
class WalletLibrary @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        System.loadLibrary("feather_wallet_jni")
    }

    /**
     * Initialize the wallet library with the application data directory
     * @param dataDir Path to the application's data directory
     * @return true if initialization successful, false otherwise
     */
    external fun initializeLibrary(dataDir: String): Boolean

    /**
     * Cleanup and unload the library (call on app termination)
     */
    external fun cleanupLibrary()

    /**
     * Check if a wallet exists at the given path
     * @param path Path to the wallet file (without extension)
     * @return true if wallet exists, false otherwise
     */
    external fun walletExists(path: String): Boolean

    /**
     * Create a new wallet from mnemonic seed
     * @param path Path where the wallet will be stored
     * @param password Password to encrypt the wallet
     * @param seed Mnemonic seed phrase
     * @param language Language of the seed (e.g., "English", "Spanish")
     * @param networkType Network type (0=mainnet, 1=testnet, 2=stagenet)
     * @return Handle to the wallet (0 on failure)
     */
    external fun createWalletFromSeed(
        path: String,
        password: String,
        seed: String,
        language: String,
        networkType: Int
    ): Long

    /**
     * Open an existing wallet
     * @param path Path to the wallet file (without extension)
     * @param password Password to decrypt the wallet
     * @param networkType Network type (0=mainnet, 1=testnet, 2=stagenet)
     * @return Handle to the wallet (0 on failure)
     */
    external fun openWallet(path: String, password: String, networkType: Int): Long

    /**
     * Close a wallet and free resources
     * @param walletHandle Handle to the wallet obtained from open/create methods
     */
    external fun closeWallet(walletHandle: Long)

    // ========================================================================
    // Wallet Operations
    // ========================================================================

    /**
     * Get the total balance of the wallet in atomic units (piconero)
     * @param walletHandle Handle to the wallet
     * @return Balance in piconero
     */
    external fun getBalance(walletHandle: Long): Long

    /**
     * Get the unlocked balance of the wallet in atomic units (piconero)
     * @param walletHandle Handle to the wallet
     * @return Unlocked balance in piconero
     */
    external fun getUnlockedBalance(walletHandle: Long): Long

    /**
     * Get wallet address for a specific subaddress account and index
     * @param walletHandle Handle to the wallet
     * @param accountIndex Account index (0 for primary account)
     * @param addressIndex Subaddress index within the account
     * @return Monero address string
     */
    external fun getAddress(walletHandle: Long, accountIndex: Int, addressIndex: Int): String

    /**
     * Get the mnemonic seed phrase of the wallet
     * @param walletHandle Handle to the wallet
     * @return Mnemonic seed phrase
     */
    external fun getSeed(walletHandle: Long): String

    /**
     * Start wallet refresh/synchronization with the daemon
     * @param walletHandle Handle to the wallet
     */
    external fun refresh(walletHandle: Long)

    /**
     * Get the current blockchain height of the wallet
     * @param walletHandle Handle to the wallet
     * @return Current block height
     */
    external fun getBlockChainHeight(walletHandle: Long): Long

    /**
     * Get the daemon's blockchain height
     * @param walletHandle Handle to the wallet
     * @param daemonAddress Address of the daemon
     * @return Daemon block height
     */
    external fun getDaemonBlockChainHeight(walletHandle: Long, daemonAddress: String): Long

    /**
     * Set the daemon connection parameters
     * @param walletHandle Handle to the wallet
     * @param daemonAddress Daemon address (host:port)
     * @param useSSL Whether to use SSL connection
     * @param username Optional username for authentication
     * @param password Optional password for authentication
     * @return true if daemon set successfully
     */
    external fun setDaemon(
        walletHandle: Long,
        daemonAddress: String,
        useSSL: Boolean,
        username: String?,
        password: String?
    ): Boolean

    /**
     * Store wallet data to disk
     * @param walletHandle Handle to the wallet
     * @return true if storage successful
     */
    external fun storeWallet(walletHandle: Long): Boolean

    /**
     * Change wallet password
     * @param walletHandle Handle to the wallet
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     */
    external fun changePassword(
        walletHandle: Long,
        oldPassword: String,
        newPassword: String
    ): Boolean

    /**
     * Send a transaction
     * @param walletHandle Handle to the wallet
     * @param address Destination address
     * @param amount Amount in piconero
     * @param mixin Ring size (number of decoys + 1)
     * @param paymentId Optional payment ID
     * @param description Optional description
     * @return Transaction ID on success, empty string on failure
     */
    external fun sendTransaction(
        walletHandle: Long,
        address: String,
        amount: Long,
        mixin: Int = 16,
        paymentId: String?,
        description: String?
    ): String

    /**
     * Get transaction history as JSON array
     * @param walletHandle Handle to the wallet
     * @return JSON array of transactions
     */
    external fun getTransactionHistoryJson(walletHandle: Long): String

    /**
     * Get wallet status code
     * @param walletHandle Handle to the wallet
     * @return Status code (0 = OK, negative values indicate errors)
     */
    external fun getStatus(walletHandle: Long): Int

    /**
     * Get last error message
     * @param walletHandle Handle to the wallet
     * @return Error message string
     */
    external fun getLastError(walletHandle: Long): String

    companion object {
        const val NETWORK_TYPE_MAINNET = 0
        const val NETWORK_TYPE_TESTNET = 1
        const val NETWORK_TYPE_STAGENET = 2
        
        // Status codes
        const val STATUS_OK = 0
        const val STATUS_ERROR = 1
        const val STATUS_CRITICAL = 2
        
        // Default mixin (ring size)
        const val DEFAULT_MIXIN = 16
    }
}
