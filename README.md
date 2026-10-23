[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/elara-app/unit-of-measure-service)

# Unit of Measure Service

`unit-of-measure-service` owns the unit of measure (UOM) catalog and the UOM status catalog for the Elara platform. It stores UOM definitions — name, description, conversion factor to the base unit and the status they reference — plus the statuses themselves, and exposes both as REST resources.

The seeded UOM ids 1 (Kilogram) and 2 (Gram) are the contract used by `inventory-service` when it validates UOM references. This service does not own inventory items or stock, authentication and authorization, audit history or domain events, and it calls no other service.

## At a glance

| Item | Value |
|---|---|
| Service ID | `unit-of-measure-service` |
| Runtime port | `8082` (`SERVER_PORT` in `centralized-configuration/unit-of-measure-service-dev.yml`; `EXPOSE 8082` in the Dockerfile) |
| Stack | Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0, Spring Data JPA, MapStruct 1.6.3, springdoc 2.8.0 |
| Datastore | PostgreSQL 16 at runtime; H2 in-memory only for Maven tests |
| Runtime dependencies | Config Server (optional import), Eureka, Vault (dev, optional), RabbitMQ + Spring Cloud Bus |
| Health endpoint | `GET /actuator/health` |
| Published image | `ghcr.io/elara-app/unit-of-measure-service:latest` |

## Responsibilities and boundaries

- Own two resources: the UOM catalog at `/` and the UOM status catalog at `/status/`.
- Enforce UOM name uniqueness case-insensitively and status name uniqueness case-sensitively.
- Track which status each UOM references, with a dedicated status-change operation.
- Provide `check-name`, name search, status filter and paginated listings for both resources.
- Do not own: inventory data, stock movements, security, audit history or event publishing.
- Do not call `inventory-service` or any other service; inventory is the caller in that relationship.

## Runtime architecture

Request flow: controller → service interface → `UomServiceImp` / `UomStatusServiceImp` → Spring Data JPA repository → PostgreSQL. MapStruct converts DTO records and entities, `GlobalExceptionHandler` maps exceptions, and `MessageService` resolves text from `messages.properties`.

- `UomController` is mapped at `/` and `UomStatusController` at `/status/`; both stay thin (validation, logging, delegation, HTTP status).
- `UomServiceImp` resolves the referenced status through `UomStatusService.findEntityById`; there is no outbound HTTP client.
- The `uom` table has a required lazy `@ManyToOne` to `uom_status`; deleting a referenced status is blocked by the foreign key, while UOM rows are not referenced by any local table.
- RabbitMQ carries Spring Cloud Bus refresh events only; there is no domain event producer or consumer.

Platform concerns stay outside this repo. Gateway routing, service discovery, stack layout and ports live in [infrastructure/README.md](https://github.com/elara-app/infrastructure#readme); profile values and Vault paths live in [`centralized-configuration`](https://github.com/elara-app/centralized-configuration).

## API

Base paths: `/` (UOM catalog) and `/status/` (status catalog). Call the service directly on port 8082, or through the gateway at `http://127.0.0.1:8080/api/v1/uom/**`.

### UOM endpoints

| Method and path | Success | Notes |
|---|---|---|
| `POST /` | 201 `UomResponse` | 400 validation; 404 unknown `uomStatusId`; 409 duplicate name (case-insensitive); 500. |
| `GET /{id}` | 200 `UomResponse` | 400 invalid id; 404 absent; 500. |
| `GET /` | 200 nested page | `page, size, sort`; no default sort. |
| `GET /search?name=` | 200 nested page | `name` required and non-blank; case-insensitive partial match. |
| `GET /filter/status/{uomStatusId}` | 200 nested page | 400 invalid id; a non-existent status id returns an empty page, not 404. |
| `GET /check-name?name=` | 200 boolean | `name` required; `true` means the name is taken; case-insensitive. |
| `PUT /{id}` | 200 `UomResponse` | 400 body validation; 404 absent; 409 duplicate name; cannot change status; path id is not validated (see limitations). |
| `PATCH /{id}/status/{newUomStatusId}` | 200 `UomResponse` | Both ids must be positive; 400 invalid ids; 404 missing UOM or status; 500. No usability check. |
| `DELETE /{id}` | 204 | 400 invalid id; 404 absent; 500. No local FK, so 409 is unreachable here. |

### UOM status endpoints

| Method and path | Success | Notes |
|---|---|---|
| `POST /status/` | 201 `UomStatusResponse` | 400 validation; 409 duplicate exact name (case-sensitive); 500. |
| `GET /status/{id}` | 200 `UomStatusResponse` | 400 invalid id; 404 absent; 500. |
| `GET /status/` | 200 nested page | Use the trailing slash; no default sort. |
| `GET /status/search?name=` | 200 nested page | `name` required and non-blank; case-insensitive partial match. |
| `GET /status/filter?isUsable=` | 200 nested page | `isUsable` required boolean; 400 missing or wrong type; 500. |
| `GET /status/check-name?name=` | 200 boolean | `true` means taken; case-sensitive. |
| `PUT /status/{id}` | 200 `UomStatusResponse` | Updates `name`/`description` only; cannot change `isUsable`; 409 exact-name duplicate; 500. |
| `PATCH /status/{id}/change-usability?isUsable=` | 204 | The query parameter is required; 400 invalid or missing; 404 absent; 500. The only way to change usability. |
| `DELETE /status/{id}` | 204 | 409 code 1001 when referenced by a UOM; 400 invalid id; 404 absent; 500. |

List endpoints return a nested page, not a flat Spring `Page`:

```json
{"content":[...],"page":{"size":20,"number":0,"totalElements":5,"totalPages":1}}
```

Callers must pass `sort=...` when they need deterministic order.

Error contract (body shape: `{code, value, message, timestamp, path}`):

| Trigger | HTTP | Code |
|---|---|---|
| Body/parameter validation, missing query parameter, type mismatch | 400 | 1002 `INVALID_DATA` |
| Wrong method / unsupported media type | 405 / 415 | 1002 `INVALID_DATA` |
| Service-detected missing resource | 404 | 1004 `RESOURCE_NOT_FOUND` |
| Service-detected duplicate or conflict | 409 | 1003 `RESOURCE_CONFLICT` |
| Data-integrity violation (unique/FK race, deleting a referenced status) | 409 | 1001 `DATABASE_ERROR` |
| `BaseException` with code 1001, 1005 or 1006 | 500 | 1001 / 1005 / 1006 |
| Any other exception | 500 | 1006 `UNEXPECTED_ERROR` |

Code 1005 (`SERVICE_UNAVAILABLE`) is declared but never thrown by normal flows; if it were thrown it would map to HTTP 500 in this service, not 503.

Interactive documentation: Swagger UI at `http://localhost:8082/swagger-ui.html`, raw document at `http://localhost:8082/v3/api-docs`, and the gateway-aggregated document at `http://127.0.0.1:8080/api/v1/uom/v3/api-docs`. The hand-maintained OpenAPI paths and example JSONs are stale for page shape and some status wording; the live behavior described above is authoritative.

## Run and verify

Prerequisites: Java 21. The Maven wrapper is not executable (mode 644), so always call it as `sh ./mvnw`.

```bash
sh ./mvnw -B clean verify -Dspring.profiles.active=test   # canonical build + quality gate
sh ./mvnw test                                            # tests only
sh ./mvnw test -Dtest=UomControllerTest                   # focused test class
sh ./mvnw test -Dtest=UomServiceImpTest#save_shouldCreateAndReturnResponse
```

`sh ./mvnw spring-boot:run` is not a standalone quickstart: the default `dev` profile expects Config Server (default `http://localhost:8888`) and PostgreSQL on `localhost:5432/unit_of_measure_db`. Without Config Server the service has no configured port or datasource from this repo.

For the full stack, follow [infrastructure/README.md](https://github.com/elara-app/infrastructure#readme). After the stack is up, verify through the gateway:

```bash
curl -s http://127.0.0.1:8080/api/v1/uom/ | jq '.page.totalElements'         # 5
curl -s http://127.0.0.1:8080/api/v1/uom/status/ | jq '.page.totalElements'  # 3
```

The dev seed creates 3 statuses (Active, Inactive, Deprecated) and 5 UOMs and is idempotent across restarts.

## Configuration

- `application.yml` — service name, active profile `dev`, Config Server import `optional:configserver:${CONFIG_URI:http://localhost:8888}`, `fail-fast:false` and retry.
- `application-dev.yml` — optional Vault import `vault://secret/unit-of-measure-service/dev`, `fail-fast:false`; JPA `ddl-auto: update`; runs `classpath:db/dev/seed.sql`.
- `application-test.yml` — disables Config Server, Vault, Bus and discovery; H2 `jdbc:h2:mem:testdb` with `create-drop`. H2 is `<scope>test</scope>`, so the packaged jar cannot run this profile.
- Port, datasource, RabbitMQ, Eureka and actuator exposure for `dev` come from Config Server (`centralized-configuration/unit-of-measure-service-dev.yml`). Vault supplies datasource and Rabbit credentials only, matching the central dev defaults.
- Config refresh endpoints `refresh` and `busrefresh` are exposed in dev; RabbitMQ is the bus transport.

The full profile matrix and Vault paths live in [`centralized-configuration`](https://github.com/elara-app/centralized-configuration). The `prod` files are sparse placeholders, not complete production configuration.

## Testing and quality gates

13 test classes with 229 tests cover controllers, services, repositories, mappers, exception handling and utilities. There is no `UnitOfMeasureServiceApplicationTests` class.

```bash
sh ./mvnw test                                            # 229 tests
sh ./mvnw -B clean verify -Dspring.profiles.active=test   # adds the JaCoCo check gate
```

JaCoCo gates are enforced in `verify`: bundle line 80%, branch 70%, missed classes 0, package line 70%. Exclusions: `**/UnitOfMeasureServiceApplication.class`, `**/config/**`, `**/dto/**`. The HTML report at `target/site/jacoco/index.html` is generated during the `test` phase.

CI runs the reusable org test workflow on pull requests to `develop` and `main`; pushes to `main` publish `ghcr.io/elara-app/unit-of-measure-service:latest` (markdown-only changes are ignored). No Checkstyle, SpotBugs, Enforcer or Failsafe gate is configured.

## Current limitations

- No audit log and no created/updated timestamps on either entity.
- Status usability is not enforced: a UOM can be created or moved to a status with `isUsable=false`, and setting a status unusable does not cascade to existing UOMs.
- `UomServiceImp.deleteById` is not `@Transactional`; the existence check and delete are separate transactions, so a race can surface as a generic 500.
- `UomStatusUpdate.name` is optional, but omitting it writes `null` through the generated MapStruct mapper.
- `PUT /{id}` does not validate the path id with `@NotNull @Positive`; 0 or negative ids reach the service and normally return 404, not 400.
- OpenAPI paths and examples are hand-maintained and stale: they mention `/states` and `change-state`, show flat page schemas and document the status `PATCH` as 204. Live responses are authoritative.
- Schema management uses Hibernate `ddl-auto` (`update` in dev, `create-drop` in tests); there are no migration scripts.
- All endpoints are unauthenticated; there is no Spring Security dependency.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `./mvnw: Permission denied` | wrapper mode is 644 | run `sh ./mvnw ...` |
| `spring-boot:run` fails on datasource or serves on 8080 | Config Server or PostgreSQL is missing | start both, or use the full stack |
| `PATCH /status/{id}/change-usability` returns 400 code 1002 | `isUsable` query parameter missing or invalid | send `?isUsable=true` or `?isUsable=false` |
| Duplicate UOM name returns 409 even with different case | UOM uniqueness is case-insensitive | choose a different name |
| `DELETE /status/{id}` returns 409 code 1001 | status is referenced by a UOM (FK) | reassign or delete those UOMs first |
| Swagger shows flat pages or `/states` | OpenAPI annotations/examples are stale | rely on the live response shape documented here |
| Gateway returns 404 for `/api/v1/uom/...` | service not registered/healthy, or gateway has no routes | check Eureka and the gateway logs |

## Documentation

- [`AGENTS.md`](AGENTS.md) — agent commands, invariants, traps and stop conditions.
- [`TESTING_GUIDE.md`](TESTING_GUIDE.md) — test classes, isolation rules, mocking and coverage.
- [infrastructure/README.md](https://github.com/elara-app/infrastructure#readme) — stack layout, ports and the canonical Compose workflow.
- [`centralized-configuration`](https://github.com/elara-app/centralized-configuration) — profile matrix, shared config and Vault paths.
- [`OpenAPI at /swagger-ui.html`](http://localhost:8082/swagger-ui.html) — interactive docs for a running instance.
