@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.i18n.dataset

import dev.nikdekur.ndkore.placeholder.PatternPlaceholderParser
import dev.nikdekur.ndkore.placeholder.PlaceholderParser
import dev.nikdekur.ndkore.reflect.KotlinXEncoderReflectMethod
import dev.nikdekur.ndkore.service.*
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.dataset.getNested
import dev.nikdekur.ornament.i18n.*
import dev.nikdekur.ornament.service.AbstractAppService

public open class DataSetI18nService<A : Application>(
    override val app: A,
    public val initialDataset: DataSetI18nDataSet? = null,
    public val datasetQualifier: Qualifier = Qualifier.Empty,
    public val i18nDatasetQualifier: Qualifier = "i18n".qualifier
) : AbstractAppService<A>(), I18nService {

    override val dependencies: Dependencies = dependencies {
        dependsOn(DataSetService::class, datasetQualifier, optional = true)
        dependsOn(DataSetService::class, i18nDatasetQualifier, optional = false)
    }

    protected val datasetService: DataSetService? by injectOrNull(datasetQualifier)
    protected val i18nDatasetService: DataSetService by inject(i18nDatasetQualifier)

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
        val foundTranslationRaw = localeSection?.getNested(key.key.split('.'), Any::class)
        val translation =
            if (foundTranslationRaw is Iterable<*>)
                foundTranslationRaw.joinToString("\n")
            else foundTranslationRaw?.toString()

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