package dev.nikdekur.ornament.protection.password.none

import dev.nikdekur.ornament.protection.password.Password
import dev.nikdekur.ornament.protection.password.PasswordEncoder
import kotlinx.serialization.Serializable

public object NonePasswordEncoder : PasswordEncoder {
    override fun createPassword(password: CharSequence): Password {
        return NonePassword(NonePasswordData, password)
    }

    override fun createPassword(
        data: Password.Data,
        password: CharSequence
    ): NonePassword {
        return NonePassword(data, password)
    }

    override fun decodePassword(password: String): Password {
        require(password.contains(":")) { "Invalid password format" }

        val split = password.split(":")

        require(split.size == 2) { "Invalid password format" }

        val (data, password) = split
        return NonePassword(
            decodePasswordData(data),
            password
        )
    }

    override fun decodePasswordData(passwordData: String): Password.Data {
        require(passwordData == "none") { "Invalid password data format" }
        return NonePasswordData
    }

    override fun encryptionDelay(): Long {
        return 1L
    }


    @Serializable
    public data object NonePasswordData : Password.Data {
        override fun serialize(): String {
            return "none"
        }
    }


    @Serializable
    public data class NonePassword(
        override val data: Password.Data,
        val password: CharSequence
    ) : Password {

        override val bytes: ByteArray
            get() = password.toString().encodeToByteArray()

        override fun serialize(): String {
            return "${data.serialize()}:${password}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NonePassword) return false

            if (password != other.password) return false

            return true
        }

        override fun hashCode(): Int {
            return password.hashCode()
        }

        override fun toString(): String {
            return "NoneProtectionPassword(password='$password')"
        }
    }
}