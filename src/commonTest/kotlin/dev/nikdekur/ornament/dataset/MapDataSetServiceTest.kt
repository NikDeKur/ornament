/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.dataset.map.MapDataSetService
import dev.nikdekur.ornament.testApplication

class MapDataSetServiceTest : DataSetServiceTest() {

    val map = hashMapOf(

        "key1" to "value1",
        "key2" to 2,
        "key3" to true,
        "key4" to 4.0,
        "key5" to hashMapOf(
            "key1" to "value1",
            "key2" to 2,
            "key3" to true,
            "key4" to 4.0
        ),
        "key6" to hashMapOf(
            "structs" to arrayListOf(
                hashMapOf(
                    "key1" to "value1",
                    "key2" to 2,
                    "key3" to true,
                    "key4" to 4.0
                ),

                hashMapOf(
                    "key1" to "value2",
                    "key2" to 3,
                    "key3" to false,
                    "key4" to 5.0
                )
            )
        )
    )

    override suspend fun getDataSet(): DataSetService {
        val server = testApplication {
            service(
                { MapDataSetService(it, map) },
                DataSetService::class
            )
        }

        return server.get()
    }
}