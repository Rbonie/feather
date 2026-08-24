package org.monero.feather.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room database for Feather Wallet
 */
@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@Singleton
abstract class FeatherDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "feather_wallet_db"

        @Volatile
        private var INSTANCE: FeatherDatabase? = null

        @Inject
        fun getInstance(@ApplicationContext context: Context): FeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FeatherDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
