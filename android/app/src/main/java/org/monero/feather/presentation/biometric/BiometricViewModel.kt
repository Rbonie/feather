package org.monero.feather.presentation.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.monero.feather.data.security.BiometricAuthManager
import javax.inject.Inject

/**
 * ViewModel для управления биометрической аутентификацией
 */
@HiltViewModel
class BiometricViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BiometricUiState())
    val uiState: StateFlow<BiometricUiState> = _uiState.asStateFlow()
    
    init {
        checkBiometricAvailability()
    }
    
    /**
     * Проверка доступности биометрической аутентификации
     */
    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val isAvailable = biometricAuthManager.isBiometricAvailable()
            val hasEnrolled = biometricAuthManager.hasEnrolledBiometrics()
            
            _uiState.value = _uiState.value.copy(
                isBiometricAvailable = isAvailable,
                hasEnrolledBiometrics = hasEnrolled
            )
        }
    }
    
    /**
     * Запуск биометрической аутентификации
     */
    fun authenticate(
        title: String = "Unlock Wallet",
        subtitle: String = "Use your biometric to access the wallet",
        onAuthenticated: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!_uiState.value.isBiometricAvailable) {
            onError("Biometric authentication is not available")
            return
        }
        
        _uiState.value = _uiState.value.copy(isAuthenticating = true)
        
        biometricAuthManager.authenticate(
            title = title,
            subtitle = subtitle,
            negativeButtonText = "Use Password",
            onAuthenticated = {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    isAuthenticated = true
                )
                onAuthenticated.invoke()
            },
            onError = { errorCode, errorMessage ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    authError = "Error $errorCode: $errorMessage"
                )
                onError(errorMessage)
            },
            onFailed = {
                _uiState.value = _uiState.value.copy(authAttempts = _uiState.value.authAttempts + 1)
            }
        )
    }
    
    /**
     * Запуск аутентификации с поддержкой пароля устройства
     */
    fun authenticateWithDeviceCredential(
        title: String = "Unlock Wallet",
        subtitle: String = "Use biometric or device credential",
        onAuthenticated: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isAuthenticating = true)
        
        biometricAuthManager.authenticateWithDeviceCredential(
            title = title,
            subtitle = subtitle,
            onAuthenticated = {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    isAuthenticated = true
                )
                onAuthenticated.invoke()
            },
            onError = { errorCode, errorMessage ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    authError = "Error $errorCode: $errorMessage"
                )
                onError(errorMessage)
            },
            onFailed = {
                _uiState.value = _uiState.value.copy(authAttempts = _uiState.value.authAttempts + 1)
            }
        )
    }
    
    /**
     * Сброс состояния аутентификации
     */
    fun resetAuthState() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            authError = null,
            authAttempts = 0
        )
    }
}

/**
 * UI состояние для биометрической аутентификации
 */
data class BiometricUiState(
    val isBiometricAvailable: Boolean = false,
    val hasEnrolledBiometrics: Boolean = false,
    val isAuthenticating: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authError: String? = null,
    val authAttempts: Int = 0
)
