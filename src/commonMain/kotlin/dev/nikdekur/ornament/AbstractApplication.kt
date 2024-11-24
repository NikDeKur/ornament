package dev.nikdekur.ornament

import dev.nikdekur.ndkore.ext.recordTiming
import dev.nikdekur.ndkore.ext.smartAwait
import dev.nikdekur.ndkore.service.manager.ServicesManager
import dev.nikdekur.ornament.Application.State
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * # AbstractApplication
 *
 * Abstract implementation of the [Application] interface.
 *
 * Provides a standard implementation of the application lifecycle.
 * This includes:
 * - Changing state
 * - Logging
 * - Start and Stop basic logic
 *
 * ### Example usage:
 * ```kotlin
 * class MyApplication : AbstractApplication() {
 *    override fun createServicesManager(): ServicesManager {
 *      return RuntimeServicesManager().apply {
 *          registerService(MyServiceImpl(), MyService::class)
 *          registerService(MySecondServiceImpl(), MySecondService::class)
 *        }
 *     }
 * }
 */
public abstract class AbstractApplication : Application {


    public open val timeSource: TimeSource = TimeSource.Monotonic

    override var state: State = State.Created

    override val logger: KLogger = KotlinLogging.logger { }

    public var initTime: TimeMark? = null
    public var lastStartTime: TimeMark? = null
    public var waiter: CompletableDeferred<Unit>? = null

    override lateinit var servicesManager: ServicesManager

    override val uptime: Duration
        get() = initTime?.elapsedNow() ?: Duration.ZERO


    /**
     * Create the [ServicesManager] of the application.
     *
     * Method is called every time the application is started.
     *
     * Method should return manager that already has all services registered.
     *
     * @return The [ServicesManager] of the application.
     */
    public abstract suspend fun createServicesManager(): ServicesManager

    override suspend fun init() {
        initTime = timeSource.markNow()
        state = State.Initialized
        servicesManager = createServicesManager()
    }

    override suspend fun start(wait: Boolean) {
        check(state.allowStart) { "Cannot start the application from the `$state` state!" }

        lastStartTime = timeSource.markNow()
        state = State.Starting

        try {
            println("OR 1")
            beforeStart()

            println("OR 2")
            logger.recordTiming(TimeSource.Monotonic, "enabling services") {
                servicesManager.enable()
            }
            println("OR 3")

            afterStart()
            println("OR 4")
        } catch (e: Throwable) {
            state = State.ErrorStarting(e)
            return
        }
        println("OR 5")

        state = RunningState {
            lastStartTime?.elapsedNow() ?: Duration.ZERO
        }

        println("OR 6")

        if (wait) {
            println("OR 6.5")
            CompletableDeferred<Unit>()
                .also { waiter = it }
                .smartAwait()
        }

        println("OR 7")
    }


    override suspend fun stop() {
        waiter?.complete(Unit)

        state = State.Stopping

        try {
            beforeStop()

            logger.recordTiming(TimeSource.Monotonic, "disabling services") {
                servicesManager.disable()
            }

            afterStop()
        } catch (e: Throwable) {
            state = State.ErrorStopping(e)
            return
        }

        state = State.Stopped
    }


    /**
     * Method called before starting the application.
     *
     * Override this method to add custom logic before starting the services.
     *
     * It is recommended to avoid using this method and split the logic into separate services.
     */
    public open suspend fun beforeStart() {
        // Do nothing by default
    }

    /**
     * Method called after starting the application.
     *
     * Override this method to add custom logic after starting the services.
     *
     * It is recommended to avoid using this method and split the logic into separate services.
     */
    public open suspend fun afterStart() {
        // Do nothing by default
    }

    /**
     * Method called before stopping the application.
     *
     * Override this method to add custom logic before stopping the services.
     *
     * It is recommended to avoid using this method and split the logic into separate services.
     */
    public open suspend fun beforeStop() {
        // Do nothing by default
    }

    /**
     * Method called after stopping the application.
     *
     * Override this method to add custom logic after stopping the services.
     *
     * It is recommended to avoid using this method and split the logic into separate services.
     */
    public open suspend fun afterStop() {
        // Do nothing by default
    }

    public class RunningState(public val uptimeFunc: () -> Duration) : State.Running {
        override val uptime: Duration
            get() = uptimeFunc()

        override fun equals(other: Any?): Boolean {
            return other is RunningState
        }

        override fun hashCode(): Int {
            return 0
        }

        override fun toString(): String {
            return "RunningState(uptime=${uptime})"
        }

    }
}