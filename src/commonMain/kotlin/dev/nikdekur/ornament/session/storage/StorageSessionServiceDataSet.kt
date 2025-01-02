package dev.nikdekur.ornament.session.storage

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
public data class StorageSessionServiceDataSet(
    @Transient
    val fabric: TokenFabric = TokenFabric.UUID,

    @Transient
    val clock: Clock? = null,

    val table: String = "sessions"
)
