@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ndkore.memory.MemoryAmount
import kotlinx.serialization.Serializable
import org.bouncycastle.crypto.params.Argon2Parameters

@Serializable
public data class Argon2ParametersMeta(
    val type: Argon2Type = Argon2Type.ARGON2id,
    val version: Int = 19,
    val parallelism: Int,
    val memory: MemoryAmount,
    val iterations: Int,
    val hashSize: Int,
)

public enum class Argon2Type(public val id: Int) {
    ARGON2d(0),
    ARGON2i(1),
    ARGON2id(2);

    public companion object {
        public fun fromId(id: Int): Argon2Type {
            return entries[id]
        }
    }
}

public inline fun Argon2Type.toBC(): Int {
    return when (this) {
        Argon2Type.ARGON2d -> Argon2Parameters.ARGON2_d
        Argon2Type.ARGON2i -> Argon2Parameters.ARGON2_i
        Argon2Type.ARGON2id -> Argon2Parameters.ARGON2_id
    }
}