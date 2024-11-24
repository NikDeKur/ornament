/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.table

import dev.nikdekur.ndkore.service.manager.getService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.runtime.RuntimeStorageService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.reflect.KClass
import kotlin.test.BeforeTest

class RuntimeStorageServiceTableTest : StorageTableTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication {
            service(::RuntimeStorageService, StorageService::class)
        }

        service = server.servicesManager.getService()
    }


    lateinit var service: StorageService


    override suspend fun <T : Any> getTable(name: String, clazz: KClass<T>) = service.getTable(name, clazz)
}