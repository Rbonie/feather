package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.monero.feather.data.repository.WalletRepository
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val autoLockMinutes: Int = 5,
    val networkType: String = "Mainnet",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: Загрузить настройки из репозитория
                // val settings = walletRepository.getSettings()
                
                kotlinx.coroutines.delay(300)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDarkMode = false,
                    isBiometricEnabled = false,
                    autoLockMinutes = 5
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
        // TODO: Сохранить настройку
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
        // TODO: Сохранить настройку и настроить биометрию
    }

    fun setAutoLock(minutes: Int) {
        _uiState.value = _uiState.value.copy(autoLockMinutes = minutes)
        // TODO: Сохранить настройку
    }

    fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            // TODO: Изменить пароль через репозиторий
            // walletRepository.changePassword(oldPassword, newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun backupWallet(): Result<String> {
        return try {
            // TODO: Создать резервную копию
            // val mnemonic = walletRepository.getMnemonic()
            Result.success("Мнемоническая фраза")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteWallet(password: String): Result<Unit> {
        return try {
            // TODO: Удалить кошелек с подтверждением пароля
            // walletRepository.deleteWallet(password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
