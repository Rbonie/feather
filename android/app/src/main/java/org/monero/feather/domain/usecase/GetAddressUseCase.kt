package org.monero.feather.domain.usecase

import org.monero.feather.domain.repository.WalletRepository
import javax.inject.Inject

class GetAddressUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(accountIndex: Int = 0, addressIndex: Int = 0): Result<String> {
        return repository.getAddress(accountIndex, addressIndex)
    }
}
