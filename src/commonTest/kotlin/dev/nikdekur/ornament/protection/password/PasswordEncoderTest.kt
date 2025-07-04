/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.password

import kotlin.test.*
import kotlin.time.measureTime

/**
 * Base test class for verifying the contract of the [PasswordEncoder] interface.
 *
 * This class provides comprehensive test coverage for any implementation of the
 * [PasswordEncoder] interface.
 * To test a specific implementation, extend this class and override the [createEncoder] method to provide
 * an instance of your implementation.
 *
 * Example:
 * ```
 * class MyPasswordEncoderTest : PasswordEncoderTest() {
 *     override open fun createEncoder(): PasswordEncoder = MyPasswordEncoder()
 * }
 * ```
 */
abstract class PasswordEncoderTest {

    /**
     * Creates an instance of [PasswordEncoder] implementation to be tested.
     * This method must be overridden by subclasses to provide the specific
     * implementation under test.
     *
     * @return A new instance of [PasswordEncoder]
     */
    protected abstract fun createEncoder(): PasswordEncoder

    private val testPassword = "P@ssw0rd123!"
    private val wrongPassword = "WrongPassword456!"

    @Test
    open fun `createPassword should return valid Password object`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)

        assertNotNull(password)
        assertNotNull(password.data)
        assertNotNull(password.bytes)
        assertTrue(password.bytes.isNotEmpty())
    }

    @Test
    open fun `createPassword should create different hashes for the same password`() {
        val encoder = createEncoder()
        val password1 = encoder.createPassword(testPassword)
        val password2 = encoder.createPassword(testPassword)

        // Even with the same input, salts should be different and thus bytes should differ
        assertFalse(password1.bytes.contentEquals(password2.bytes))
    }

    @Test
    open fun `createPassword with data should use provided data`() {
        val encoder = createEncoder()
        val originalPassword = encoder.createPassword(testPassword)
        val recreatedPassword = encoder.createPassword(originalPassword.data, testPassword)

        // Using the same data (with the same salt) should produce the same hash
        assertContentEquals(originalPassword.bytes, recreatedPassword.bytes)
    }

    @Test
    open fun `serialize and decode password should preserve data`() {
        val encoder = createEncoder()
        val originalPassword = encoder.createPassword(testPassword)
        val serialized = originalPassword.serialize()
        val decoded = encoder.decodePassword(serialized)

        // Test that serialization/deserialization preserves the password
        assertEquals(originalPassword.data.serialize(), decoded.data.serialize())
        assertContentEquals(originalPassword.bytes, decoded.bytes)
    }

    @Test
    open fun `serialize and decode password data should preserve data`() {
        val encoder = createEncoder()
        val originalPassword = encoder.createPassword(testPassword)
        val serializedData = originalPassword.data.serialize()
        val decodedData = encoder.decodePasswordData(serializedData)

        // Test that serialization/deserialization preserves the password data
        assertEquals(originalPassword.data.serialize(), decodedData.serialize())
    }

    @Test
    open fun `matches should return true for matching password`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)

        assertTrue(encoder.matches(password, testPassword))
    }

    @Test
    open fun `matches should return false for non-matching password`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)

        assertFalse(encoder.matches(password, wrongPassword))
    }

    @Test
    open fun `matches with encoded string should return true for matching password`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)
        val serialized = password.serialize()

        assertTrue(encoder.matches(serialized, testPassword))
    }

    @Test
    open fun `matches with encoded string should return false for non-matching password`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)
        val serialized = password.serialize()

        assertFalse(encoder.matches(serialized, wrongPassword))
    }

    @Test
    open fun `encryptionDelay should return positive value`() {
        val encoder = createEncoder()
        val delay = encoder.encryptionDelay()

        assertTrue(delay > 0, "Encryption delay should be positive")
    }

    @Test
    open fun `actual encryption should take approximately the reported delay time`() {
        val encoder = createEncoder()
        val reportedDelay = encoder.encryptionDelay()

        // We allow some margin of error, as timing can vary
        val timeTaken = measureTime {
            encoder.createPassword(testPassword)
        }.inWholeMilliseconds

        // The actual time shouldn't be more than 3x the reported time or less than 1/3.
        // This is a loose check as performance can vary significantly by environment
        val lowerBound = reportedDelay / 3
        val upperBound = reportedDelay * 3

        assertTrue(
            timeTaken in lowerBound..upperBound,
            "Encryption took $timeTaken ms, which is outside the expected range " +
                    "of $lowerBound to $upperBound ms based on reported delay of $reportedDelay ms"
        )
    }

    @Test
    open fun `different passwords should produce different hashes with same data`() {
        val encoder = createEncoder()
        val originalPassword = encoder.createPassword(testPassword)
        val differentPassword = encoder.createPassword(originalPassword.data, wrongPassword)

        // Using the same data but different password should produce different hash
        assertFalse(originalPassword.bytes.contentEquals(differentPassword.bytes))
    }

    @Test
    open fun `password hash should be deterministic with same input`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)
        val recreated1 = encoder.createPassword(password.data, testPassword)
        val recreated2 = encoder.createPassword(password.data, testPassword)

        // Multiple creations with the same data and password should be identical
        assertContentEquals(recreated1.bytes, recreated2.bytes)
    }

    @Test
    open fun `serialized format should be parseable`() {
        val encoder = createEncoder()
        val password = encoder.createPassword(testPassword)
        val serialized = password.serialize()

        // Shouldn't throw exceptions
        encoder.decodePassword(serialized)
    }

    @Test
    open fun `decodePassword should throw on invalid input`() {
        val encoder = createEncoder()

        assertFailsWith<IllegalArgumentException> {
            encoder.decodePassword("invalid-format")
        }
    }

    @Test
    open fun `decodePasswordData should throw on invalid input`() {
        val encoder = createEncoder()

        assertFailsWith<IllegalArgumentException> {
            encoder.decodePasswordData("invalid-format")
        }
    }

    @Test
    open fun `should work with empty passwords`() {
        val encoder = createEncoder()
        val password = encoder.createPassword("")

        assertTrue(encoder.matches(password, ""))
        assertFalse(encoder.matches(password, " "))  // Single space should not match
    }

    @Test
    open fun `should work with long passwords`() {
        val encoder = createEncoder()
        val longPassword = "A".repeat(1000)
        val password = encoder.createPassword(longPassword)

        assertTrue(encoder.matches(password, longPassword))
    }

    @Test
    open fun `should work with Unicode passwords`() {
        val encoder = createEncoder()
        val unicodePassword = "пароль123!@#€£¥•あいうえお"
        val password = encoder.createPassword(unicodePassword)

        assertTrue(encoder.matches(password, unicodePassword))
    }
}