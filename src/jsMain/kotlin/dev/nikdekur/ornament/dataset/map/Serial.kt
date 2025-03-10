@file:OptIn(InternalSerializationApi::class)

package dev.nikdekur.ornament.dataset.map

import dev.nikdekur.ndkore.serial.LenientDurationSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.time.Duration

@Suppress("UNCHECKED_CAST")
public actual fun getSerializersModule(): SerializersModule {
    return SerializersModule {
        contextual(Duration::class, LenientDurationSerializer)
        contextual(Any::class, DynamicLookupSerializer)
        contextual(
            ArrayList::class,
            ListSerializer(DynamicLookupSerializer) as KSerializer<ArrayList<*>>
        )
        contextual(
            HashMap::class,
            MapSerializer(String::class.serializer(), DynamicLookupSerializer) as KSerializer<HashMap<*, *>>
        )
        contextual(
            LinkedHashMap::class,
            MapSerializer(
                String::class.serializer(),
                DynamicLookupSerializer
            ) as KSerializer<LinkedHashMap<*, *>>
        )
    }
}