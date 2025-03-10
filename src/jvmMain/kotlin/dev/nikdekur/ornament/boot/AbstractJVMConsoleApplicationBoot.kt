package dev.nikdekur.ornament.boot

import dev.nikdekur.ndkore.ext.addBlockingShutdownHook
import dev.nikdekur.ornament.Application.State
import dev.nikdekur.ornament.environment.ConsoleMapEnvironment
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

/**
 * # AbstractJVMConsoleApplicationBoot
 *
 * Implement the [ApplicationBoot] interface for booting JVM application with console arguments.
 *
 * It is not required to use this class at all, but ornament provides a few implementations to help you.
 */
public abstract class AbstractJVMConsoleApplicationBoot : ApplicationBoot {


    public val logger: KLogger = KotlinLogging.logger { }


    public open val wait: Boolean = true
    public open val throwOnException: Boolean = true

    /**
     * Boot the application with console arguments.
     *
     * This method will create the application instance and start it.
     *
     * It Could be called infinite number of times.
     *
     * @param args The console arguments.
     */
    public suspend fun boot(args: Array<String>) {

        var finishCalled = false
        fun onFinish() {
            if (finishCalled) return

            logger.info { "Bye!" }

            finishCalled = true
        }

        var id: String? = null
        try {
            logger.trace { "Booting application..." }
            val environment = ConsoleMapEnvironment.fromCommandLineArgs(args)

            logger.trace { "Creating application..." }
            val app = createApp(environment)

            id = app.id

            logger.trace { "Initializing application..." }
            app.init()

            logger.info { "Starting `${app.id}`..." }

            addBlockingShutdownHook {
                logger.info { "Uptime: `${app.uptime}`" }
                logger.info { "Shutting down `${id}`..." }

                app.stop()

                onFinish()
            }

            logger.trace { "Shutdown hook added." }
            app.start(
                wait = wait,
                throwOnException = throwOnException
            )

            val state = app.state
            if (state is State.ErrorStarting) throw state.error

            logger.info { "Application `$id` started!" }

        } catch (e: Throwable) {
            logger.error(e) { "Fatal error occurred during `${id ?: "UNKNOWN_ID"}` initialization. Shutting down..." }
            onFinish()
            exitProcess(1)

        }
    }
}