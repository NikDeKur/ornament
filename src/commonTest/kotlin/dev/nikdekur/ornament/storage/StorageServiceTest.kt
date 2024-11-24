/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage

import dev.nikdekur.ndkore.test.assertEmpty
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

abstract class StorageServiceTest {

    abstract val service: StorageService


    @Test
    fun testGetTablesWhenNoTablesExists() = runTest {
        val tables = service.getAllTables().toList()
        assertEmpty(tables)
    }


    @Test
    fun testGetTable() = runTest {
        service.getTable<Any>("test")
    }
}