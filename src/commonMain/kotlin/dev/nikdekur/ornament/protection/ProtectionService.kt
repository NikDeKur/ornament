/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection

/**
 * # Protection Service
 *
 * The protection service is responsible for creating and managing protected data.
 * Examples of protected data are passwords, encryption keys, etc.
 *
 * Different implementations of this service can provide different levels of security
 * or either no security at all.
 */
public interface ProtectionService {

    /**
     * Creates a password from a string.
     *
     * May take some time to complete, depending on the implementation.
     *
     * @param string The string to create the password from.
     * @param significance The significance of the password. The protection will be adjusted based on this.
     * @return The created password.
     */
    public fun createPassword(string: String, significance: Password.Significance): Password

    /**
     * Deserializes a password from a string.
     *
     * May take some time to complete, depending on the implementation.
     *
     * String is guaranteed to be a result of a previous call to [Password.serialize].
     *
     * @param string The string to deserialize the password from.
     * @return The deserialized password.
     */
    public fun deserializePassword(string: String): Password

    /**
     * Imitates password encryption.
     *
     * Used for security, when the actual encryption is unnecessary.
     * It Should take some time to complete, depending on the implementation.
     */
    public suspend fun imitatePasswordEncryption(significance: Password.Significance)
}