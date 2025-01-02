@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.cert.builder

import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.readString
import java.security.KeyStore

/**
 * # Certificates Builder
 */
public fun interface CertificateFactory {

    /**
     * Creates a key store from a certificate and a key files with an alias.
     *
     * KeyStore is used to store keys and certificates.
     *
     * @param certFileContent The content of the certificate file.
     * @param keyFileContent The content of the key file.
     * @param alias The alias of the key.
     * @return The created key store.
     */
    public fun createKeyStore(certFileContent: String, keyFileContent: String, alias: String): KeyStore
}


/**
 * Creates a key store from a certificate and a key files with an alias.
 *
 * KeyStore is used to store keys and certificates.
 *
 * Alias is the alias of the key.
 *
 * @param fileSystem The file system to use.
 * @param certFile The path to the certificate file.
 * @param keyFile The path to the key file.
 * @param alias The alias of the key.
 * @return The created key store.
 */
public inline fun CertificateFactory.createKeyStore(
    fileSystem: FileSystem,
    certFile: Path,
    keyFile: Path,
    alias: String
): KeyStore {
    return createKeyStore(
        fileSystem.source(certFile).buffered().readString(),
        fileSystem.source(keyFile).buffered().readString(),
        alias
    )
}