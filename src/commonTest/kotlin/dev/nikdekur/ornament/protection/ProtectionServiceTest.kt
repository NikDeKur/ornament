/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class ProtectionServiceTest {

    abstract val service: ProtectionService

    @Test
    fun testCreatePasswordLowestSignificance() = runTest {
        service.createPassword("password", Password.Significance.LOWEST)
    }

    @Test
    fun testCreatePasswordHighestSignificance() = runTest {
        service.createPassword("password", Password.Significance.HIGHEST)
    }


    @Test
    fun testCreateAndComparePasswords() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val equals = password.isEqual("password")
        assertTrue(equals)
    }

    @Test
    fun testCreateAndSerializePassword() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        password.serialize()
    }

    @Test
    fun testCreateSerializeAndDeserializePassword() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val serialized = password.serialize()
        val deserialized = service.deserializePassword(serialized)

        assertEquals(password, deserialized)
        assertTrue(password.isEqual("password"))
        assertTrue(deserialized.isEqual("password"))
    }


//    @Test
//    fun testBenchmark() {
//        Password.Significance.entries.forEach { significance ->
//            TimeSource.Monotonic.printAverageExecTime(100, "Significance: $significance") {
//                service.createPassword("password", significance)
//            }
//        }
//    }
}