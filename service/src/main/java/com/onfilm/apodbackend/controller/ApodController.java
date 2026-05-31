package com.onfilm.apodbackend.controller;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.service.ApodService;
import com.onfilm.apodbackend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * REST controller for exposing APOD API endpoints.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ApodController {

    private final ApodService apodService;
    private final SessionService sessionService;

    /**
     * Authenticates the user, creates a session, and returns a session cookie.
     *
     * @return A Mono emitting a success message.
     */
    @GetMapping("/authenticate")
    public Mono<ResponseEntity<String>> authenticate() {
        String sessionId = sessionService.createSession();
        ResponseCookie cookie = ResponseCookie.from("session-id", sessionId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .build();
        return Mono.just(ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Authenticated"));
    }

    /**
     * Invalidates the session and clearing the session cookie.
     *
     * @param sessionId The session ID from the cookie.
     * @return A Mono emitting a success message.
     */
    @PostMapping("/invalidate")
    public Mono<ResponseEntity<String>> invalidate(@CookieValue(name = "session-id", required = false) String sessionId) {
        if (sessionId != null) {
            sessionService.invalidateSession(sessionId);
        }

        // Create a cookie that expires immediately to clear it from the browser.
        ResponseCookie deleteCookie = ResponseCookie.from("session-id", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return Mono.just(ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).body("Logged out"));
    }

    /**
     * Retrieves the APOD data for a specific date.
     *
     * @param date The date for which to fetch the APOD.
     * @return A Mono emitting the ApodResponse.
     */
    @GetMapping("/apod")
    public Mono<ApodResponse> getApod(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate requestDate = (date == null) ? LocalDate.now() : date;
        log.info("Received request to fetch APOD for date: {}", requestDate);
        return apodService.getApod(requestDate)
                .doOnSuccess(response -> log.info("Successfully fetched APOD for date: {}", requestDate))
                .doOnError(error -> log.error("Failed to fetch APOD for date: {}", requestDate, error));
    }

    /**
     * Retrieves a list of APOD data with optional sorting and pagination.
     *
     * @param sort The field to sort by (e.g., "date").
     * @param order The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    @GetMapping("/apods")
    public Flux<ApodResponse> getApods(
            @RequestParam(value = "_sort", required = false) String sort,
            @RequestParam(value = "_order", required = false) String order,
            @RequestParam(value = "_offset", required = false) Integer offset,
            @RequestParam(value = "_size", required = false, defaultValue = "20") Integer size) {
        log.info("Received request to fetch APODs with sort: {}, order: {}, offset: {}, size: {}", sort, order, offset, size);
        return apodService.getApods(sort, order, offset, size)
                .doOnComplete(() -> log.info("Successfully fetched APODs with sort: {}, order: {}, offset: {}, size: {}", sort, order, offset, size))
                .doOnError(error -> log.error("Failed to fetch APODs with sort: {}, order: {}, offset: {}, size: {}", sort, order, offset, size, error));
    }

    /**
     * Searches for APODs based on a search term and returns a paginated list.
     *
     * @param searchTerm The term to search for in APOD titles or explanations.
     * @param sort The field to sort by (e.g., "date").
     * @param order The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    @GetMapping(value = "/apods", params = "q")
    public Flux<ApodResponse> searchApods(
            @RequestParam(value = "q") String searchTerm,
            @RequestParam(value = "_sort", required = false) String sort,
            @RequestParam(value = "_order", required = false) String order,
            @RequestParam(value = "_offset", required = false) Integer offset,
            @RequestParam(value = "_size", required = false, defaultValue = "20") Integer size) {
        log.info("Received request to search APODs with term: {}, sort: {}, order: {}, offset: {}, size: {}", searchTerm, sort, order, offset, size);
        return apodService.searchApods(searchTerm, sort, order, offset, size)
                .doOnComplete(() -> log.info("Successfully searched APODs with term: {}, sort: {}, order: {}, offset: {}, size: {}", searchTerm, sort, order, offset, size))
                .doOnError(error -> log.error("Failed to search APODs with term: {}, sort: {}, order: {}, offset: {}, size: {}", searchTerm, sort, order, offset, size, error));
    }
}