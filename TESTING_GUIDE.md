# Testing Guide — Unit of Measure Service

How the tests in this repository are organized and how to run, extend and debug them. The API contract is in [`README.md`](README.md); agent invariants are in [`AGENTS.md`](AGENTS.md).

## Commands

The Maven wrapper is mode 644, so call it through `sh`:

```bash
sh ./mvnw -B clean verify -Dspring.profiles.active=test   # full gate (CI equivalent)
sh ./mvnw test                                            # all tests
sh ./mvnw test -Dtest=UomControllerTest                   # one class
sh ./mvnw test -Dtest=UomServiceImpTest#save_shouldCreateAndReturnResponse
```

The last verified local run executed `sh ./mvnw -B test -Dspring.profiles.active=test` and reported 229 tests in 13 classes, with 0 failures and 0 errors. Surefire output is written to `target/surefire-reports`.

## Test inventory

| Test class | Tests | Style | Notes |
|---|---:|---|---|
| `UomControllerTest` | 38 | `@WebMvcTest` + MockMvc | Mock service/message beans from a nested `@TestConfiguration` |
| `UomStatusControllerTest` | 22 | `@WebMvcTest` + MockMvc | Same pattern; base URL is `/status/` |
| `UomServiceImpTest` | 26 | Mockito extension | `@Spy @InjectMocks`; no `@AfterEach` reset |
| `UomStatusServiceImpTest` | 33 | Mockito extension | `@Mock`/`@InjectMocks`; resets in `@AfterEach` |
| `UomRepositoryTest` | 18 | `@DataJpaTest` | H2; seeds statuses with `TestEntityManager` |
| `UomStatusRepositoryTest` | 8 | `@DataJpaTest` | H2; persists through the repository |
| `UomMapperTest` | 19 | `@SpringBootTest` | Full context; autowired `UomMapper` |
| `UomStatusMapperTest` | 5 | `@SpringBootTest` | Full context; autowired `UomStatusMapper` |
| `GlobalExceptionHandlerTest` | 34 | Mockito extension | Handler direct calls; status/code assertions |
| `GlobalExceptionHandlerLoggingTest` | 5 | Mockito + Logback | `ListAppender` checks log levels and sanitization |
| `CustomExceptionsTest` | 15 | Plain JUnit | Exception construction and code/value assertions |
| `ApplicationContextHolderTest` | 2 | `@SpringBootTest` | Context lookup through the holder |
| `MessageServiceTest` | 4 | Mockito extension | Mocked `MessageSource`; locale fixed in `@BeforeEach` |

There is no `UnitOfMeasureServiceApplicationTests`. Context loading is covered indirectly by the mapper and context-holder tests, which use `@SpringBootTest`.

## Service tests

Service tests use `@ExtendWith(MockitoExtension.class)` with the repository, mapper, `MessageService` and (for UOM) `UomStatusService` mocked.

`UomStatusServiceImpTest` is the straightforward pattern:

```java
@ExtendWith(MockitoExtension.class)
class UomStatusServiceImpTest {
    @Mock private UomStatusRepository repository;
    @Mock private UomStatusMapper mapper;
    @Mock private MessageService messageService;
    @InjectMocks private UomStatusServiceImp service;

    @AfterEach
    void tearDown() {
        reset(repository, mapper, messageService);
    }
}
```

`UomServiceImpTest` differs in two ways that matter:

- It declares the implementation as `@Spy @InjectMocks UomServiceImp service`, because several tests stub the service's own `isNameTaken(...)` method.
- It has no `@AfterEach` and no explicit `reset()`. It relies on `MockitoExtension` and JUnit's per-test lifecycle instead. Do not present this class as the standard isolation pattern when adding new service tests.

Stubbing the spy uses `doReturn`/`doNothing`:

```java
doReturn(false).when(service).isNameTaken("Kilogram");
doNothing().when(mapper).updateEntityFromDto(existing, updateRequest);
```

Keep the Given-When-Then layout used throughout: set up mocks, call the service, assert with AssertJ and verify interactions. The existing tests verify both outcomes and selected interactions (`verify(mapper, never()).updateEntityFromDto(any(), any())`).

## Controller tests

Both controller tests use:

```java
@WebMvcTest(controllers = UomController.class)
@ActiveProfiles("test")
@Import({GlobalExceptionHandler.class, UomControllerTest.TestConfig.class})
class UomControllerTest { ... }
```

The `TestConfig` is a static nested `@TestConfiguration` that publishes the collaborators as Mockito mocks:

```java
@TestConfiguration
static class TestConfig {
    @Bean UomService uomService() { return mock(UomService.class); }
    @Bean MessageService messageService() { return mock(MessageService.class); }
}
```

The test autowires the mock with a qualifier (`@Qualifier("uomService")` or `@Qualifier("uomStatusService")`). It does not use `@MockitoBean`. Both controller tests reset the mock service and message service in `@AfterEach`.

`GlobalExceptionHandler` is imported explicitly so error responses are exercised through MockMvc. Assertions use the nested page shape:

```java
.andExpect(jsonPath("$.content", hasSize(2)))
.andExpect(jsonPath("$.page.totalElements").value(2))
```

`UomControllerTest` checks `$.page.*` in its paging tests. `UomStatusControllerTest` mostly checks `$.content`; add `$.page.*` assertions when you need to prove pagination metadata.

Path caveat: `UomStatusControllerTest` sets `BASE_URL = "/status/"` and several requests build `BASE_URL + "/{id}"`, which produces `/status//{id}`. Those tests pass but do not prove the canonical single-slash path. New tests should use `/status/{id}`.

No test asserts OpenAPI output. `OpenApiConfig` and `src/main/resources/examples/*.json` are hand-maintained and stale (flat page schemas, `/states`/`change-state` wording, status PATCH documented as 204), so do not derive request/response expectations from them. Use the live controller behavior and `<controller>Test` assertions as the reference.

For the usability PATCH, the query parameter is mandatory:

```java
mockMvc.perform(patch("/status/{id}/change-usability", 1L)
        .param("isUsable", "true"))
    .andExpect(status().isNoContent());
```

Omitting `.param("isUsable", ...)` produces 400 code 1002, not 204.

## Repository tests and H2

Repository tests use `@DataJpaTest` plus `@ActiveProfiles("test")`. The test profile in `src/main/resources/application-test.yml` supplies:

- H2 URL `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- `spring.jpa.hibernate.ddl-auto: create-drop`
- Config Server, Vault, Bus and discovery disabled; Eureka client disabled

H2 is `<scope>test</scope>`, so this profile only exists for Maven tests; the packaged jar cannot run it. The bundled test profile disables the H2 console, so do not document `/h2-console` as available. The central `unit-of-measure-service-test.yml` also enables the console, but tests never fetch it because Config Server is disabled.

`UomRepositoryTest` creates `Active` and `Inactive` statuses in `@BeforeEach` with `TestEntityManager` and flushes them before each test. `UomStatusRepositoryTest` persists entities directly through the repository. `@DataJpaTest` rolls each test back, so no manual cleanup is needed.

Repository coverage includes the ignore-case name searches, `existsByNameIgnoreCase`, status filtering, the lazy status relationship and basic CRUD. Database constraints are exercised through JPA, not through raw SQL.

## Mapper tests

Mapper tests are full-context Spring tests:

```java
@SpringBootTest
@ActiveProfiles("test")
class UomMapperTest {
    @Autowired private UomMapper mapper;
}
```

They assert field-by-field mapping for `toEntity`, `toResponse` and `updateEntityFromDto`. Because they start the whole context, a failure here can also mean a context/configuration problem rather than a mapping bug. There are no mapper tests for omitting `UomStatusUpdate.name`; the null-overwrite behavior is a source-level trap, not covered by an HTTP test.

## Exception handler tests

`GlobalExceptionHandlerTest` calls the handler directly with `@InjectMocks` and mocked `MessageService`/`HttpServletRequest`. It stubs `request.getRequestURI()` in `@BeforeEach` and resets both mocks in `@AfterEach`.

The handler tests assert both HTTP status and body code/value. Relevant mappings:

- validation, constraint, missing parameter and type mismatch → 400 code 1002
- `ResourceConflictException`/1003 → 409; `ResourceNotFoundException`/1004 → 404
- `BaseException` with 1001, 1005 or 1006 → 500
- `DataIntegrityViolationException` → 409 code 1001
- generic `Exception` → 500 code 1006

`GlobalExceptionHandlerLoggingTest` attaches a Logback `ListAppender` to the handler logger. It verifies 4xx `BaseException` logs at WARN without a throwable, 5xx at ERROR with a throwable, and that data-integrity logging includes the SQL state but not row values. Some message stubs use `lenient()` to avoid unnecessary-stubbing failures.

## Isolation and mocking rules

- Reset mocks in `@AfterEach` in new controller and service tests; follow `UomStatusServiceImpTest` and the controller tests.
- `UomServiceImpTest` is the exception: it uses a spy and no explicit reset. If you add tests there, follow its existing `@Spy`/`doReturn` style; for new test classes use the explicit reset pattern shown above.
- Prefer `@Mock` + `@InjectMocks` for pure unit tests and `@TestConfiguration` mock beans for `@WebMvcTest` slices.
- Mockito runs with strict stubs. Remove unused stubs or mark genuinely optional ones `lenient()`.
- `@DataJpaTest` provides transactional rollback; do not add manual table cleanup.
- Use `isEqualByComparingTo` for `BigDecimal` assertions; `isEqualTo` is scale-sensitive.
- `UomServiceImpTest` needs `doReturn(...).when(service).isNameTaken(...)` (spy), not `when(service.isNameTaken(...))`.

## Coverage gates and exclusions

JaCoCo is configured in `pom.xml`:

- `prepare-agent` instruments tests.
- `report` is bound to the `test` phase, so `sh ./mvnw test` writes `target/site/jacoco/index.html`.
- `check` runs in `verify` and fails the build below the gates.

Gates:

| Scope | Metric | Minimum |
|---|---|---|
| BUNDLE | line coverage | 80% |
| BUNDLE | branch coverage | 70% |
| BUNDLE | missed classes | 0 |
| PACKAGE | line coverage | 70% |

Exclusions for both report and check: `**/UnitOfMeasureServiceApplication.class`, `**/config/**`, `**/dto/**`.

The package rule also contains the stale exclude `com.elara.app.UnitOfMeasureServiceApplication.config.*`, which matches no class in this package layout. Do not copy it into new rules; the effective config exclusion is `**/config/**`.

No Checkstyle, SpotBugs, Enforcer or Failsafe gate is configured. `sonar-maven-plugin` is declared without an execution.

## Continuous integration

- `.github/workflows/test.yml` runs the reusable org test workflow on pull requests to `develop` and `main`. The reusable workflow body lives in `elara-app/.github`; run the local equivalent `sh ./mvnw -B clean verify -Dspring.profiles.active=test` before pushing.
- `.github/workflows/publish-image.yml` runs on pushes to `main` (markdown-only changes ignored) and publishes `ghcr.io/elara-app/unit-of-measure-service`.
- Dependabot checks GitHub Actions and Docker weekly against `develop`.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `./mvnw: Permission denied` | wrapper is mode 644 | run `sh ./mvnw ...` |
| `-Dtest=UomImpTest` runs nothing | class does not exist | use `-Dtest=UomServiceImpTest` |
| Context tries Config Server or Vault | Spring test lacks the test profile | add `@ActiveProfiles("test")`; the canonical command also passes `-Dspring.profiles.active=test` |
| H2 schema or seed errors | test profile not active, or PostgreSQL seed assumed | use `application-test.yml`; the dev seed is not loaded by tests |
| `UnnecessaryStubbingException` | strict stubs and an unused `when(...)` | remove the stub or use `lenient()` |
| Controller mock is null | collaborator not published as a bean | add it to the nested `@TestConfiguration` and use `@Qualifier` |
| Spy calls the real method | `when(spy.method())` instead of `doReturn` | use `doReturn(...).when(service).isNameTaken(...)` |
| Page assertion `$.totalElements` is null | `VIA_DTO` nested page shape | assert `$.page.totalElements` |
| BigDecimal assertion fails | scale differs (`1.0` vs `1.000`) | use `isEqualByComparingTo` |
| Coverage report missing | report is bound to `test`, check to `verify` | run `sh ./mvnw test`, or `verify` for the gate |
| Status PATCH test returns 400 | `isUsable` not sent | add `.param("isUsable", "true")` |
| Status endpoint test passes with `//` | test builds `/status//{id}` | use the canonical `/status/{id}` in new tests |
| `verify` fails only in CI | profile or command differs | reproduce with `sh ./mvnw -B clean verify -Dspring.profiles.active=test` |

## Quick reference

| Annotation / tool | Used for |
|---|---|
| `@WebMvcTest` | Controller slice with MockMvc |
| `@DataJpaTest` | Repository slice with H2 |
| `@SpringBootTest` | Mapper and context-holder tests (full context) |
| `@ActiveProfiles("test")` | All Spring-based tests |
| `@ExtendWith(MockitoExtension.class)` | Pure Mockito tests |
| `@Mock`, `@InjectMocks`, `@Spy` | Service, handler and utility tests |
| `@TestConfiguration` | Mock beans for controller slices |
| `@Nested`, `@DisplayName` | Test grouping and readable reports |
| AssertJ | Result and exception assertions |
| MockMvc `jsonPath` | HTTP body and nested page assertions |
