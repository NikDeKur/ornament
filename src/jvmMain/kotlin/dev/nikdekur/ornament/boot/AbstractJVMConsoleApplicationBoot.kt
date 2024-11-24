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
        var id: String? = null
        try {
            println("LOL 1")
            val environment = ConsoleMapEnvironment.fromCommandLineArgs(args)
            println("LOL 2")
            val app = createApp(environment)
            println("LOL 3")
            id = app.id

            println("LOL 4")
            app.init()

            println("LOL 5")
            logger.info { "Starting `${app.id}`..." }

            println("LOL 6")
            addBlockingShutdownHook {
                logger.info { "Uptime: `${app.uptime}`" }

                logger.info { "Shutting down `${id}`..." }
                app.stop()
            }

            println("LOL 7")
            app.start(wait = true)

            val state = app.state
            if (state is State.ErrorStarting) throw state.error

            println("LOL 8")

        } catch (e: Throwable) {
            logger.error(e) { "Fatal error occurred during `${id ?: "UNKNOWN_ID"}` initialization. Shutting down..." }
            exitProcess(1)

        } finally {
            logger.info { "Bye!" }
        }
    }
}