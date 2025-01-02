/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.runtime

import dev.nikdekur.ndkore.ext.CompAny
import dev.nikdekur.ndkore.ext.getNested
import dev.nikdekur.ndkore.reflect.KotlinXEncoderReflectMethod
import dev.nikdekur.ndkore.reflect.ReflectMethod
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.index.IndexOptions
import dev.nikdekur.ornament.storage.request.CompOperator
import dev.nikdekur.ornament.storage.request.Filter
import dev.nikdekur.ornament.storage.request.Order
import dev.nikdekur.ornament.storage.request.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.modules.SerializersModule

public class RuntimeStorageTable<T : Any> : StorageTable<T> {

    public val reflectMethod: ReflectMethod = KotlinXEncoderReflectMethod(SerializersModule { })

    public val storage: MutableList<T> = mutableListOf()

    override suspend fun count(vararg filters: Filter): Long {
        return storage.asSequence()
            .filter { item -> filters.all { applyFilter(item, it) } }
            .count()
            .toLong()
    }

    override suspend fun insertOne(data: T) {
        storage.add(data)
    }

    override suspend fun insertMany(data: List<T>) {
        storage.addAll(data)
    }

    override suspend fun replaceOne(data: T, vararg filters: Filter): Boolean {
        val index = storage.indexOfFirst { item -> filters.all { applyFilter(item, it) } }
        if (index != -1) {
            storage[index] = data
            return true
        }
        return false
    }

    override fun find(
        vararg filters: Filter,
        sort: Sort?,
        limit: Int?,
        skip: Int?
    ): Flow<T> = flow {
        val filteredData = storage.asSequence()
            .filter { item -> filters.all { applyFilter(item, it) } != false }
            .let { sequence ->
                sort?.let { applySort(sequence, it) } ?: sequence
            }
            .let { sequence ->
                sequence.drop(skip ?: 0).take(limit ?: sequence.count())
            }
            .toList()
        emitAll(filteredData.asFlow())
    }

    override suspend fun deleteOne(vararg filters: Filter): Boolean {
        val index = storage.indexOfFirst { item -> filters.all { applyFilter(item, it) } }
        if (index != -1) {
            storage.removeAt(index)
            return true
        }
        return false
    }

    override suspend fun deleteMany(vararg filters: Filter): Long {
        var count = 0L

        storage.removeAll { item ->
            filters.all {
                applyFilter(item, it)
            }.also { if (it) count++ }
        }

        return count
    }

    override suspend fun createIndex(keys: Map<String, Int>, options: IndexOptions) {
        // No implementation for now
    }

    public fun <T> compare(a: Comparable<T>, b: Comparable<*>): Int {
        if (a is Number && b is Number)
            return a.toDouble().compareTo(b.toDouble())

        @Suppress("UNCHECKED_CAST")
        return a.compareTo(b as T)
    }

    public fun applyFilter(item: Any, filter: Filter): Boolean {
        val value = findField(item, filter.key)
        val value2 = filter.value

        @Suppress("UNCHECKED_CAST")
        return when (filter.operator) {
            CompOperator.EQUALS -> value == value2
            CompOperator.NOT_EQUALS -> value != value2
            else -> {
                if (value == null) return false

                if (value2 == null) return false
                if (value2 !is Comparable<*>) return false
                when (filter.operator) {
                    CompOperator.GREATER_THAN -> compare(value, value2) > 0
                    CompOperator.GREATER_THAN_OR_EQUALS -> compare(value, value2) >= 0
                    CompOperator.LESS_THAN -> compare(value, value2) < 0
                    CompOperator.LESS_THAN_OR_EQUALS -> compare(value, value2) <= 0
                    else -> false
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    public fun findField(any: Any, field: String): CompAny? {
        val path = field.split(".")
        return (if (any is Map<*, *>) {
            any as Map<String, Any>
            any.getNested(path)
        } else {
            var res = any
            path.forEach { part ->
                val value = reflectMethod.findValue(res, part)
                if (value == ReflectMethod.NotFound || value == null) return null
                res = value
            }
            res
        }) as? CompAny
    }

    public fun applySort(sequence: Sequence<T>, sort: Sort): Sequence<T> {
        val comparator = Comparator<T> { a, b ->
            val field = sort.field
            val valueA = findField(a, field)
            val valueB = findField(b, field)
            if (valueA == null || valueB == null) return@Comparator 0
            when (sort.order) {
                Order.ASCENDING -> valueA.compareTo(valueB)
                Order.DESCENDING -> valueB.compareTo(valueA)
            }
        }
        return sequence.sortedWith(comparator)
    }
}
