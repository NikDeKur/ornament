package dev.nikdekur.ornament.i18n

import dev.nikdekur.ndkore.placeholder.PatternPlaceholderParser
import dev.nikdekur.ndkore.reflect.KotlinXEncoderReflectMethod
import kotlinx.coroutines.test.TestResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [I18nService].
 *
 * Predefined translations:
 * - `testing` (bundle):
 *   - `en-US`: (locale)
 *     - `key1`: `Test`
 *     - `key2`: `Test {value}`
 *     - `key3`: `Let's go test! {value} {value2}`
 *
 *   - "ru-RU":
 *     - `key1`: `Тест`
 *     - `key2`: `Тест {value}`
 *     - `key3`: `Поехали тестировать! {value} {value2}`
 *
 * - "default" (bundle):
 *   - `en-US`:
 *     - `key1`: `Default test`
 *
 *   - `ru-RU`:
 *     - `key1`: `Тест по умолчанию`
 */
abstract class I18nServiceTest {

    abstract val service: I18nService

    val parser = PatternPlaceholderParser("\\{", "\\}", KotlinXEncoderReflectMethod())

    val testingBundle: Bundle
        get() = Bundle("testing")

    val defaultBundle: Bundle
        get() = Bundle("default")

    val nonExistingBundle: Bundle
        get() = Bundle("non_existing")

    public fun runTest(
        context: CoroutineContext = EmptyCoroutineContext,
        timeout: Duration = 60.seconds,
        testBody: suspend I18nService.() -> Unit
    ): TestResult {
        return kotlinx.coroutines.test.runTest(context, timeout) {
            service.testBody()
        }
    }

    @Test
    fun testGetDefaultLocale() = runTest {
        service.defaultLocale
    }

    @Test
    fun testGetTestingEnglishKey1() = runTest {
        val key = Key(testingBundle, "key1")
            .withLocale(Locale.ENGLISH_US)

        val translation = key.translate()
        assertEquals("Test", translation)
    }


    @Test
    fun testGetTestingRussianKey1() = runTest {
        val key = Key(testingBundle, "key1")
            .withLocale(Locale.RUSSIAN_RU)

        val translation = key.translate()
        assertEquals("Тест", translation)
    }


    @Test
    fun testGetTestingEnglishKey2() = runTest {
        val key = Key(testingBundle, "key2")
            .withLocale(Locale.ENGLISH_US)
            .withPlaceholders("value" to 500)

        val translation = key.translate()
        assertEquals("Test 500", translation)
    }


    @Test
    fun testGetTestingRussianKey2() = runTest {
        val key = Key(testingBundle, "key2")
            .withLocale(Locale.RUSSIAN_RU)
            .withPlaceholders("value" to 500)

        val translation = key.translate()
        assertEquals("Тест 500", translation)
    }


    @Test
    fun testGetTestingEnglishKey3() = runTest {
        val key = Key(testingBundle, "key3")
            .withLocale(Locale.ENGLISH_US)
            .withPlaceholders("value" to 500, "value2" to "test")

        val translation = key.translate()
        assertEquals("Let's go test! 500 test", translation)
    }


    @Test
    fun testGetTestingRussianKey3() = runTest {
        val key = Key(testingBundle, "key3")
            .withLocale(Locale.RUSSIAN_RU)
            .withPlaceholders("value" to 500, "value2" to "тест")

        val translation = key.translate()
        assertEquals("Поехали тестировать! 500 тест", translation)
    }


    @Test
    fun testGetDefaultEnglishKey1() = runTest {
        val key = Key(defaultBundle, "key1")
            .withLocale(Locale.ENGLISH_US)

        val translation = key.translate()
        assertEquals("Default test", translation)
    }


    @Test
    fun testGetDefaultRussianKey1() = runTest {
        val key = Key(defaultBundle, "key1")
            .withLocale(Locale.RUSSIAN_RU)

        val translation = key.translate()
        assertEquals("Тест по умолчанию", translation)
    }


    @Test
    fun testGetNonExistingKey() = runTest {
        val key = Key(testingBundle, "nonExistingKey")
            .withLocale(Locale.ENGLISH_US)

        val translation = key.translate()

        // The key does not exist, so the translation should be the key itself.
        assertEquals("nonExistingKey", translation)
    }


    @Test
    fun testGetNonExistingBundle() = runTest {
        val default = "default-for-key1"

        val key = Key(nonExistingBundle, "key1")
            .withLocale(Locale.ENGLISH_US)
            .withDefault(default)

        val translation = key.translate()

        assertEquals(default, translation)
    }


    @Test
    fun testGetKeyFromWrongBundle() = runTest {
        val key = Key(defaultBundle, "key2")
            .withLocale(Locale.ENGLISH_US)

        val translation = key.translate()

        // The key does not exist in the bundle, so the translation should be the key itself.
        assertEquals("key2", translation)
    }
}