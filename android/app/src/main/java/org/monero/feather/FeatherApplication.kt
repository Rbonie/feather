package org.monero.feather

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Feather Wallet Android.
 * Initializes Hilt dependency injection.
 */
@HiltAndroidApp
class FeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize wallet library with app data directory
        // This will be done in the repository implementation
    }
}
