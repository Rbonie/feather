package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.monero.feather.domain.usecase.CreateWalletUseCase
import org.monero.feather.domain.model.NetworkType
import javax.inject.Inject

/**
 * ViewModel for creating a new wallet
 */
@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase
) : ViewModel() {

    data class CreateWalletState(
        val walletName: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val networkType: NetworkType = NetworkType.MAINNET,
        val isPasswordVisible: Boolean = false,
        val mnemonic: String? = null,
        val walletCreated: Boolean = false
    )

    private val _state = MutableStateFlow(CreateWalletState())
    val state: StateFlow<CreateWalletState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        object ShowSeedPhrase : NavigationEvent()
        object NavigateToMain : NavigationEvent()
    }

    fun updateWalletName(name: String) {
        _state.value = _state.value.copy(walletName = name)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun updateConfirmPassword(password: String) {
        _state.value = _state.value.copy(confirmPassword = password)
    }

    fun updateNetworkType(networkType: NetworkType) {
        _state.value = _state.value.copy(networkType = networkType)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
    }

    fun clearError() {
        _error.value = null
    }

    fun validateInputs(): Boolean {
        val currentState = _state.value
        
        if (currentState.walletName.isBlank()) {
            _error.value = "Wallet name cannot be empty"
            return false
        }
        
        if (currentState.password.length < 8) {
            _error.value = "Password must be at least 8 characters"
            return false
        }
        
        if (currentState.password != currentState.confirmPassword) {
            _error.value = "Passwords do not match"
            return false
        }
        
        return true
    }

    fun createWallet() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = createWalletUseCase(
                name = _state.value.walletName,
                password = _state.value.password,
                language = "English"
            )

            _isLoading.value = false

            result.fold(
                onSuccess = { wallet ->
                    _state.value = _state.value.copy(
                        mnemonic = wallet.mnemonic,
                        walletCreated = true
                    )
                    _navigationEvent.emit(NavigationEvent.ShowSeedPhrase)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to create wallet"
                }
            )
        }
    }

    fun onSeedPhraseConfirmed() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToMain)
        }
    }
}
