package org.monero.feather.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер для синхронизации сессий между десктоп и мобильными устройствами
 * Использует DataStore для хранения состояния сессии
 */
@Singleton
class SessionSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_sync")
        
        val SESSION_ID_KEY = stringPreferencesKey("session_id")
        val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")
        val DESKTOP_DEVICE_ID_KEY = stringPreferencesKey("desktop_device_id")
        val SYNC_ENABLED_KEY = stringPreferencesKey("sync_enabled")
        val ENCRYPTED_SESSION_KEY_KEY = stringPreferencesKey("encrypted_session_key")
    }
    
    /**
     * Поток ID сессии
     */
    val sessionIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SESSION_ID_KEY]
    }
    
    /**
     * Поток времени последней синхронизации
     */
    val lastSyncTimestampFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIMESTAMP_KEY]
    }
    
    /**
     * Поток ID десктоп устройства
     */
    val desktopDeviceIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DESKTOP_DEVICE_ID_KEY]
    }
    
    /**
     * Поток состояния включения синхронизации
     */
    val syncEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_ENABLED_KEY]?.toBoolean() ?: false
    }
    
    /**
     * Сохранение ID сессии
     */
    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[SESSION_ID_KEY] = sessionId
        }
    }
    
    /**
     * Сохранение времени последней синхронизации
     */
    suspend fun updateLastSyncTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }
    
    /**
     * Сохранение ID десктоп устройства
     */
    suspend fun saveDesktopDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[DESKTOP_DEVICE_ID_KEY] = deviceId
        }
    }
    
    /**
     * Включение/выключение синхронизации
     */
    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_ENABLED_KEY] = enabled.toString()
        }
    }
    
    /**
     * Сохранение зашифрованного ключа сессии
     */
    suspend fun saveEncryptedSessionKey(encryptedKey: String) {
        context.dataStore.edit { preferences ->
            preferences[ENCRYPTED_SESSION_KEY_KEY] = encryptedKey
        }
    }
    
    /**
     * Получение зашифрованного ключа сессии
     */
    suspend fun getEncryptedSessionKey(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[ENCRYPTED_SESSION_KEY_KEY]
        }.firstOrNull()
    }
    
    /**
     * Очистка всех данных сессии
     */
    suspend fun clearSessionData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Проверка необходимости синхронизации
     */
    suspend fun needsSync(syncIntervalMs: Long = 300000): Boolean {
        val lastSync = lastSyncTimestampFlow.firstOrNull() ?: return true
        val isEnabled = syncEnabledFlow.firstOrNull() ?: return false
        
        if (!isEnabled) return false
        
        return (System.currentTimeMillis() - lastSync) > syncIntervalMs
    }
}
