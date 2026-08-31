package org.monero.feather.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.monero.feather.data.local.WalletLibrary
import org.monero.feather.data.local.dao.TransactionDao
import org.monero.feather.data.local.dao.WalletDao
import org.monero.feather.data.local.database.FeatherDatabase
import org.monero.feather.data.repository.WalletRepository
import org.monero.feather.data.repository.impl.WalletRepositoryImpl
import org.monero.feather.data.security.BiometricAuthManager
import org.monero.feather.data.security.EncryptionManager
import org.monero.feather.data.sync.SessionSyncManager
import org.monero.feather.domain.usecase.*
import javax.inject.Singleton

/**
 * Hilt module for providing data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Module
    @InstallIn(SingletonComponent::class)
    object UseCaseModule {

        @Provides
        @Singleton
        fun provideCreateWalletUseCase(
            walletDao: WalletDao,
            encryptionManager: EncryptionManager
        ): CreateWalletUseCase {
            return CreateWalletUseCase(walletDao, encryptionManager)
        }

        @Provides
        @Singleton
        fun provideRestoreWalletUseCase(repository: WalletRepository): RestoreWalletUseCase {
            return RestoreWalletUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideOpenWalletUseCase(repository: WalletRepository): OpenWalletUseCase {
            return OpenWalletUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideCloseWalletUseCase(repository: WalletRepository): CloseWalletUseCase {
            return CloseWalletUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideGetBalanceUseCase(repository: WalletRepository): GetBalanceUseCase {
            return GetBalanceUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideGetAddressUseCase(repository: WalletRepository): GetAddressUseCase {
            return GetAddressUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideGetTransactionsUseCase(repository: WalletRepository): GetTransactionsUseCase {
            return GetTransactionsUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideSendTransactionUseCase(repository: WalletRepository): SendTransactionUseCase {
            return SendTransactionUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideSyncWalletUseCase(
            walletDao: WalletDao,
            encryptionManager: EncryptionManager
        ): SyncWalletUseCase {
            return SyncWalletUseCase(walletDao, encryptionManager)
        }

        @Provides
        @Singleton
        fun provideInitializeDesktopSyncUseCase(
            sessionSyncManager: SessionSyncManager,
            encryptionManager: EncryptionManager
        ): InitializeDesktopSyncUseCase {
            return InitializeDesktopSyncUseCase(sessionSyncManager, encryptionManager)
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object DatabaseModule {

        @Provides
        @Singleton
        fun provideFeatherDatabase(@ApplicationContext context: Context): FeatherDatabase {
            return FeatherDatabase.getDatabase(context)
        }

        @Provides
        @Singleton
        fun provideWalletDao(database: FeatherDatabase): WalletDao {
            return database.walletDao()
        }

        @Provides
        @Singleton
        fun provideTransactionDao(database: FeatherDatabase): TransactionDao {
            return database.transactionDao()
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object SecurityModule {

        @Provides
        @Singleton
        fun provideEncryptionManager(): EncryptionManager {
            return EncryptionManager()
        }

        @Provides
        @Singleton
        fun provideSessionSyncManager(@ApplicationContext context: Context): SessionSyncManager {
            return SessionSyncManager(context)
        }
    }
}
