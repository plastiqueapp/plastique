package io.plastique.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.KeyGenerator

class CryptorTest {
    private val keyAlias = UUID.randomUUID().toString()
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
    private val cryptor = Cryptor.create()

    @Before
    fun setUp() {
        keyStore.load(null)
    }

    @After
    fun tearDown() {
        keyStore.deleteEntry(keyAlias)
    }

    @Test
    fun encrypt_decrypt() {
        val input = randomData(400)

        val encrypted = cryptor.encrypt(input, keyAlias)
        assertFalse(input.contentEquals(encrypted))

        val decrypted = cryptor.decrypt(encrypted, keyAlias)
        assertTrue(input.contentEquals(decrypted))
    }

    @Test
    fun encryptString_decryptString() {
        val input = UUID.randomUUID().toString()

        val encrypted = cryptor.encryptString(input, keyAlias)
        assertNotEquals(input, encrypted)

        val decrypted = cryptor.decryptString(encrypted, keyAlias)
        assertEquals(input, decrypted)
    }

    @Test
    fun encrypt_regeneratesKeyIfExistingIsInvalid() {
        val input = randomData(64)
        generateSecretKey(keyAlias)

        cryptor.encrypt(input, keyAlias)
    }

    @Test
    fun decrypt_deletesInvalidKey() {
        val input = randomData(64)
        val encrypted = cryptor.encrypt(input, keyAlias)

        val invalidKeyAlias = UUID.randomUUID().toString()
        generateSecretKey(invalidKeyAlias)

        try {
            cryptor.decrypt(encrypted, invalidKeyAlias)
            fail("Should have thrown InvalidKeyException")
        } catch (e: InvalidKeyException) {
            // expected
        }

        assertFalse(keyStore.containsAlias(invalidKeyAlias))
    }

    @Test
    fun ivIsRandom() {
        val input = randomData(400)

        val encrypted = cryptor.encrypt(input, keyAlias)
        val encrypted2 = cryptor.encrypt(input, keyAlias)
        assertFalse(encrypted.contentEquals(encrypted2))
    }

    private fun randomData(length: Int): ByteArray {
        return ByteArray(length).also {
            SecureRandom().nextBytes(it)
        }
    }

    private fun generateSecretKey(alias: String): Key {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build())
        return keyGenerator.generateKey()
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }
}
