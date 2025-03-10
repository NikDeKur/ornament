/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

abstract class DataProtectionServiceTest {

    abstract val protectionService: PasswordProtectionService
    abstract val service: DataProtectionService

    @Test
    fun testCreateDataLowestSignificance() = runTest {
        val password = protectionService.createPassword("password", Password.Significance.LOWEST)
        val data = "data".encodeToByteArray()
        service.encrypt(data, password)
    }

    @Test
    fun testCreateDataHighestSignificance() = runTest {
        val password = protectionService.createPassword("password", Password.Significance.HIGHEST)
        val data = "data".encodeToByteArray()
        service.encrypt(data, password)
    }


    @Test
    fun testCreateAndComparePasswords() = runTest {
        val passwordStr = "password"
        val password = protectionService.createPassword(passwordStr, Password.Significance.LOWEST)

        val data = """
            OMG! This is a very secret message that should not be seen by anyone!
            It is so secret that I can't even tell you what it is about!
        """.trimIndent()

        val dataBytes = data.encodeToByteArray()

        val encrypted = service.encrypt(dataBytes, password)
        val decryptedBytes = service.decrypt(encrypted.serialize(), passwordStr)


        assertContentEquals(decryptedBytes, dataBytes)

        val decrypted = decryptedBytes.decodeToString()
        assertEquals(data, decrypted)
    }

    @Test
    fun testEmptyDataEncryption() = runTest {
        val passwordStr = "password"
        val password = protectionService.createPassword(passwordStr, Password.Significance.LOWEST)
        val data = "".encodeToByteArray()
        val encrypted = service.encrypt(data, password)
        val decryptedBytes = service.decrypt(encrypted.serialize(), passwordStr)

        // Должен быть пустой результат после расшифровки
        assertContentEquals(decryptedBytes, data)
    }

    @Test
    fun testDifferentDataSizes() = runTest {
        val passwordStr = "password"
        val password = protectionService.createPassword(passwordStr, Password.Significance.LOWEST)

        val smallData = "small".encodeToByteArray()
        val largeData = ByteArray(10_000) { it.toByte() }

        // Шифруем и дешифруем маленькие данные
        val encryptedSmall = service.encrypt(smallData, password)
        val decryptedSmall = service.decrypt(encryptedSmall.serialize(), passwordStr)
        assertContentEquals(decryptedSmall, smallData)

        // Шифруем и дешифруем большие данные
        val encryptedLarge = service.encrypt(largeData, password)
        val decryptedLarge = service.decrypt(encryptedLarge.serialize(), passwordStr)
        assertContentEquals(decryptedLarge, largeData)
    }

    @Test
    fun testSignificanceLevels() = runTest {
        val passwordStr = "password"
        val lowPassword = protectionService.createPassword(passwordStr, Password.Significance.LOWEST)
        val highPassword = protectionService.createPassword(passwordStr, Password.Significance.HIGHEST)

        val data = "Important Data".encodeToByteArray()

        // Для самого низкого уровня значимости данные все равно должны быть защищены
        val encryptedLow = service.encrypt(data, lowPassword)
        val decryptedLow = service.decrypt(encryptedLow.serialize(), passwordStr)
        assertContentEquals(decryptedLow, data)

        // Для самого высокого уровня значимости результат должен быть такой же
        val encryptedHigh = service.encrypt(data, highPassword)
        val decryptedHigh = service.decrypt(encryptedHigh.serialize(), passwordStr)
        assertContentEquals(decryptedHigh, data)
    }

    @Test
    fun testPasswordLengths() = runTest {
        val shortPasswordStr = "short"
        val longPasswordStr = "thisisaverylongpasswordusedfortesting"
        val shortPassword = protectionService.createPassword(shortPasswordStr, Password.Significance.LOWEST)
        val longPassword = protectionService.createPassword(longPasswordStr, Password.Significance.LOWEST)

        val data = "Test data".encodeToByteArray()

        val encryptedShort = service.encrypt(data, shortPassword)
        val decryptedShort = service.decrypt(encryptedShort.serialize(), shortPasswordStr)
        assertContentEquals(decryptedShort, data)

        val encryptedLong = service.encrypt(data, longPassword)
        val decryptedLong = service.decrypt(encryptedLong.serialize(), longPasswordStr)
        assertContentEquals(decryptedLong, data)
    }
}