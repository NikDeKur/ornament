package dev.nikdekur.ornament

import dev.nikdekur.ndkore.service.manager.RuntimeServicesManager
import dev.nikdekur.ndkore.service.manager.ServicesManager
import dev.nikdekur.ornament.environment.Environment
import dev.nikdekur.ornament.environment.EnvironmentBuilder
import kotlin.reflect.KClass

public typealias ServiceConstructor<T> = (Application) -> T


public class TestApplication(
    public val services: List<Pair<ServiceConstructor<*>, KClass<out Any>>>,
    override val environment: Environment,
    public val onStop: suspend () -> Unit
) : AbstractApplication() {

    override suspend fun createServicesManager(): ServicesManager {
        return RuntimeServicesManager {}.also {
            services.forEach { (service, serviceInterface) ->
                it.registerInternal(service, serviceInterface)
            }
        }
    }

    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    public suspend fun <T : Any> ServicesManager.registerInternal(
        service: ServiceConstructor<*>,
        serviceInterface: KClass<T>
    ) {
        registerService(service(this@TestApplication) as T, serviceInterface)
    }

    override suspend fun stop() {
        onStop()
    }
}

public class TestApplicationBuilder {
    public var init: Boolean = true
    public var start: Boolean = true
    public val services: MutableList<Pair<ServiceConstructor<*>, KClass<out Any>>> = mutableListOf()
    public var environment: Environment = Environment.Empty
    public var onStop: () -> Unit = {}

    public fun <T : Any> service(service: ServiceConstructor<T>, serviceInterface: KClass<T>) {
        services.add(service to serviceInterface)
    }

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
        val server = TestApplication(services, environment, onStop)
        if (init) server.init()
        if (start) server.start()

        return server
    }
}


public suspend inline fun testApplication(block: TestApplicationBuilder.() -> Unit): Application =
    TestApplicationBuilder().apply(block).build()