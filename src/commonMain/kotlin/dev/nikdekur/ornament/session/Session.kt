package dev.nikdekur.ornament.session

import kotlinx.datetime.Instant
import kotlin.time.Duration

public interface Session {
    public val userId: String
    public val tokenHashed: String

    public val revoked: Boolean

    public val ttl: Duration
    public val issuedAt: Instant
}