package org.monero.feather.data.repository.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.monero.feather.data.local.WalletLibrary
import org.monero.feather.data.repository.PendingTransactionInfo
import org.monero.feather.data.repository.WalletRepository
import org.monero.feather.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WalletRepository using JNI to interact with C++ wallet library.
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletLibrary: WalletLibrary,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WalletRepository {

    private val _currentWallet = MutableStateFlow<Wallet?>(null)
    override val currentWallet: Flow<Wallet?> = _currentWallet.asStateFlow()

    private val _balance = MutableStateFlow(Balance(0, 0, 0))
    override val balance: Flow<Balance> = _balance.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: Flow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow(0L to 0L)
    override val syncProgress: Flow<Pair<Long, Long>> = _syncProgress.asStateFlow()

    private var currentWalletHandle: Long = 0L
    private var currentWalletData: Wallet? = null

    override suspend fun walletExists(path: String): Boolean = withContext(ioDispatcher) {
        walletLibrary.walletExists(path)
    }

    override suspend fun createWalletFromSeed(
        path: String,
        password: String,
        seed: String,
        language: String,
        networkType: NetworkType
    ): Result<Wallet> = withContext(ioDispatcher) {
        try {
            val handle = walletLibrary.createWalletFromSeed(
                path = path,
                password = password,
                seed = seed,
                language = language,
                networkType = networkType.value
            )

            if (handle == 0L) {
                Result.Error("Failed to create wallet from seed")
            } else {
                currentWalletHandle = handle
                val wallet = Wallet(
                    id = System.currentTimeMillis().toString(),
                    name = path.substringAfterLast('/'),
                    path = path,
                    networkType = networkType,
                    isViewOnly = false,
                    isHardwareBacked = false
                )
                currentWalletData = wallet
                _currentWallet.value = wallet
                Result.Success(wallet)
            }
        } catch (e: Exception) {
            Result.Error("Error creating wallet: ${e.message}", e)
        }
    }

    override suspend fun createWalletWithKeys(
        path: String,
        password: String,
        networkType: NetworkType
    ): Result<Wallet> = withContext(ioDispatcher) {
        // TODO: Implement wallet creation with random keys
        Result.Error("Not yet implemented")
    }

    override suspend fun openWallet(
        path: String,
        password: String,
        networkType: NetworkType
    ): Result<Wallet> = withContext(ioDispatcher) {
        try {
            val handle = walletLibrary.openWallet(
                path = path,
                password = password,
                networkType = networkType.value
            )

            if (handle == 0L) {
                Result.Error("Failed to open wallet. Check password and path.")
            } else {
                currentWalletHandle = handle
                val wallet = Wallet(
                    id = System.currentTimeMillis().toString(),
                    name = path.substringAfterLast('/'),
                    path = path,
                    networkType = networkType,
                    isViewOnly = false,
                    isHardwareBacked = false
                )
                currentWalletData = wallet
                _currentWallet.value = wallet
                
                // Update balance after opening
                updateBalance()
                
                Result.Success(wallet)
            }
        } catch (e: Exception) {
            Result.Error("Error opening wallet: ${e.message}", e)
        }
    }

    override suspend fun closeWallet(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (currentWalletHandle != 0L) {
                walletLibrary.closeWallet(currentWalletHandle)
                currentWalletHandle = 0L
                currentWalletData = null
                _currentWallet.value = null
                _balance.value = Balance(0, 0, 0)
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Error closing wallet: ${e.message}", e)
        }
    }

    override suspend fun getBalance(): Result<Balance> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            updateBalance()
            Result.Success(_balance.value)
        } catch (e: Exception) {
            Result.Error("Error getting balance: ${e.message}", e)
        }
    }

    private suspend fun updateBalance() {
        if (currentWalletHandle == 0L) return
        
        val total = walletLibrary.getBalance(currentWalletHandle)
        val unlocked = walletLibrary.getUnlockedBalance(currentWalletHandle)
        val locked = total - unlocked
        
        _balance.value = Balance(total, unlocked, locked)
    }

    override suspend fun getAddress(
        accountIndex: Int,
        addressIndex: Int
    ): Result<String> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            val address = walletLibrary.getAddress(currentWalletHandle, accountIndex, addressIndex)
            if (address.isEmpty()) {
                Result.Error("Failed to get address")
            } else {
                Result.Success(address)
            }
        } catch (e: Exception) {
            Result.Error("Error getting address: ${e.message}", e)
        }
    }

    override suspend fun getSeed(): Result<String> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            val seed = walletLibrary.getSeed(currentWalletHandle)
            if (seed.isEmpty()) {
                Result.Error("Failed to get seed or wallet is view-only")
            } else {
                Result.Success(seed)
            }
        } catch (e: Exception) {
            Result.Error("Error getting seed: ${e.message}", e)
        }
    }

    override suspend fun startRefresh(): Result<Unit> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            _connectionStatus.value = ConnectionStatus.SYNCHRONIZING
            walletLibrary.refresh(currentWalletHandle)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Error starting refresh: ${e.message}", e)
        }
    }

    override suspend fun pauseRefresh(): Result<Unit> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            // TODO: Implement pause refresh in JNI
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Error pausing refresh: ${e.message}", e)
        }
    }

    override suspend fun setDaemon(
        address: String,
        useSSL: Boolean,
        username: String?,
        password: String?
    ): Result<Unit> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            val success = walletLibrary.setDaemon(
                walletHandle = currentWalletHandle,
                daemonAddress = address,
                useSSL = useSSL,
                username = username,
                password = password
            )

            if (success) {
                _connectionStatus.value = ConnectionStatus.CONNECTING
                Result.Success(Unit)
            } else {
                Result.Error("Failed to set daemon")
            }
        } catch (e: Exception) {
            Result.Error("Error setting daemon: ${e.message}", e)
        }
    }

    override suspend fun storeWallet(): Result<Unit> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            val success = walletLibrary.storeWallet(currentWalletHandle)
            if (success) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to store wallet")
            }
        } catch (e: Exception) {
            Result.Error("Error storing wallet: ${e.message}", e)
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(ioDispatcher) {
        if (currentWalletHandle == 0L) {
            return@withContext Result.Error("No wallet opened")
        }

        try {
            val success = walletLibrary.changePassword(
                walletHandle = currentWalletHandle,
                oldPassword = oldPassword,
                newPassword = newPassword
            )
            if (success) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to change password")
            }
        } catch (e: Exception) {
            Result.Error("Error changing password: ${e.message}", e)
        }
    }

    override suspend fun getTransactions(): Result<List<Transaction>> = withContext(ioDispatcher) {
        // TODO: Implement transaction history retrieval via JNI
        Result.Success(emptyList())
    }

    override suspend fun getSubaddressAccounts(): Result<List<SubaddressAccount>> = withContext(ioDispatcher) {
        // TODO: Implement subaddress accounts retrieval via JNI
        Result.Success(emptyList())
    }

    override suspend fun createSubaddressAccount(label: String): Result<SubaddressAccount> = withContext(ioDispatcher) {
        // TODO: Implement subaddress account creation via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun getSubaddresses(accountIndex: Int): Result<List<Subaddress>> = withContext(ioDispatcher) {
        // TODO: Implement subaddresses retrieval via JNI
        Result.Success(emptyList())
    }

    override suspend fun createSubaddress(
        accountIndex: Int,
        label: String
    ): Result<Subaddress> = withContext(ioDispatcher) {
        // TODO: Implement subaddress creation via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun getOutputs(): Result<List<Output>> = withContext(ioDispatcher) {
        // TODO: Implement outputs retrieval via JNI
        Result.Success(emptyList())
    }

    override suspend fun createTransaction(
        address: String,
        amount: Long,
        feeLevel: Int,
        description: String,
        subtractFeeFromAmount: Boolean
    ): Result<PendingTransactionInfo> = withContext(ioDispatcher) {
        // TODO: Implement transaction creation via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun sendTransaction(txInfo: PendingTransactionInfo): Result<String> = withContext(ioDispatcher) {
        // TODO: Implement transaction sending via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun sweepAll(address: String, feeLevel: Int): Result<String> = withContext(ioDispatcher) {
        // TODO: Implement sweep all via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun exportKeyImages(path: String): Result<String> = withContext(ioDispatcher) {
        // TODO: Implement key images export via JNI
        Result.Error("Not yet implemented")
    }

    override suspend fun importKeyImages(path: String): Result<Int> = withContext(ioDispatcher) {
        // TODO: Implement key images import via JNI
        Result.Error("Not yet implemented")
    }
}
