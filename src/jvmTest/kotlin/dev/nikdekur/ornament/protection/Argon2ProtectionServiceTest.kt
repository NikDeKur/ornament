package dev.nikdekur.ornament.protection

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.argon2.Argon2ProtectionService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class Argon2ProtectionServiceTest : ProtectionServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::Argon2ProtectionService, ProtectionService::class)
        }
        service = server.get()
    }


    override lateinit var service: Argon2ProtectionService<*>
}