/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.runtime

import co.touchlab.stately.collections.ConcurrentMutableMap
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.StorageTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlin.reflect.KClass

public class RuntimeStorageService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), StorageService {

    public val tables: MutableMap<String, RuntimeStorageTable<*>> = ConcurrentMutableMap()

    override fun getAllTables(): Flow<String> {
        return tables.keys.asFlow()
    }

    override suspend fun <T : Any> getTable(
        name: String,
        clazz: KClass<T>
    ): StorageTable<T> {

        @Suppress("UNCHECKED_CAST")
        return tables.getOrPut(name) {
            RuntimeStorageTable<T>()
        } as StorageTable<T>
    }
}