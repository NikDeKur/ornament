@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.nikdekur.ornament

import dev.nikdekur.ndkore.service.Definition
import dev.nikdekur.ndkore.service.Qualifier
import dev.nikdekur.ndkore.service.manager.RuntimeServicesManager
import dev.nikdekur.ndkore.service.manager.ServicesManager
import dev.nikdekur.ndkore.time.clock.FixedStartClock
import dev.nikdekur.ornament.environment.Environment
import dev.nikdekur.ornament.environment.EnvironmentBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.reflect.KClass

public typealias ServiceConstructor<T> = (Application) -> T

public open class TestDefinition(
    public val construct: ServiceConstructor<Any>,
    override val qualifier: Qualifier,
    override val bindTo: Iterable<KClass<*>>
) : Definition<Any> {

    override lateinit var service: Any


    public fun initialize(application: Application) {
        service = construct(application)
    }
}

public open class TestApplication(
    override val clock: Clock,
    public val services: List<TestDefinition> = emptyList(),
    override val environment: Environment = Environment.Empty,
    public val onStop: suspend () -> Unit = {}
) : AbstractApplication() {

    override suspend fun createServicesManager(): ServicesManager {
        return RuntimeServicesManager {}.also { manager ->
            services.forEach {
                it.initialize(this)

                manager.registerService(it)
            }
        }
    }

    override suspend fun stop() {
        super.stop()
        onStop()
    }
}

public class TestApplicationBuilder {
    public var init: Boolean = true
    public var start: Boolean = true

    public var clock: Clock = Clock.System
    public val services: MutableList<TestDefinition> = mutableListOf()
    public var environment: Environment = Environment.Empty
    public var onStop: () -> Unit = {}


    public fun <T : Any> service(
        service: ServiceConstructor<T>,
        bindTo: Iterable<KClass<out T>>,
        qualifier: Qualifier = Qualifier.Empty
    ) {
        val definition = TestDefinition(
            construct = service,
            qualifier = qualifier,
            bindTo = bindTo
        )
        services.add(definition)
    }

    public fun <T : Any> service(
        service: ServiceConstructor<T>,
        vararg bindTo: KClass<out T>,
        qualifier: Qualifier = Qualifier.Empty
    ): Unit = service(service, bindTo.asIterable(), qualifier)

    public fun environment(environment: Environment) {
        this.environment = environment
    }

    public inline fun TestApplicationBuilder.environment(block: EnvironmentBuilder.() -> Unit) {
        val env = EnvironmentBuilder().apply(block).build()
        environment(env)
    }


    public fun onStop(onStop: () -> Unit) {
        this.onStop = onStop
    }

    public suspend fun build(): Application {
        val server = TestApplication(clock, services, environment, onStop)

        if (init) server.init()
        if (start) server.start()

        return server
    }
}


public suspend inline fun testApplication(
    scope: TestScope? = null,
    block: TestApplicationBuilder.() -> Unit
): Application =
    TestApplicationBuilder()
        .apply {
            if (scope != null)
                clock = FixedStartClock(
                    Instant.fromEpochMilliseconds(0),
                    scope.testTimeSource.markNow()
                )
        }
        .apply(block)
        .build()