/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.index

import kotlinx.serialization.Serializable

@Serializable
public data class IndexOptions(
    val name: String,
    val unique: Boolean = false
)

@Serializable
public class IndexOptionsBuilder {
    public var name: String = ""
    public var unique: Boolean = false

    public fun build(): IndexOptions {
        return IndexOptions(name, unique)
    }
}

public inline fun indexOptions(block: IndexOptionsBuilder.() -> Unit): IndexOptions {
    return IndexOptionsBuilder().apply(block).build()
}