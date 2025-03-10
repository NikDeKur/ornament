/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset

import kotlin.reflect.KClass

public interface DataSetSection {
    public fun getSection(key: String): DataSetSection?
    public fun <T : Any> get(key: String?, clazz: KClass<T>): T?
    public fun contains(key: String): Boolean
}

public fun <T : Any> DataSetSection.getNested(keys: List<String>, clazz: KClass<T>): T? {
    var section: DataSetSection = this
    for (i in 0 until keys.size - 1) {
        val key = keys[i]
        section = section.getSection(key) ?: return null
    }

    return section.get(keys.last(), clazz)
}


public interface MutableDataSetSection : DataSetSection {

    override fun getSection(key: String): MutableDataSetSection?
    public fun set(key: String?, value: Any)
    public fun remove(key: String)
    public fun clear()
}


public open class SerializationException(
    public val key: String?,
    public val clazz: KClass<*>,
    public val actual: String
) : RuntimeException("Failed to deserialize key '$key' to class '${clazz.simpleName}'. Value: $actual")


public inline fun <reified T : Any> DataSetSection.get(key: String?) = get(key, T::class)