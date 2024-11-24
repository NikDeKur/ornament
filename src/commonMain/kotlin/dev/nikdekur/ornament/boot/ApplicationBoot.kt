package dev.nikdekur.ornament.boot

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.environment.Environment

/**
 * # ApplicationBoot
 *
 * Represent the boot mechanism of the application.
 * Boot mechanism is used to start up the application and create the application instance.
 *
 * It is not required to use this interface at all, but ornament provides a few implementations to help you.
 *
 */
public interface ApplicationBoot {

    public fun createApp(environment: Environment): Application
}