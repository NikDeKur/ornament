package dev.nikdekur.ornament.protection.argon2

import dev.nikdekur.ndkore.memory.MemoryAmount
import dev.nikdekur.ndkore.memory.MemoryAmount.Companion.mebiBytes
import dev.nikdekur.ornament.protection.Password
import kotlinx.serialization.SerialName

public data class Argon2SignificanceDataSet(
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
        public val LOWEST: Argon2SignificanceDataSet = Argon2SignificanceDataSet(
            saltSize = 16,
            hashSize = 32,
            iterations = 4,
            memory = 1.mebiBytes,
            lanes = 1,
            hashTime = 0..0
        )

        public val LOW: Argon2SignificanceDataSet = Argon2SignificanceDataSet(
            saltSize = 16,
            hashSize = 32,
            iterations = 6,
            memory = 2.mebiBytes,
            lanes = 2,
            hashTime = 0..0
        )

        public val MEDIUM: Argon2SignificanceDataSet = Argon2SignificanceDataSet(
            saltSize = 16,
            hashSize = 32,
            iterations = 8,
            memory = 4.mebiBytes,
            lanes = 4,
            hashTime = 0..0
        )

        public val HIGH: Argon2SignificanceDataSet = Argon2SignificanceDataSet(
            saltSize = 16,
            hashSize = 32,
            iterations = 12,
            memory = 8.mebiBytes,
            lanes = 6,
            hashTime = 0..0
        )

        public val HIGHEST: Argon2SignificanceDataSet = Argon2SignificanceDataSet(
            saltSize = 32,
            hashSize = 64,
            iterations = 16,
            memory = 16.mebiBytes,
            lanes = 8,
            hashTime = 0..0
        )

        public fun getDefaultFor(significance: Password.Significance): Argon2SignificanceDataSet {
            return when (significance) {
                Password.Significance.LOWEST -> LOWEST
                Password.Significance.LOW -> LOW
                Password.Significance.MEDIUM -> MEDIUM
                Password.Significance.HIGH -> HIGH
                Password.Significance.HIGHEST -> HIGHEST
            }
        }
    }
}