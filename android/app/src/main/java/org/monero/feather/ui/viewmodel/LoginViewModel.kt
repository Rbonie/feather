package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.monero.feather.domain.usecase.OpenWalletUseCase
import javax.inject.Inject

/**
 * ViewModel for the Login screen (wallet password entry)
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val openWalletUseCase: OpenWalletUseCase
) : ViewModel() {

    data class LoginState(
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun openWallet(walletName: String) {
        if (_state.value.password.isEmpty()) {
            _state.value = _state.value.copy(error = "Please enter your password")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = openWalletUseCase(
                name = walletName,
                password = _state.value.password
            )

            _state.value = _state.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    _navigationEvent.emit(NavigationEvent.NavigateToMain)
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(error = exception.message ?: "Failed to open wallet")
                }
            )
        }
    }
}
