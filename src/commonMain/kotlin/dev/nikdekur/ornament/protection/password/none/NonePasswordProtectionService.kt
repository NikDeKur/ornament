/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.password.none

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.protection.password.Password.Data
import dev.nikdekur.ornament.protection.password.Password.Significance
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.serialization.Serializable

public open class NonePasswordProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), PasswordProtectionService {

    override fun createPassword(
        string: String,
        significance: Significance
    ): dev.nikdekur.ornament.protection.password.Password {
        return Password(PasswordData(significance), string)
    }

    override fun deserializePassword(string: String): dev.nikdekur.ornament.protection.password.Password {
        val (data, password) = string.split(":")
        return Password(deserializePasswordData(data), password)
    }

    override fun deserializePasswordData(data: String): Data {
        return PasswordData(Significance.valueOf(data.uppercase()))
    }

    override suspend fun imitatePasswordEncryption(significance: Significance) {
        // Do nothing
    }


    @Serializable
    public data class PasswordData(
        override val significance: Significance
    ) : Data {

        override fun toPassword(password: String): Password {
            return Password(this, password)
        }

        override fun serialize(): String {
            return significance.name
        }
    }


    @Serializable
    public data class Password(
        override val data: Data,
        val password: String
    ) : dev.nikdekur.ornament.protection.password.Password {

        override val bytes: ByteArray
            get() = password.encodeToByteArray()

        override fun isEqual(password: String): Boolean {
            return this.password == password
        }

        override fun serialize(): String {
            return "${data.serialize()}:${password}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Password) return false

            if (password != other.password) return false

            return true
        }

        override fun hashCode(): Int {
            return password.hashCode()
        }

        override fun toString(): String {
            return "NoneProtectionPassword(password='$password')"
        }

    }
}