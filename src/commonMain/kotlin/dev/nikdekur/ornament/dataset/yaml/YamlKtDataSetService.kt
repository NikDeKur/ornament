/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.dataset.yaml

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.MutableDataSetSection
import dev.nikdekur.ornament.dataset.map.MutableMapDataSetService
import dev.nikdekur.ornament.dataset.map.defaultJson
import dev.nikdekur.ornament.dataset.map.getSerializersModule
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml
import kotlin.reflect.KClass

public inline fun defaultYaml(): Yaml = Yaml {
    serializersModule = getSerializersModule()
}

public abstract class YamlKtDataSetService<A : Application>(
    public open val yaml: Yaml = defaultYaml(),
    public val json: Json = defaultJson()
) : AbstractAppService<A>(), DataSetService {

    protected var delegateOrNull: MutableMapDataSetService<*>? = null
    public val delegate: MutableMapDataSetService<*>
        get() = delegateOrNull ?: error("Config not loaded!")

    public abstract fun read(): String

    override suspend fun onEnable() {
        val text = read()

        @Suppress("UNCHECKED_CAST")
        delegateOrNull = MutableMapDataSetService(
            app,
            yaml.decodeMapFromString(text).toMutableMap() as MutableMap<String, Any>,
            json
        ).also { it.enable() }
    }

    override suspend fun onDisable() {
        val delegate = delegateOrNull ?: return
        delegate.disable()
    }


    override fun <T : Any> get(key: String?, clazz: KClass<T>): T? = delegate.get<T>(key, clazz)
    override fun getSection(key: String): MutableDataSetSection? = delegate.getSection(key)
}