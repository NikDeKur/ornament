package dev.nikdekur.ornament.dataset

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.dataset.yaml.YamlKtFileDataSetService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.TestScope

class YamlKtFileDataSetServiceTest : DataSetServiceTest() {

    override suspend fun TestScope.getDataSet(): DataSetService {
        val server = testApplication(this) {
            environment {
                value("env", "src/commonTest/resources/dataset/file")
            }

            service(
                ::YamlKtFileDataSetService,
                DataSetService::class
            )
        }

        return server.get()
    }
}