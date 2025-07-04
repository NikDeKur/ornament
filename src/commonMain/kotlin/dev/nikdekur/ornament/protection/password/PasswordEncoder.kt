@file:Suppress("NOTHING_TO_INLINE")

package dev.nikdekur.ornament.protection.password

import dev.nikdekur.ndkore.ext.constantTimeEquals

/**
 * # PasswordEncoder
 *
 * Interface for encoding passwords and managing password verification.
 * This interface provides methods for creating and verifying password hashes
 * in a secure manner.
 *
 * The implementation should provide strong cryptographic password hashing
 * with appropriate configuration for security parameters like memory cost,
 * CPU cost, and parallelism.
 *
 * ## Usage
 *
 * ```kotlin
 * // Create a new password from a plaintext string
 * val password = passwordEncoder.createPassword("mySecretPassword")
 *
 * // Store the serialized password in the database
 * val serialized = password.serialize()
 *
 * // Later, verify a password attempt
 * val isMatch = passwordEncoder.matches(serialized, "attemptedPassword")
 * ```
 */
public interface PasswordEncoder {
    /**
     * Creates a new [Password] instance from a plaintext password string.
     *
     * This method will generate all necessary cryptographic elements (like salt)
     * using secure random generation and apply the configured hashing algorithm.
     *
     * @param password The plaintext password to encode
     * @return A new [Password] instance containing the hashed password and its metadata
     */
    public fun createPassword(password: CharSequence): Password

    /**
     * Creates a [Password] instance using the provided [Password.Data] and plaintext password.
     *
     * This method is useful when you need to recreate a password hash with the same parameters
     * and salt as an existing password, typically for verification purposes.
     *
     * @param data The password metadata containing parameters and salt
     * @param password The plaintext password to encode
     * @return A new [Password] instance with the hashed password using the provided data
     */
    public fun createPassword(data: Password.Data, password: CharSequence): Password

    /**
     * Decodes a serialized password data string back into a [Password.Data] object.
     *
     * This method parses the string representation of password data (containing parameters
     * and salt) back into a structured object.
     *
     * @param passwordData The serialized password data string
     * @return The decoded [Password.Data] object
     * @throws IllegalArgumentException if the provided string is improperly formatted
     */
    public fun decodePasswordData(passwordData: String): Password.Data

    /**
     * Decodes a fully serialized password string back into a [Password] object.
     *
     * This method parses the complete serialized password (containing both data and hash)
     * back into a structured object.
     *
     * @param password The serialized password string
     * @return The decoded [Password] object
     * @throws IllegalArgumentException if the provided string is improperly formatted
     */
    public fun decodePassword(password: String): Password

    /**
     * Returns the estimated time in milliseconds that encryption should take.
     *
     * This method can be used to understand the performance characteristics of the password
     * encoding implementation. It might be used for logging, monitoring, or to provide
     * feedback to users during password creation.
     *
     * @return The estimated encryption delay in milliseconds
     */
    public fun encryptionDelay(): Long
}

/**
 * Verifies if a plaintext password matches a [Password] object.
 *
 * This extension function recreates the password hash using the original password data
 * and the provided plaintext password, then compares it with the stored hash.
 *
 * @param password The existing password object to check against
 * @param rawPassword The plaintext password to verify
 * @return `true` if the password matches, `false` otherwise
 */
public inline fun PasswordEncoder.matches(password: Password, rawPassword: CharSequence): Boolean {
    val encrypted = createPassword(password.data, rawPassword)
    return password.bytes.constantTimeEquals(encrypted.bytes)
}

/**
 * Verifies if a plaintext password matches a serialized password string.
 *
 * This extension function first decodes the serialized password string,
 * then verifies if the provided plaintext password matches the decoded password.
 *
 * @param encodedPassword The serialized password string to check against
 * @param rawPassword The plaintext password to verify
 * @return `true` if the password matches, `false` otherwise
 */
public inline fun PasswordEncoder.matches(encodedPassword: String, rawPassword: CharSequence): Boolean {
    return matches(decodePassword(encodedPassword), rawPassword)
}