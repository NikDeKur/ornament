/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.none.NoneProtectionService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class NoneProtectionServiceTest : ProtectionServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::NoneProtectionService, ProtectionService::class)
        }
        service = server.get()
    }


    override lateinit var service: ProtectionService
}
