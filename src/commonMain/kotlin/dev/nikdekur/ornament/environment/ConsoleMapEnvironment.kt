/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.environment

import dev.nikdekur.ndkore.ext.input
import kotlin.jvm.JvmStatic

/**
 * # Console Map Environment
 *
 * A boot environment that is backed by a map for reading values
 * and requesting values from the console for requesting values.
 *
 * @param values the map
 * @constructor creates a new boot environment
 */
public class ConsoleMapEnvironment(
    public val values: Map<String, String>
) : Environment {

    override fun getValue(key: String): String? {
        return values[key]
    }

    override fun requestValue(key: String, description: String): String? {
        return input(description).takeIf { it.isNotBlank() }
    }


    public companion object {


        /**
         * Parses command line arguments into a [dev.nikdekur.nexushub.boot.Environment].
         *
         * The arguments should be in the form of `key=value` pairs.
         *
         * @param args the command line arguments
         * @return the boot environment
         */
        @JvmStatic
        public fun fromCommandLineArgs(args: Array<String>): ConsoleMapEnvironment {
            if (args.isEmpty())
                return ConsoleMapEnvironment(emptyMap())

            val values = args
                .map { it.split("=") }
                .onEach {
                    require(it.size == 2) { "Invalid command line argument: `$it`" }
                }
                .associate { it[0] to it[1] }

            return ConsoleMapEnvironment(values)
        }
    }
}