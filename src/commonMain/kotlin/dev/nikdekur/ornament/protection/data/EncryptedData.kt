package dev.nikdekur.ornament.protection.data

/**
 * # EncryptedData
 *
 * Interface representing encrypted data along with all the metadata necessary for decryption.
 *
 * This interface encapsulates the encrypted bytes along with any additional information
 * required to decrypt the data when the correct password is provided.
 * This may include elements like initialization vectors, salt values, and algorithm parameters.
 *
 * Implementations should ensure that all necessary decryption parameters (except the password)
 * are stored within the object and included in the serialized representation.
 */
public interface EncryptedData {

    /**
     * The encrypted bytes of data.
     *
     * These bytes represent the encrypted form of the original data and cannot be
     * interpreted without decryption using the correct password and metadata.
     */
    public val data: ByteArray

    /**
     * Serializes the encrypted data including all metadata to a byte array.
     *
     * This method produces a byte array containing both the encrypted data and all
     * the metadata required for decryption.
     * The serialized form can be stored and later passed to a compatible [DataEncoder] for decryption.
     *
     * Note: This method is different from [toString], which is intended for debugging
     * purposes and might not include all necessary information for decryption.
     *
     * @return A byte array containing the serialized encrypted data with all metadata
     */
    public fun serialize(): ByteArray
}