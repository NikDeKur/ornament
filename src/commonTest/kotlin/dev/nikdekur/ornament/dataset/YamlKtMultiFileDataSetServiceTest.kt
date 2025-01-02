@file:Suppress("ClassName", "PropertyName")

package dev.nikdekur.ornament.dataset

import dev.nikdekur.ndkore.ext.encodeToMap
import dev.nikdekur.ndkore.ext.toJsonElement
import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.dataset.map.MapDataSetSection
import dev.nikdekur.ornament.dataset.yaml.YamlKtMultiFileDataSetService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MultiFileStruct(

    @SerialName("key1-2")
    val key1_2: Key1_2,

    @SerialName("key3-4")
    val key3_4: Key3_4,

    @SerialName("key5-6")
    val key5_6: Key5_6
)

@Serializable
data class Key1_2(
    val key1: Key1,
    val key2: Key2
)

@Serializable
data class Key1(
    val key1: String
)

@Serializable
data class Key2(
    val key2: Int
)


@Serializable
data class Key3_4(
    val key3: Key3,
    val key4: Key4
)

@Serializable
data class Key3(
    val key3: Boolean
)

@Serializable
data class Key4(
    val key4: Double
)


@Serializable
data class Key5_6(
    val key5: Key5,
    val key6: Key6
)

@Serializable
data class Key5(
    val key5: TestStruct
)

@Serializable
data class Key6(
    val key6: NestedStruct
)


class YamlKtMultiFileDataSetServiceTest : DataSetServiceTest() {

    override suspend fun TestScope.getDataSet(): DataSetSection {
        val server = testApplication(this) {
            environment {
                value("env", "src/commonTest/resources/dataset/multi-file")
            }

            service(
                ::YamlKtMultiFileDataSetService,
                DataSetService::class
            )
        }

        val service = server.get<YamlKtMultiFileDataSetService<*>>()

        // Checks that structure is correct
        val data = service.get<MultiFileStruct>(null) ?: error("MultiFileStruct is null")

        val json = service.delegate.getRoot().json
        val map = mapOf(
            "key1" to data.key1_2.key1.key1.toJsonElement(),
            "key2" to data.key1_2.key2.key2.toJsonElement(),

            "key3" to data.key3_4.key3.key3.toJsonElement(),
            "key4" to data.key3_4.key4.key4.toJsonElement(),

            "key5" to json.encodeToMap(data.key5_6.key5.key5),
            "key6" to json.encodeToMap(data.key5_6.key6.key6)
        )
        val fakeSection = MapDataSetSection(json, map)
        return fakeSection
    }

//    @Test
//    fun justTest() = runTest {
//        getDataSet()
//    }
}