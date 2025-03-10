package dev.nikdekur.ornament.service

import dev.nikdekur.ornament.Application

public class DelegateService<A : Application>(
    override val app: A,
    public val getService: () -> AbstractAppService<A>
) : AbstractAppService<A>() {

    override suspend fun onEnable() {

    }

}