/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.password.argon2

import dev.nikdekur.ndkore.memory.MemoryAmount
import dev.nikdekur.ndkore.memory.MemoryUnit
import dev.nikdekur.ndkore.memory.toInt
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import java.util.*


public data class Argon2Encryptor(
    val saltSize: Int,
    val hashSize: Int,
    val iterations: Int,
    val memory: MemoryAmount,
    val lanes: Int,
    val hashTime: () -> Int
) {
    public inline fun averageHashTime(): Int = hashTime()

    public fun encryptNew(password: String): Pair<ByteArray, Salt> {
        val salt = newSalt()
        return encrypt(password, salt) to salt
    }

    public fun encrypt(password: String, salt: Salt): ByteArray {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13) // 19
            .withIterations(iterations)
            .withMemoryAsKB(memory.toInt(MemoryUnit.KiB))
            .withParallelism(lanes)
            .withSalt(salt.byte)
            .build()

        val argon2 = Argon2BytesGenerator()
        argon2.init(builder)

        val result = ByteArray(hashSize)
        argon2.generateBytes(password.encodeToByteArray(), result, 0, result.size)
        return result
    }


    // Note: SecureRandom is slower nearly 10 times than Random, but it's more secure
    public val random: Random = SecureRandom()

    // SecureRandom (and Random) is not thread-safe, so we need to synchronise it
    @Synchronized
    public fun newSalt(): Salt {
        val array = ByteArray(saltSize)
        random.nextBytes(array)
        return Salt(array)
    }
}