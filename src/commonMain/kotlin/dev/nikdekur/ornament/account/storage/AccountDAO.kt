/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalStdlibApi::class)

package dev.nikdekur.ornament.account.storage

import dev.nikdekur.ornament.account.Permission
import kotlinx.serialization.Serializable

@Serializable
public data class AccountDAO(
    val login: String,

    // Serialized password
    val password: String,
    val permissions: Collection<Permission>
)