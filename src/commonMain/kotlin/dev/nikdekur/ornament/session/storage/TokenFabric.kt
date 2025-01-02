package dev.nikdekur.ornament.session.storage

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public fun interface TokenFabric {

    public fun createToken(): String

    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public val UUID: TokenFabric = TokenFabric {

            // Uuid.random() uses SecureRandom, so it's safe to use as a token generator.
            Uuid.random().toString()
        }
    }
}