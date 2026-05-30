# Gemini Code Assist - Project Instructions & Skills

## 1. Project Context
* **Project Name:** APOD API Service
* **Tech Stack:** Java 17, Spring Boot 3.x, Gradle, Spring Web, Spring Data, JUnit.
* **Core Function:** Fetch, parse, cache, and serve NASA's Astronomical Picture of the Day.
* **Module Structure:**
    * `service/`: Contains all production application code, API implementations, controllers, business logic, dependency injection, and runtime configurations.
    * `service-hermetic/`: Contains hermetic/integration/end-to-end test suites, test-only utilities, fixtures, environments, and mock infrastructure for hermetic testing.

## 2. Developer Persona & Coding Standards
* **Language:** Java (Modern, utilizing records, pattern matching where applicable).
* **Framework:** Spring Boot idiomatic style (Constructor injection, `@RestControllerAdvice` for global error handling, DTOs for request/response mapping).
* **Database/Caching:** Use a repository pattern. Cache NASA API responses locally to avoid rate-limiting.
* **Documentation**: Always add short Javadoc comments for all classes and methods when generating code.

## 3. Workflow Constraints (Non-Negotiable)

### 🚀 Small, Atomic Commits Strategy
* Do not give me a massive block of code containing a controller, service, and repository all at once.
* Break down tasks into incremental steps. For example:
    1. Define the DTO/Record for the APOD payload.
    2. Create the Repository interface.
    3. Create the Service layer method with a mock response.
    4. Implement the actual NASA API Feign/WebClient integration.
* Prioritize giving me one file or one logical modification per response.

### 🔄 Dynamic Instruction Optimization
* At the end of a feature implementation, analyze if our current setup or patterns can be optimized.
* Suggest additions to this `config.md` file to prevent future code drift (e.g., "Add a rule for handling NASA's 429 Rate Limit error").
* Always run the full unit test suite before considering a code change request complete.

## 4. Specific "Skills" & Code Patterns

### Skill: Resilient External API Consuming
When writing clients to fetch data from NASA:
* Always implement a fallback mechanism or clear error handling for when NASA's API goes down.
* Ensure API keys are injected via `@Value` or `@ConfigurationProperties`, never hardcoded.

### Skill: Test-Driven Development (TDD) Support
* Whenever I ask for a feature, write or suggest the test cases *first* or alongside the implementation.
* Prefer writing slices like `@WebMvcTest` for controllers and pure unit tests for services.

## 5. Automation Workflow Configuration

This section outlines the configuration for the automated APOD data fetching workflow.

### API Configuration
- **API Endpoint:** `https://apod.ellanan.com/api`
- **Method:** `GET`
- **Query Parameter:** `date` (format: `YYYY-MM-DD`)
- **Resilience:** The fetching script includes a retry mechanism with exponential backoff to handle transient API failures.

### Workflow Configuration
- **File:** `.github/workflows/update-apod.yml`
- **Triggers:**
    - **Scheduled:** Runs daily at 06:00 UTC (`cron: '0 6 * * *'`).
    - **Manual:** Can be triggered via `workflow_dispatch`.
- **Permissions:** Requires `contents: write` to commit updates.

### Workflow Inputs
- **`date`** (Optional): Target date for manual backfill in `YYYY-MM-DD` format. Defaults to the current date in the `America/New_York` timezone.

### Output File Structure
- **File:** `service/src/main/resources/apod_data.json`
- **Format:** Chronologically sorted JSON array of APOD data objects, with `date` acting as a unique key to prevent duplicates.

### Branch Strategy
- **Target Branch:** Automatically commits to the `main` branch.

## 6. Build and Test Commands

### Build
* **Full Project Build:** `./gradlew build`
* **Service Module Build:** `./gradlew :service:build`
* **Service Hermetic Module Build:** `./gradlew :service-hermetic:build`

### Test
* **Run All Tests:** `./gradlew test`
* **Run Service Unit Tests:** `./gradlew :service:test`
* **Run Service Hermetic Tests:** `./gradlew :service-hermetic:test`

## 7. CI/CD Expectations

### Build Pipeline
* The CI pipeline should build the `service` module to produce deployable artifacts.

### Test Pipeline
* The CI pipeline should run both `service` unit tests and `service-hermetic` tests.

### PR Validation
* All pull requests must validate both `service` and `service-hermetic` modules (build and test).

### Artifacts
* Deployable artifacts will be generated from the `service` module.

### Coverage
* Existing code coverage reporting should be preserved and configured for the new module structure.

## 8. Development Workflow

### Local Development
* Developers can run the `service` module directly using Spring Boot's run configurations or `./gradlew :service:bootRun`.

### Hermetic Testing
* Hermetic tests can be executed via `./gradlew :service-hermetic:test` for isolated, comprehensive validation.
