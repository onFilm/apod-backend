package com.onfilm.apodbackend.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onfilm.apodbackend.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * A web filter that intercepts incoming requests to validate a session cookie.
 * This filter is responsible for ensuring that only authenticated requests
 * (i.e., those with a valid session-id cookie) are allowed to proceed.
 *
 * <p>The filter allows unauthenticated access to the {@code /api/v1/authenticate}
 * endpoint and bypasses security for OPTIONS requests. For all other requests,
 * it checks for a valid session-id cookie. If the cookie is missing or invalid,
 * it returns an HTTP 401 Unauthorized response.</p>
 */
@Component
@Slf4j
@Order(1)
public class CookieAuthFilter implements WebFilter {

    private final ObjectMapper objectMapper;
    private final SessionService sessionService;

    /**
     * Constructs a new {@code CookieAuthFilter} with the given dependencies.
     *
     * @param objectMapper   The {@link ObjectMapper} to use for JSON serialization.
     * @param sessionService The {@link SessionService} to use for session validation.
     */
    public CookieAuthFilter(ObjectMapper objectMapper, SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.sessionService = sessionService;
    }

    /**
     * Filters incoming web requests to enforce cookie-based authentication.
     *
     * <p>This method intercepts all incoming requests and performs the following checks:
     * <ul>
     *     <li>If the request is an OPTIONS request, it is allowed to pass through.</li>
     *     <li>If the request is for the {@code /api/v1/authenticate} endpoint, it is allowed to pass through.</li>
     *     <li>For all other requests, it checks for a "session-id" cookie. If the cookie is present and valid,
     *         the request is allowed to proceed.</li>
     *     <li>If the session cookie is missing or invalid, the request is rejected with an
     *         HTTP 401 Unauthorized status.</li>
     * </ul>
     *
     * @param exchange The current {@link ServerWebExchange}.
     * @param chain    The {@link WebFilterChain} to pass control to the next filter.
     * @return A {@link Mono} that indicates the completion of the filtering process.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("CookieAuthFilter: Intercepting request for {}", request.getURI().getPath());

        // Allow OPTIONS requests to pass through
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("CookieAuthFilter: OPTIONS request, passing through.");
            return chain.filter(exchange);
        }

        // Allow access to the authentication endpoint without a cookie
        if (request.getURI().getPath().equals("/api/v1/authenticate")) {
            log.info("CookieAuthFilter: Authenticate endpoint, passing through.");
            return chain.filter(exchange);
        }

        // Get the session cookie and validate it
        HttpCookie sessionCookie = request.getCookies().getFirst("session-id");
        if (sessionCookie != null && sessionService.isValidSession(sessionCookie.getValue())) {
            log.info("CookieAuthFilter: Found valid session-id cookie, passing through.");
            return chain.filter(exchange);
        }

        // If no valid cookie, reject the request
        log.warn("CookieAuthFilter: No valid session-id cookie found for {}. Rejecting request.", request.getURI().getPath());
        return writeErrorResponse(exchange.getResponse());
    }

    /**
     * Writes an HTTP 401 Unauthorized error response to the client.
     *
     * <p>This method sets the HTTP status code to 401 and writes a JSON error message
     * to the response body, indicating that a valid session cookie is required.</p>
     *
     * @param response The {@link ServerHttpResponse} to write the error to.
     * @return A {@link Mono} that completes when the response has been written.
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> errorDetails = Map.of(
                "error", "Unauthorized",
                "message", "A valid session-id cookie is required to access this endpoint."
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorDetails);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON error response", e);
            // Fallback for if JSON processing fails
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}