package org.monero.feather.domain.usecase

import org.monero.feather.data.sync.SessionSyncManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase для инициализации синхронизации с десктоп устройством
 */
@Singleton
class InitializeDesktopSyncUseCase @Inject constructor(
    private val sessionSyncManager: SessionSyncManager,
    private val encryptionManager: EncryptionManager
) {
    
    /**
     * Инициализация синхронизации с десктоп устройством
     * @param sessionId ID сессии синхронизации
     * @param desktopDeviceId ID десктоп устройства
     * @param sessionKey ключ сессии (будет зашифрован)
     */
    suspend fun execute(
        sessionId: String,
        desktopDeviceId: String,
        sessionKey: String
    ) {
        // Шифруем ключ сессии
        val encryptedKey = encryptionManager.encrypt(sessionKey)
        
        // Сохраняем данные сессии
        sessionSyncManager.saveSessionId(sessionId)
        sessionSyncManager.saveDesktopDeviceId(desktopDeviceId)
        sessionSyncManager.saveEncryptedSessionKey(encryptedKey)
        sessionSyncManager.setSyncEnabled(true)
        sessionSyncManager.updateLastSyncTimestamp()
    }
}
