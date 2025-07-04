package dev.nikdekur.ornament.protection.password

class Argon2PasswordEncoderTest : PasswordEncoderTest() {
    override fun createEncoder(): PasswordEncoder {
        return Argon2PasswordEncoder.owasp_recommended_2024(100L..500L)
    }
}