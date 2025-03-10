/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.password

import kotlinx.coroutines.test.runTest
import kotlin.test.*

abstract class PasswordProtectionServiceTest {

    abstract val service: PasswordProtectionService

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
        assertTrue(password.isEqual("password"))
        assertFalse(password.isEqual("password1"))
    }

    @Test
    fun testCreateAndSerializeAndDeserializePassword() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val serialized = password.serialize()
        val deserialized = service.deserializePassword(serialized)

        assertEquals(password, deserialized)
        assertTrue(password.isEqual("password"))
        assertTrue(deserialized.isEqual("password"))
    }


    @Test
    fun testCreateAndSerializeAndDeserializePasswordData() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val data = password.data
        val serialized = data.serialize()
        val deserialized = service.deserializePasswordData(serialized)

        assertEquals(data, deserialized)
    }


    @Test
    fun testConvertDataToPassword() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val data = password.data

        val toPassword = data.toPassword("password")
        assertEquals(password, toPassword)
    }


    @Test
    fun testConvertDataToWrongPassword() = runTest {
        val password = service.createPassword("password", Password.Significance.LOWEST)
        val data = password.data

        val toPassword = data.toPassword("password1")
        assertNotEquals(password, toPassword)
    }
}