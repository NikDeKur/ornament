package dev.nikdekur.ornament.session

import dev.nikdekur.ornament.protection.Password
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes

abstract class SessionServiceTest {

    abstract val service: SessionService


    @Test
    fun testCreateSession() = runTest {
        val userId = "user"
        val ttl = 15.minutes
        val significance = Password.Significance.LOW

        val (_, session) = service.createSession(userId, ttl, significance)
        assertEquals(userId, session.userId)
        assertEquals(ttl, session.ttl)
    }


    @Test
    fun testGetInvalidSession() = runTest {
        val userId = "user"

        val session = service.getSession(userId, "invalid")
        assertNull(session)
    }


    @Test
    fun testCreateAndGetSession() = runTest {
        val userId = "user"
        val (token, session) = service.createSession(userId, 15.minutes, Password.Significance.LOW)
        val retrieved = service.getSession(userId, token)
        assertEquals(session, retrieved)
    }

    @Test
    fun testCreateRevokeAndGetSession() = runTest {
        val userId = "user"
        val (token, _) = service.createSession(userId, 15.minutes, Password.Significance.LOW)

        service.revokeSession(userId, token)
        val session = service.getSession(userId, token)

        assertNotNull(session)
        assertTrue(session.revoked)
    }


    @Test
    fun testRevokeNonExistingSession() = runTest {
        val userId = "user"
        val invalidated = service.revokeSession(userId, "invalid")
        assertFalse(invalidated)
    }


    @Test
    fun testRevokeExistingSession() = runTest {
        val userId = "user"
        val (token, _) = service.createSession(userId, 12.minutes, Password.Significance.LOW)
        val invalidated = service.revokeSession(userId, token)
        assertTrue(invalidated)

        val session = service.getSession(userId, token)
        assertNotNull(session)
        assertTrue(session.revoked)
    }
}