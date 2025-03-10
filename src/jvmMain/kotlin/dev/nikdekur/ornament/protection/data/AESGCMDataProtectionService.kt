@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ndkore.service.injectOrNull
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.serialization.barray.ByteArrayFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

public open class AESGCMDataProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), DataProtectionService {

    protected val datasetService: DataSetService? by injectOrNull()
    protected val passwordProtectionService: PasswordProtectionService by inject()
    public lateinit var dataset: AESGCMDataProtectionDataSet


    // Use getter instead of field to guarantee thread safety
    public inline fun newCipher(): Cipher {
        return Cipher.getInstance("AES/GCM/NoPadding")
    }

    // Use getter instead of field to guarantee thread safety
    public inline fun newMac(): Mac {
        return Mac.getInstance("HmacSHA256")
    }

    // SecureRandom is thread safe
    public val random: SecureRandom = SecureRandom.getInstanceStrong()

    override suspend fun onEnable() {
        dataset = datasetService?.get<AESGCMDataProtectionDataSet>("data_protection")
            ?: AESGCMDataProtectionDataSet()
    }

    override fun encrypt(data: ByteArray, password: Password): EncryptedData {
        val salt = ByteArray(32)
        random.nextBytes(salt)
        val key = generateKeyFromUnEncryptedPassword(password.bytes, salt)
        val passwordDataSerialized = password.data.serialize().encodeToByteArray()

        val iv = ByteArray(dataset.ivSize).apply { random.nextBytes(this) }

        val cipher = newCipher()

        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(dataset.authTagSize, iv))

        val encryptedData = cipher.doFinal(data)

        return AESGCMEncryptedData(
            data = encryptedData,
            passwordDataSerialized = passwordDataSerialized,
            hkdfSalt = salt,
            iv = iv
        )
    }

    override fun decrypt(data: ByteArray, password: String): ByteArray {

        val data = try {
            ByteArrayFormat.decodeFromByteArray<AESGCMEncryptedData>(data)
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode from byte array" }
            throw MalformedDataException(e.message)
        }

        val passwordHashed = try {
            val passwordDataSerialized = data.passwordDataSerialized.decodeToString()
            val realPasswordData = passwordProtectionService.deserializePasswordData(passwordDataSerialized)
            realPasswordData.toPassword(password)
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode password data" }
            throw MalformedDataException(e.message)
        }

        val key = generateKeyFromUnEncryptedPassword(passwordHashed.bytes, data.hkdfSalt)

        val iv = data.iv
        val encryptedData = data.data

        val cipher = newCipher()
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(dataset.authTagSize, iv))

        return try {
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            throw InvalidPasswordException(e.message)
        }
    }


    /**
     * Derives a key using HKDF (based on HMAC-SHA256).
     *
     * @param passwordHashed - the result of the preliminary hashing (e.g. Argon2)
     * @return SecretKey suitable for AES-256
     */
    private fun generateKeyFromUnEncryptedPassword(passwordHashed: ByteArray, salt: ByteArray): SecretKeySpec {
        // Key size for AES-256: 32 bytes
        val keyLength = 32

        // Use HKDF with an 'info' parameter containing a constant string to separate key usages.
        val info = "AESGCMDataProtectionService".toByteArray(Charsets.UTF_8)
        val derivedKey = hkdfExtractAndExpand(passwordHashed, info, salt, keyLength)
        return SecretKeySpec(derivedKey, "AES")
    }

    /**
     * Implementation of HKDF (RFC 5869) using HMAC-SHA256.
     *
     * @param ikm The input keying material
     * @param info Context-dependent information (can be empty, but here a constant is provided)
     * @param outputLength Desired length of the output key in bytes
     * @return The pair of derived key and salt
     */
    private fun hkdfExtractAndExpand(ikm: ByteArray, info: ByteArray, salt: ByteArray, outputLength: Int): ByteArray {
        val mac = newMac()
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        // HKDF-Extract
        val prk = mac.doFinal(ikm)

        // HKDF-Expand
        val okmStream = ByteArrayOutputStream()
        var previousBlock = ByteArray(0)
        var counter = 1.toByte()
        while (okmStream.size() < outputLength) {
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            mac.reset()
            mac.update(previousBlock)
            mac.update(info)
            mac.update(counter)
            previousBlock = mac.doFinal()
            okmStream.write(previousBlock)
            counter++
        }
        val okm = okmStream.toByteArray()
        return okm.copyOfRange(0, outputLength)
    }


    @Serializable
    public data class AESGCMDataProtectionDataSet(
        val ivSize: Int = 12,
        val keySize: Int = 256,
        val authTagSize: Int = 128,
    )
}

@Serializable
public data class AESGCMEncryptedData(
    override val data: ByteArray,
    val passwordDataSerialized: ByteArray,
    val hkdfSalt: ByteArray,
    val iv: ByteArray,
) : EncryptedData {

    override fun serialize(): ByteArray {
        return ByteArrayFormat.encodeToByteArray(this)
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
