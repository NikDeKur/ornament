package dev.nikdekur.ornament.cert

import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.cert.builder.BCCertificatesFactory
import dev.nikdekur.ornament.cert.builder.createKeyStore
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.service.AbstractAppService
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.security.KeyStore

public open class BCCertificatesService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), CertificatesService {

    override val dependencies: Dependencies = dependencies {
        +DataSetService::class
    }

    public val dataset: DataSetService by inject()

    override suspend fun onEnable() {
        val factory = BCCertificatesFactory
        val ssl = dataset.get<SSLDataSet>("ssl")

        keyStore = ssl?.let {
            factory.createKeyStore(
                SystemFileSystem,
                Path(ssl.key),
                Path(ssl.cert),
                ssl.alias
            )
        }
    }

    override suspend fun onDisable() {
        keyStore = null
    }

    override var keyStore: KeyStore? = null
}