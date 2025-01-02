/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.none

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.protection.Password
import dev.nikdekur.ornament.protection.ProtectionService
import dev.nikdekur.ornament.service.AbstractAppService

public open class NoneProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), ProtectionService {

    override fun createPassword(string: String, significance: Password.Significance): Password {
        return NoneProtectionPassword(significance, string)
    }

    override fun deserializePassword(string: String): Password {
        val (signStr, password) = string.split(":")
        val significance = Password.Significance.valueOf(signStr.uppercase())
        return createPassword(password, significance)
    }

    override suspend fun imitatePasswordEncryption(significance: Password.Significance) {
        // Do nothing
    }
}