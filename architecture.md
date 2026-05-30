# Architecture

This document outlines the architecture of the APOD-Backend application.

## 1. Tech Stack

- **Java**: 17
- **Spring Boot**: 3.3.1
- **Build Tool**: Gradle
- **Caching**: Spring Data Redis
- **HTTP Client**: Spring WebFlux `WebClient`
- **Testing**: JUnit 5, Mockito

## 2. Project Structure

The project follows a standard layered architecture, organized by feature into the following packages:

- `com.onfilm.apodbackend`: The root package.
  - `config`: Contains all Spring configuration classes (e.g., `WebClientConfig`, `NasaApiConfig`).
  - `controller`: Houses RESTful controllers that expose the application's API endpoints.
  - `dto`: Contains Data Transfer Objects used for API request/response bodies (e.g., `ApodResponse`).
  - `exception`: Defines custom exception classes for handling application-specific errors.
  - `model`: (To be created) Domain entities for persistence.
  - `repository`: (To be created) Spring Data repositories for database interaction.
  - `service`: Contains business logic and service layer classes (e.g., `NasaClient`).
  - `util`: Holds utility classes and helper methods.

## 3. External API Integration

- **NASA APOD API**: The application integrates with the NASA "Astronomical Picture of the Day" API to fetch data.
  - **Configuration**: The API key and base URL are managed in `application.properties` and loaded via the `NasaApiConfig` class.
  - **Client**: The `NasaClient` service uses a non-blocking `WebClient` to communicate with the API.

## 4. Caching Strategy

- **Provider**: Redis will be used as the caching provider.
- **Implementation**: (To be implemented) A caching layer will be added to the service to store and retrieve APOD data, reducing redundant calls to the NASA API.
