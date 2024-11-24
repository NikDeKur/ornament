/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package dev.nikdekur.ornament.dataset.map

import dev.nikdekur.ndkore.ext.toBooleanSmartOrNull
import dev.nikdekur.ornament.dataset.DataSetSection
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

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

public fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Array<*> -> toJsonArray()
        is List<*> -> toJsonArray()
        is Map<*, *> -> toJsonObject()
        is JsonElement -> this
        else -> JsonNull
    }
}


public fun Array<*>.toJsonArray(): JsonArray {
    val array = mutableListOf<JsonElement>()
    this.forEach { array.add(it.toJsonElement()) }
    return JsonArray(array)
}


public fun List<*>.toJsonArray(): JsonArray {
    val array = mutableListOf<JsonElement>()
    this.forEach { array.add(it.toJsonElement()) }
    return JsonArray(array)
}


public fun Map<*, *>.toJsonObject(): JsonObject {
    val map = mutableMapOf<String, JsonElement>()
    this.forEach {
        val key = it.key
        if (key is String) {
            map[key] = it.value.toJsonElement()
        }
    }
    return JsonObject(map)
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


    public fun <T : Any> resolve(obj: Any, clazz: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (clazz.isInstance(obj)) return obj as T
        @Suppress("UNCHECKED_CAST")
        when (clazz) {
            String::class -> return obj.toString() as T
            Byte::class -> return obj.toString().toByteOrNull() as T
            Short::class -> return obj.toString().toShortOrNull() as T
            Int::class -> return obj.toString().toIntOrNull() as T
            Long::class -> return obj.toString().toLongOrNull() as T
            Float::class -> return obj.toString().toFloatOrNull() as T
            Double::class -> return obj.toString().toDoubleOrNull() as T
            Boolean::class -> return obj.toString().toBooleanSmartOrNull() as T
        }

        val jsonElement = obj.toJsonElement()
        return json.decodeFromJsonElement(clazz.serializer(), jsonElement)
    }


    override operator fun <T : Any> get(key: String?, clazz: KClass<T>): T? {
        if (key == null) return resolve(map, clazz)
        val at = map[key] ?: return null
        return resolve(at, clazz)
    }
}