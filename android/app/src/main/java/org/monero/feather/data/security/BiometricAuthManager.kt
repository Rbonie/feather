package org.monero.feather.data.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Biometric authentication manager for fingerprint/face unlock
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ActivityContext private val context: Context
) {

    private val authResultChannel = Channel<BiometricResult>(Channel.BUFFERED)
    val authResultFlow = authResultChannel.receiveAsFlow()

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Get the reason why biometric auth is not available
     */
    fun getBiometricUnavailableReason(): String {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            else -> "Unknown error"
        }
    }

    /**
     * Show biometric authentication prompt
     */
    fun authenticate(
        activity: Activity,
        title: String = "Authenticate",
        subtitle: String = "Use your biometric to unlock",
        negativeButtonText: String = "Cancel"
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authResultChannel.trySend(BiometricResult.Success)
            }

            override fun onAuthenticationFailed() {
                authResultChannel.trySend(BiometricResult.Failed)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authResultChannel.trySend(BiometricResult.Error(errorCode, errString.toString()))
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Authenticate with device credentials (PIN/Pattern/Password) as fallback
     */
    fun authenticateWithDeviceCredential(
        activity: Activity,
        title: String = "Authenticate",
        subtitle: String = "Use your device credentials"
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authResultChannel.trySend(BiometricResult.Success)
            }

            override fun onAuthenticationFailed() {
                authResultChannel.trySend(BiometricResult.Failed)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authResultChannel.trySend(BiometricResult.Error(errorCode, errString.toString()))
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Sealed class representing biometric authentication results
 */
sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Error(val errorCode: Int, val message: String) : BiometricResult()
}
