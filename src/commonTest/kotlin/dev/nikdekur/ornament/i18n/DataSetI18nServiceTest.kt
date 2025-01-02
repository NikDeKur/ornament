package dev.nikdekur.ornament.i18n

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ndkore.service.qualifier
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.yaml.YamlKtMultiFileDataSetService
import dev.nikdekur.ornament.i18n.dataset.DataSetI18nService
import dev.nikdekur.ornament.testApplication
import kotlin.test.BeforeTest

class DataSetI18nServiceTest : I18nServiceTest() {

    @BeforeTest
    fun setup() = kotlinx.coroutines.test.runTest {
        val server = testApplication(this) {
            environment {
                value("env", "src/commonTest/resources/i18n/yaml")
            }

            service(::YamlKtMultiFileDataSetService, DataSetService::class, qualifier = "i18n".qualifier)
            service(::DataSetI18nService, I18nService::class)
        }
        service = server.get()
    }


    override lateinit var service: I18nService
}