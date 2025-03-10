/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.account

import dev.nikdekur.ndkore.service.get
import dev.nikdekur.ornament.account.storage.StorageAccountsService
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.protection.password.none.NonePasswordProtectionService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.runtime.RuntimeStorageService
import dev.nikdekur.ornament.testApplication
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

class StorageAccountsServiceTest : AccountsServiceTest() {

    @BeforeTest
    fun setup() = runTest {
        val server = testApplication(this) {
            service(
                ::RuntimeStorageService,
                StorageService::class
            )
            service(
                ::NonePasswordProtectionService,
                PasswordProtectionService::class
            )
            service(
                ::StorageAccountsService,
                AccountsService::class
            )
        }
        service = server.get()
    }

    override lateinit var service: AccountsService
}