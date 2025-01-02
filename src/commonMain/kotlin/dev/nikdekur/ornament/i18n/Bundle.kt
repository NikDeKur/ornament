/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.nikdekur.ornament.i18n

import kotlinx.serialization.Serializable

@Serializable
public data class Bundle(
    val name: String
) {
    override fun toString(): String =
        "Bundle($name)"


    public companion object {
        public val Default: Bundle = Bundle("default")
    }
}