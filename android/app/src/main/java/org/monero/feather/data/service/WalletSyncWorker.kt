package org.monero.feather.data.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.monero.feather.data.local.dao.WalletDao
import org.monero.feather.domain.usecase.SyncWalletUseCase
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker для фоновой синхронизации кошелька через WorkManager
 */
@HiltWorker
class WalletSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val walletDao: WalletDao,
    private val syncWalletUseCase: SyncWalletUseCase
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "wallet_sync_work"
        const val WALLET_ID_KEY = "wallet_id"
        const val PROGRESS_KEY = "sync_progress"
        
        private const val SYNC_INTERVAL_MINUTES = 5L
    }
    
    override suspend fun doWork(): Result {
        val walletId = inputData.getString(WALLET_ID_KEY)
            ?: return Result.failure()
        
        Timber.d("Starting wallet sync for: $walletId")
        
        return try {
            // Получаем информацию о кошельке
            val wallet = walletDao.getWalletById(walletId)
                ?: return Result.failure()
            
            // Выполняем синхронизацию
            setProgress(workDataOf(PROGRESS_KEY to 0))
            
            syncWalletUseCase.execute(walletId) { progress ->
                setProgress(workDataOf(PROGRESS_KEY to progress))
            }
            
            // Обновляем высоту последней синхронизации
            walletDao.updateSyncHeight(
                walletId = walletId,
                height = wallet.lastSyncHeight,
                timestamp = System.currentTimeMillis()
            )
            
            Timber.d("Wallet sync completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Wallet sync failed")
            
            // Повторяем при ошибке сети
            if (isNetworkRelated(e)) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private fun isNetworkRelated(exception: Exception): Boolean {
        // Проверка на ошибки сети для повторной попытки
        return exception.message?.contains("network", ignoreCase = true) == true ||
               exception.message?.contains("connection", ignoreCase = true) == true ||
               exception.message?.contains("timeout", ignoreCase = true) == true
    }
    
    /**
     * Планирование периодической синхронизации
     */
    companion object Scheduler {
        
        fun schedulePeriodicSync(context: Context, walletId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(false)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<WalletSyncWorker>(
                SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
            .setInputData(workDataOf(WALLET_ID_KEY to walletId))
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(walletId)
            .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            Timber.d("Periodic wallet sync scheduled")
        }
        
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("Periodic wallet sync cancelled")
        }
        
        fun getSyncProgressFlow(context: Context): androidx.work.WorkInfo.State? {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()
            
            return workInfos.firstOrNull()?.state
        }
    }
}
