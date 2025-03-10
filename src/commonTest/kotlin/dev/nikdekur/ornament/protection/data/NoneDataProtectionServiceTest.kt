package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.data.none.NoneDataProtectionService
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.protection.password.none.NonePasswordProtectionService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class NoneDataProtectionServiceTest : DataProtectionServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::NonePasswordProtectionService, PasswordProtectionService::class)
            service(::NoneDataProtectionService, DataProtectionService::class)
        }

        service = server.get()
        protectionService = server.get()
    }


    override lateinit var service: DataProtectionService
    override lateinit var protectionService: PasswordProtectionService

}