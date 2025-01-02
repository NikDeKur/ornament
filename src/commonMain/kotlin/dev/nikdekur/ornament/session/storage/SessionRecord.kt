@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.session.storage

import dev.nikdekur.ornament.session.Session
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
public data class SessionRecord(
    override val userId: String,
    override val tokenHashed: String,

    override val revoked: Boolean,

    override val ttl: Duration,
    override val issuedAt: Instant,
) : Session


public inline fun SessionRecord.isOutdated(clock: Instant): Boolean {
    return (issuedAt + ttl) < clock
}
