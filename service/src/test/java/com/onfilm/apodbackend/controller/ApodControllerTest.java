package com.onfilm.apodbackend.controller;

import com.onfilm.apodbackend.config.JacksonConfig;
import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.filter.CookieAuthFilter;
import com.onfilm.apodbackend.service.ApodService;
import com.onfilm.apodbackend.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ApodController} class.
 */
@WebFluxTest(ApodController.class)
@Import({CookieAuthFilter.class, JacksonConfig.class})
class ApodControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApodService apodService;

    @MockBean
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        // Mock the session service to always return a valid session
        when(sessionService.isValidSession(anyString())).thenReturn(true);

        // Set default mock behaviors for the APOD service
        when(apodService.getApod(any(LocalDate.class))).thenReturn(Mono.empty());
        when(apodService.getApods(any(String.class), any(String.class), any(Integer.class), any(Integer.class))).thenReturn(Flux.empty());
        when(apodService.searchApods(any(String.class), any(String.class), any(String.class), any(Integer.class), any(Integer.class))).thenReturn(Flux.empty());
    }

    /**
     * Tests that the authenticate endpoint returns a session cookie.
     */
    @Test
    void authenticate_shouldReturnSessionCookie() {
        when(sessionService.createSession()).thenReturn("test-session-id");

        webTestClient.get()
                .uri("/api/v1/authenticate")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Set-Cookie", ".*session-id=test-session-id.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Path=/.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Max-Age=3600.*")
                .expectHeader().valueMatches("Set-Cookie", ".*HttpOnly.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Secure.*");
    }

    /**
     * Tests that the invalidate endpoint clears the session cookie.
     */
    @Test
    void invalidate_shouldClearSessionCookie() {
        webTestClient.post()
                .uri("/api/v1/invalidate")
                .cookie("session-id", "test-session-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Set-Cookie", ".*session-id=.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Path=/.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Max-Age=0.*")
                .expectHeader().valueMatches("Set-Cookie", ".*HttpOnly.*")
                .expectHeader().valueMatches("Set-Cookie", ".*Secure.*");
    }

    /**
     * Retrieves the APOD data for a specific date.
     */
    @Test
    void getApod_WithDate_ShouldReturnApodResponse() {
        LocalDate date = LocalDate.of(2023, 10, 1);
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("Test Title");
        mockResponse.setDate(date);

        when(apodService.getApod(date)).thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/apod?date=2023-10-01")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Title")
                .jsonPath("$.date").isEqualTo("2023-10-01");
    }

    /**
     * Tests that {@code getApod} returns the APOD for the current date when no date is specified.
     */
    @Test
    void getApod_WithoutDate_ShouldReturnCurrentDateApod() {
        LocalDate currentDate = LocalDate.now();
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("Current Day's APOD");
        mockResponse.setDate(currentDate);

        when(apodService.getApod(currentDate)).thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/apod")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Current Day's APOD")
                .jsonPath("$.date").isEqualTo(currentDate.toString());
    }

    /**
     * Tests that {@code getApod} returns an internal server error when the service throws an exception.
     */
    @Test
    void getApod_ServiceThrowsException_ShouldReturnInternalServerError() {
        LocalDate date = LocalDate.of(2023, 10, 1);
        when(apodService.getApod(date)).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apod?date=2023-10-01")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    /**
     * Tests that {@code getApods} with no parameters returns a list of {@link ApodResponse}.
     */
    @Test
    void getApods_NoParams_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.getApods(isNull(), isNull(), isNull(), eq(20))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code getApods} with sort and order parameters returns a list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSortAndOrder_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.getApods(eq("date"), eq("asc"), isNull(), eq(20))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_sort=date&_order=asc")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code getApods} with offset parameter returns a paginated list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithOffset_ShouldReturnPaginatedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod2); // Only apod2 after offset 1

        when(apodService.getApods(isNull(), isNull(), eq(1), eq(20))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_offset=1")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod2);
    }

    /**
     * Tests that {@code getApods} with size parameter returns a limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSize_ShouldReturnLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1); // Only apod1 with size 1

        when(apodService.getApods(isNull(), isNull(), isNull(), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_size=1")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod1);
    }

    /**
     * Tests that {@code getApods} with offset and size parameters returns a paginated and limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithOffsetAndSize_ShouldReturnPaginatedAndLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod2); // Only apod2 with offset 1 and size 1

        when(apodService.getApods(isNull(), isNull(), eq(1), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_offset=1&_size=1")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod2);
    }

    /**
     * Tests that {@code getApods} with sort, order, offset, and size parameters returns a sorted, paginated, and limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSortOrderOffsetAndSize_ShouldReturnSortedPaginatedAndLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1); // Assuming sorted by date desc, offset 1, size 1 would give apod1

        when(apodService.getApods(eq("date"), eq("desc"), eq(1), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_sort=date&_order=desc&_offset=1&_size=1")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod1);
    }

    /**
     * Tests that {@code getApods} returns an internal server error when the service throws an exception.
     */
    @Test
    void getApods_ServiceThrowsException_ShouldReturnInternalServerError() {
        when(apodService.getApods(isNull(), isNull(), isNull(), eq(20))).thenReturn(Flux.error(new RuntimeException("Apods service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apods")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    /**
     * Tests that {@code searchApods} with a search term and size returns a list of {@link ApodResponse}.
     */
    @Test
    void searchApods_WithSearchTermAndSize_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Mars Rover");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Jupiter Moons");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.searchApods(eq("Mars"), isNull(), isNull(), isNull(), eq(20))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?q=Mars&_size=20")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code searchApods} with a search term, sort, order, offset, and size returns a list of {@link ApodResponse}.
     */
    @Test
    void searchApods_WithAllParams_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Mars Rover 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Mars Rover 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.searchApods(eq("Mars"), eq("date"), eq("asc"), eq(0), eq(2))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?q=Mars&_sort=date&_order=asc&_offset=0&_size=2")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code searchApods} returns an internal server error when the service throws an exception.
     */
    @Test
    void searchApods_ServiceThrowsException_ShouldReturnInternalServerError() {
        when(apodService.searchApods(any(String.class), isNull(), isNull(), isNull(), any(Integer.class))).thenReturn(Flux.error(new RuntimeException("Search service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apods?q=Mars&_size=20")
                .cookie("session-id", "valid-session")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}