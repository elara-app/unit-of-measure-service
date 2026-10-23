# AGENTS — unit-of-measure-service

Operating guide for agents changing this repository. It records commands, contracts, traps and stop conditions. The API contract itself lives in [`README.md`](README.md); do not duplicate endpoint tables here.

## Read first

- [`README.md`](README.md) — purpose, API tables, error contract, run/verify, limitations.
- [`TESTING_GUIDE.md`](TESTING_GUIDE.md) — real test classes, isolation rules, H2 notes and coverage details.
- [`infra/README.md#verify-the-stack`](../infra/README.md#verify-the-stack) — canonical full-stack Compose block; link to it, never duplicate it.
- [`centralized-configuration`](https://github.com/elara-app/centralized-configuration) — profile matrix, shared properties and Vault paths.

## Scope and boundaries

- This repo owns the UOM catalog (`/`) and the UOM status catalog (`/status/`). Nothing else.
- Do not add inventory stock logic, authentication/authorization, audit tables, domain events or outbound service calls.
- The only cross-service contract is with `inventory-service`: seeded UOM ids 1 (Kilogram) and 2 (Gram) must keep existing for inventory writes.
- Known code-level quirks are documented as traps below. Changing them is a deliberate decision, not a drive-by fix; see Stop and ask.

## Quick facts

| Item | Value |
|---|---|
| Maven coordinates | `com.elara.app:unit-of-measure-service:1.3.1` |
| Main class / package | `UnitOfMeasureServiceApplication`, `com.elara.app.unit_of_measure_service` |
| Service ID / port | `unit-of-measure-service` / `8082` (port comes from Config Server; the Dockerfile exposes 8082) |
| Java / Boot / Cloud | Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0 |
| Persistence | PostgreSQL (`precision = 10, scale = 3` for the conversion factor); H2 only for Maven tests |
| Runtime dependencies | Eureka client, Config Server client, optional Vault, RabbitMQ + Spring Cloud Bus |
| Controllers | `UomController` at `/`, `UomStatusController` at `/status/` |
| Service implementations | `service/implementation/UomServiceImp`, `service/implementation/UomStatusServiceImp` |

## Commands

The wrapper is mode 644. Always use `sh ./mvnw`; never `./mvnw`, and do not instruct `chmod +x`.

```bash
sh ./mvnw -B clean verify -Dspring.profiles.active=test   # full gate (CI equivalent)
sh ./mvnw test                                            # tests only
sh ./mvnw test -Dtest=UomControllerTest                   # focused class
sh ./mvnw test -Dtest=UomServiceImpTest#save_shouldCreateAndReturnResponse
sh ./mvnw spring-boot:run                                 # needs Config Server + PostgreSQL
```

Full-stack verification: [infra/README.md#verify-the-stack](../infra/README.md#verify-the-stack).

## Repository map

| Package | Contents |
|---|---|
| `controller/` | `UomController`, `UomStatusController`; validation, logging, delegation |
| `service/interfaces/` | `UomService`, `UomStatusService` |
| `service/implementation/` | `UomServiceImp`, `UomStatusServiceImp`; business invariants and transactions |
| `repository/` | `UomRepository`, `UomStatusRepository` (Spring Data JPA) |
| `model/` | `Uom`, `UomStatus` entities |
| `dto/request`, `dto/update`, `dto/response` | request, update and response records |
| `mapper/` | `UomMapper`, `UomStatusMapper` (MapStruct, `componentModel = "spring"`) |
| `config/` | `GlobalExceptionHandler`, `ErrorResponse`, `OpenApiConfig` (there is no `AppConfig`) |
| `exceptions/` | `BaseException` and its subclasses |
| `utils/` | `ErrorCode`, `MessageService`, `ApplicationContextHolder` |

## Contracts and invariants

- UOM name uniqueness is case-insensitive (`existsByNameIgnoreCase`); status name uniqueness is case-sensitive (`existsByName`).
- Both update guards compare with `equals`, which is case-sensitive: a case-only UOM rename re-enters the case-insensitive check and returns 409, while a case-only status rename is allowed.
- `PATCH /{id}/status/{newUomStatusId}` returns 200 with a `UomResponse`. The OpenAPI annotation says 204 — ignore it.
- `PATCH /status/{id}/change-usability?isUsable=` returns 204 and requires the query parameter; `PUT /status/{id}` cannot change `isUsable`.
- `PUT /{id}` preserves the existing `uomStatus` even though `UomUpdate` omits it.
- No usability veto: create and change-status accept any existing status, usable or not, and making a status unusable later does not cascade.
- List endpoints use `@PageableDefault(size = 20)` with no default sort; page JSON is nested (`content` + `page`) via `VIA_DTO`.
- Status delete relies on the FK: `DataIntegrityViolationException` becomes 409 code 1001. UOM delete has no reverse dependency and no reachable 409.
- Error mapping: validation/missing parameter/type mismatch → 400 code 1002; service 404 → 1004; service 409 → 1003; data integrity → 409 code 1001; codes 1001/1005/1006 → 500; other exceptions → 500 code 1006. `BaseException` is a concrete class.
- `conversionFactorToBase` is a `BigDecimal` greater than zero with column scale 3. There is no `abbreviation` field and no `@Digits` on UOM DTOs.

## Configuration and runtime

- `application.yml` sets active profile `dev` and imports `optional:configserver:${CONFIG_URI:http://localhost:8888}` with `fail-fast:false` and retry.
- `application-dev.yml` imports `optional:vault://secret/unit-of-measure-service/dev`, uses `ddl-auto: update` and runs the idempotent `db/dev/seed.sql` (3 statuses, 5 UOMs).
- `application-test.yml` disables Config Server, Vault, Bus and discovery and uses H2 with `create-drop`. H2 is test-scope, so the packaged jar cannot run `test`.
- Port, datasource, RabbitMQ, Eureka and actuator exposure for dev come from Config Server; Vault supplies credentials only.
- Config refresh uses RabbitMQ/Spring Cloud Bus, not a domain event bus. `refresh` and `busrefresh` are exposed in dev.

## Testing and quality gates

- 13 test classes, 229 tests: `UomControllerTest` (38), `UomStatusControllerTest` (22), `UomServiceImpTest` (26), `UomStatusServiceImpTest` (33), `UomRepositoryTest` (18), `UomStatusRepositoryTest` (8), `UomMapperTest` (19), `UomStatusMapperTest` (5), `GlobalExceptionHandlerTest` (34), `GlobalExceptionHandlerLoggingTest` (5), `CustomExceptionsTest` (15), `ApplicationContextHolderTest` (2), `MessageServiceTest` (4).
- There is no `UnitOfMeasureServiceApplicationTests`; context loading is covered indirectly by the mapper tests and `ApplicationContextHolderTest`, which use `@SpringBootTest`.
- Controller tests use `@WebMvcTest` with `@TestConfiguration` beans returning `mock(...)`, not `@MockitoBean`.
- `UomServiceImpTest` uses `@Spy @InjectMocks` and has no `@AfterEach`/`reset()`; do not copy it as the standard isolation pattern. `UomStatusServiceImpTest` and both controller tests do reset mocks.
- JaCoCo in `verify`: bundle line 80%, branch 70%, missed classes 0; package line 70%. Exclusions are `**/UnitOfMeasureServiceApplication.class`, `**/config/**` and `**/dto/**`.
- CI: pull requests to `develop`/`main` run the reusable org test workflow; pushes to `main` publish the image. No Checkstyle, SpotBugs or Enforcer gate.

## Definition of done

- `sh ./mvnw -B clean verify -Dspring.profiles.active=test` exits 0.
- New or changed behavior has tests and introduces no uncovered class (the gate enforces this).
- Status codes, nested page shape and error codes still match [`README.md`](README.md); update its tables when behavior changes intentionally.
- OpenAPI annotations and `src/main/resources/examples/*.json` stay aligned with live responses; fix stale `/states`, `change-state`, flat page and PATCH 204 wording when touching them.
- `OpenApiConfig` `info.version` matches the `pom.xml` version.

## Stop and ask

- Changing the UOM id contract used by inventory (ids 1/2) or the seed data shape.
- Making `UomServiceImp.deleteById` transactional or changing delete semantics.
- Changing the MapStruct null-overwrite behavior for `UomStatusUpdate`/`UomUpdate`, or adding `isUsable` to `PUT`.
- Adding path validation to `PUT /{id}` or changing either case-sensitivity rule.
- Adding security, audit fields, migrations, domain events or new external calls.
- Changing gateway routes, profiles or property ownership in another repository.

## Known traps

- Status names are case-sensitive; UOM names are case-insensitive.
- No `abbreviation` field; no `@Digits`; the conversion factor column scale is 3.
- `BaseException` is concrete, not abstract.
- Error 1005 maps to HTTP 500 here, not 503, and is not used by normal flows.
- `UomStatusUpdate.name` is optional, but omission nulls the stored name through the generated mapper.
- `PUT /{id}` has no path-id validation; invalid ids reach the service and normally return 404.
- `UomServiceImp.deleteById` is not transactional; a race can produce a generic 500.
- OpenAPI still says `/states`/`change-state`, shows flat pages and documents the status PATCH as 204.
- `./mvnw` fails on the host because the wrapper is mode 644; only the Docker build runs `chmod +x`.

## Documentation maintenance

- README owns the API contract; AGENTS stays free of duplicated endpoint tables.
- Update `OpenApiConfig` and `examples/*.json` together with controller changes; flag stale wording instead of copying it.
- Keep cross-repo facts (ports, routes, Vault path, seed counts) pointing at `infra` and `centralized-configuration` instead of restating them.
