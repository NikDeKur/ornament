/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset.yaml

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetSection
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.map.MapDataSetService
import dev.nikdekur.ornament.service.AbstractAppService
import net.mamoe.yamlkt.Yaml
import kotlin.reflect.KClass

// Abstract for [configPath] and [yaml] properties
public abstract class YamlKtDataSetService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), DataSetService {

    private var _delegate: MapDataSetService<*>? = null
    protected val delegate: MapDataSetService<*>
        get() = _delegate ?: error("Config not loaded!")

    public abstract fun read(): String

    public open val yaml: Yaml = Yaml

    override suspend fun onEnable() {
        val text = read()

        @Suppress("UNCHECKED_CAST")
        _delegate = MapDataSetService(
            app,
            yaml.decodeMapFromString(text) as Map<String, Any>
        ).also { it.enable() }
    }

    override suspend fun onDisable() {
        delegate.disable()
        // Don't nullify delegate here, as might be necessary for shutdown tasks
    }


    override fun <T : Any> get(key: String?, clazz: KClass<T>): T? = delegate.get<T>(key, clazz)
    override fun getSection(key: String): DataSetSection? = delegate.getSection(key)
}