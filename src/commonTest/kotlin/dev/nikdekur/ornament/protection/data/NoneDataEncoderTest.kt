package dev.nikdekur.ornament.protection.data

class NoneDataEncoderTest : DataEncoderTest() {
    override fun createEncoder() = NoneDataEncoder

    override fun `encrypt should create different output for same input`() {
        // No need to test this, as NoneDataEncoder does not encrypt data
    }
}