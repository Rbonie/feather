package org.monero.feather.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.monero.feather.data.local.WalletLibrary
import org.monero.feather.data.repository.WalletRepository
import org.monero.feather.data.repository.impl.WalletRepositoryImpl
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
        fun provideCreateWalletUseCase(repository: WalletRepository): CreateWalletUseCase {
            return CreateWalletUseCase(repository)
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
    }
}
