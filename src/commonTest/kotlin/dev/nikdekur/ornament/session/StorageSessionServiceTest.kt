package dev.nikdekur.ornament.session

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.protection.password.none.NonePasswordProtectionService
import dev.nikdekur.ornament.session.storage.StorageSessionService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.runtime.RuntimeStorageService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class StorageSessionServiceTest : SessionServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(::NonePasswordProtectionService, PasswordProtectionService::class)
            service(::RuntimeStorageService, StorageService::class)
            service(::StorageSessionService, SessionService::class)
        }
        service = server.get()
    }


    override lateinit var service: SessionService
}