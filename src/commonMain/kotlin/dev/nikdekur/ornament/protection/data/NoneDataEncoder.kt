package dev.nikdekur.ornament.protection.data

import dev.nikdekur.ndkore.ext.readInt
import dev.nikdekur.ndkore.ext.writeInt

public object NoneDataEncoder : DataEncoder {
    override fun encrypt(
        data: ByteArray,
        password: String
    ): EncryptedData {
        val encodedPassword = password.encodeToByteArray()
        val array = ByteArray(4)
        array.writeInt(0, data.size)

        val serialize = array + data + encodedPassword
        return decodeData(serialize)
    }

    override fun decodeData(serialized: ByteArray): EncryptedData {
        return object : EncryptedData {
            override val data: ByteArray = serialized
            override fun serialize(): ByteArray {
                return serialized
            }
        }
    }

    override fun decrypt(
        data: EncryptedData,
        password: String
    ): ByteArray {
        // Allow passing data that are not a result of `encrypt`, but validate it first
        if (!data.data.contentEquals(data.serialize())) {
            throw MalformedDataException("Data is not valid")
        }

        val encodedPassword = password.encodeToByteArray()
        val size = data.data.readInt(0)
        val encodedData = data.data.copyOfRange(4, size + 4)
        val password = data.data.copyOfRange(size + 4, data.data.size)

        if (!password.contentEquals(encodedPassword)) {
            throw InvalidPasswordException("Password is not valid")
        }

        return encodedData
    }
}