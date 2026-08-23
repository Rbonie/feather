package org.monero.feather.domain.usecase

import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class SendTransactionUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(
        address: String,
        amount: Long,
        fee: Long = 0L,
        paymentId: String? = null
    ): Result<String> {
        return repository.sendTransaction(address, amount, fee, paymentId)
    }
}
