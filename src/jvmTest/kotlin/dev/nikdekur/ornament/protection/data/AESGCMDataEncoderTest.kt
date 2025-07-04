package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.memory.bytes
import dev.nikdekur.ndkore.memory.toInt
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordEncoder
import java.security.SecureRandom
import kotlin.random.asKotlinRandom
import kotlin.test.*

/**
 * Test class for the [AESGCMDataEncoder] implementation.
 * Extends the base [DataEncoderTest] class to inherit all standard interface tests.
 */
class AESGCMDataEncoderTest : DataEncoderTest() {

    // Create a mock password encoder for testing
    private val mockPasswordEncoder = createMockPasswordEncoder()

    val sampleHKDFInfo = "sampleInfo".encodeToByteArray()

    override fun createEncoder(): DataEncoder {
        return AESGCMDataEncoder.nist_sp800_38d_recommended(
            passwordEncoder = mockPasswordEncoder,
            hkdfInfo = sampleHKDFInfo,
        )
    }

    @Test
    fun `constructor parameters should be correctly initialized`() {
        val customIvSize = 16.bytes
        val customKeySize = 24.bytes
        val customAuthTagSize = 12.bytes

        val encoder = AESGCMDataEncoder(
            passwordEncoder = mockPasswordEncoder,
            ivSize = customIvSize,
            keySize = customKeySize,
            authTagSize = customAuthTagSize,
            hkdfInfo = sampleHKDFInfo,
        )

        assertEquals(customIvSize, encoder.ivSize)
        assertEquals(customKeySize, encoder.keySize)
        assertEquals(customAuthTagSize, encoder.authTagSize)
    }

    @Test
    fun `newCipher should return AES GCM cipher instance`() {
        val encoder = createEncoder() as AESGCMDataEncoder
        val cipher = encoder.newCipher()

        assertEquals("AES/GCM/NoPadding", cipher.algorithm)
    }

    @Test
    fun `random should be initialized with SecureRandom`() {
        val encoder = createEncoder() as AESGCMDataEncoder
        assertNotNull(encoder.random)
    }

    @Test
    fun `encrypted data should include all necessary components`() {
        val encoder = createEncoder() as AESGCMDataEncoder
        val originalData = "Test data".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, "password") as AESGCMEncryptedData

        assertNotNull(encrypted.data)
        assertNotNull(encrypted.passwordDataSerialized)
        assertNotNull(encrypted.hkdfSalt)
        assertNotNull(encrypted.iv)

        assertEquals(encoder.ivSize.toInt(), encrypted.iv.size)
        assertEquals(32, encrypted.hkdfSalt.size) // Fixed size of 32 bytes in the implementation
    }

    @Test
    fun `encrypt should use password encoder to create password`() {
        // This test verifies that the AESGCMDataEncoder correctly uses the provided password encoder
        val mockEncoder = MockPasswordEncoder()
        val dataEncoder =
            AESGCMDataEncoder.nist_sp800_38d_recommended(passwordEncoder = mockEncoder, hkdfInfo = sampleHKDFInfo)

        val originalData = "Test with mock".encodeToByteArray()
        dataEncoder.encrypt(originalData, "testPassword")

        assertTrue(mockEncoder.createPasswordCalled)
    }

    @Test
    fun `decrypt should use password encoder to decode password data`() {
        // This test verifies that the AESGCMDataEncoder correctly uses the provided password encoder for decoding
        val mockEncoder = MockPasswordEncoder()
        val dataEncoder =
            AESGCMDataEncoder.nist_sp800_38d_recommended(passwordEncoder = mockEncoder, hkdfInfo = sampleHKDFInfo)

        val originalData = "Test with mock".encodeToByteArray()
        val encrypted = dataEncoder.encrypt(originalData, "testPassword")

        mockEncoder.createPasswordCalled = false // Reset flag
        dataEncoder.decrypt(encrypted, "testPassword")

        assertTrue(mockEncoder.decodePasswordDataCalled)
    }

    @Test
    fun `decrypt should throw InvalidPasswordException for wrong password`() {
        val encoder = createEncoder()
        val originalData = "Secret data".encodeToByteArray()
        val encrypted = encoder.encrypt(originalData, "correctPassword")

        assertFailsWith<InvalidPasswordException> {
            encoder.decrypt(encrypted, "wrongPassword")
        }
    }

    @Test
    fun `decrypt should throw MalformedDataException for invalid encrypted data type`() {
        val encoder = createEncoder()

        val invalidData = object : EncryptedData {
            override val data: ByteArray = ByteArray(10)
            override fun serialize(): ByteArray = ByteArray(10)
        }

        assertFailsWith<MalformedDataException> {
            encoder.decrypt(invalidData, "password")
        }
    }

    @Test
    fun `AESGCMEncryptedData equals and hashCode should work properly`() {
        val data1 = AESGCMEncryptedData(
            data = "test".encodeToByteArray(),
            passwordDataSerialized = "passwordData".encodeToByteArray(),
            hkdfSalt = ByteArray(32) { 1 },
            iv = ByteArray(12) { 2 }
        )

        val data2 = AESGCMEncryptedData(
            data = "test".encodeToByteArray(),
            passwordDataSerialized = "passwordData".encodeToByteArray(),
            hkdfSalt = ByteArray(32) { 1 },
            iv = ByteArray(12) { 2 }
        )

        val data3 = AESGCMEncryptedData(
            data = "different".encodeToByteArray(),
            passwordDataSerialized = "passwordData".encodeToByteArray(),
            hkdfSalt = ByteArray(32) { 1 },
            iv = ByteArray(12) { 2 }
        )

        assertEquals(data1, data2)
        assertEquals(data1.hashCode(), data2.hashCode())
        assertNotEquals(data1, data3)
        assertNotEquals(data1.hashCode(), data3.hashCode())
    }

    // Helper functions and mock classes for testing

    private fun createMockPasswordEncoder(): PasswordEncoder {
        return object : PasswordEncoder {
            private val random = SecureRandom().asKotlinRandom()

            override fun createPassword(password: CharSequence): Password {
                val mockData = object : Password.Data {
                    override fun serialize(): String = "mockPasswordData"
                }

                return object : Password {
                    override val data: Password.Data = mockData
                    override val bytes: ByteArray = password.toString().encodeToByteArray()
                    override fun serialize(): String = "mockPassword"
                }
            }

            override fun createPassword(data: Password.Data, password: CharSequence): Password {
                return object : Password {
                    override val data: Password.Data = data
                    override val bytes: ByteArray = password.toString().encodeToByteArray()
                    override fun serialize(): String = "mockPassword"
                }
            }

            override fun decodePasswordData(passwordData: String): Password.Data {
                return object : Password.Data {
                    override fun serialize(): String = passwordData
                }
            }

            override fun decodePassword(password: String): Password {
                val mockData = object : Password.Data {
                    override fun serialize(): String = "mockPasswordData"
                }

                return object : Password {
                    override val data: Password.Data = mockData
                    override val bytes: ByteArray = ByteArray(10)
                    override fun serialize(): String = password
                }
            }

            override fun encryptionDelay(): Long = 100
        }
    }

    private class MockPasswordEncoder : PasswordEncoder {
        var createPasswordCalled = false
        var decodePasswordDataCalled = false

        override fun createPassword(password: CharSequence): Password {
            createPasswordCalled = true
            val mockData = object : Password.Data {
                override fun serialize(): String = "mockData"
            }

            return object : Password {
                override val data: Password.Data = mockData
                override val bytes: ByteArray = password.toString().encodeToByteArray()
                override fun serialize(): String = "serialized"
            }
        }

        override fun createPassword(data: Password.Data, password: CharSequence): Password {
            val mockData = object : Password.Data {
                override fun serialize(): String = "mockData"
            }

            return object : Password {
                override val data: Password.Data = mockData
                override val bytes: ByteArray = password.toString().encodeToByteArray()
                override fun serialize(): String = "serialized"
            }
        }

        override fun decodePasswordData(passwordData: String): Password.Data {
            decodePasswordDataCalled = true
            return object : Password.Data {
                override fun serialize(): String = passwordData
            }
        }

        override fun decodePassword(password: String): Password {
            val mockData = object : Password.Data {
                override fun serialize(): String = "mockData"
            }

            return object : Password {
                override val data: Password.Data = mockData
                override val bytes: ByteArray = ByteArray(10)
                override fun serialize(): String = password
            }
        }

        override fun encryptionDelay(): Long = 100
    }
}