/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.account.storage

import dev.nikdekur.ornament.account.Account
import dev.nikdekur.ornament.account.Permission
import dev.nikdekur.ornament.protection.password.Password
import kotlinx.serialization.Serializable

@Serializable
public data class SetAccount(
    override val login: String,
    override var password: Password,
    val set: MutableSet<Permission>
) : Account {

    override suspend fun changePassword(newPassword: Password) {
        password = newPassword
    }

    override suspend fun hasPermission(permission: Permission): Boolean {
        return set.contains(permission)
    }


    override suspend fun getPermissions(): MutableSet<Permission> = set

    override suspend fun allowPermission(permission: Permission) {
        set.add(permission)
    }

    override suspend fun disallowPermission(permission: Permission) {
        set.remove(permission)
    }

    override suspend fun clearPermission() {
        set.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false

        if (login != other.login) return false

        return true
    }

    override fun hashCode(): Int {
        return login.hashCode()
    }
}