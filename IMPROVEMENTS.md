# Suggested Improvements

This document tracks potential improvements and future work for the APOD-Backend application.

## 1. Implement Redis Caching

**Description**: To improve performance and reduce the number of calls to the external NASA API, a caching layer should be implemented.

**Suggested Implementation**:
- Use Spring's Cache Abstraction with the `spring-boot-starter-data-redis` dependency.
- Annotate the `ApodServiceImpl.getApod()` method with `@Cacheable`.
- Configure the cache name (e.g., "apod") and a TTL (Time To Live) appropriate for the APOD data (e.g., 24 hours), since the picture of the day changes daily.
- Ensure Redis is properly configured in `application.properties` (host, port).

**Example Snippet (`ApodServiceImpl.java`)**:
```java
@Override
@Cacheable(value = "apod", key = "#date.toString()")
public Mono<ApodResponse> getApod(LocalDate date) {
    return nasaClient.fetchApod(date);
}
```
This will ensure that once an APOD for a specific date is fetched, it is stored in the Redis cache, and subsequent requests for the same date will be served from the cache until the entry expires.
