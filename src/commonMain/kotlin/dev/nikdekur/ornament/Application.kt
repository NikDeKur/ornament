package dev.nikdekur.ornament

import dev.nikdekur.ndkore.`interface`.Unique
import dev.nikdekur.ndkore.service.ServicesComponent
import dev.nikdekur.ndkore.service.manager.ServicesManager
import dev.nikdekur.ornament.environment.Environment
import io.github.oshai.kotlinlogging.KLogger
import kotlin.time.Duration

/**
 * # Application
 *
 * Represent the main Application of any application.
 * Create a standard to application lifecycle, allowing to start, stop and reload the application.
 *
 * Use the ndkore Services System to manage services and allow to split application into smaller parts.
 */
public interface Application : ServicesComponent, Unique<String> {

    /**
     * The id of the application.
     */
    override val id: String
        get() = this::class.toString()

    /**
     * The current state of the application.
     *
     * @see State
     */
    public val state: State


    /**
     * The total uptime of the application since initialization
     *
     * If the application is not initialized ([init]), return [Duration.ZERO]
     */
    public val uptime: Duration

    /**
     * The logger of the application.
     *
     * Used to log messages from the application.
     */
    public val logger: KLogger

    /**
     * The environment of the application.
     *
     * Environment contains raw on-start arguments.
     */
    public val environment: Environment

    /**
     * The [ServicesManager] of the application.
     *
     * Used to manage services of the application.
     */
    public val servicesManager: ServicesManager


    public suspend fun init()

    /**
     * Start the application.
     *
     * Start the application and all services.
     *
     * @throws IllegalStateException if [init] is not called before.
     */
    public suspend fun start(wait: Boolean = false)

    /**
     * Stop the application.
     *
     * Stop the application and all services.
     */
    public suspend fun stop()

    /**
     * Reload the application.
     *
     * Stop and start the application.
     */
    public suspend fun reload() {
        stop()
        start()
    }


    override val manager: ServicesManager
        get() = servicesManager


    /**
     * The state of the application.
     */
    public sealed interface State {

        public val allowStart: Boolean
            get() = this == Initialized || this is ErrorStarting || this is ErrorStopping || this is Stopped

        public data object Created : State

        public data object Initialized : State

        /**
         * The application is starting.
         *
         * [Application.start] has been called, but the application is not running yet.
         */
        public data object Starting : State

        /**
         * An error (exception) occurred while starting the application.
         *
         * [Application.start] has been called, but an error occurred while starting the application.
         *
         * @param error The error that occurred.
         */
        public data class ErrorStarting(val error: Throwable) : State

        /**
         * The application is running.
         *
         * The application is running and all services are running.
         */
        public interface Running : State {
            /**
             * The uptime of the application, since the [start] **successfully** called.
             */
            public val uptime: Duration
        }


        /**
         * The application is stopping.
         *
         * [Application.stop] has been called, but the application is not stopped yet.
         */
        public data object Stopping : State

        /**
         * An error (exception) occurred while stopping the application.
         *
         * [Application.stop] has been called, but an error occurred while stopping the application.
         *
         * @param error The error that occurred.
         */
        public data class ErrorStopping(val error: Throwable) : State

        /**
         * The application is stopped.
         *
         * The application is stopped and all services are stopped.
         */
        public data object Stopped : State
    }
}