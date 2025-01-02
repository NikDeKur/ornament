package dev.nikdekur.ornament.serial

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.serial.gson.GsonSerialService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class GsonSerialTest : SerialServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::GsonSerialService, SerialService::class)
        }
        service = server.get()
    }

    override lateinit var service: SerialService
}