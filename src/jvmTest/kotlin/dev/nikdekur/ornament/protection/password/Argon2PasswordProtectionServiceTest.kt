package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.password.argon2.Argon2PasswordProtectionService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import org.junit.Before

class Argon2PasswordProtectionServiceTest : PasswordProtectionServiceTest() {

    @Before
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::Argon2PasswordProtectionService, PasswordProtectionService::class)
        }
        service = server.get()
    }


    override lateinit var service: Argon2PasswordProtectionService<*>
}