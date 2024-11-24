/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package dev.nikdekur.ornament.dataset.map

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetSection
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.properties.Delegates
import kotlin.reflect.KClass

public expect fun getSerializersModule(): SerializersModule

public open class MapDataSetService<A : Application>(
    override val app: A,
    public val map: Map<String, Any>
) : AbstractAppService<A>(), DataSetService {

    public open var root: MapDataSetSection by Delegates.notNull()

    override suspend fun onEnable() {
        val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = getSerializersModule()
        }

        root = MapDataSetSection(json, map)
    }

    override fun getSection(key: String): DataSetSection? = root.getSection(key)
    override fun <T : Any> get(key: String?, clazz: KClass<T>): T? = root[key, clazz]
}