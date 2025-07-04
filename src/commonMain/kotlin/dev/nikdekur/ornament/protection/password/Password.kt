/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection.password


/**
 * # Password
 *
 * Interface representing a password with its associated metadata and hash.
 * This interface provides methods to access and work with password data securely.
 */
public interface Password {

    /**
     * The metadata associated with this password.
     *
     * This metadata contains all the information needed to recreate the password hash
     * when provided with the original plaintext password. This includes parameters like
     * salt, algorithm configuration, and other hashing parameters.
     */
    public val data: Data

    /**
     * The raw bytes of the password hash.
     *
     * These bytes represent the actual hash value produced by applying the
     * hashing algorithm to the plaintext password with the specified parameters.
     */
    public val bytes: ByteArray

    /**
     * Serializes the password to a string format.
     *
     * This method produces a string representation of the entire password object,
     * including both the metadata and the hash. This string can be stored in a database
     * and later used to recreate the password object using [PasswordEncoder.decodePassword].
     *
     * @return A string representation of the password
     */
    public fun serialize(): String

    /**
     * Interface representing password metadata.
     *
     * This interface encapsulates all the data required to recreate a password hash
     * when provided with the original plaintext password. It typically includes parameters
     * like salt, algorithm configuration, and other hashing parameters.
     */
    public fun interface Data {
        /**
         * Serializes the password metadata to a string format.
         *
         * This method produces a string representation of just the password metadata,
         * which can be stored and later used to recreate the Data object using
         * [PasswordEncoder.decodePasswordData].
         *
         * @return A string representation of the password metadata
         */
        public fun serialize(): String
    }
}