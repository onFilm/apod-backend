# apod-backend

This is a Spring Boot application that serves as the backend for the Astronomy Picture of the Day (APOD) application. It fetches data from NASA's APOD API and provides its own endpoints for clients to consume.

## Technologies Used

* Java 17
* Spring Boot 3.3.1
* Project Reactor (Webflux)
* Redis (for caching)
* Lombok
* JUnit 5 & Reactor Test

## Features

* Fetches the Astronomy Picture of the Day from NASA's API.
* Provides a REST API to access the APOD data.
* Includes basic error handling and validation.
* Exposes actuator endpoints for monitoring.
* Reactive non-blocking architecture using Spring WebFlux.

## Contributing

We use `semantic-release` to automate our release process. To make this work, we follow a convention for our commit messages. Please follow this convention for your commit messages.

### Commit Message Format

Each commit message should be a one-liner with the following format:

`<type>: <subject>`

The **type** must be one of the following:

*   **feat**: A new feature
*   **fix**: A bug fix
*   **docs**: Documentation only changes
*   **chore**: Changes to the build process or auxiliary tools and libraries such as documentation generation

Example commit messages:

*   `feat: add new endpoint for user profiles`
*   `fix: correct issue with cache invalidation`
*   `docs: update README with contributing guidelines`

## Getting Started

### Prerequisites

* Java 17 or higher
* Gradle 8.x
* Docker and Docker Compose (for running Redis)

### Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/apod-backend.git
    cd apod-backend
    ```

2.  **Start the Redis container:**

    ```bash
    docker-compose up -d
    ```

3.  **Build the application:**

    ```bash
    ./gradlew build
    ```

## API Endpoints

### 1. Get APOD Data

Retrieves the APOD data for a specific date or today if no date is provided.

*   **URL:** `/api/v1/apod`
*   **Method:** `GET`
*   **Query Parameters:**
    *   `date` (optional): The date for which to fetch the APOD in `YYYY-MM-DD` format. Defaults to today's date if not provided.

**Example Request:**

```bash
curl -X GET 'http://localhost:8080/api/v1/apod?date=2023-10-26'
```

## Configuration

The application is configured using properties. Important settings include:

*   **NASA API Key:** The application uses a default `DEMO_KEY` to connect to NASA API. You can override it via `application.properties`:
    ```properties
    nasa.api.key=YOUR_NASA_API_KEY
    nasa.api.base-url=https://api.nasa.gov/planetary/apod
    ```

## Running the application

You can run the application using the following command:

```bash
./gradlew bootRun
```

The application will be available at `http://localhost:8080`.

## Running tests

You can run the tests using the following command:

```bash
./gradlew test
```