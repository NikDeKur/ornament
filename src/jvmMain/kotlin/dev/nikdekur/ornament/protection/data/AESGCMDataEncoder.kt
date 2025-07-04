@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.ext.HKDFUtils
import dev.nikdekur.ndkore.ext.randomBytes
import dev.nikdekur.ndkore.memory.MemoryAmount
import dev.nikdekur.ndkore.memory.bitsInt
import dev.nikdekur.ndkore.memory.bytes
import dev.nikdekur.ndkore.memory.toInt
import dev.nikdekur.ornament.protection.password.PasswordEncoder
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/**
 * # AESGCMDataEncoder
 *
 * This class provides methods for encrypting and decrypting data using AES-GCM encryption.
 * It uses a password-based key derivation function (PBKDF) to derive a key from the provided password.
 *
 * @property passwordEncoder The password encoder used to create and decode passwords.
 * @property ivSize The size of the initialization vector (IV) used in AES-GCM encryption.
 * @property keySize The size of the key used in AES-GCM encryption.
 * @property authTagSize The size of the authentication tag used in AES-GCM encryption.
 *
 * @constructor Creates an instance of AESGCMDataEncoder with the specified parameters.
 */
public open class AESGCMDataEncoder(
    public val passwordEncoder: PasswordEncoder,
    public val ivSize: MemoryAmount,
    public val keySize: MemoryAmount,
    public val authTagSize: MemoryAmount,
    public val hkdfInfo: ByteArray,
) : DataEncoder {

    public val logger: KLogger = KotlinLogging.logger {}

    /**
     * Creates a new AES/GCM/NoPadding cipher instance.
     *
     * This method returns a new Cipher instance configured for AES-GCM encryption
     * without padding. Using a function instead of a property ensures thread safety
     * as Cipher instances are not thread-safe.
     *
     * @return A new [Cipher] instance for AES-GCM encryption/decryption
     */
    public inline fun newCipher(): Cipher {
        return Cipher.getInstance("AES/GCM/NoPadding")
    }

    /**
     * Secure random number generator for cryptographic operations.
     *
     * This is initialized with a cryptographically strong SecureRandom instance
     * and is thread-safe.
     */
    public val random: Random = SecureRandom.getInstanceStrong().asKotlinRandom()


    /**
     * Encrypts the provided data using the specified password.
     *
     * This method:
     * 1. Creates a password hash using the configured [passwordEncoder]
     * 2. Generates a random HKDF salt for key derivation
     * 3. Derives an encryption key using HKDF
     * 4. Generates a random initialization vector (IV)
     * 5. Encrypts the data using AES-GCM
     *
     * @param data The data to encrypt
     * @param password The password to use for encryption
     * @return An [AESGCMEncryptedData] instance containing the encrypted data and all necessary metadata
     * @throws MalformedDataException if password creation fails
     */
    override fun encrypt(
        data: ByteArray,
        password: String
    ): EncryptedData {

        val password = try {
            passwordEncoder.createPassword(password)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create password" }
            throw MalformedDataException(e.message)
        }

        val hkdfSalt = random.randomBytes(32)

        val key = generateKeyFromUnEncryptedPassword(password.bytes, hkdfSalt)

        val passwordDataSerialized = password.data.serialize().encodeToByteArray()

        val iv = random.randomBytes(ivSize.toInt())

        val cipher = newCipher()

        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(authTagSize.bitsInt, iv))

        val encryptedData = cipher.doFinal(data)

        return AESGCMEncryptedData(
            data = encryptedData,
            passwordDataSerialized = passwordDataSerialized,
            hkdfSalt = hkdfSalt,
            iv = iv
        )
    }


    override fun decodeData(serialized: ByteArray): EncryptedData {
        val buffer = ByteBuffer.wrap(serialized)

        val dataSize = buffer.int
        val data = ByteArray(dataSize)
        buffer[data]

        val passwordDataSize = buffer.int
        val passwordDataSerialized = ByteArray(passwordDataSize)
        buffer[passwordDataSerialized]

        val hkdfSaltSize = buffer.int
        val hkdfSalt = ByteArray(hkdfSaltSize)
        buffer[hkdfSalt]

        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer[iv]

        return AESGCMEncryptedData(
            data = data,
            passwordDataSerialized = passwordDataSerialized,
            hkdfSalt = hkdfSalt,
            iv = iv
        )
    }


    /**
     * Decrypts previously encrypted data using the specified password.
     *
     * This method:
     * 1. Extract the password data from the encrypted data
     * 2. Create a password hash using the provided password and extract data
     * 3. Derive the encryption key using HKDF with the stored salt
     * 4. Decrypts the data using AES-GCM with the stored IV
     *
     * @param data The encrypted data to decrypt (must be an [AESGCMEncryptedData] instance)
     * @param password The password to use for decryption
     * @return The original decrypted data
     * @throws MalformedDataException if the data is not an [AESGCMEncryptedData] instance or password data is invalid
     * @throws InvalidPasswordException if the provided password is incorrect
     */
    override fun decrypt(
        data: EncryptedData,
        password: String
    ): ByteArray {
        val data = data as? AESGCMEncryptedData
            ?: throw MalformedDataException("Invalid data type")

        val passwordHashed = try {
            val passwordDataSerialized = data.passwordDataSerialized.decodeToString()
            val realPasswordData = passwordEncoder.decodePasswordData(passwordDataSerialized)
            passwordEncoder.createPassword(realPasswordData, password)
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode password data" }
            throw MalformedDataException(e.message)
        }

        val key = generateKeyFromUnEncryptedPassword(passwordHashed.bytes, data.hkdfSalt)

        val iv = data.iv
        val encryptedData = data.data

        val cipher = newCipher()
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(authTagSize.bitsInt, iv))

        return try {
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            throw InvalidPasswordException(e.message)
        }
    }


    /**
     * Derives an encryption key from a password hash using HKDF.
     *
     * This method uses HKDF (HMAC-based Key Derivation Function) to derive a key
     * from the provided password hash and salt. The derived key is suitable for
     * use with AES encryption.
     *
     * @param passwordHashed The result of preliminary password hashing (e.g. Argon2)
     * @param salt The salt to use for key derivation
     * @return A [SecretKeySpec] suitable for AES encryption
     */
    protected fun generateKeyFromUnEncryptedPassword(passwordHashed: ByteArray, salt: ByteArray): SecretKeySpec {
        val derivedKey = HKDFUtils.extractAndExpand(passwordHashed, hkdfInfo, salt, keySize.toInt())
        return SecretKeySpec(derivedKey, "AES")
    }


    public companion object {

        /**
         * Creates an instance of [AESGCMDataEncoder] with recommended by NIST SP800-38D parameters.
         *
         * This method is a factory function that allows you to create an instance
         * of [AESGCMDataEncoder] with a custom password encoder and HKDF info.
         *
         * The parameters are set to recommended values:
         * - IV size: 12 bytes
         * - Key size: 32 bytes
         * - Authentication tag size: 16 bytes
         *
         * @param passwordEncoder The password encoder to use for creating and decoding passwords
         * @param hkdfInfo The HKDF info to use for key derivation
         * @return A new instance of [AESGCMDataEncoder]
         */
        @Suppress("FunctionName", "kotlin:S100")
        public fun nist_sp800_38d_recommended(
            passwordEncoder: PasswordEncoder,
            hkdfInfo: ByteArray
        ): AESGCMDataEncoder {
            return AESGCMDataEncoder(
                passwordEncoder = passwordEncoder,
                ivSize = 12.bytes,
                keySize = 32.bytes,
                authTagSize = 16.bytes,
                hkdfInfo = hkdfInfo
            )
        }
    }
}

/**
 * # AESGCMEncryptedData
 *
 * Implementation of [EncryptedData] that stores data encrypted with AES-GCM.
 *
 * This class contains all the information needed to decrypt the data when provided
 * with the correct password:
 * - The encrypted data bytes
 * - The serialized password data (containing algorithm parameters and salt)
 * - The HKDF salt used for key derivation
 * - The initialization vector (IV) used for encryption
 *
 * @property data The encrypted data bytes
 * @property passwordDataSerialized The serialized password data (parameters and salt)
 * @property hkdfSalt The salt used for HKDF key derivation
 * @property iv The initialization vector used for AES-GCM encryption
 */
@Serializable
public data class AESGCMEncryptedData(
    override val data: ByteArray,
    val passwordDataSerialized: ByteArray,
    val hkdfSalt: ByteArray,
    val iv: ByteArray,
) : EncryptedData {

    override fun serialize(): ByteArray {
        val buffer = ByteBuffer.allocate(
            4 * 4 +
                    data.size +
                    passwordDataSerialized.size +
                    hkdfSalt.size +
                    iv.size
        )

        buffer.putInt(data.size)
        buffer.put(data)

        buffer.putInt(passwordDataSerialized.size)
        buffer.put(passwordDataSerialized)

        buffer.putInt(hkdfSalt.size)
        buffer.put(hkdfSalt)

        buffer.putInt(iv.size)
        buffer.put(iv)

        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AESGCMEncryptedData

        if (!data.contentEquals(other.data)) return false
        if (!passwordDataSerialized.contentEquals(other.passwordDataSerialized)) return false
        if (!hkdfSalt.contentEquals(other.hkdfSalt)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + passwordDataSerialized.contentHashCode()
        result = 31 * result + hkdfSalt.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
