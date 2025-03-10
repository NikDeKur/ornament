/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.account

import dev.nikdekur.ornament.protection.password.Password

public interface Account {

    public val login: String
    public val password: Password

    public suspend fun changePassword(newPassword: Password)

    public suspend fun hasPermission(permission: Permission): Boolean
    public suspend fun getPermissions(): Collection<Permission>
    public suspend fun allowPermission(permission: Permission)
    public suspend fun disallowPermission(permission: Permission)
    public suspend fun clearPermission()
}

public typealias Permission = String