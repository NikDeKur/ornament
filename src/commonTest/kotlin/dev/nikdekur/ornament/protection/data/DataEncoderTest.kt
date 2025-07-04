package dev.nikdekur.ornament.protection.data

import kotlin.random.Random
import kotlin.test.*

/**
 * Base test class for verifying the contract of the [DataEncoder] interface.
 *
 * This class provides comprehensive test coverage for any implementation of the
 * [DataEncoder] interface. To test a specific implementation, extend
 * this class and override the [createEncoder] method to provide an instance of
 * your implementation.
 *
 * Example:
 * ```
 * class MyDataEncoderTest : DataEncoderTest() {
 *     override open fun createEncoder(): DataEncoder = MyDataEncoder()
 * }
 * ```
 */
abstract class DataEncoderTest {

    /**
     * Creates an instance of [DataEncoder] implementation to be tested.
     * This method must be overridden by subclasses to provide the specific
     * implementation under test.
     *
     * @return A new instance of [DataEncoder]
     */
    protected abstract fun createEncoder(): DataEncoder

    private val testPassword = "P@ssw0rd123!"
    private val wrongPassword = "WrongPassword456!"

    @Test
    open fun `encrypt should return valid EncryptedData object`() {
        val encoder = createEncoder()
        val data = "Test data".encodeToByteArray()
        val encrypted = encoder.encrypt(data, testPassword)

        assertNotNull(encrypted)
        assertNotNull(encrypted.data)
        assertTrue(encrypted.data.isNotEmpty())
    }

    @Test
    open fun `encrypt should create different output for same input`() {
        val encoder = createEncoder()
        val data = "Test data".encodeToByteArray()
        val encrypted1 = encoder.encrypt(data, testPassword)
        val encrypted2 = encoder.encrypt(data, testPassword)

        // Due to random initialization vectors/salts, encrypting the same data twice should produce different results
        assertFalse(encrypted1.data.contentEquals(encrypted2.data))
    }

    @Test
    open fun `decrypt should correctly recover original data`() {
        val encoder = createEncoder()
        val originalData = "Test data for encryption and decryption".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, testPassword)
        val decrypted = encoder.decrypt(encrypted, testPassword)

        assertTrue(originalData.contentEquals(decrypted))
    }

    @Test
    open fun `decrypt should throw InvalidPasswordException with wrong password`() {
        val encoder = createEncoder()
        val originalData = "Secret information".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, testPassword)

        assertFailsWith<InvalidPasswordException> {
            encoder.decrypt(encrypted, wrongPassword)
        }
    }

    @Test
    open fun `decrypt should throw MalformedDataException with invalid data`() {
        val encoder = createEncoder()

        val invalidData = object : EncryptedData {
            override val data: ByteArray = ByteArray(10) { 0 }
            override fun serialize(): ByteArray = ByteArray(20) { 1 }
        }

        assertFailsWith<MalformedDataException> {
            encoder.decrypt(invalidData, testPassword)
        }
    }

    @Test
    open fun `serialize and deserialize should preserve data`() {
        val encoder = createEncoder()
        val originalData = "Data that needs to be serialized and deserialized".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, testPassword)
        val serialized = encrypted.serialize()

        val deserialized = encoder.decodeData(serialized)
        val decrypted = encoder.decrypt(deserialized, testPassword)

        assertTrue(originalData.contentEquals(decrypted))
    }

    @Test
    open fun `should work with empty data`() {
        val encoder = createEncoder()
        val emptyData = ByteArray(0)
        val encrypted = encoder.encrypt(emptyData, testPassword)
        val decrypted = encoder.decrypt(encrypted, testPassword)

        assertEquals(0, decrypted.size)
    }

    @Test
    open fun `should work with large data`() {
        val encoder = createEncoder()
        val largeData = ByteArray(1_000_000) { Random.nextInt().toByte() }
        val encrypted = encoder.encrypt(largeData, testPassword)
        val decrypted = encoder.decrypt(encrypted, testPassword)

        assertTrue(largeData.contentEquals(decrypted))
    }

    @Test
    open fun `should work with empty passwords`() {
        val encoder = createEncoder()
        val data = "Data with empty password".encodeToByteArray()
        val encrypted = encoder.encrypt(data, "")
        val decrypted = encoder.decrypt(encrypted, "")

        assertTrue(data.contentEquals(decrypted))
    }

    @Test
    open fun `should work with long passwords`() {
        val encoder = createEncoder()
        val data = "Data with very long password".encodeToByteArray()
        val longPassword = "A".repeat(1000)
        val encrypted = encoder.encrypt(data, longPassword)
        val decrypted = encoder.decrypt(encrypted, longPassword)

        assertTrue(data.contentEquals(decrypted))
    }

    @Test
    open fun `should work with Unicode passwords`() {
        val encoder = createEncoder()
        val data = "Data with Unicode password".encodeToByteArray()
        val unicodePassword = "пароль123!@#€£¥•あいうえお"
        val encrypted = encoder.encrypt(data, unicodePassword)
        val decrypted = encoder.decrypt(encrypted, unicodePassword)

        assertTrue(data.contentEquals(decrypted))
    }

    @Test
    open fun `encrypted data should be significantly different from original`() {
        val encoder = createEncoder()
        val originalData = "This is sensitive information".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, testPassword)

        // The encrypted data should be different from the original
        // We'll check that at least 90% of bytes are different
        var differentBytes = 0
        val minLength = minOf(originalData.size, encrypted.data.size)

        for (i in 0 until minLength) {
            if (originalData[i] != encrypted.data[i]) {
                differentBytes++
            }
        }

        val percentDifferent = differentBytes.toDouble() / minLength
        assertTrue(percentDifferent > 0.9, "Encrypted data is too similar to original")
    }
}