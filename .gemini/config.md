# Gemini Code Assist - Project Instructions & Skills

## 1. Project Context
* **Project Name:** APOD API Service
* **Tech Stack:** Java 17, Spring Boot 3.x, Maven, Spring Web, Spring Data, TestNG/JUnit.
* **Core Function:** Fetch, parse, cache, and serve NASA's Astronomical Picture of the Day.

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