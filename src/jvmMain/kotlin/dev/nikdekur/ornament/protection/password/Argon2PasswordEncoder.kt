@file:OptIn(ExperimentalEncodingApi::class)

package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ndkore.ext.constantTimeEquals
import dev.nikdekur.ndkore.ext.randomBytes
import dev.nikdekur.ndkore.ext.toHEX
import dev.nikdekur.ndkore.memory.*
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.random.asKotlinRandom
import org.bouncycastle.crypto.params.Argon2Parameters as BCArgon2Parameters

public class Argon2PasswordEncoder(
    public val type: Argon2Type = Argon2Type.ARGON2id,
    public val version: Int = 19,
    public val hashSize: MemoryAmount,
    public val saltSize: MemoryAmount,
    public val parallelism: Int,
    public val memory: MemoryAmount,
    public val iterations: Int,
    public val hashTime: LongRange
) : PasswordEncoder {

    public val defaultAsMeta: Argon2ParametersMeta = Argon2ParametersMeta(
        type = type,
        version = version,
        parallelism = parallelism,
        memory = memory,
        iterations = iterations,
        hashSize = hashSize.toInt(),
    )

    // Random is thread-safe
    public val random: Random = SecureRandom.getInstanceStrong().asKotlinRandom()

    override fun createPassword(password: CharSequence): Password {
        val salt = random.randomBytes(saltSize.toInt())
        val hash = encrypt(password, salt, defaultAsMeta)

        val data = Argon2Data(
            meta = defaultAsMeta,
            salt = salt
        )

        return Argon2Password(
            data = data,
            bytes = hash
        )
    }

    override fun createPassword(data: Password.Data, password: CharSequence): Password {
        require(data is Argon2Data) { "Data must be of type Argon2Data" }

        val hash = encrypt(password, data.salt, data.meta)

        return Argon2Password(
            data = data,
            bytes = hash
        )
    }

    public fun encrypt(password: CharSequence, salt: ByteArray, parameters: Argon2ParametersMeta): ByteArray {
        val builder = BCArgon2Parameters.Builder(parameters.type.toBC())
            .withVersion(parameters.version)
            .withIterations(parameters.iterations)
            .withMemoryAsKB(parameters.memory.toInt(MemoryUnit.KiB))
            .withParallelism(parameters.parallelism)
            .withSalt(salt)
            .build()

        val argon2 = Argon2BytesGenerator()
        argon2.init(builder)

        val result = ByteArray(parameters.hashSize)
        argon2.generateBytes(password.toString().encodeToByteArray(), result, 0, result.size)
        return result
    }


    // Example string: $argon2id$v=19$m=65536,t=4,p=2,h=32$<base64-encoded-salt>$<base64-encoded-hash>

    override fun decodePasswordData(passwordData: String): Argon2Data {
        val parts = passwordData.split("$")
        require(parts.size >= 4) { "Invalid encoded Argon2-hash" }

        // Start parsing from index 1 (after first '$')
        var currentPart = 1

        // Parse algorithm type
        val algorithmType = when (parts[currentPart++]) {
            "argon2d" -> BCArgon2Parameters.ARGON2_d
            "argon2i" -> BCArgon2Parameters.ARGON2_i
            "argon2id" -> BCArgon2Parameters.ARGON2_id
            else -> throw IllegalArgumentException("Invalid algorithm type: ${parts[1]}")
        }

        val builder = BCArgon2Parameters.Builder(algorithmType)

        // Initialize default values
        var memory = 0
        var iterations = 0
        var parallelism = 0
        var hashSize = 0

        // Flexible parsing for remaining parameters
        for (i in currentPart until parts.size - 1) {
            val part = parts[i]

            when {
                part.startsWith("v=") -> {
                    val version = part.substring(2).toInt()
                    builder.withVersion(version)
                    currentPart++
                }

                part.contains(",") -> {
                    // Performance parameters section
                    val perfParams = part.split(",")

                    for (param in perfParams) {
                        when {
                            param.startsWith("m=") -> {
                                memory = param.substring(2).toInt()
                                builder.withMemoryAsKB(memory)
                            }

                            param.startsWith("t=") -> {
                                iterations = param.substring(2).toInt()
                                builder.withIterations(iterations)
                            }

                            param.startsWith("p=") -> {
                                parallelism = param.substring(2).toInt()
                                builder.withParallelism(parallelism)
                            }

                            param.startsWith("h=") -> {
                                hashSize = param.substring(2).toInt()
                            }

                            else -> throw IllegalArgumentException("Unknown parameter: $param")
                        }
                    }
                    currentPart++
                }

                else -> break // Stop parsing if we encounter something unexpected
            }
        }

        // Validate that all required parameters were found
        require(memory > 0) { "Memory parameter missing or invalid" }
        require(iterations > 0) { "Iterations parameter missing or invalid" }
        require(parallelism > 0) { "Parallelism parameter missing or invalid" }
        require(hashSize > 0) { "Hash size parameter missing or invalid" }

        // Build parameters and get salt
        val ready = builder.build()
        val salt = Base64.decode(parts[currentPart])

        // Create and return the result
        return Argon2Data(
            meta = Argon2ParametersMeta(
                type = Argon2Type.fromId(ready.type),
                version = ready.version,
                parallelism = ready.lanes,
                memory = ready.memory.bytes,
                iterations = ready.iterations,
                hashSize = hashSize
            ),
            salt = salt
        )
    }

    override fun decodePassword(password: String): Password {
        val parts = password.split("$")
        require(parts.size >= 4) { "Invalid encoded Argon2-hash" }

        val hash = Base64.decode(parts.last())
        val data = decodePasswordData(password.substringBeforeLast("$"))

        return Argon2Password(
            data = data,
            bytes = hash
        )
    }

    override fun encryptionDelay(): Long {
        val averageTime = hashTime.random(random)
        return averageTime
    }


    public companion object {
        @Suppress("FunctionName", "kotlin:S100")
        /**
         * OWASP Recommended Argon2 Parameters for 2024
         *
         * This function returns the recommended parameters for Argon2 password hashing as per OWASP
         * 2024 guidelines. The parameters are designed to provide a balance between security and
         * performance.
         *
         * @param hashTime The time in milliseconds to hash the password. It highly depends on the
         * user's hardware, so it is recommended to test the hashing time on the user's hardware.
         * @return [Argon2PasswordEncoder] with recommended parameters.
         */
        public fun owasp_recommended_2024(hashTime: LongRange): Argon2PasswordEncoder {
            return Argon2PasswordEncoder(
                hashSize = 32.bytes,
                saltSize = 16.bytes,
                parallelism = 2,
                memory = 64.mebiBytes,
                iterations = 4,
                hashTime = hashTime
            )
        }
    }


    public inner class Argon2Data(
        public val meta: Argon2ParametersMeta,
        public val salt: ByteArray,
    ) : Password.Data {

        override fun serialize(): String {
            return buildString {
                val type = "$" + meta.type.name.lowercase()
                append(type)
                append("\$v=")
                append(meta.version)
                append("\$m=")
                append(meta.memory.bytes)
                append(",t=")
                append(meta.iterations)
                append(",p=")
                append(meta.parallelism)
                append(",h=")
                append(meta.hashSize)

                if (salt.isNotEmpty()) {
                    append("$").append(Base64.encode(salt))
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            val other = other as? Argon2Data ?: return false

            if (meta != other.meta) return false
            if (!salt.constantTimeEquals(other.salt)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = meta.hashCode()
            result = 31 * result + salt.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "Argon2Data(parameters=$meta, salt=${Base64.encode(salt)})"
        }
    }

    public inner class Argon2Password(
        override val data: Argon2Data,
        override val bytes: ByteArray,
    ) : Password {

        override fun serialize(): String {
            return "${data.serialize()}$${Base64.encode(bytes)}"
        }

        override fun equals(other: Any?): Boolean {
            val other = other as? Argon2Password ?: return false

            if (data != other.data) return false
            if (!bytes.constantTimeEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "Argon2Password(data=$data, bytes=${bytes.toHEX()})"
        }
    }
}