package dev.nikdekur.ornament.service

import dev.nikdekur.ndkore.service.AbstractService
import dev.nikdekur.ornament.Application

public abstract class AbstractAppService<A : Application> : AbstractService(), AppService<A>