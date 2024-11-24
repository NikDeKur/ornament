package dev.nikdekur.ornament.dataset.yaml

import dev.nikdekur.ornament.Application
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

public open class YamlKtFileDataSetService<A : Application>(
    app: A
) : YamlKtDataSetService<A>(app) {

    public open val rootDir: Path?
        get() = Path(app.environment.getValue("env") ?: "environment")

    public open val configPath: Path
        get() {
            val path = app.environment.getValue("config") ?: "config.yml"
            return rootDir?.let {
                Path(it, path)
            } ?: Path(path)
        }

    override fun read(): String {
        return SystemFileSystem.source(configPath).buffered().use {
            it.readByteArray().decodeToString()
        }
    }
}