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

data class CoinsUiState(
    val outputs: List<org.monero.feather.domain.model.Output> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedOutputs: Set<String> = emptySet()
)

@HiltViewModel
class CoinsViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinsUiState())
    val uiState: StateFlow<CoinsUiState> = _uiState.asStateFlow()

    init {
        loadOutputs()
    }

    fun loadOutputs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // TODO: Загрузить выходы из репозитория
                // val outputs = walletRepository.getOutputs()
                
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    outputs = emptyList(),
                    totalBalance = 0.0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка загрузки выходов"
                )
            }
        }
    }

    fun toggleOutputSelection(keyImage: String) {
        val current = _uiState.value.selectedOutputs
        _uiState.value = _uiState.value.copy(
            selectedOutputs = if (keyImage in current) {
                current - keyImage
            } else {
                current + keyImage
            }
        )
    }

    fun freezeOutputs(): Result<Unit> {
        return try {
            val selected = _uiState.value.selectedOutputs
            if (selected.isEmpty()) {
                return Result.failure(IllegalStateException("Выберите выходы для заморозки"))
            }
            
            // TODO: Заморозить выбранные выходы
            // walletRepository.freezeOutputs(selected)
            
            _uiState.value = _uiState.value.copy(selectedOutputs = emptySet())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun thawOutputs(): Result<Unit> {
        return try {
            val selected = _uiState.value.selectedOutputs
            if (selected.isEmpty()) {
                return Result.failure(IllegalStateException("Выберите выходы для разморозки"))
            }
            
            // TODO: Разморозить выбранные выходы
            // walletRepository.thawOutputs(selected)
            
            _uiState.value = _uiState.value.copy(selectedOutputs = emptySet())
            loadOutputs() // Перезагрузить список
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAvailableBalance(): Double {
        return _uiState.value.outputs
            .filter { !it.isSpent && !it.isFrozen }
            .sumOf { it.amountXMR }
    }
}
