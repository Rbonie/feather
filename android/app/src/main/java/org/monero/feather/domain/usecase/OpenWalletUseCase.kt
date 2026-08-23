package org.monero.feather.domain.usecase

import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class OpenWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(name: String, password: String): Result<Unit> {
        return repository.openWallet(name, password)
    }
}
