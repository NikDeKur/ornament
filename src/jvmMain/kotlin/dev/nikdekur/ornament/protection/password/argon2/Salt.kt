/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.password.argon2

import dev.nikdekur.ndkore.ext.toHEX
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
public value class Salt(public val byte: ByteArray) {

    @OptIn(ExperimentalStdlibApi::class)
    public val hex: String
        get() = byte.toHEX()
}
