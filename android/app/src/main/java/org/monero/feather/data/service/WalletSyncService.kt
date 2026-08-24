package org.monero.feather.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import org.monero.feather.MainActivity
import org.monero.feather.R
import org.monero.feather.data.repository.WalletRepository
import org.monero.feather.domain.model.Wallet

/**
 * Background service for wallet synchronization
 */
@AndroidEntryPoint
class WalletSyncService : Service() {

    @Inject
    lateinit var walletRepository: WalletRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    companion object {
        const val CHANNEL_ID = "wallet_sync_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SYNC = "org.monero.feather.START_SYNC"
        const val ACTION_STOP_SYNC = "org.monero.feather.STOP_SYNC"

        fun getStartIntent(context: Context): Intent {
            return Intent(context, WalletSyncService::class.java).apply {
                action = ACTION_START_SYNC
            }
        }

        fun getStopIntent(context: Context): Intent {
            return Intent(context, WalletSyncService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SYNC -> startSync()
            ACTION_STOP_SYNC -> stopSync()
        }
        return START_STICKY
    }

    private fun startSync() {
        showNotification("Syncing wallet...", 0)

        syncJob = serviceScope.launch {
            try {
                // Get current wallet and start synchronization
                walletRepository.getCurrentWallet()?.let { wallet ->
                    walletRepository.refreshWallet(wallet.handle)
                    
                    // Update notification with progress
                    withContext(Dispatchers.Main) {
                        showNotification("Wallet synced", 100)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showNotification("Sync failed: ${e.message}", -1)
                }
            }
        }
    }

    private fun stopSync() {
        syncJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wallet Synchronization",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows wallet synchronization progress"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: String, progress: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Feather Wallet")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(progress < 0 || progress < 100)
            .apply {
                if (progress >= 0 && progress <= 100) {
                    setProgress(100, progress, false)
                }
            }
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        syncJob?.cancel()
        serviceScope.cancel()
    }
}
