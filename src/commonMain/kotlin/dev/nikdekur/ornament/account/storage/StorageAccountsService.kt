/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.account.storage

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.account.Account
import dev.nikdekur.ornament.account.AccountAlreadyExistsException
import dev.nikdekur.ornament.account.AccountsService
import dev.nikdekur.ornament.account.Permission
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.getTable
import dev.nikdekur.ornament.storage.index.indexOptions
import dev.nikdekur.ornament.storage.request.eq
import kotlinx.coroutines.flow.toList

// TODO: Исправить эту тотальную трагедию
public open class StorageAccountsService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), AccountsService {

    override val dependencies: Dependencies = dependencies {
        +StorageService::class
    }

    public val passwordProtectionService: PasswordProtectionService by inject()
    public val storage: StorageService by inject()


    public lateinit var table: StorageTable<AccountDAO>

    public val accounts: MutableMap<String, Account> = ConcurrentMutableMap<String, Account>()

    override suspend fun onEnable() {
        table = storage.getTable("accounts")
        table.createIndex(
            mapOf("login" to 1),
            indexOptions {
                unique = true
            }
        )


        table.find().toList().forEach {
            registerAccount(it)
        }
    }


    override suspend fun onDisable() {
        accounts.clear()
    }

    override suspend fun getAccounts(): Collection<Account> {
        return accounts.values
    }


    override suspend fun getAccount(login: String): Account? {
        return accounts[login]
    }

    public suspend fun registerAccount(dao: AccountDAO): Account {
        val password = passwordProtectionService.deserializePassword(dao.password)
        val account = SetAccount(dao.login, password, ConcurrentMutableSet())
        return AccountWrapper(this, account).also {
            dao.permissions.forEach {
                account.allowPermission(it)
            }
            accounts[dao.login] = account
        }
    }


    public suspend fun update(dao: Account) {
        val dao = AccountDAO(
            login = dao.login,
            password = dao.password.serialize(),
            permissions = dao.getPermissions()
        )
        table.replaceOne(dao, AccountDAO::login eq dao.login)
    }

    public suspend fun createAccount(dao: AccountDAO): Account {
        table.insertOne(dao)
        return registerAccount(dao)
    }

    override suspend fun createAccount(
        login: String,
        password: String,
        passwordSignificance: Password.Significance,
        allowedScopes: Iterable<String>
    ): Account {
        if (getAccount(login) != null)
            throw AccountAlreadyExistsException(login)

        val password = passwordProtectionService.createPassword(password, passwordSignificance)
        val dao = AccountDAO(
            login = login,
            password = password.serialize(),
            permissions = allowedScopes.toSet()
        )
        return createAccount(dao)
    }


    /**
     * Deletes an account from the database and the cache.
     *
     * Note that method will not disconnect clients that are currently using the account.
     *
     * @param login The login of the account to delete.
     */
    override suspend fun deleteAccount(login: String) {
        val filter = "login" eq login
        table.deleteOne(filter)
        accounts.remove(login)
    }


    public class AccountWrapper(
        public val service: StorageAccountsService<*>,
        public val delegate: Account
    ) : Account by delegate {

        override suspend fun changePassword(newPassword: Password) {
            delegate.changePassword(newPassword)
            service.update(this)
        }

        override suspend fun allowPermission(permission: Permission) {
            delegate.allowPermission(permission)
            service.update(this)
        }

        override suspend fun disallowPermission(permission: Permission) {
            delegate.disallowPermission(permission)
            service.update(this)
        }

        override suspend fun clearPermission() {
            delegate.clearPermission()
            service.update(this)
        }
    }
}