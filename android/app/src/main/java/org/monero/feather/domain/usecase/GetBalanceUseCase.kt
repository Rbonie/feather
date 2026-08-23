package org.monero.feather.domain.usecase

import org.monero.feather.domain.model.Balance
import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class GetBalanceUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): Result<Balance> {
        return repository.getBalance()
    }
}
