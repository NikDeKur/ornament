/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage

import dev.nikdekur.ornament.storage.index.IndexOptions
import dev.nikdekur.ornament.storage.request.Filter
import dev.nikdekur.ornament.storage.request.Sort
import kotlinx.coroutines.flow.Flow

public interface StorageTable<T : Any> {

    public suspend fun insertOne(data: T)
    public suspend fun insertMany(data: List<T>)

    public suspend fun count(vararg filters: Filter): Long

    public fun find(
        vararg filters: Filter,
        sort: Sort? = null,
        limit: Int? = null,
        skip: Int? = null
    ): Flow<T>

    public suspend fun replaceOne(data: T, vararg filters: Filter): Boolean

    public suspend fun deleteOne(vararg filters: Filter): Boolean
    public suspend fun deleteMany(vararg filters: Filter): Long


    public suspend fun createIndex(keys: Map<String, Int>, options: IndexOptions)
}