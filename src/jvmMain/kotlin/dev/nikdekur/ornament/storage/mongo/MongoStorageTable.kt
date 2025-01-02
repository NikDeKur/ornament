/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.mongo

import com.mongodb.kotlin.client.coroutine.MongoCollection
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.index.IndexOptions
import dev.nikdekur.ornament.storage.request.Filter
import dev.nikdekur.ornament.storage.request.Sort
import kotlinx.coroutines.flow.Flow
import org.bson.Document

public class MongoStorageTable<T : Any>(
    public val collection: MongoCollection<T>
) : StorageTable<T> {

    override suspend fun count(vararg filters: Filter): Long {
        return collection.countDocuments(filters.toBson())
    }

    override suspend fun insertOne(data: T) {
        collection.insertOne(data)
    }

    override suspend fun insertMany(data: List<T>) {
        collection.insertMany(data)
    }

    override suspend fun replaceOne(data: T, vararg filters: Filter): Boolean {
        return collection.replaceOne(filters.toBson(), data).matchedCount == 1L
    }

    override fun find(
        vararg filters: Filter,
        sort: Sort?,
        limit: Int?,
        skip: Int?
    ): Flow<T> {
        val filter = filters.toBson()
        val request = collection.find(filter)

        if (sort != null)
            request.sort(sort.toBson())

        if (limit != null)
            request.limit(limit)

        if (skip != null)
            request.skip(skip)

        return request
    }

    override suspend fun deleteOne(vararg filters: Filter): Boolean {
        return collection.deleteOne(filters.toBson()).deletedCount == 1L
    }

    override suspend fun deleteMany(vararg filters: Filter): Long {
        return collection.deleteMany(filters.toBson()).deletedCount
    }

    override suspend fun createIndex(keys: Map<String, Int>, options: IndexOptions) {
        val options = mongoIndexOptions {
            name(options.name)
            unique(options.unique)
        }

        val bson = Document()
        keys.forEach { (key, value) -> bson[key] = value }
        collection.createIndex(bson, options)
    }
}