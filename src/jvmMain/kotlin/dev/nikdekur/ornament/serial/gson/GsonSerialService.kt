/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.serial.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.serial.MapData
import dev.nikdekur.ornament.serial.SerialService
import dev.nikdekur.ornament.service.AbstractAppService
import java.lang.reflect.Type

public open class GsonSerialService<A : Application>(
    override val app: A,
    protected val gson: Gson? = null,
    protected val dataType: Type = object : TypeToken<MapData>() {}.type
) : AbstractAppService<A>(), SerialService {

    internal lateinit var json: Gson
    internal lateinit var type: Type

    override suspend fun onEnable() {
        json = gson ?: GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

        type = dataType
    }

    override fun serialize(data: MapData): String {
        return json.toJson(data)
    }

    override fun deserialize(data: String): MapData {
        return json.fromJson(data, type)
    }
}