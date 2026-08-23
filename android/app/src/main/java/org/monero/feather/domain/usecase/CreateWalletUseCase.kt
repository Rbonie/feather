package org.monero.feather.domain.usecase

import org.monero.feather.domain.model.Wallet
import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(name: String, password: String, language: String = "English"): Result<Wallet> {
        return repository.createWallet(name, password, language)
    }
}
