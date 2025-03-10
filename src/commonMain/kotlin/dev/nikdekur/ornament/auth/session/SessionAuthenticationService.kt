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
import dev.nikdekur.ndkore.service.injectOrNull
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.account.AccountsService
import dev.nikdekur.ornament.auth.AuthenticationService
import dev.nikdekur.ornament.auth.AuthenticationService.*
import dev.nikdekur.ornament.auth.Headers
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.session.SessionService
import dev.nikdekur.ornament.session.storage.validBy
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi

public open class SessionAuthenticationService<A : Application>(
    override val app: A,
    public val initialDataset: SessionAuthenticationServiceDataSet? = null
) : AbstractAppService<A>(), AuthenticationService {


    override val dependencies: Dependencies = dependencies {
        +DataSetService::class
    }

    protected val datasetService: DataSetService? by injectOrNull()
    protected val accountsService: AccountsService by inject()
    protected val sessionService: SessionService by inject()


    public inline val clock: Clock
        get() = dataSet.clock ?: app.clock

    public lateinit var dataSet: SessionAuthenticationServiceDataSet
    public lateinit var scheduler: Scheduler

    override suspend fun onEnable() {
        dataSet = datasetService?.get<SessionAuthenticationServiceDataSet>("authentication")
            ?: initialDataset ?: SessionAuthenticationServiceDataSet()

        scheduler = CoroutineScheduler.fromSupervisor(Dispatchers.Default)
    }

    override suspend fun onDisable() {
        scheduler.shutdown()
    }


    override suspend fun login(login: String, password: String): LoginResult {
        val account = accountsService.getAccount(login)
        if (account == null)
            return LoginResult.AccountNotFound

        val match = account.password.isEqual(password)
        if (!match)
            return LoginResult.WrongCredentials

        val (token, record) = sessionService.createSession(
            userId = login,
            ttl = dataSet.expires,
            significance = dataSet.significance
        )

        return LoginResult.Success(
            mapOf(
                "login" to login,
                "token" to token,
                "valid_by" to record.validBy.epochSeconds.toString()
            )
        )
    }

    override suspend fun logout(headers: Headers): LogoutResult {
        val (login, token) = getToken(headers) ?: return LogoutResult.NotAuthenticated

        sessionService.revokeSession(login, token)

        return LogoutResult.Success
    }

    public inline fun getToken(headers: Headers): Pair<String, String>? {
        val login = headers["Login"]?.first()
        val token = headers["Authorization"]?.first()

        if (login == null || token == null)
            return null

        return login to token
    }

    override suspend fun getAuthState(headers: Headers): AuthState {
        val (login, token) = getToken(headers) ?: return AuthState.NotAuthenticated

        val session = sessionService.getSession(
            userId = login,
            token = token
        ) ?: return AuthState.NotAuthenticated

        return AuthState.Authenticated(session.userId)
    }

}