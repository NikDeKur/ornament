package dev.nikdekur.ornament.dataset.yaml

import dev.nikdekur.ndkore.ext.ensurePathExists
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.MutableDataSetService
import dev.nikdekur.ornament.dataset.map.defaultJson
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml

public open class YamlKtFileDataSetService<A : Application>(
    override val app: A,
    public val dataSet: DataSet = DataSet
) : YamlKtDataSetService<A>(dataSet.yaml, dataSet.json), MutableDataSetService {

    public open val envDir: Path
        get() = Path(app.environment.getValue("env") ?: "environment")

    public open val configPath: Path
        get() {
            val path = app.environment.getValue("config") ?: "config.yml"
            return Path(envDir, path)
        }

    public var initialHash: Int? = null

    override fun read(): String {
        val fs = dataSet.fileSystem
        fs.ensurePathExists(configPath)

        val text = fs.source(configPath).buffered().use {
            it.readByteArray().decodeToString()
        }

        initialHash = text.hashCode()

        logger.info { "Loaded File data set." }
        logger.debug { text }

        return text
    }

    override suspend fun onDisable() {
        val fs = dataSet.fileSystem

        // If the config file is not found, do not write to it
        if (delegateOrNull != null && fs.exists(configPath)) {
            val yaml = yaml.encodeToString(delegate.map)

            // If the data set has not been modified, do not write to the file
            if (yaml.hashCode() == initialHash) return

            fs.sink(configPath).buffered().use {
                it.write(yaml.encodeToByteArray())
            }
        }

        super.onDisable() // Respect the super class
    }


    override fun set(key: String?, value: Any): Unit = delegate.set(key, value)
    override fun remove(key: String): Unit = delegate.remove(key)
    override fun clear(): Unit = delegate.clear()

    @Serializable
    public open class DataSet(
        @Transient
        public val fileSystem: FileSystem = SystemFileSystem,

        @Transient
        public val yaml: Yaml = Yaml,

        @Transient
        public val json: Json = defaultJson()
    ) {
        public companion object Default : DataSet()
    }
}