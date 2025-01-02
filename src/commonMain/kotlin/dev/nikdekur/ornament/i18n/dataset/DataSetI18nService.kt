@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.i18n.dataset

import dev.nikdekur.ndkore.placeholder.PatternPlaceholderParser
import dev.nikdekur.ndkore.placeholder.PlaceholderParser
import dev.nikdekur.ndkore.reflect.KotlinXEncoderReflectMethod
import dev.nikdekur.ndkore.service.*
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.i18n.*
import dev.nikdekur.ornament.service.AbstractAppService

public open class DataSetI18nService<A : Application>(
    override val app: A,
    public val initialDataset: DataSetI18nDataSet? = null,
    public val datasetQualifier: Qualifier = "i18n".qualifier
) : AbstractAppService<A>(), I18nService {

    override val dependencies: Dependencies = dependencies {
        -DataSetService::class
        dependsOn(DataSetService::class, datasetQualifier)
    }

    protected val datasetService: DataSetService? by injectOrNull()
    protected val i18nDatasetService: DataSetService by inject(datasetQualifier)

    override lateinit var defaultLocale: Locale
    public lateinit var parser: PlaceholderParser

    override suspend fun onEnable() {
        val dataset = initialDataset
            ?: datasetService?.get<DataSetI18nDataSet>("i18n")
            ?: DataSetI18nDataSet()


        parser = PatternPlaceholderParser("\\{", "\\}", KotlinXEncoderReflectMethod())

        defaultLocale = dataset.defaultLocale
    }

    public inline fun getLocaleFor(key: Key): String {
        return (key.locale ?: defaultLocale).toLanguageTag()
    }

    public inline fun getBundleFor(key: Key): String {
        return (key.bundle ?: Bundle.Default).name
    }

    override fun translateKey(key: Key): String {
        val locale = key.locale
        val bundle = key.bundle

        val keyDefault = key.default

        val bundleSection = i18nDatasetService.getSection(getBundleFor(key))
        val localeSection = bundleSection?.getSection(getLocaleFor(key))
        val foundTranslation = localeSection?.get<ArrayList<String>>(key.key)
        val translation = foundTranslation?.joinToString("\n")

        val allReplacements = key.placeholders.mapValues {
            if (key.translateNestedKeys && it.value is Key) {
                (it.value as Key)
                    .withBundle(bundle, false)
                    .withLocale(locale, false)
                    .translate()
            } else {
                it.value
            }
        }

        val parser = key.parser ?: parser
        val translated = translation ?: keyDefault
        val parsed = parser.parse(translated, allReplacements)
        return key.postProcess(parsed)
    }

}