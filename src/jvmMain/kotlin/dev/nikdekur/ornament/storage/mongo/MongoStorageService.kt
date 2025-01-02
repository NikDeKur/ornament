/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.storage.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.StorageTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


public open class MongoStorageService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), StorageService {

    override val dependencies: Dependencies = dependencies {
        +DataSetService::class
    }

    protected val dataSetService: DataSetService by inject()

    public var client: MongoClient? = null
    public var database: MongoDatabase? = null

    override suspend fun onEnable() {
        logger.info { "Initializing Database" }

        val config = dataSetService.get<MongoDataSet>("mongo")
            ?: error("Config for Mongo not found")

        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()

        val pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        val connectionString = ConnectionString(config.url)


        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .codecRegistry(pojoCodecRegistry)
            .applyToConnectionPoolSettings {
                it.maxSize(100) //connections count
                    .minSize(5)
                    .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                    .maxConnectionIdleTime(0, TimeUnit.MILLISECONDS)
            }
            .applyToSocketSettings {
                it.connectTimeout(2000, TimeUnit.MILLISECONDS)
            }
            .serverApi(serverApi)
            .build()


        // Create a new client and connect to the server
        val client = MongoClient.create(mongoClientSettings)
            .also { this.client = it }
        val database = client.getDatabase(config.database)
            .also { this.database = it }


        // Ping the server to see if it's alive
        database.runCommand(Document("ping", 1))

        logger.info { "Database initialized" }
    }

    override suspend fun onDisable() {
        val client = client
        if (client != null) {
            logger.info { "Disconnecting from Database" }
            client.close()
        } else {
            logger.warn { "Database was not initialized" }
        }
    }


    override fun getAllTables(): Flow<String> {
        return database?.listCollectionNames() ?: emptyFlow()
    }

    override suspend fun <T : Any> getTable(name: String, clazz: KClass<T>): StorageTable<T> {
        val database = database ?: error("Database not initialized")
        return MongoStorageTable(database.getCollection(name, clazz.java))
    }
}