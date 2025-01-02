/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.*


@Serializable
data class TestStruct(
    val key1: String,
    val key2: Int,
    val key3: Boolean,
    val key4: Double
)

@Serializable
data class NestedStruct(
    val structs: List<TestStruct>
)


@Serializable
data class RootNestedStruct(
    val key1: String,
    val key2: Int,
    val key3: Boolean,
    val key4: Double,
    val key5: TestStruct,
    val key6: NestedStruct
)

@Serializable
data class WrongTypesStruct(
    val key1: String,
    val key2: Int,
    val key3: Boolean,
    val key4: Double,
    val key5: NestedStruct, // Actually TestStruct
    val key6: TestStruct  // Actually NestedStruct
)

abstract class DataSetServiceTest {

    enum class Section {
        DEFAULT,
        EXTRA
    }

    abstract suspend fun TestScope.getDataSet(): DataSetSection

    @Test
    fun testGetString() = runTest {
        val string = getDataSet().get<String>("key1")
        assertEquals("value1", string)
    }

    @Test
    fun testGetInt() = runTest {
        val int = getDataSet().get<Int>("key2")
        assertEquals(2, int)
    }

    @Test
    fun testGetBoolean() = runTest {
        val boolean = getDataSet().get<Boolean>("key3")
        assertEquals(true, boolean)
    }

    @Test
    fun testGetDouble() = runTest {
        val double = getDataSet().get<Double>("key4")
        assertEquals(4.0, double)
    }

    @Test
    fun testNonExistingGet() = runTest {
        val string = getDataSet().get<String>("non-existing")
        assertNull(string)
    }


    @Test
    fun testGetStructure() = runTest {
        val struct = getDataSet().get<TestStruct>("key5")
        assertEquals(
            TestStruct("value1", 2, true, 4.0),
            struct
        )
    }


    @Test
    fun testGetNestedStructure() = runTest {
        val struct = getDataSet().get<NestedStruct>("key6")
        assertNotNull(struct)

        assertEquals(
            NestedStruct(
                listOf(
                    TestStruct("value1", 2, true, 4.0),
                    TestStruct("value2", 3, false, 5.0)
                )
            ),
            struct
        )
    }


    @Test
    fun testGetNesterStructureFromRoot() = runTest {
        val struct = getDataSet().get<RootNestedStruct>(null)
        assertNotNull(struct)

        assertEquals(
            RootNestedStruct(
                "value1",
                2,
                true,
                4.0,
                TestStruct("value1", 2, true, 4.0),
                NestedStruct(
                    listOf(
                        TestStruct("value1", 2, true, 4.0),
                        TestStruct("value2", 3, false, 5.0)
                    )
                )
            ),
            struct
        )
    }


    @Test
    fun testThrowExceptionOnWrongType() = runTest {
        val e = assertFailsWith<SerializationException> {
            getDataSet().get<Int>("key1")
        }

        assertEquals(e.key, "key1")
        assertEquals(e.clazz, Int::class)
        assertEquals(e.actual, "value1")
    }


    @Test
    fun testThrowExceptionOnWrongNestedType() = runTest {
        val e = assertFailsWith<SerializationException> {
            getDataSet().get<WrongTypesStruct>(null)
        }

        assertEquals(e.key, null)
        assertEquals(e.clazz, WrongTypesStruct::class)
        // Don't check the actual value as it's nested and will depend on serialization format.
    }


}


abstract class MutableDataSetServiceTest : DataSetServiceTest() {

    abstract override suspend fun TestScope.getDataSet(): MutableDataSetSection

    suspend fun TestScope.getActualDataSet() = getDataSet().also { it.clear() }

    @Test
    fun testSetString() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key1", "value2")

        val string = dataSet.get<String>("key1")
        assertEquals("value2", string)
    }

    @Test
    fun testSetInt() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key2", 3)

        val int = dataSet.get<Int>("key2")
        assertEquals(3, int)
    }

    @Test
    fun testSetBoolean() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key3", false)

        val boolean = dataSet.get<Boolean>("key3")
        assertEquals(false, boolean)
    }

    @Test
    fun testSetDouble() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key4", 5.0)

        val double = dataSet.get<Double>("key4")
        assertEquals(5.0, double)
    }

    @Test
    fun testSetStructure() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key5", TestStruct("value2", 3, false, 5.0))

        val struct = dataSet.get<TestStruct>("key5")
        assertEquals(
            TestStruct("value2", 3, false, 5.0),
            struct
        )
    }

    @Test
    fun testSetNestedStructure() = runTest {
        val dataSet = getDataSet()
        dataSet.set("key6", NestedStruct(listOf(TestStruct("value2", 3, false, 5.0))))

        val struct = dataSet.get<NestedStruct>("key6")
        assertNotNull(struct)

        assertEquals(
            NestedStruct(
                listOf(
                    TestStruct("value2", 3, false, 5.0)
                )
            ),
            struct
        )
    }

    @Test
    fun testSetNesterStructureFromRoot() = runTest {
        val dataSet = getDataSet()

        dataSet.set(
            null,
            RootNestedStruct(
                "value2",
                3,
                false,
                5.0,
                TestStruct("value2", 3, false, 5.0),
                NestedStruct(
                    listOf(
                        TestStruct("value2", 3, false, 5.0),
                        TestStruct("value3", 4, true, 6.0)
                    )
                ),
            )
        )

        val struct = dataSet.get<RootNestedStruct>(null)
        assertNotNull(struct)

        assertEquals(
            RootNestedStruct(
                "value2",
                3,
                false,
                5.0,
                TestStruct("value2", 3, false, 5.0),
                NestedStruct(
                    listOf(
                        TestStruct("value2", 3, false, 5.0),
                        TestStruct("value3", 4, true, 6.0)
                    )
                )
            ),
            struct
        )
    }
}