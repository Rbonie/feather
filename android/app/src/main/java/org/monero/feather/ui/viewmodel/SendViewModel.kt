package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.monero.feather.domain.usecase.SendTransactionUseCase
import org.monero.feather.domain.usecase.GetBalanceUseCase
import javax.inject.Inject

data class SendUiState(
    val address: String = "",
    val amount: String = "",
    val paymentId: String = "",
    val estimatedFee: String = "0.00012",
    val totalAmount: String = "",
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SendViewModel @Inject constructor(
    private val sendTransactionUseCase: SendTransactionUseCase,
    private val getBalanceUseCase: GetBalanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendUiState())
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(
            address = address.trim(),
            isValid = validateForm(address.trim(), _uiState.value.amount)
        )
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount.trim(),
            totalAmount = calculateTotal(amount.trim(), _uiState.value.estimatedFee),
            isValid = validateForm(_uiState.value.address, amount.trim())
        )
    }

    fun onPaymentIdChange(paymentId: String) {
        _uiState.value = _uiState.value.copy(
            paymentId = paymentId.trim()
        )
    }

    fun setMaxAmount() {
        viewModelScope.launch {
            try {
                val balanceResult = getBalanceUseCase()
                balanceResult.fold(
                    onSuccess = { balance ->
                        val fee = _uiState.value.estimatedFee.toDoubleOrNull() ?: 0.0
                        val maxAmount = (balance.unlockedPico - fee.toLong() * 1000000000000L).coerceAtLeast(0L)
                        val maxAmountXMR = maxAmount / 1000000000000.0
                        _uiState.value = _uiState.value.copy(
                            amount = String.format("%.12f", maxAmountXMR),
                            totalAmount = String.format("%.12f", maxAmountXMR + fee),
                            isValid = validateForm(_uiState.value.address, String.format("%.12f", maxAmountXMR))
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error getting balance: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error getting balance: ${e.message}"
                )
            }
        }
    }

    fun sendTransaction() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isSuccess = false)
            
            try {
                val amount = _uiState.value.amount.toDoubleOrNull() 
                    ?: throw IllegalArgumentException("Invalid amount")
                
                // Convert XMR to pico (1 XMR = 10^12 pico)
                val amountPico = (amount * 1000000000000.0).toLong()
                val feePico = (_uiState.value.estimatedFee.toDouble() * 1000000000000.0).toLong()
                
                val result = sendTransactionUseCase(
                    address = _uiState.value.address,
                    amount = amountPico,
                    fee = feePico,
                    paymentId = _uiState.value.paymentId.ifBlank { null }
                )
                
                result.fold(
                    onSuccess = { txHash ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            address = "",
                            amount = "",
                            paymentId = "",
                            totalAmount = ""
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error sending transaction"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error sending transaction"
                )
            }
        }
    }

    private fun validateForm(address: String, amount: String): Boolean {
        return address.isNotEmpty() && 
               address.length >= 95 && // Minimum Monero address length
               amount.isNotEmpty() && 
               amount.toDoubleOrNull() != null &&
               amount.toDoubleOrNull()!! > 0
    }

    private fun calculateTotal(amount: String, fee: String): String {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        val feeValue = fee.toDoubleOrNull() ?: 0.0
        return String.format("%.12f", amountValue + feeValue)
    }
}
