package org.monero.feather.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security manager for encrypting sensitive data using Android Keystore
 */
@Singleton
class SecurityManager @Inject constructor() {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
    }

    private val keyAlias = "feather_wallet_key"

    init {
        generateKeyIfNotExists()
    }

    /**
     * Generate encryption key if it doesn't exist
     */
    private fun generateKeyIfNotExists() {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )

            val parameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()

            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Get the secret key from keystore
     */
    private fun getSecretKey(): SecretKey {
        return keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
    }

    /**
     * Encrypt data using AES-GCM
     * @param plaintext Data to encrypt
     * @return EncryptedData containing IV and ciphertext
     */
    fun encrypt(plaintext: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)

        return EncryptedData(iv, ciphertext)
    }

    /**
     * Decrypt data using AES-GCM
     * @param encryptedData Encrypted data with IV and ciphertext
     * @return Decrypted plaintext
     */
    fun decrypt(encryptedData: EncryptedData): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        return cipher.doFinal(encryptedData.ciphertext)
    }

    /**
     * Encrypt a string
     */
    fun encryptString(plaintext: String): EncryptedData {
        return encrypt(plaintext.toByteArray(Charsets.UTF_8))
    }

    /**
     * Decrypt a string
     */
    fun decryptString(encryptedData: EncryptedData): String {
        return String(decrypt(encryptedData), Charsets.UTF_8)
    }

    /**
     * Delete the encryption key (use with caution!)
     */
    fun deleteKey() {
        keyStore.deleteEntry(keyAlias)
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val TAG_LENGTH_BIT = 128
    }
}

/**
 * Data class representing encrypted data
 */
data class EncryptedData(
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedData
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}
