package dev.nikdekur.ornament.i18n

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Locale.Serializer::class)
public data class Locale(
    val language: String,
    val region: String = "",
    val variant: String = "",
    val script: String = "",
) {

    public object Serializer : KSerializer<Locale> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Locale) {
            encoder.encodeString(value.toLanguageTag())
        }

        override fun deserialize(decoder: Decoder): Locale =
            decoder.decodeString().toLocale()
    }


    public companion object {
        public val ENGLISH_US: Locale = Locale("en", "US")
        public val ENGLISH_UK: Locale = Locale("en", "UK")

        public val FRENCH: Locale = Locale("fr")
        public val FRENCH_CA: Locale = Locale("fr", "CA")
        public val FRENCH_FR: Locale = Locale("fr", "FR")

        public val GERMAN: Locale = Locale("de")
        public val GERMAN_DE: Locale = Locale("de", "DE")

        public val ITALIAN: Locale = Locale("it")
        public val ITALIAN_IT: Locale = Locale("it", "IT")

        public val JAPANESE: Locale = Locale("ja")
        public val JAPANESE_JP: Locale = Locale("ja", "JP")

        public val KOREAN: Locale = Locale("ko")
        public val KOREAN_KR: Locale = Locale("ko", "KR")

        public val PORTUGUESE: Locale = Locale("pt")
        public val PORTUGUESE_BR: Locale = Locale("pt", "BR")

        public val RUSSIAN: Locale = Locale("ru")
        public val RUSSIAN_RU: Locale = Locale("ru", "RU")

        public val SPANISH: Locale = Locale("es")
        public val SPANISH_ES: Locale = Locale("es", "ES")

        public val CHINESE: Locale = Locale("zh")
        public val CHINESE_CN: Locale = Locale("zh", "CN")
        public val CHINESE_TW: Locale = Locale("zh", "TW")

        public val ARABIC: Locale = Locale("ar")
        public val ARABIC_AE: Locale = Locale("ar", "AE")

        public val HINDI: Locale = Locale("hi")
        public val HINDI_IN: Locale = Locale("hi", "IN")

        public val TURKISH: Locale = Locale("tr")
        public val TURKISH_TR: Locale = Locale("tr", "TR")

        public val POLISH: Locale = Locale("pl")
        public val POLISH_PL: Locale = Locale("pl", "PL")

        public val DUTCH: Locale = Locale("nl")
        public val DUTCH_NL: Locale = Locale("nl", "NL")

        public val CZECH: Locale = Locale("cs")
        public val CZECH_CZ: Locale = Locale("cs", "CZ")

        public val SWEDISH: Locale = Locale("sv")
        public val SWEDISH_SE: Locale = Locale("sv", "SE")

        public val DANISH: Locale = Locale("da")
        public val DANISH_DK: Locale = Locale("da", "DK")

        public val FINNISH: Locale = Locale("fi")
        public val FINNISH_FI: Locale = Locale("fi", "FI")

        public val NORWEGIAN: Locale = Locale("no")
        public val NORWEGIAN_NO: Locale = Locale("no", "NO")

        public val GREEK: Locale = Locale("el")
        public val GREEK_GR: Locale = Locale("el", "GR")

        public val HUNGARIAN: Locale = Locale("hu")
        public val HUNGARIAN_HU: Locale = Locale("hu", "HU")

        public val ROMANIAN: Locale = Locale("ro")
        public val ROMANIAN_RO: Locale = Locale("ro", "RO")

        public val SLOVAK: Locale = Locale("sk")
        public val SLOVAK_SK: Locale = Locale("sk", "SK")

        public val BULGARIAN: Locale = Locale("bg")
        public val BULGARIAN_BG: Locale = Locale("bg", "BG")

        public val CROATIAN: Locale = Locale("hr")
        public val CROATIAN_HR: Locale = Locale("hr", "HR")

        public val SERBIAN: Locale = Locale("sr")
        public val SERBIAN_RS: Locale = Locale("sr", "RS")

        public val SLOVENIAN: Locale = Locale("sl")
        public val SLOVENIAN_SI: Locale = Locale("sl", "SI")

        public val ESTONIAN: Locale = Locale("et")
        public val ESTONIAN_EE: Locale = Locale("et", "EE")

        public val LATVIAN: Locale = Locale("lv")
        public val LATVIAN_LV: Locale = Locale("lv", "LV")
    }
}

public fun Locale.toLanguageTag(): String {
    val builder = StringBuilder(language)

    if (script.isNotEmpty()) {
        builder.append("-").append(script)
    }

    if (region.isNotEmpty()) {
        builder.append("-").append(region)
    }

    if (variant.isNotEmpty()) {
        builder.append("-").append(variant)
    }

    return builder.toString()
}

public fun String.toLocaleOrNull(): Locale? {
    val parts = split("-", "_", ";", ",", " ")

    if (parts.isEmpty() || parts[0].length !in 2..3) return null

    val language = parts[0]
    val script = if (parts.size > 1 && parts[1].length == 4) parts[1] else ""
    val region = if (parts.size > 1 && parts[1].length != 4) parts.getOrNull(1) ?: "" else ""
    val variant = if (parts.size > 2) parts.drop(2).joinToString("-") else ""

    // Check that the region has a length of 2
    if (region.isNotEmpty() && region.length != 2) return null

    // Check that the script has a length of 4
    if (script.isNotEmpty() && script.length != 4) return null

    return Locale(
        language = language,
        script = script,
        region = region,
        variant = variant
    )
}

public fun String.toLocale(): Locale = toLocaleOrNull() ?: throw IllegalArgumentException("Invalid locale: $this")

