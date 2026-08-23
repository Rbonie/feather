package org.monero.feather.domain.usecase

import org.monero.feather.domain.model.Wallet
import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class RestoreWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(
        name: String,
        password: String,
        mnemonic: String,
        restoreHeight: Long = 0L,
        language: String = "English"
    ): Result<Wallet> {
        return repository.restoreWallet(name, password, mnemonic, restoreHeight, language)
    }
}
