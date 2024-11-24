package dev.nikdekur.ornament.service

import dev.nikdekur.ndkore.service.ServicesComponent
import dev.nikdekur.ndkore.service.manager.ServicesManager
import dev.nikdekur.ornament.Application

public interface AppServicesComponent<A : Application> : ServicesComponent {

    public val app: A

    override val manager: ServicesManager
        get() = app.servicesManager
}