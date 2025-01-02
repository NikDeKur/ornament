package dev.nikdekur.ornament.account

import kotlinx.serialization.Serializable

@Serializable
public data class AccountCredentials(
    val login: String,
    val password: String
)
