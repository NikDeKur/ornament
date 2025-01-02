package dev.nikdekur.ornament.auth.session

import dev.nikdekur.ndkore.ext.LenientDurationSerializer
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
public data class SessionAuthenticationServiceDataSet(
    @Transient
    val clock: Clock? = null,

    val table: String = "session_tokens",

    @Serializable(with = LenientDurationSerializer::class)
    val expires: Duration = 15.minutes
)