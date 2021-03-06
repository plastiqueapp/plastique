package io.plastique.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

abstract class Cryptor {
    fun encryptString(input: String, keyAlias: String): String {
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val encrypted = encrypt(inputBytes, keyAlias)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptString(input: String, keyAlias: String): String {
        val inputBytes = Base64.decode(input, Base64.DEFAULT)
        val decrypted = decrypt(inputBytes, keyAlias)
        return String(decrypted, Charsets.UTF_8)
    }

    abstract fun encrypt(input: ByteArray, keyAlias: String): ByteArray

    abstract fun decrypt(input: ByteArray, keyAlias: String): ByteArray
}

class NoCryptor : Cryptor() {
    override fun encrypt(input: ByteArray, keyAlias: String): ByteArray = input

    override fun decrypt(input: ByteArray, keyAlias: String): ByteArray = input
}

@RequiresApi(Build.VERSION_CODES.M)
class AesCryptor : Cryptor() {
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)

    init {
        keyStore.load(null)
    }

    override fun encrypt(input: ByteArray, keyAlias: String): ByteArray {
        val iv = generateIv(IV_LENGTH)
        var secretKey = keyStore.getKey(keyAlias, null) ?: generateSecretKey(keyAlias)

        val cipherText = try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey, iv)
            cipher.doFinal(input)
        } catch (e: InvalidKeyException) {
            keyStore.deleteEntry(keyAlias)
            secretKey = generateSecretKey(keyAlias)

            val cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey, iv)
            cipher.doFinal(input)
        }

        val result = ByteBuffer.allocate(Int.SIZE_BYTES + iv.size + cipherText.size)
        result.putInt(iv.size)
        result.put(iv)
        result.put(cipherText)
        return result.array()
    }

    override fun decrypt(input: ByteArray, keyAlias: String): ByteArray {
        val secretKey = keyStore.getKey(keyAlias, null)
            ?: throw IllegalStateException("Secret key with alias '$keyAlias' doesn't exist")

        val buffer = ByteBuffer.wrap(input)
        val ivLength = buffer.int
        if (ivLength != IV_LENGTH && ivLength != IV_LENGTH_MAX) {
            throw IllegalArgumentException("Invalid IV length: $ivLength")
        }
        val iv = ByteArray(ivLength)
        buffer.get(iv)

        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)

        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE, secretKey, iv)
            return cipher.doFinal(cipherText)
        } catch (e: InvalidKeyException) {
            keyStore.deleteEntry(keyAlias)
            throw e
        }
    }

    private fun getCipher(mode: Int, secretKey: Key, iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(mode, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher
    }

    private fun generateIv(length: Int): ByteArray {
        val iv = ByteArray(length)
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun generateSecretKey(alias: String): Key {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build())
        return keyGenerator.generateKey()
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val IV_LENGTH = 12
        private const val IV_LENGTH_MAX = 16
        private const val GCM_TAG_LENGTH = 128
    }
}
