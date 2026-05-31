package com.onfilm.apodbackend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages user sessions, including creation, validation, and invalidation.
 * This service uses an in-memory store for session management.
 */
@Service
public class SessionService {

    private static final long SESSION_DURATION_SECONDS = 3600; // 1 hour

    // A simple in-memory store for sessions.
    // Key: Session ID, Value: Expiration time (Instant)
    private final ConcurrentMap<String, Instant> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session, stores it, and returns the session ID.
     * @return The newly created session ID.
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        Instant expirationTime = Instant.now().plusSeconds(SESSION_DURATION_SECONDS);
        sessions.put(sessionId, expirationTime);
        return sessionId;
    }

    /**
     * Validates a session ID.
     * @param sessionId The session ID to validate.
     * @return true if the session is valid and not expired, false otherwise.
     */
    public boolean isValidSession(String sessionId) {
        if (sessionId == null) {
            return false;
        }

        Instant expirationTime = sessions.get(sessionId);
        if (expirationTime == null) {
            return false; // Session ID not found
        }

        // Check if the session is expired
        if (Instant.now().isAfter(expirationTime)) {
            sessions.remove(sessionId); // Clean up expired session
            return false;
        }

        return true; // Session is valid
    }

    /**
     * Invalidates a session by removing it from the store.
     * @param sessionId The session ID to invalidate.
     */
    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
}