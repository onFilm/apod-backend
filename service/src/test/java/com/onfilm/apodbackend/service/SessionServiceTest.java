package com.onfilm.apodbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SessionService} class.
 */
class SessionServiceTest {

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService();
    }

    /**
     * Tests that a new session can be created and is considered valid.
     */
    @Test
    void createSession_shouldReturnValidSessionId() {
        String sessionId = sessionService.createSession();
        assertNotNull(sessionId, "The created session ID should not be null.");
        assertTrue(sessionService.isValidSession(sessionId), "The newly created session should be valid.");
    }

    /**
     * Tests that a valid, non-expired session is correctly identified as valid.
     */
    @Test
    void isValidSession_withValidSession_shouldReturnTrue() {
        String sessionId = sessionService.createSession();
        assertTrue(sessionService.isValidSession(sessionId), "A valid session should be recognized as such.");
    }

    /**
     * Tests that a non-existent session ID is correctly identified as invalid.
     */
    @Test
    void isValidSession_withInvalidSession_shouldReturnFalse() {
        assertFalse(sessionService.isValidSession("non-existent-session-id"), "A non-existent session should be invalid.");
    }

    /**
     * Tests that a null session ID is correctly identified as invalid.
     */
    @Test
    void isValidSession_withNullSessionId_shouldReturnFalse() {
        assertFalse(sessionService.isValidSession(null), "A null session ID should be considered invalid.");
    }

    /**
     * Tests that an expired session is correctly identified as invalid and is removed.
     * This test uses reflection to manually expire a session to avoid waiting.
     *
     * @throws NoSuchFieldException   if the 'sessions' field cannot be found.
     * @throws IllegalAccessException if the 'sessions' field cannot be accessed.
     */
    @Test
    void isValidSession_withExpiredSession_shouldReturnFalse() throws NoSuchFieldException, IllegalAccessException {
        String sessionId = sessionService.createSession();

        // Use reflection to access the private 'sessions' map
        Field sessionsField = SessionService.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        ConcurrentMap<String, Instant> sessions = (ConcurrentMap<String, Instant>) sessionsField.get(sessionService);

        // Manually expire the session
        sessions.put(sessionId, Instant.now().minusSeconds(1));

        assertFalse(sessionService.isValidSession(sessionId), "An expired session should be invalid.");
        assertNull(sessions.get(sessionId), "Expired sessions should be removed from the store.");
    }

    /**
     * Tests that invalidating a session removes it from the session store.
     */
    @Test
    void invalidateSession_shouldRemoveSession() {
        String sessionId = sessionService.createSession();
        assertTrue(sessionService.isValidSession(sessionId), "Session should be valid before invalidation.");

        sessionService.invalidateSession(sessionId);
        assertFalse(sessionService.isValidSession(sessionId), "Session should be invalid after invalidation.");
    }

    /**
     * Tests that attempting to invalidate a null session ID does not cause an error.
     */
    @Test
    void invalidateSession_withNullSessionId_shouldNotThrowException() {
        assertDoesNotThrow(() -> sessionService.invalidateSession(null), "Invalidating a null session ID should not throw an exception.");
    }
}