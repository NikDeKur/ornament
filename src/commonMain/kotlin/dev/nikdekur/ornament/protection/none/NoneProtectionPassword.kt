/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalStdlibApi::class)

package dev.nikdekur.ornament.protection.none

import dev.nikdekur.ornament.protection.Password
import kotlinx.serialization.Serializable


@Serializable
public data class NoneProtectionPassword(
    override val significance: Password.Significance,
    val password: String
) : Password {

    override fun isEqual(password: String): Boolean {
        return this.password == password
    }

    override fun serialize(): String {
        return "${significance.name}:${password}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoneProtectionPassword) return false

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