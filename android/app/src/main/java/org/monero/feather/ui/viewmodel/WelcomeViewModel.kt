package org.monero.feather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.monero.feather.data.repository.WalletRepository
import org.monero.feather.domain.model.NetworkType
import org.monero.feather.domain.model.Result
import javax.inject.Inject

/**
 * ViewModel for the Welcome screen
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }
}
