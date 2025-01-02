/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.auth.session

import dev.nikdekur.ndkore.scheduler.CoroutineScheduler
import dev.nikdekur.ndkore.scheduler.Scheduler
import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.account.AccountsService
import dev.nikdekur.ornament.auth.AuthenticationService
import dev.nikdekur.ornament.auth.AuthenticationService.*
import dev.nikdekur.ornament.auth.Headers
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.getTable
import dev.nikdekur.ornament.storage.request.eq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


public open class SessionAuthenticationService<A : Application>(
    override val app: A,
    public val dataSet: SessionAuthenticationServiceDataSet = SessionAuthenticationServiceDataSet()
) : AbstractAppService<A>(), AuthenticationService {


    override val dependencies: Dependencies = dependencies {
        +StorageService::class
    }

    protected val storageService: StorageService by inject()
    protected val accountsService: AccountsService by inject()

    public lateinit var scheduler: Scheduler
    public lateinit var tokensTable: StorageTable<SessionToken>

    override suspend fun onEnable() {
        tokensTable = storageService.getTable(dataSet.table)

        scheduler = CoroutineScheduler.fromSupervisor(Dispatchers.Default)
    }

    override suspend fun onDisable() {
        scheduler.shutdown()
    }

    public inline val clock: Clock
        get() = dataSet.clock ?: app.clock

    override suspend fun login(login: String, password: String): LoginResult {
        val account = accountsService.getAccount(login)
        if (account == null)
            return LoginResult.AccountNotFound

        val match = account.password.isEqual(password)
        if (!match)
            return LoginResult.WrongCredentials

        // Remove all previous tokens
        tokensTable.deleteMany(
            SessionToken::login eq login
        )

        val token = Uuid.random()
        val expires = dataSet.expires

        val sessionToken = SessionToken(
            login = login,
            token = token.toString(),
            validBy = (clock.now() + expires)
        )

        tokensTable.insertOne(sessionToken)

        return LoginResult.Success(
            mapOf(
                "token" to sessionToken.token,
                "valid_by" to sessionToken.validBy
            )
        )
    }

    override suspend fun logout(headers: Headers): LogoutResult {
        val auth = getAuthState(headers)
        if (auth != AuthState.AUTHENTICATED)
            return LogoutResult.NotAuthenticated

        val token = getToken(headers) ?: return LogoutResult.NotAuthenticated

        tokensTable.deleteOne(
            SessionToken::token eq token
        )

        return LogoutResult.Success
    }

    public inline fun getToken(headers: Headers): String? = headers["Authorization"]?.first()

    override suspend fun getAuthState(headers: Headers): AuthState {
        val authTokenId = getToken(headers)
        if (authTokenId == null)
            return AuthState.UNAUTHENTICATED

        val filter = SessionToken::token eq authTokenId
        val realToken = tokensTable.find(filter)
            .toList()
            .firstOrNull()

        if (realToken == null || !realToken.isValid(clock))
            return AuthState.BAD_CREDENTIALS

        return AuthState.AUTHENTICATED
    }

}