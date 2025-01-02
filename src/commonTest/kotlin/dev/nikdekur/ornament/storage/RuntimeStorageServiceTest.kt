/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.storage.runtime.RuntimeStorageService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class RuntimeStorageServiceTest : StorageServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::RuntimeStorageService, StorageService::class)
        }

        service = server.servicesManager.get()
    }


    override lateinit var service: StorageService
}