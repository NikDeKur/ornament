package dev.nikdekur.ornament.i18n.dataset

import dev.nikdekur.ornament.i18n.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DataSetI18nDataSet(

    @SerialName("default_locale")
    val defaultLocale: Locale = Locale.ENGLISH_US
)