package org.monero.feather.domain.usecase

import org.monero.feather.domain.model.Transaction
import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(count: Int = 50, offset: Int = 0): Result<List<Transaction>> {
        return repository.getTransactions(count, offset)
    }
}
