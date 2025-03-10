package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.protection.password.argon2.Argon2PasswordProtectionService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import org.junit.Before

class AESGCMDataProtectionServiceTest : DataProtectionServiceTest() {

    @Before
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::Argon2PasswordProtectionService, PasswordProtectionService::class)
            service(::AESGCMDataProtectionService, DataProtectionService::class)
        }

        service = server.get()
        protectionService = server.get()
    }


    override lateinit var service: DataProtectionService
    override lateinit var protectionService: PasswordProtectionService
}