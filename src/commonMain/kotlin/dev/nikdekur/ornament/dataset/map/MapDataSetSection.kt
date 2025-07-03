/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package dev.nikdekur.ornament.dataset.map

import dev.nikdekur.ndkore.ext.encodeToMap
import dev.nikdekur.ndkore.ext.toBooleanSmartOrNull
import dev.nikdekur.ndkore.ext.toJsonElement
import dev.nikdekur.ornament.dataset.DataSetSection
import dev.nikdekur.ornament.dataset.MutableDataSetSection
import dev.nikdekur.ornament.dataset.SerializationException
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlinx.serialization.SerializationException as XSerializationException

public object DynamicLookupSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class, null, emptyArray()).descriptor

    override fun serialize(encoder: Encoder, value: Any) {
        val actualSerializer = encoder.serializersModule.getContextual(value::class) ?: value::class.serializer()
        @Suppress("UNCHECKED_CAST")
        encoder.encodeSerializableValue(actualSerializer as KSerializer<Any>, value)
    }

    override fun deserialize(decoder: Decoder): Any {
        error("Unsupported")
    }
}

public open class MapDataSetSection(
    public val json: Json,
    public val map: Map<String, Any>
) : DataSetSection {


    override fun getSection(key: String): DataSetSection? {
        @Suppress("UNCHECKED_CAST")
        val map = map[key] as? Map<String, Any> ?: return null
        return MapDataSetSection(json, map)
    }


    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> resolve(obj: Any, clazz: KClass<T>): T? {
        if (clazz.isInstance(obj)) return obj as T

        when (clazz) {
            String::class -> return obj.toString() as T
            Byte::class -> return obj.toString().toByteOrNull() as? T
            Short::class -> return obj.toString().toShortOrNull() as? T
            Int::class -> return obj.toString().toIntOrNull() as? T
            Long::class -> return obj.toString().toLongOrNull() as? T
            Float::class -> return obj.toString().toFloatOrNull() as? T
            Double::class -> return obj.toString().toDoubleOrNull() as? T
            Boolean::class -> return obj.toString().toBooleanSmartOrNull() as? T
        }

        val serializer = json.serializersModule.serializer(clazz, emptyList(), false) as KSerializer<T>

        val jsonElement = obj.toJsonElement()
        return json.decodeFromJsonElement(serializer, jsonElement)
    }


    override operator fun <T : Any> get(key: String?, clazz: KClass<T>): T? {
        val actual = if (key == null) map else map[key] ?: return null

        if (actual is Map<*, *> && MapDataSetSection::class == clazz) {
            @Suppress("kotlin:S6530", "UNCHECKED_CAST") // We know the type is correct
            return when (key) {
                null -> this
                else -> getSection(key)
            } as T
        }

        try {
            return resolve(actual, clazz) ?: throw SerializationException(key, clazz, actual.toString())
        } catch (e: XSerializationException) {
            e.printStackTrace()
            throw SerializationException(key, clazz, actual.toString())
        } catch (e: IllegalArgumentException) {
            throw SerializationException(key, clazz, actual.toString())
        }
    }

    override fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun toString(): String {
        return "MapDataSetSection(map=$map)"
    }
}


public open class MutableMapDataSetSection(
    json: Json,
    public val mutableMap: MutableMap<String, Any>
) : MapDataSetSection(json, mutableMap), MutableDataSetSection {

    override fun getSection(key: String): MutableDataSetSection? {
        @Suppress("UNCHECKED_CAST")
        val map = mutableMap[key] as? Map<String, Any> ?: return null
        val mutable = map as? MutableMap
            ?: map.toMutableMap().also { mutableMap[key] = it }

        return MutableMapDataSetSection(json, mutable)
    }

    override fun set(key: String?, value: Any) {
        @Suppress("UNCHECKED_CAST")
        if (key == null) {
            val serializer = json.serializersModule.serializer(value::class, emptyList(), false) as KSerializer<Any>
            val map = json.encodeToMap(value, serializer) as Map<String, Any>

            mutableMap.clear()
            mutableMap.putAll(map)
        } else {
            mutableMap[key] = value
        }
    }

    override fun remove(key: String) {
        mutableMap.remove(key)
    }

    override fun clear() {
        mutableMap.clear()
    }


    override fun toString(): String {
        return "MutableMapDataSetSection(map=$mutableMap)"
    }
}