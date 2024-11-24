/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

public interface StorageService {

    public fun getAllTables(): Flow<String>
    public suspend fun <T : Any> getTable(name: String, clazz: KClass<T>): StorageTable<T>
}

public suspend inline fun <reified T : Any> StorageService.getTable(name: String): StorageTable<T> {
    return getTable(name, T::class)
}