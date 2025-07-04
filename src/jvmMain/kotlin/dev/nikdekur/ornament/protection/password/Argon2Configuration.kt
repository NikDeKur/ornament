package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ndkore.memory.MemoryAmount
import dev.nikdekur.ndkore.memory.mebiBytes
import kotlinx.serialization.SerialName

public data class Argon2Configuration(
    @SerialName("salt_size")
    val saltSize: Int? = null,

    @SerialName("hash_size")
    val hashSize: Int? = null,

    val iterations: Int? = null,

    val memory: MemoryAmount? = null,

    val lanes: Int? = null,

    @SerialName("hash_time")
    val hashTime: IntRange? = null
) {

    public companion object {
        public val LOWEST: Argon2Configuration = Argon2Configuration(
            saltSize = 16,
            hashSize = 32,
            iterations = 4,
            memory = 1.mebiBytes,
            lanes = 1,
            hashTime = 0..0
        )

        public val LOW: Argon2Configuration = Argon2Configuration(
            saltSize = 16,
            hashSize = 32,
            iterations = 6,
            memory = 2.mebiBytes,
            lanes = 2,
            hashTime = 0..0
        )

        public val MEDIUM: Argon2Configuration = Argon2Configuration(
            saltSize = 16,
            hashSize = 32,
            iterations = 8,
            memory = 4.mebiBytes,
            lanes = 4,
            hashTime = 0..0
        )

        public val HIGH: Argon2Configuration = Argon2Configuration(
            saltSize = 16,
            hashSize = 32,
            iterations = 12,
            memory = 8.mebiBytes,
            lanes = 6,
            hashTime = 0..0
        )

        public val HIGHEST: Argon2Configuration = Argon2Configuration(
            saltSize = 32,
            hashSize = 64,
            iterations = 16,
            memory = 16.mebiBytes,
            lanes = 8,
            hashTime = 0..0
        )
    }
}