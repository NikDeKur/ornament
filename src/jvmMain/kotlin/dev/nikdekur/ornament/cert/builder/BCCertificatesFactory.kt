package dev.nikdekur.ornament.cert.builder

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

public object BCCertificatesFactory : CertificateFactory {

    override fun createKeyStore(certFileContent: String, keyFileContent: String, alias: String): KeyStore {
        // Load private key from a PEM file
        val privateKey: PrivateKey = PEMParser(StringReader(keyFileContent)).use { pemParser ->
            val pemObject = pemParser.readObject() as PrivateKeyInfo
            JcaPEMKeyConverter().getPrivateKey(pemObject)
        }

        // Load certificate from a PEM file
        val certificate: X509Certificate = PEMParser(StringReader(certFileContent)).use { pemParser ->
            val pemObject = pemParser.readObject() as X509CertificateHolder
            JcaX509CertificateConverter().getCertificate(pemObject)
        }

        // Create a KeyStore and add the private key and certificate
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setKeyEntry(alias, privateKey, null, arrayOf(certificate))

        return keyStore
    }
}