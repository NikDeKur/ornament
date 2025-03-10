/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.i18n

import dev.nikdekur.ndkore.placeholder.PlaceholderParser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public typealias PostProcessor = Key.(translation: String) -> String

@Serializable
public data class Key(
    public val bundle: Bundle? = null,

    public val key: String,

    public val default: String = key,

    public val locale: Locale? = null,

    public val translateNestedKeys: Boolean = true,

    @Transient
    public val parser: PlaceholderParser? = null,

    @Transient
    public val placeholders: Map<String, Any?> = mapOf(),

    @Transient
    public val postProcessors: List<PostProcessor> = listOf()
) {

    public fun withDefault(default: String): Key =
        copy(default = default)

    public fun withPostProcessor(processor: PostProcessor): Key =
        copy(postProcessors = postProcessors + processor)

    public fun withPostProcessors(processors: Collection<PostProcessor>): Key =
        copy(postProcessors = postProcessors + processors)

    public fun filterPostProcessors(body: (PostProcessor) -> Boolean): Key =
        copy(postProcessors = postProcessors.filter(body))

    public fun withParser(parser: PlaceholderParser?): Key =
        copy(parser = parser)

    public fun withPlaceholders(placeholders: Map<String, Any?>): Key =
        copy(placeholders = placeholders + placeholders)

    public fun withTranslateNestedKeys(option: Boolean): Key =
        copy(translateNestedKeys = option)

    public fun withBundle(bundle: Bundle?, overwrite: Boolean = true): Key =
        if (bundle == this.bundle) {
            this
        } else if (this.bundle == null || overwrite) {
            copy(bundle = bundle)
        } else {
            this
        }

    public fun withLocale(locale: Locale?, overwrite: Boolean = true): Key =
        if (locale == this.locale) {
            this
        } else if (this.locale == null || overwrite) {
            copy(locale = locale)
        } else {
            this
        }

    public fun withBoth(
        bundle: Bundle?,
        locale: Locale?,
        overwriteBundle: Boolean = true,
        overwriteLocale: Boolean = true,
    ): Key {
        val newBundle = if (this.bundle == null || overwriteBundle) {
            bundle
        } else {
            this.bundle
        }

        val newLocale = if (this.locale == null || overwriteLocale) {
            locale
        } else {
            this.locale
        }

        if (newBundle == this.bundle && newLocale == this.locale) {
            this
        }

        return copy(bundle = newBundle, locale = newLocale)
    }


    public fun postProcess(string: String): String {
        var result = string

        postProcessors.forEach {
            result = it.invoke(this, result)
        }

        return result
    }

    // Key "translation.key" (Bundle "name" / Locale "en_GB")
    override fun toString(): String =
        buildString {
            append("Key \"$key\"")

            if (bundle != null || locale != null) {
                append("(")

                if (bundle != null) {
                    append(bundle)

                    if (locale != null) {
                        append(" / ")
                    }
                }

                if (locale != null) {
                    append("Locale \"${locale.toLanguageTag()}\"")
                }

                append(")")
            }
        }
}

public fun Key.withPlaceholders(vararg placeholders: Pair<String, Any?>): Key =
    copy(placeholders = this.placeholders + placeholders)


public object StringKeySerializer : KSerializer<Key> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Key) {
        encoder.encodeString(value.key)
    }

    override fun deserialize(decoder: Decoder): Key {
        return Key(key = decoder.decodeString())
    }
}