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

data class HistoryUiState(
    val transactions: List<org.monero.feather.domain.model.Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: TransactionFilter = TransactionFilter.ALL
)

enum class TransactionFilter {
    ALL,
    INCOMING,
    OUTGOING,
    PENDING
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // TODO: Загрузить транзакции из репозитория
                // val transactions = walletRepository.getTransactions()
                
                // Имитация загрузки
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    transactions = emptyList() // Заменить на реальные данные
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка загрузки истории"
                )
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        // Фильтрация будет применена в UI
    }

    fun getFilteredTransactions(): List<org.monero.feather.domain.model.Transaction> {
        return when (_uiState.value.selectedFilter) {
            TransactionFilter.ALL -> _uiState.value.transactions
            TransactionFilter.INCOMING -> _uiState.value.transactions.filter { it.amount > 0 }
            TransactionFilter.OUTGOING -> _uiState.value.transactions.filter { it.amount < 0 }
            TransactionFilter.PENDING -> _uiState.value.transactions.filter { it.isPending }
        }
    }
}
