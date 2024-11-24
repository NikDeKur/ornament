/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalContracts::class)

package dev.nikdekur.ornament.environment

import dev.nikdekur.ornament.environment.Environment.Empty.getValue
import dev.nikdekur.ornament.environment.Environment.Empty.requestValue
import kotlin.contracts.ExperimentalContracts

/**
 * # Environment
 *
 * Represents the environment of the application.
 *
 * The boot environment is a key-value store that contains
 * the configuration of the application at startup.
 */
public interface Environment {

    /**
     * Gets the value of the specified key.
     *
     * @param key the key
     * @return the value, or `null` if the key is not present
     */
    public fun getValue(key: String): String?

    /**
     * Request a value to be entered.
     *
     * @param key the key shortly describing the value
     * @return the value, or `null` if the value can't be requested
     */
    public fun requestValue(key: String, description: String): String?

    /**
     * # Empty Environment
     *
     * Represents an empty environment that does not contain any values.
     *
     * Return `null` for [getValue] and [requestValue].
     */
    public object Empty : Environment {
        override fun getValue(key: String): String? = null
        override fun requestValue(key: String, description: String): String? = null
    }
}