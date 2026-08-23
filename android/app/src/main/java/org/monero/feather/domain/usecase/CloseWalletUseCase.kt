package org.monero.feather.domain.usecase

import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class CloseWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.closeWallet()
    }
}
