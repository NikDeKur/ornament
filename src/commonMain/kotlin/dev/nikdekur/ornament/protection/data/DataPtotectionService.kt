package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ornament.protection.password.Password

public interface DataProtectionService {
    /**
     * Encrypts the data using the password.
     *
     * Encrypts the data using the password. The data can be decrypted using the [decrypt] function.
     *
     * @param data the data to encrypt
     * @param password the password to use for encryption
     * @return the encrypted data
     */
    public fun encrypt(data: ByteArray, password: Password): EncryptedData

    /**
     * Decrypts the data using the password.
     *
     * Decrypts the data that was previously encrypted using the [encrypt] function.
     *
     * @param data the data to decrypt. Must be the same data returned from [encrypt]
     * @param password the password to use for decryption. Must be the same password used for encryption
     * @return the decrypted data
     *
     * @throws MalformedDataException if the data is malformed (e.g. not result of [encrypt])
     * @throws InvalidPasswordException if the password is invalid
     */
    public fun decrypt(data: ByteArray, password: String): ByteArray
}


public abstract class DecryptionException(message: String?) : RuntimeException(message)
public open class MalformedDataException(message: String?) : DecryptionException(message)
public open class InvalidPasswordException(message: String?) : DecryptionException(message)