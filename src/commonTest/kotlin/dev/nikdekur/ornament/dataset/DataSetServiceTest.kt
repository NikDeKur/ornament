/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


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

abstract class DataSetServiceTest {

    abstract suspend fun getDataSet(): DataSetService

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

}
