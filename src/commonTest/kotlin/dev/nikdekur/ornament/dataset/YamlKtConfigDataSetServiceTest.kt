/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.dataset

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.yaml.YamlKtDataSetService
import dev.nikdekur.ornament.testApplication

class YamlKtConfigDataSetServiceTest : DataSetServiceTest() {

    val config = """
                key1: value1
                key2: 2
                key3: true
                key4: 4.0
                key5:
                  key1: value1
                  key2: 2
                  key3: true
                  key4: 4.0
                key6:
                  structs:
                    - key1: value1
                      key2: 2
                      key3: true
                      key4: 4.0
                      
                    - key1: value2
                      key2: 3
                      key3: false
                      key4: 5.0
                """.trimIndent()


    override suspend fun getDataSet(): DataSetService {
        val server = testApplication {
            service({
                object : YamlKtDataSetService<Application>(it) {
                    override fun read(): String {
                        return config
                    }
                }
            }, DataSetService::class)
        }

        return server.get()
    }
}