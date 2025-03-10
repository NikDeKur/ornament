package dev.nikdekur.ornament.session

import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.session.storage.SessionRecord
import kotlin.time.Duration

public interface SessionService {

    public suspend fun createSession(
        userId: String,
        ttl: Duration,
        significance: Password.Significance
    ): Pair<String, SessionRecord>

    public suspend fun getSession(
        userId: String,
        token: String
    ): SessionRecord?

    public suspend fun revokeSession(
        userId: String,
        token: String
    ): Boolean
}