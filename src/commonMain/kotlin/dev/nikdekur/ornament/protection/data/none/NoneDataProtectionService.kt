package dev.nikdekur.ornament.protection.data.none

import dev.nikdekur.ornament.Application
import dev.nikdekur.ornament.protection.data.DataProtectionService
import dev.nikdekur.ornament.protection.data.EncryptedData
import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.service.AbstractAppService

public class NoneDataProtectionService<A : Application>(
    override val app: A
) : AbstractAppService<A>(), DataProtectionService {

    override fun encrypt(
        data: ByteArray,
        password: Password
    ): EncryptedData {
        return object : EncryptedData {
            override val data: ByteArray = data
            override fun serialize(): ByteArray {
                return data
            }
        }
    }

    override fun decrypt(
        data: ByteArray,
        password: String
    ): ByteArray {
        return data
    }
}