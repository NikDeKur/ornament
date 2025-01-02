package dev.nikdekur.ornament.serial.json

import dev.nikdekur.ndkore.ext.toJsonObject
import dev.nikdekur.ndkore.ext.toMap
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.serial.MapData
import dev.nikdekur.ornament.serial.SerialService
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

public open class JsonSerialService<A : Application>(
    override val app: A,
    protected val initialJson: Json? = null,
) : AbstractAppService<A>(), SerialService {

    public lateinit var json: Json

    override suspend fun onEnable() {
        json = initialJson ?: Json {
            isLenient = true
        }
    }

    override fun serialize(data: MapData): String {
        return data.toJsonObject().toString()
    }

    override fun deserialize(data: String): MapData {
        val obj = json.parseToJsonElement(data)
        return obj.jsonObject.toMap()
    }

}