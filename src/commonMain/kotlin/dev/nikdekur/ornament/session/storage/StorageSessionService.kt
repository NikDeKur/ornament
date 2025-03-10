package dev.nikdekur.ornament.session.storage

import dev.nikdekur.ndkore.service.Dependencies
import dev.nikdekur.ndkore.service.dependencies
import dev.nikdekur.ndkore.service.inject
import dev.nikdekur.ndkore.service.injectOrNull
import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.dataset.DataSetService
import dev.nikdekur.ornament.dataset.get
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordProtectionService
import dev.nikdekur.ornament.service.AbstractAppService
import dev.nikdekur.ornament.session.SessionService
import dev.nikdekur.ornament.storage.StorageService
import dev.nikdekur.ornament.storage.StorageTable
import dev.nikdekur.ornament.storage.getTable
import dev.nikdekur.ornament.storage.request.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.time.Duration

public open class StorageSessionService<A : Application>(
    override val app: A,
    public val initialDataset: StorageSessionServiceDataSet? = null,
    public val datasetPath: String = "sessions"
) : AbstractAppService<A>(), SessionService {

    override val dependencies: Dependencies = dependencies {
        -DataSetService::class
        +PasswordProtectionService::class
        +StorageService::class
    }

    protected val datasetService: DataSetService? by injectOrNull()
    protected val passwordProtectionService: PasswordProtectionService by inject()
    protected val storageService: StorageService by inject()

    public lateinit var dataset: StorageSessionServiceDataSet
    public lateinit var table: StorageTable<SessionRecord>

    public val clock: Clock
        get() = dataset.clock ?: app.clock

    override suspend fun onEnable() {
        dataset = initialDataset ?: datasetService?.get<StorageSessionServiceDataSet>(datasetPath)
                ?: StorageSessionServiceDataSet()

        table = storageService.getTable(dataset.table)

//        table.createIndex(
//            mapOf(
//                "expiresAt" to 1 , "expireAfterSeconds" to 0
//            ),
//            indexOptions {
//                name = "expiresAt"
//            }
//        )
    }


    override suspend fun createSession(
        userId: String,
        ttl: Duration,
        significance: Password.Significance
    ): Pair<String, SessionRecord> {
        val originalToken = dataset.fabric.createToken()

        val encrypted = passwordProtectionService.createPassword(originalToken, significance)
        val encryptedToken = encrypted.serialize()
        val record = SessionRecord(
            userId = userId,
            tokenHashed = encryptedToken,
            revoked = false,
            ttl = ttl,
            issuedAt = clock.now()
        )
        table.insertOne(record)

        return originalToken to record
    }

    public suspend fun updateSession(
        original: SessionRecord,
        new: SessionRecord
    ): Boolean {
        return table.replaceOne(
            new,
            SessionRecord::userId eq original.userId,
            SessionRecord::tokenHashed eq original.tokenHashed
        )
    }

    /**
     * Checks if the session is outdated and revokes it if it is.
     *
     * @param session the session to check
     * @return true if the session is outdated
     */
    public suspend fun checkOutdated(
        session: SessionRecord
    ): Boolean {
        val outdated = (session.issuedAt + session.ttl) <= clock.now()

        if (outdated)
            updateSession(session, session.copy(revoked = true))

        return outdated
    }

    override suspend fun getSession(
        userId: String,
        token: String
    ): SessionRecord? {
        return table
            .find(SessionRecord::userId eq userId)
            .firstOrNull {
                if (checkOutdated(it)) return@firstOrNull false

                val deserialized = passwordProtectionService.deserializePassword(it.tokenHashed)
                deserialized.isEqual(token)
            }
    }

    override suspend fun revokeSession(userId: String, token: String): Boolean {
        val session = getSession(userId, token) ?: return false

        // Maybe change to more meaningful return?
        if (session.revoked) return true

        val updated = session.copy(revoked = true)
        updateSession(session, updated)

        return true
    }
}
