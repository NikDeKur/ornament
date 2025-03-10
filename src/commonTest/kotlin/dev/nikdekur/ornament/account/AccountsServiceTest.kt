/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.account

import dev.nikdekur.ornament.protection.password.Password
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * We guarantee that the [service] will be the new instance of [dev.nikdekur.nexushub.account.AccountsService] for each test.
 */
abstract class AccountsServiceTest {

    abstract val service: AccountsService

    @Test
    fun `test create account`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))
    }

    @Test
    fun `test create account with existing login`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        assertFailsWith<AccountAlreadyExistsException> {
            service.createAccount(
                "test_login",
                "test_password",
                Password.Significance.LOWEST,
                setOf("test_scope")
            )
        }
    }

    @Test
    fun `test get non-existing account`() = runTest {
        assertNull(service.getAccount("test_login"))
    }

    @Test
    fun `test get existing account`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        assertNotNull(service.getAccount("test_login"))
    }

    @Test
    fun `test get existing account and non-existing account`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        assertNotNull(service.getAccount("test_login"))
        assertNull(service.getAccount("test_login2"))
    }

    @Test
    fun `test get accounts`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))
        service.createAccount("test_login2", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        val accounts = service.getAccounts()
        assertTrue(accounts.size == 2)

        val first = accounts.first()
        val second = accounts.last()

        // We don't know the order of the accounts
        assertTrue(first.login == "test_login" || first.login == "test_login2")
        assertTrue(second.login == "test_login" || second.login == "test_login2")
    }

    @Test
    fun `test delete account`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        assertNotNull(service.getAccount("test_login"))

        service.deleteAccount("test_login")

        assertNull(service.getAccount("test_login"))
    }

    @Test
    fun `test update account`() = runTest {
        service.createAccount("test_login", "test_password", Password.Significance.LOWEST, setOf("test_scope"))

        val account = service.getAccount("test_login")!!
        account.allowPermission("test_scope2")

        val updatedAccount = service.getAccount("test_login")!!
        assertTrue(updatedAccount.getPermissions().contains("test_scope2"))
    }


    @Test
    fun `test create account and check password`() = runTest {
        val account = service.createAccount(
            "test_login",
            "test_password",
            Password.Significance.LOWEST,
            setOf("test_scope")
        )
        assertTrue(account.password.isEqual("test_password"))
        assertFalse(account.password.isEqual("test_password2"))
    }

}