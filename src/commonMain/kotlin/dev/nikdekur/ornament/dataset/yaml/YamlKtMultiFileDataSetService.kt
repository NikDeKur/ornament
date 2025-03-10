package dev.nikdekur.ornament.dataset.yaml

import dev.nikdekur.ndkore.ext.ensurePathExists
import dev.nikdekur.ndkore.ext.extension
import dev.nikdekur.ndkore.ext.nameWithoutExtension
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
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

public open class YamlKtMultiFileDataSetService<A : Application>(
    override val app: A,
    public val dataSet: DataSet = DataSet
) : YamlKtDataSetService<A>(dataSet.yaml, dataSet.json), DataSetService {

    public open val envDir: Path
        get() = Path(app.environment.getValue("env") ?: "environment")

    public open val configsPath: Path
        get() {
            val path = app.environment.getValue("configs") ?: return envDir
            return Path(envDir, path)
        }

    protected fun readYaml(fs: FileSystem, path: Path, root: Boolean): String {
        val sb = StringBuilder()

        val isDirectory = fs.metadataOrNull(path)?.isDirectory == true
        if (isDirectory) {
            val text = fs.list(path)
                .joinToString("\n") { readYaml(fs, it, false) }

            sb.appendLine(
                if (!root) text.addRootSection(path.name) else text
            )
            return sb.toString()
        }

        val extension = path.extension
        if (extension != "yml" && extension != "yaml") return ""

        val fileName = path.nameWithoutExtension

        sb.append(
            fs.source(path).buffered().use {
                it.readByteArray().decodeToString()
            }.addRootSection(fileName)
        )

        return sb.toString()
    }

    override fun read(): String {
        val fs = dataSet.fileSystem
        fs.ensurePathExists(configsPath, isFile = false)

        val text = readYaml(fs, configsPath, true)

        logger.info { "Loaded MultiFile data set." }
        logger.debug { text }

        return text
    }


    @Serializable
    public open class DataSet(
        @Transient
        public val fileSystem: FileSystem = SystemFileSystem,

        @Transient
        public val yaml: Yaml = defaultYaml(),

        @Transient
        public val json: Json = defaultJson()
    ) {
        public companion object Default : DataSet()
    }
}