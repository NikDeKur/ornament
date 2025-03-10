package dev.nikdekur.ornament.protection.data

public interface EncryptedData {
    public val data: ByteArray

    /**
     * Serialize the data to a string.
     *
     * This string should be able to be used to recreate the data using [DataProtectionService.decrypt].
     *
     * Distinct from [toString] which is for debugging purposes.
     */
    public fun serialize(): ByteArray
}