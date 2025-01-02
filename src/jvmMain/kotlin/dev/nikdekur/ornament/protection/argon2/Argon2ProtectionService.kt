/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.argon2

import dev.nikdekur.ndkore.ext.delay
import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.injectOrNull
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.protection.Password
import dev.nikdekur.ornament.protection.ProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import java.util.*

public open class Argon2ProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), ProtectionService {

    override val dependencies: Dependencies = dependencies {
        -DataSetService::class
    }

    protected val dataset: DataSetService? by injectOrNull()


    public val encryptors: MutableMap<Password.Significance, Argon2Encryptor> =
        EnumMap(Password.Significance::class.java)

    override suspend fun onEnable() {
        val section = dataset?.getSection("argon2")
        Password.Significance.entries.forEach {
            val default = Argon2SignificanceDataSet.getDefaultFor(it)
            val data = section?.get<Argon2SignificanceDataSet>(it.name.lowercase())

            val encryptor = Argon2Encryptor(
                saltSize = data?.saltSize ?: default.saltSize!!,
                hashSize = data?.hashSize ?: default.hashSize!!,
                iterations = data?.iterations ?: default.iterations!!,
                memory = data?.memory ?: default.memory!!,
                lanes = data?.lanes ?: default.lanes!!,
                hashTime = { data?.hashTime?.random() ?: default.hashTime!!.random() }
            )

            encryptors[it] = encryptor
        }
    }

    public inline fun getEncryptor(significance: Password.Significance): Argon2Encryptor {
        return encryptors[significance] ?: error("No encryptor found for significance: $significance")
    }

    internal fun quickEncrypt(significance: Password.Significance, string: String, salt: Salt): ByteArray {
        val encryptor = getEncryptor(significance)
        return encryptor.encrypt(string, salt)
    }

    internal fun createPassword(
        significance: Password.Significance,
        bytes: ByteArray,
        salt: Salt
    ): Argon2Password {
        return Argon2Password(significance, bytes, salt) { pswd, salt -> quickEncrypt(significance, pswd, salt) }
    }

    override fun createPassword(string: String, significance: Password.Significance): Argon2Password {
        val data = getEncryptor(significance).encryptNew(string)
        return createPassword(significance, data.first, data.second)
    }

    override fun deserializePassword(string: String): Password {
        val (signStr, bytesStr, saltStr) = string.split(":")

        val sign = Password.Significance.valueOf(signStr.uppercase())
        val bytes = bytesStr.fromHEX()
        val salt = Salt(saltStr.fromHEX())

        return createPassword(sign, bytes, salt)
    }

    override suspend fun imitatePasswordEncryption(significance: Password.Significance) {
        val averageTime = getEncryptor(significance).averageHashTime()
        delay(averageTime)
    }
}