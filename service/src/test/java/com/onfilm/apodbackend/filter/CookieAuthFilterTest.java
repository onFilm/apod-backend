package com.onfilm.apodbackend.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onfilm.apodbackend.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CookieAuthFilter} class.
 */
@ExtendWith(MockitoExtension.class)
class CookieAuthFilterTest {

    private WebTestClient webTestClient;

    @Mock
    private SessionService sessionService;

    @Mock
    private ObjectMapper objectMapper;

    /**
     * A simple test controller to act as the downstream service protected by the filter.
     * CrossOrigin is added so that Spring's default CORS processor doesn't reject the OPTIONS request.
     */
    @RestController
    @CrossOrigin(origins = "http://example.com")
    static class TestController {
        @GetMapping("/test")
        public Mono<String> testEndpoint() {
            return Mono.just("success");
        }

        @GetMapping("/api/v1/authenticate")
        public Mono<String> authEndpoint() {
            return Mono.just("auth");
        }
    }

    @BeforeEach
    void setUp() {
        // Instantiate the filter with its dependencies
        CookieAuthFilter cookieAuthFilter = new CookieAuthFilter(objectMapper, sessionService);

        // Build a WebTestClient that is bound to the test controller and explicitly includes the filter
        webTestClient = WebTestClient.bindToController(new TestController())
                .webFilter(cookieAuthFilter)
                .build();
    }

    /**
     * Tests that the filter allows requests with a valid session cookie to pass through.
     * @throws JsonProcessingException if the ObjectMapper fails to write the value
     */
    @Test
    void filter_whenRequestHasValidSessionCookie_shouldAllowRequest() throws JsonProcessingException {
        when(sessionService.isValidSession("valid-session-id")).thenReturn(true);

        webTestClient.get().uri("/test")
                .cookie("session-id", "valid-session-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("success");
    }

    /**
     * Tests that the filter rejects requests that do not have a session cookie with an HTTP 401 Unauthorized status.
     * @throws JsonProcessingException if the ObjectMapper fails to write the value
     */
    @Test
    void filter_whenRequestHasNoSessionCookie_shouldReturnUnauthorized() throws JsonProcessingException {
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{\"error\":\"Unauthorized\",\"message\":\"A valid session-id cookie is required to access this endpoint.\"}".getBytes());

        webTestClient.get().uri("/test")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").isEqualTo("A valid session-id cookie is required to access this endpoint.");
    }

    /**
     * Tests that the filter rejects requests with an invalid session cookie with an HTTP 401 Unauthorized status.
     * @throws JsonProcessingException if the ObjectMapper fails to write the value
     */
    @Test
    void filter_whenRequestHasInvalidSessionCookie_shouldReturnUnauthorized() throws JsonProcessingException {
        when(sessionService.isValidSession("invalid-session-id")).thenReturn(false);
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{\"error\":\"Unauthorized\",\"message\":\"A valid session-id cookie is required to access this endpoint.\"}".getBytes());

        webTestClient.get().uri("/test")
                .cookie("session-id", "invalid-session-id")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Tests that the filter allows requests to the authentication endpoint to pass through without a session cookie.
     */
    @Test
    void filter_whenRequestIsForAuthEndpoint_shouldAllowRequestWithoutCookie() {
        webTestClient.get().uri("/api/v1/authenticate")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("auth");
    }

    /**
     * Tests that the filter allows OPTIONS requests to pass through without a session cookie,
     * which is necessary for CORS preflight requests.
     */
    @Test
    void filter_whenRequestIsOptions_shouldAllowRequestWithoutCookie() {
        webTestClient.options().uri("http://localhost/test")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://example.com")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Tests that the filter returns a 500 Internal Server Error when the ObjectMapper fails to write the error response.
     * @throws JsonProcessingException if the ObjectMapper fails to write the value
     */
    @Test
    void filter_whenObjectMapperThrowsException_shouldReturnInternalServerError() throws JsonProcessingException {
        when(objectMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("Test Exception") {});

        webTestClient.get().uri("/test")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}