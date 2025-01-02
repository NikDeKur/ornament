/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.protection

/**
 * # Password
 *
 * Interface representing the basic functionality of a password.
 */
public interface Password {

    /**
     * The significance of the password.
     *
     * The significance this password has and was created with.
     *
     * Significance is used to adjust the protection of the password.
     *
     * @see Significance
     */
    public val significance: Significance

    /**
     * Check if the password is equal to the given string.
     *
     * Might take some time to compute depending on the implementation.
     *
     * @param password The password to compare to
     * @return True if the password is equal to the given string, false otherwise
     */
    public fun isEqual(password: String): Boolean

    /**
     * Serialize the password to a string.
     *
     * This string should be able to be used to recreate the password using [ProtectionService.deserializePassword].
     *
     * Distinct from [toString] which is for debugging purposes.
     */
    public fun serialize(): String


    /**
     * # Significance
     *
     * Enum represents the significance of a password.
     *
     * The password protection will be adjusted based on the significance.
     */
    public enum class Significance {
        LOWEST,
        LOW,
        MEDIUM,
        HIGH,
        HIGHEST
    }
}