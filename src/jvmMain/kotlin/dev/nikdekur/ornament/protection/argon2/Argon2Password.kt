/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalStdlibApi::class)

package dev.nikdekur.ornament.protection.argon2

import dev.nikdekur.ornament.protection.Password


public data class Argon2Password(
    override val significance: Password.Significance,
    val byte: ByteArray,
    val salt: Salt,
    val encrypt: (String, Salt) -> ByteArray
) : Password {

    val hex: String
        get() = byte.toHEX()

    override fun isEqual(password: String): Boolean {
        val encryptedData = encrypt(password, salt)
        return encryptedData.contentEquals(byte)
    }

    override fun serialize(): String {
        val hexStr = hex.toString()
        val saltStr = salt.hex.toString()
        return "${significance.name}:$hexStr:$saltStr"
    }

    override fun equals(other: Any?): Boolean {
        val other = other as? Argon2Password ?: return false
        return significance == other.significance &&
                byte.contentEquals(other.byte) &&
                salt.byte.contentEquals(other.salt.byte)
    }

    override fun hashCode(): Int {
        var result = significance.hashCode()
        result = 31 * result + byte.contentHashCode()
        result = 31 * result + salt.hashCode()
        return result
    }

    override fun toString(): String {
        return "Argon2Password(significance=$significance byte=$hex, salt=${salt.hex})"
    }
}