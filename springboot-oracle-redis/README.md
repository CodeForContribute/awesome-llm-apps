# Spring Boot CRUD API with Oracle and Redis

This sample project demonstrates how to build a simple CRUD REST API with Spring Boot, Oracle Database as the persistence layer, and Redis as a cache in front of the database. It uses Spring Data JPA, Hibernate, and Spring Cache abstractions.

## Features

- CRUD endpoints for managing `Customer` resources.
- Oracle database connectivity using Spring Data JPA and Hibernate.
- Redis-backed cache for read operations, including TTL configuration.
- Global exception handling and request validation.

## Requirements

- Java 17+
- Maven 3.9+
- Oracle Database (XE, Free, or commercial). Update the connection string in `src/main/resources/application.yml` to match your environment.
- Redis server (local or remote).

## Getting Started

1. **Install dependencies**

   Ensure Oracle and Redis are running and reachable from the application. Adjust the credentials in [`application.yml`](src/main/resources/application.yml) accordingly.

2. **Build the application**

   ```bash
   mvn -f pom.xml clean package
   ```

3. **Run the application**

   ```bash
   mvn -f pom.xml spring-boot:run
   ```

4. **Interact with the API**

   Example using `curl`:

   ```bash
   # Create a customer
   curl -X POST http://localhost:8080/api/customers \
        -H 'Content-Type: application/json' \
        -d '{"name":"Ada Lovelace","email":"ada@example.com"}'

   # Retrieve a customer (served from cache on subsequent calls)
   curl http://localhost:8080/api/customers/1

   # Update a customer
   curl -X PUT http://localhost:8080/api/customers/1 \
        -H 'Content-Type: application/json' \
        -d '{"name":"Ada Byron","email":"ada.byron@example.com"}'

   # Delete a customer
   curl -X DELETE http://localhost:8080/api/customers/1
   ```

## Redis Cache Behavior

The cache is configured in [`CacheConfig`](src/main/java/com/example/demo/config/CacheConfig.java) to store entries for 5 minutes and to avoid caching null values. Methods in [`CustomerService`](src/main/java/com/example/demo/customer/CustomerService.java) use `@Cacheable`, `@CachePut`, and `@CacheEvict` to keep the cache synchronized with the database.

## Database Schema

Hibernate's `ddl-auto` property is set to `update`, which will automatically create or update the `customers` table during development. For production deployments, manage schema migrations manually (e.g., with Flyway or Liquibase).

## Testing

The project includes Spring Boot's testing dependencies so you can add unit or integration tests as needed. Use the following command to run the default test suite:

```bash
mvn -f pom.xml test
```
