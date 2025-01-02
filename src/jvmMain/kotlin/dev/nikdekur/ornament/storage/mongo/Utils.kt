/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.storage.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.nikdekur.ornament.storage.request.CompOperator
import dev.nikdekur.ornament.storage.request.Filter
import dev.nikdekur.ornament.storage.request.Order
import dev.nikdekur.ornament.storage.request.Sort
import kotlinx.coroutines.flow.firstOrNull
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson

public suspend inline fun <reified T : Any> MongoDatabase.ensureCollectionExists(
    name: String,
    create: MongoCollection<T>.() -> Unit = {}
): MongoCollection<T> {

    val existingTable = listCollectionNames().firstOrNull {
        it == name
    }

    if (existingTable == null)
        return getCollection(name)


    createCollection(name)
    val collection = getCollection<T>(name)
    create(collection)
    return collection
}

public inline fun mongoIndexOptions(block: IndexOptions.() -> Unit): IndexOptions {
    return IndexOptions().apply(block)
}

public inline fun nullError(): Nothing = error("Value must be provided for greater than operator")

public fun Filter.toBson(): Bson {
    return when (operator) {
        CompOperator.EQUALS -> Filters.eq(
            key,
            value
        )

        CompOperator.NOT_EQUALS -> Filters.ne(
            key,
            value
        )

        CompOperator.GREATER_THAN -> Filters.gt(
            key,
            value ?: nullError()
        )

        CompOperator.LESS_THAN -> Filters.lt(
            key,
            value ?: nullError()
        )

        CompOperator.GREATER_THAN_OR_EQUALS -> Filters.gte(
            key,
            value ?: nullError()
        )

        CompOperator.LESS_THAN_OR_EQUALS -> Filters.lte(
            key,
            value ?: nullError()
        )
    }
}

public inline fun Array<out Filter>.toBson(): Bson {
    if (isEmpty()) return BsonDocument()
    return Filters.and(map(Filter::toBson))
}

public inline fun Iterable<Filter>.toBson(): Bson {
    if (none()) return BsonDocument()
    return Filters.and(map(Filter::toBson))
}

public inline fun Sort.toBson(): Bson {
    val ascending = order == Order.ASCENDING
    val num = if (ascending) 1 else -1
    return Document(field, num)
}
