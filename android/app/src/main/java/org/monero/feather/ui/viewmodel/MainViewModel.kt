package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.monero.feather.domain.usecase.GetBalanceUseCase
import org.monero.feather.domain.usecase.GetAddressUseCase
import org.monero.feather.domain.usecase.CloseWalletUseCase
import org.monero.feather.domain.model.Balance
import org.monero.feather.domain.model.ConnectionStatus
import javax.inject.Inject

/**
 * ViewModel for the main wallet screen
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getAddressUseCase: GetAddressUseCase,
    private val closeWalletUseCase: CloseWalletUseCase
) : ViewModel() {

    data class MainUiState(
        val balance: Balance = Balance(0, 0, 0),
        val address: String = "",
        val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
        val syncProgress: Pair<Long, Long> = 0L to 0L,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _balance = MutableStateFlow(Balance(0, 0, 0))
    val balance: StateFlow<Balance> = _balance.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow(0L to 0L)
    val syncProgress: StateFlow<Pair<Long, Long>> = _syncProgress.asStateFlow()

    init {
        loadBalance()
        loadAddress()
    }

    fun loadBalance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getBalanceUseCase().fold(
                onSuccess = { balance ->
                    _uiState.value = _uiState.value.copy(balance = balance, isLoading = false)
                    _balance.value = balance
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to load balance", isLoading = false)
                }
            )
        }
    }

    fun loadAddress() {
        viewModelScope.launch {
            getAddressUseCase().fold(
                onSuccess = { address ->
                    _uiState.value = _uiState.value.copy(address = address)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to load address")
                }
            )
        }
    }

    fun startSync() {
        // In real implementation, this would trigger wallet synchronization
        viewModelScope.launch {
            // Simulate sync progress
            _syncProgress.value = 100L to 100L
            _connectionStatus.value = ConnectionStatus.CONNECTED
        }
    }

    fun closeWallet() {
        viewModelScope.launch {
            closeWalletUseCase().fold(
                onSuccess = { /* Wallet closed successfully */ },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to close wallet")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
