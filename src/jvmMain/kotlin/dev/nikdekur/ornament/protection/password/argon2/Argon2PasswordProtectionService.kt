/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.password.argon2

import dev.nikdekur.ndkore.ext.delay
import dev.nikdekur.ndkore.ext.fromHEX
import dev.nikdekur.ndkore.ext.toHEX
import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.injectOrNull
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.Password.Significance
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import java.util.*

public open class Argon2PasswordProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), PasswordProtectionService {

    override val dependencies: Dependencies = dependencies {
        -DataSetService::class
    }

    protected val dataset: DataSetService? by injectOrNull()


    public val encryptors: MutableMap<Significance, Argon2Encryptor> =
        EnumMap(Significance::class.java)

    override suspend fun onEnable() {
        val section = dataset?.getSection("argon2")
        Significance.entries.forEach {
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

    public inline fun getEncryptor(significance: Significance): Argon2Encryptor {
        return encryptors[significance] ?: error("No encryptor found for significance: $significance")
    }

    internal fun quickEncrypt(data: Argon2Data, string: String): ByteArray {
        val encryptor = getEncryptor(data.significance)
        return encryptor.encrypt(string, data.salt)
    }

    internal fun createPassword(
        significance: Significance,
        bytes: ByteArray,
        salt: Salt
    ): Argon2Password {
        val data = Argon2Data(significance, salt)
        return Argon2Password(data, bytes)
    }

    override fun createPassword(string: String, significance: Significance): Argon2Password {
        val data = getEncryptor(significance).encryptNew(string)
        return createPassword(significance, data.first, data.second)
    }

    override fun deserializePassword(string: String): Password {
        val (signStr, saltStr, bytesStr) = string.split(":")

        val sign = Significance.fromKey(signStr.toInt())
        val salt = Salt(saltStr.fromHEX())
        val bytes = bytesStr.fromHEX()

        return createPassword(sign, bytes, salt)
    }

    override fun deserializePasswordData(data: String): Password.Data {
        val (signStr, saltStr) = data.split(":")

        val sign = Significance.fromKey(signStr.toInt())
        val salt = Salt(saltStr.fromHEX())

        return Argon2Data(sign, salt)
    }

    override suspend fun imitatePasswordEncryption(significance: Significance) {
        val averageTime = getEncryptor(significance).averageHashTime()
        delay(averageTime)
    }

    public inner class Argon2Data(
        override val significance: Significance,
        public val salt: Salt,
    ) : Password.Data {
        override fun toPassword(password: String): Password {
            return createPassword(significance, quickEncrypt(this, password), salt)
        }

        override fun serialize(): String {
            return "${significance.ordinal}:${salt.hex}"
        }

        override fun equals(other: Any?): Boolean {
            val other = other as? Argon2PasswordProtectionService<*>.Argon2Data ?: return false

            if (significance != other.significance) return false
            if (!salt.byte.contentEquals(other.salt.byte)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = significance.hashCode()
            result = 31 * result + salt.hashCode()
            return result
        }

        override fun toString(): String {
            return "Argon2Data(significance=$significance, salt=${salt.hex})"
        }
    }

    public inner class Argon2Password(
        override val data: Argon2Data,
        override val bytes: ByteArray,
    ) : Password {

        public val hex: String
            get() = bytes.toHEX()

        override fun isEqual(password: String): Boolean {
            val encryptedData = quickEncrypt(data, password)
            return encryptedData.contentEquals(bytes)
        }

        override fun serialize(): String {
            return "${data.serialize()}:${bytes.toHEX()}"
        }

        override fun equals(other: Any?): Boolean {
            val other = other as? Argon2PasswordProtectionService<*>.Argon2Password ?: return false

            if (data != other.data) return false
            if (!bytes.contentEquals(other.bytes)) return false

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