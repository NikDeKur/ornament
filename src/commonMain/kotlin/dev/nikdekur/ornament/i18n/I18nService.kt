/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.nikdekur.ornament.i18n

/**
 * Translation provider interface, in charge of taking string keys and returning translated strings.
 *
 * @param defaultLocaleBuilder Builder returning the default locale - available in [defaultLocale], which calls it
 * the first time it's accessed.
 */
public interface I18nService {

    /**
     * Default locale of the service.
     */
    public val defaultLocale: Locale


    /** Check whether a translation key exists. **/
    public fun hasKey(key: Key): Boolean = translateKey(key) != key.default


    /** Get a translation by key. **/
    public fun translateKey(key: Key): String

    public fun Key.translate(): String = translateKey(this)
}
