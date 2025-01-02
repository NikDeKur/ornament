@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.auth.session

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
public data class SessionToken(
    val login: String,
    val token: String,
    val validBy: Instant
) {

    public inline fun isValid(clock: Clock): Boolean {
        return clock.now() < validBy
    }

    public inline fun toMap(): Map<String, Any> {
        return mapOf(
            "token" to token,
            "valid_by" to validBy
        )
    }
}