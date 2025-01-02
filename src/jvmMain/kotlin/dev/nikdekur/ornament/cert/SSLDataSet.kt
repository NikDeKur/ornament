package dev.nikdekur.ornament.cert

import kotlinx.serialization.Serializable

@Serializable
public data class SSLDataSet(
    val cert: String,
    val key: String,
    val alias: String = "ktor"
)
