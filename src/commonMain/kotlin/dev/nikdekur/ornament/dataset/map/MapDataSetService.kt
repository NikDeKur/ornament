/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.dataset.map

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetSection
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.MutableDataSetSection
import dev.nikdekur.ornament.dataset.MutableDataSetService
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmName
import kotlin.properties.Delegates
import kotlin.reflect.KClass

public expect fun getSerializersModule(): SerializersModule

public inline fun defaultJson(): Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializersModule = getSerializersModule()
}

public open class MapDataSetService<A : Application>(
    override val app: A,
    public open val map: Map<String, Any>,
    public val json: Json = defaultJson()
) : AbstractAppService<A>(), DataSetService {

    @get:JvmName("rootVal")
    protected open var root: MapDataSetSection by Delegates.notNull()

    public open fun getRoot(): MapDataSetSection = root

    protected open fun createRoot(json: Json, map: Map<String, Any>): MapDataSetSection = MapDataSetSection(json, map)

    override suspend fun onEnable() {
        root = createRoot(json, map)
    }

    override fun getSection(key: String): DataSetSection? = root.getSection(key)
    override fun <T : Any> get(key: String?, clazz: KClass<T>): T? = root[key, clazz]
}


public open class MutableMapDataSetService<A : Application>(
    app: A,
    public override val map: MutableMap<String, Any>,
    json: Json = defaultJson()
) : MapDataSetService<A>(app, map, json), MutableDataSetService {

    override fun getRoot(): MutableMapDataSetSection = root as MutableMapDataSetSection

    override fun createRoot(json: Json, map: Map<String, Any>): MapDataSetSection {
        return MutableMapDataSetSection(json, map.toMutableMap())
    }

    override fun getSection(key: String): MutableDataSetSection? = getRoot().getSection(key)
    override fun set(key: String?, value: Any): Unit = getRoot().set(key, value)
    override fun remove(key: String): Unit = getRoot().remove(key)
    override fun clear(): Unit = getRoot().clear()
}