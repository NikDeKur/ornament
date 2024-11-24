/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.table

import dev.nikdekur.ndkore.test.assertEmpty
import dev.nikdekur.ndkore.test.assertSize
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.request.asc
import dev.nikdekur.ornament.storage.request.desc
import dev.nikdekur.ornament.storage.request.eq
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


@Serializable
data class TestObject(
    val field1: String,
    val field2: Int,
    val field3: Boolean
)

abstract class StorageTableTest {

    abstract suspend fun <T : Any> getTable(name: String, clazz: KClass<T>): StorageTable<T>

    fun createTestData(index: Int = 0) = TestObject(
        field1 = "test-$index",
        field2 = index,
        field3 = index % 2 == 0
    )

    @Test
    fun testGetTable() = runTest {
        getTable<TestObject>("test")
    }

    @Test
    fun testInsertOne() = runTest {
        val table = getTable<TestObject>("test")
        table.insertOne(createTestData())
    }

    @Test
    fun testInsertOneAndFind() = runTest {
        val table = getTable<TestObject>("test")
        val obj = createTestData()
        table.insertOne(obj)

        val data = table.find().toList()
        assertSize(data, 1)
        assertEquals(obj, data.first())
    }

    @Test
    fun testInsertOneFewTimes() = runTest {
        val table = getTable<TestObject>("test")
        repeat(5) {
            table.insertOne(createTestData(it))
        }
    }

    @Test
    fun testInsertOneFewTimesAndFind() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)

        objects.forEach {
            table.insertOne(it)
        }

        val data = table.find().toList()

        assertSize(data, 5)
        assertContentEquals(objects, data)
    }

    @Test
    fun testInsertMany() = runTest {
        val table = getTable<TestObject>("test")
        val list = List(3, ::createTestData)
        table.insertMany(list)
    }

    @Test
    fun testInsertManyAndFind() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(3, ::createTestData)
        table.insertMany(objects)

        val data = table.find().toList()
        assertSize(data, 3)
        assertContentEquals(objects, data)
    }

    @Test
    fun testInsertManyAndFindWithLimit() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(limit = 3).toList()
        assertSize(data, 3)
        assertContentEquals(objects.take(3), data)
    }

    @Test
    fun testInsertManyAndFindWithSkip() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(skip = 2).toList()
        assertSize(data, 3)
        assertContentEquals(objects.drop(2), data)
    }

    @Test
    fun testInsertManyAndFindWithLimitAndSkip() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(limit = 2, skip = 2).toList()
        assertSize(data, 2)
        assertContentEquals(objects.drop(2).take(2), data)
    }

    @Test
    fun testInsertManyAndFindWithFilters() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(TestObject::field2 eq 2).toList()
        assertSize(data, 1)
        assertEquals(objects[2], data.first())
    }

    @Test
    fun testInsertManyAndFindWithSortDesc() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(sort = TestObject::field2.desc()).toList()
        assertSize(data, 5)
        assertContentEquals(objects.sortedByDescending { it.field2 }, data)
    }

    @Test
    fun testInsertManyAndFindWithSortAsc() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(sort = TestObject::field2.asc()).toList()
        assertSize(data, 5)
        assertContentEquals(objects.sortedBy { it.field2 }, data)
    }

    @Test
    fun testInsertManyAndFindWithSortAndLimit() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(sort = TestObject::field2.desc(), limit = 2).toList()
        assertSize(data, 2)
        assertContentEquals(objects.sortedByDescending { it.field2 }.take(2), data)
    }

    @Test
    fun testInsertManyAndFindWithSortAndSkip() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(sort = TestObject::field2.desc(), skip = 2).toList()
        assertSize(data, 3)
        assertContentEquals(objects.sortedByDescending { it.field2 }.drop(2), data)
    }

    @Test
    fun testInsertManyAndFindWithSortLimitAndSkip() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(sort = TestObject::field2.desc(), limit = 2, skip = 2).toList()
        assertSize(data, 2)
        assertContentEquals(objects.sortedByDescending { it.field2 }.drop(2).take(2), data)
    }

    @Test
    fun testInsertManyAndFindWithFiltersAndSort() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(
            TestObject::field2 eq 2,
            sort = TestObject::field2.desc()
        ).toList()
        assertSize(data, 1)
        assertEquals(objects[2], data.first())
    }

    @Test
    fun testInsertManyAndFindWithFiltersSortLimitAndSkip() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(
            TestObject::field2 eq 2,
            sort = TestObject::field2.desc(),
            limit = 1,
            skip = 0
        ).toList()
        assertSize(data, 1)
        assertEquals(objects[2], data.first())
    }

    @Test
    fun testInsertManyAndFindWithFiltersSortLimitAndSkip1() = runTest {
        val table = getTable<TestObject>("test")
        val objects = List(5, ::createTestData)
        table.insertMany(objects)

        val data = table.find(
            TestObject::field2 eq 2,
            sort = TestObject::field2.desc(),
            limit = 1,
            skip = 1
        ).toList()
        assertEmpty(data)
    }

    @Test
    fun testCountWhenNoData() = runTest {
        val table = getTable<TestObject>("test")
        val amount = table.count()
        assertEquals(0, amount)
    }

    @Test
    fun testInsertDataAndCount() = runTest {
        val table = getTable<TestObject>("test")
        table.insertOne(createTestData())
        val amount = table.count()
        assertEquals(1, amount)
    }

    @Test
    fun testInsertManyDataAndCount() = runTest {
        val table = getTable<TestObject>("test")
        table.insertMany(List(3, ::createTestData))

        val amount = table.count()
        assertEquals(3, amount)
    }
}

suspend inline fun <reified T : Any> StorageTableTest.getTable(name: String) = getTable(name, T::class)