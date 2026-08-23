package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.monero.feather.domain.usecase.RestoreWalletUseCase
import org.monero.feather.domain.model.NetworkType
import javax.inject.Inject

/**
 * ViewModel for restoring a wallet from seed phrase
 */
@HiltViewModel
class RestoreWalletViewModel @Inject constructor(
    private val restoreWalletUseCase: RestoreWalletUseCase
) : ViewModel() {

    data class RestoreWalletState(
        val walletName: String = "",
        val seedPhrase: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val networkType: NetworkType = NetworkType.MAINNET,
        val restoreHeight: Long = 0L,
        val isPasswordVisible: Boolean = false,
        val walletRestored: Boolean = false
    )

    private val _state = MutableStateFlow(RestoreWalletState())
    val state: StateFlow<RestoreWalletState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
    }

    fun updateWalletName(name: String) {
        _state.value = _state.value.copy(walletName = name)
    }

    fun updateSeedPhrase(seed: String) {
        _state.value = _state.value.copy(seedPhrase = seed.trim())
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

    fun updateRestoreHeight(height: Long) {
        _state.value = _state.value.copy(restoreHeight = height)
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
        
        if (currentState.seedPhrase.isBlank()) {
            _error.value = "Seed phrase cannot be empty"
            return false
        }
        
        // Basic validation for seed phrase word count (12, 13, 24, or 25 words)
        val wordCount = currentState.seedPhrase.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        if (wordCount !in listOf(12, 13, 24, 25)) {
            _error.value = "Seed phrase must be 12, 13, 24, or 25 words"
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

    fun restoreWallet() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = restoreWalletUseCase(
                name = _state.value.walletName,
                password = _state.value.password,
                mnemonic = _state.value.seedPhrase,
                restoreHeight = _state.value.restoreHeight,
                language = "English"
            )

            _isLoading.value = false

            result.fold(
                onSuccess = { wallet ->
                    _state.value = _state.value.copy(walletRestored = true)
                    _navigationEvent.emit(NavigationEvent.NavigateToMain)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to restore wallet"
                }
            )
        }
    }
}
