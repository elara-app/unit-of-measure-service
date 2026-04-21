[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/elara-app/unit-of-measure-service)

# Unit of Measure Service

Spring Boot microservice for unit of measure catalog and status governance within a distributed platform.

The repository contains a complete `unit-of-measure-service` implementation where delivery quality is visible in the product itself: clear architecture boundaries, strict validation, consistent error contracts, documented APIs, and build-enforced testing standards for a reference-data service consumed by other domains.

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
- Full lifecycle management for UOM statuses (k/read/update/delete, usability changes, and status search/filter operations).
- Strict request validation for IDs, text fields, and numeric values.
- Business safeguards such as case-insensitive name uniqueness, status association rules, and status usability control.
- Stable error contract through centralized exception handling and structured error responses.
- OpenAPI documentation with reusable schemas and example payloads.

Implementation references:
- `src/main/java/com/elara/app/unit_of_measure_service/controller/UomController.java`
- `src/main/java/com/elara/app/unit_of_measure_service/controller/UomStatusController.java`
- `src/main/java/com/elara/app/unit_of_measure_service/service/implementation/UomServiceImp.java`
- `src/main/java/com/elara/app/unit_of_measure_service/service/implementation/UomStatusServiceImp.java`
- `src/main/java/com/elara/app/unit_of_measure_service/config/GlobalExceptionHandler.java`
- `src/main/java/com/elara/app/unit_of_measure_service/config/OpenApiConfig.java`

## Microservice Ecosystem Context

Unit of Measure Service operates as one service in a broader microservices' architecture. Peer services keep their own repositories and documentation, while this repository captures interactions where UOM data is the reusable platform reference:

- **Inbound request routing through API Gateway**, with service resolution through Eureka.
- **Service discovery and client-side load balancing** for service registration and lookup.
- **Centralized configuration** through Config Server.
- **Secrets management** through Vault in the `dev` profile.
- **Distributed config refresh** through Spring Cloud Bus (AMQP).
- **Consumed by other services** like Inventory Service for UOM dependency validation.

Key references:
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/java/com/elara/app/unit_of_measure_service/controller/UomController.java`
- `src/main/java/com/elara/app/unit_of_measure_service/controller/UomStatusController.java`

## Service Interaction in the Microservices Architecture

Unit of Measure Service participates as a core reference-data and state-governance service in the platform interaction model:

1. Client-facing traffic is typically received through [api-gateway](https://github.com/elara-app/api-gateway.git), which routes requests using registry data from [discovery-service](https://github.com/elara-app/discovery-service.git).
2. On startup and refresh, Unit of Measure Service loads profile-specific configuration from [config-service](https://github.com/elara-app/config-service.git), sourced from [centralized-configuration](https://github.com/elara-app/centralized-configuration.git).
3. [inventory-service](https://github.com/elara-app/inventory-service.git) consumes Unit of Measure endpoints to validate UOM dependencies in inventory workflows.
4. Secrets for supported profiles are resolved via Vault integration, and distributed configuration updates are propagated through Spring Cloud Bus.

This service remains the system-of-record for UOM catalog definitions and UOM status rules, while gateway, discovery, config, and secret-management concerns stay delegated to platform infrastructure services.

## API Surface

Base paths: `/` (UOM) and `/status/` (UOM status catalog)

UOM endpoints:

- `POST /` - create unit of measure
- `GET /{id}` - retrieve by id
- `GET /` - paginated listing
- `GET /search?name=...` - paginated name search
- `GET /filter/status/{uomStatusId}` - filter UOMs by status
- `GET /check-name?name=...` - check UOM name availability
- `PUT /{id}` - update by id
- `PATCH /{id}/status/{newUomStatusId}` - change status association
- `DELETE /{id}` - delete by id

UOM status endpoints:

- `POST /status/` - create status
- `GET /status/{id}` - retrieve status by id
- `GET /status/` - paginated status listing
- `GET /status/search?name=...` - paginated status name search
- `GET /status/filter?isUsable=true|false` - filter statuses by usability
- `GET /status/check-name?name=...` - check status name availability
- `PUT /status/{id}` - update status
- `PATCH /status/{id}/change-usability` - change usability flag
- `DELETE /status/{id}` - delete status

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
./mvnw test -Dtest=UomServiceImpTest#save_shouldCreateAndReturnResponse
```


## Related Documentation

- `AGENTS.md` - coding conventions, architecture patterns, and operational rules
- `TESTING_GUIDE.md` - detailed testing practices and examples
