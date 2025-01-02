@file:OptIn(ExperimentalEncodingApi::class)

package dev.nikdekur.ornament.session.storage

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

public class RandomTokenFabric(
    public val random: Random,
    public val length: Int = 32
) : TokenFabric {

    override fun createToken(): String {
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.Default.withPadding(Base64.PaddingOption.ABSENT).encode(bytes)
    }
}