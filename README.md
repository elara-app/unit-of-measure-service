# Unit of Measure Service

Spring Boot microservice for unit of measure management within a distributed platform.

The repository contains a complete `unit-of-measure-service` implementation where delivery quality is visible in the product itself: clear architecture boundaries, strict validation, consistent error contracts, documented APIs, and build-enforced testing standards.

## Project Snapshot

| Area | Details |
|------|---------|
| Language & Runtime | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Build Tool | Maven (`./mvnw`) |
| Data Layer | Spring Data JPA + PostgreSQL (runtime) + H2 (tests) |
| API | REST + OpenAPI (springdoc) |
| Mapping | MapStruct |
| Service Discovery / Config | Eureka Client, Config Server, Vault |
| Config Refresh | Spring Cloud Bus (AMQP) |
| Quality Gates | JaCoCo (80% line, 70% branch, zero uncovered classes at bundle level with project exclusions) |

## What This Service Delivers

- Full lifecycle management for units of measure (`create`, `update`, `delete`, `findById`, paginated `findAll`, paginated name search, status change).
- Strict request validation for IDs, text fields, and numeric values.
- Business safeguards such as case-insensitive name uniqueness and status management.
- Stable error contract through centralized exception handling and structured error responses.
- OpenAPI documentation with reusable schemas and example payloads.

Implementation references:
- `src/main/java/com/elara/app/unit_of_measure_service/controller/UomController.java`
- `src/main/java/com/elara/app/unit_of_measure_service/service/imp/UomImp.java`
- `src/main/java/com/elara/app/unit_of_measure_service/config/GlobalExceptionHandler.java`
- `src/main/java/com/elara/app/unit_of_measure_service/config/OpenApiConfig.java`

## Microservice Ecosystem Context

Unit of Measure Service operates as one service in a broader microservices architecture. Peer services keep their own repositories and documentation, while this repository captures the interactions as a dependency for Inventory Service:

- **Service discovery and client-side load balancing** for inter-service communication.
- **Centralized configuration** through Config Server.
- **Secrets management** through Vault in the `dev` profile.
- **Distributed config refresh** through Spring Cloud Bus (AMQP).
- **Consumed by other services** like Inventory Service for UOM dependency validation.

Key references:
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/java/com/elara/app/unit_of_measure_service/config/AppConfig.java`

## API Surface

Base path: `/`

- `POST /` - create unit of measure
- `GET /{id}` - retrieve by id
- `GET /` - paginated listing
- `GET /search?name=...` - paginated name search
- `PUT /{id}` - update by id
- `DELETE /{id}` - delete by id
- `PATCH /{id}/status` - change status

Detailed request/response schemas and examples are configured in:
- `src/main/java/com/elara/app/unit_of_measure_service/config/OpenApiConfig.java`
- `src/main/resources/examples/`

## Technical Highlights

- Layered internal design across `controller`, `service`, `repository`, `mapper`, `exceptions`, and `dto` packages.
- DTO-first API boundaries (records), MapStruct-based mapping, and transactional service methods.
- Centralized exception handling with structured error responses and standard error codes.
- Multi-layer testing strategy (controller, service, repository, mapper, exceptions, utilities).
- Mock isolation patterns (`@AfterEach` + `reset(...)`) with Given-When-Then test structure.
- JaCoCo quality gates enforced in Maven build lifecycle.

References:
- `TESTING_GUIDE.md`
- `AGENTS.md`
- `pom.xml`

## Build, Test, Run

```bash
./mvnw clean install
./mvnw test
./mvnw clean verify
./mvnw spring-boot:run
```

Targeted test examples:

```bash
./mvnw test -Dtest=UomControllerTest
./mvnw test -Dtest=UomImpTest#save_withValidRequest_createsAndReturnsResponse
```


## Related Documentation

- `AGENTS.md` - coding conventions, architecture patterns, and operational rules
- `TESTING_GUIDE.md` - detailed testing practices and examples

