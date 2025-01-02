package dev.nikdekur.ornament.serial

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.serial.json.JsonSerialService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class JsonSerialServiceTest : SerialServiceTest() {
    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::JsonSerialService, SerialService::class)
        }
        service = server.get()
    }

    override lateinit var service: SerialService
}