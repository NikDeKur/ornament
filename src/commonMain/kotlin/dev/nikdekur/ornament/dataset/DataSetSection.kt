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
}


public inline fun <reified T : Any> DataSetSection.get(key: String?) = get(key, T::class)