package org.monero.feather.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.monero.feather.data.service.SyncState
import org.monero.feather.data.service.WalletSyncWorker
import org.monero.feather.data.sync.SessionSyncManager
import org.monero.feather.domain.usecase.InitializeDesktopSyncUseCase
import javax.inject.Inject

/**
 * ViewModel для управления синхронизацией с десктопом и фоновой синхронизацией
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val sessionSyncManager: SessionSyncManager,
    private val initializeDesktopSyncUseCase: InitializeDesktopSyncUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()
    
    init {
        observeSyncState()
    }
    
    /**
     * Наблюдение за состоянием синхронизации
     */
    private fun observeSyncState() {
        viewModelScope.launch {
            sessionSyncManager.syncEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(
                    isSyncEnabled = enabled
                )
            }
        }
        
        viewModelScope.launch {
            sessionSyncManager.sessionIdFlow.collect { sessionId ->
                _uiState.value = _uiState.value.copy(
                    sessionId = sessionId,
                    isSyncInitialized = sessionId != null
                )
            }
        }
        
        viewModelScope.launch {
            sessionSyncManager.desktopDeviceIdFlow.collect { deviceId ->
                _uiState.value = _uiState.value.copy(
                    desktopDeviceId = deviceId
                )
            }
        }
        
        viewModelScope.launch {
            sessionSyncManager.lastSyncTimestampFlow.collect { timestamp ->
                _uiState.value = _uiState.value.copy(
                    lastSyncTimestamp = timestamp
                )
            }
        }
    }
    
    /**
     * Инициализация синхронизации с десктоп устройством
     */
    fun initializeDesktopSync(
        sessionId: String,
        desktopDeviceId: String,
        sessionKey: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                initializeDesktopSyncUseCase.execute(
                    sessionId = sessionId,
                    desktopDeviceId = desktopDeviceId,
                    sessionKey = sessionKey
                )
                
                _uiState.value = _uiState.value.copy(
                    isSyncInitialized = true,
                    isSyncEnabled = true
                )
                
                onSuccess.invoke()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to initialize sync")
            }
        }
    }
    
    /**
     * Включение/выключение синхронизации
     */
    fun toggleSync(enabled: Boolean) {
        viewModelScope.launch {
            sessionSyncManager.setSyncEnabled(enabled)
            _uiState.value = _uiState.value.copy(isSyncEnabled = enabled)
            
            if (enabled) {
                // Планируем синхронизацию
            } else {
                // Отменяем синхронизацию
            }
        }
    }
    
    /**
     * Принудительная синхронизация
     */
    fun forceSync(context: android.content.Context, walletId: String) {
        WalletSyncWorker.schedulePeriodicSync(context, walletId)
        _uiState.value = _uiState.value.copy(isSyncing = true)
    }
    
    /**
     * Обновление состояния синхронизации из сервиса
     */
    fun updateSyncState(syncState: SyncState) {
        when (syncState) {
            is SyncState.IDLE -> {
                _uiState.value = _uiState.value.copy(isSyncing = false, syncProgress = 0)
            }
            is SyncState.SYNCING -> {
                _uiState.value = _uiState.value.copy(isSyncing = true, syncProgress = syncState.progress)
            }
            is SyncState.COMPLETED -> {
                _uiState.value = _uiState.value.copy(isSyncing = false, syncProgress = 100)
            }
            is SyncState.ERROR -> {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = syncState.message
                )
            }
        }
    }
    
    /**
     * Получение времени последней синхронизации в читаемом формате
     */
    fun getLastSyncTimeString(): String {
        val timestamp = _uiState.value.lastSyncTimestamp ?: return "Never"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> "${diff / 86400000} days ago"
        }
    }
    
    /**
     * Очистка данных сессии
     */
    fun clearSessionData(onCleared: () -> Unit) {
        viewModelScope.launch {
            sessionSyncManager.clearSessionData()
            _uiState.value = SyncUiState()
            onCleared.invoke()
        }
    }
}

/**
 * UI состояние для синхронизации
 */
data class SyncUiState(
    val isSyncEnabled: Boolean = false,
    val isSyncInitialized: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgress: Int = 0,
    val sessionId: String? = null,
    val desktopDeviceId: String? = null,
    val lastSyncTimestamp: Long? = null,
    val syncError: String? = null
)
