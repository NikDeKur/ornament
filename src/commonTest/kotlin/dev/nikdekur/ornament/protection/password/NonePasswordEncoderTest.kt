package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ornament.protection.password.none.NonePasswordEncoder

class NonePasswordEncoderTest : PasswordEncoderTest() {
    override fun createEncoder(): PasswordEncoder {
        return NonePasswordEncoder
    }

    override fun `createPassword should create different hashes for the same password`() {
        // No need to test this for NonePasswordEncoder
        // It always returns the same password
    }
}