# Testing Guide - Unit of Measure Service

Comprehensive testing practices and patterns for the Unit of Measure Service microservice. This guide covers testing strategy, test organization, and best practices with real examples from the codebase.

## Table of Contents

1. [Testing Philosophy](#testing-philosophy)
2. [Test Organization](#test-organization)
3. [Test Types](#test-types)
4. [Common Patterns](#common-patterns)
5. [Mocking Strategies](#mocking-strategies)
6. [Test Fixtures & Data](#test-fixtures--data)
7. [Best Practices](#best-practices)
8. [Coverage Requirements](#coverage-requirements)
9. [Troubleshooting](#troubleshooting)

---

## Testing Philosophy

### Core Principles

1. **Test in Isolation** - Each test is independent; no shared state between tests
2. **Arrange-Act-Assert** - Organize tests with Given-When-Then structure
3. **Single Responsibility** - One test, one behavior verification
4. **Meaningful Names** - Test names describe what is being tested and expected
5. **Fast Feedback** - Tests run quickly to encourage frequent execution
6. **No Implementation Leakage** - Tests verify behavior, not internal details

### Coverage Strategy

The project enforces strict coverage metrics via JaCoCo:
- **Line Coverage:** 80% minimum (enforced)
- **Branch Coverage:** 70% minimum (enforced)
- **Class Coverage:** 0 uncovered classes (enforced)
- **Exclusions:** DTOs, config classes, `UnitOfMeasureServiceApplication`

Test different aspects:
- **Happy path:** Normal, expected behavior
- **Edge cases:** Boundary values, null handling, empty collections
- **Error paths:** Exceptions, validation failures, conflicts
- **Integration:** Component interactions, database operations

---

## Test Organization

### Test Naming Convention

Follow the pattern: `method_condition_expectedResult`

**Examples:**
- `save_withUniqueName_createsAndReturnsResponse`
- `findById_withNonExistentId_throwsResourceNotFoundException`
- `update_withSameNameButDifferentCase_updatesSuccessfully`
- `changeStatus_withValidId_updatesStatusAndReturnsResponse`

### Test Structure

All tests use `@DisplayName` for human-readable names in reports:

```java
@DisplayName("UomImp Service Tests")
class UomImpTest {
    // ... tests here
}
```

Use `@Nested` classes to logically group related tests:

```java
@Nested
@DisplayName("Save Operations")
class SaveOperations {
    // Related save tests here
}
```

### Test Directories

```
src/test/java/com/elara/app/unit_of_measure_service/
├── controller/                # REST endpoint tests
├── service/imp/              # Service implementation tests
├── repository/               # Data access tests
├── mapper/                   # DTO mapping tests
├── exceptions/               # Exception class tests
├── utils/                    # Utility class tests
└── UnitOfMeasureServiceApplicationTests.java  # Integration tests
```

---

## Test Types

### 1. Unit Tests - Service Layer

Test business logic in isolation using mocks.

**Framework:** `@ExtendWith(MockitoExtension.class)`  
**Mocks:** Repositories, other services, external dependencies  
**Scope:** Pure method logic, no Spring context

**Example:**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("UomImp Service Tests")
class UomImpTest {
    
    @Mock
    private UomRepository repository;
    
    @Mock
    private UomMapper mapper;
    
    @InjectMocks
    private UomImp service;
    
    @AfterEach
    void tearDown() {
        reset(repository, mapper);
    }
    
    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        
        @Test
        @DisplayName("save: with unique name, creates and returns response")
        void save_withUniqueName_createsAndReturnsResponse() {
            // Given
            UomRequest request = createStandardRequest();
            Uom entity = createStandardEntity();
            UomResponse expectedResponse = createStandardResponse();
            
            when(repository.existsByNameIgnoreCase(request.name()))
                .thenReturn(false);
            when(mapper.toEntity(request))
                .thenReturn(entity);
            when(repository.save(entity))
                .thenReturn(entity);
            when(mapper.toResponse(entity))
                .thenReturn(expectedResponse);
            
            // When
            UomResponse result = service.save(request);
            
            // Then
            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedResponse);
            verify(repository).existsByNameIgnoreCase(request.name());
            verify(repository).save(entity);
        }
        
        @Test
        @DisplayName("save: with duplicate name, throws ResourceConflictException")
        void save_withDuplicateName_throwsResourceConflictException() {
            // Given
            UomRequest request = createStandardRequest();
            when(repository.existsByNameIgnoreCase(request.name()))
                .thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already exists");
            
            verify(repository).existsByNameIgnoreCase(request.name());
            verify(repository, never()).save(any());
        }
    }
}
```

### 2. Integration Tests - Repository Layer

Test JPA queries and database operations with real schema.

**Framework:** `@DataJpaTest`  
**Database:** H2 in-memory (test profile)  
**Scope:** Query correctness, constraints, relationships

**Example:**
```java
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UomRepository")
class UomRepositoryTest {
    
    @Autowired
    private UomRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Nested
    @DisplayName("FindByNameIgnoreCase")
    class FindByNameIgnoreCaseTests {
        
        @Test
        @DisplayName("findByNameIgnoreCase: with existing name (different case), returns item")
        void findByNameIgnoreCase_withExistingNameDifferentCase_returnsItem() {
            // Given
            Uom uom = createAndPersistUom(
                "Meter",
                "Length measurement unit",
                "m",
                new BigDecimal("1.0")
            );
            
            // When
            Optional<Uom> result = repository.findByNameIgnoreCase("meter");
            
            // Then
            assertThat(result)
                .isPresent()
                .contains(uom);
        }
        
        @Test
        @DisplayName("findByNameIgnoreCase: with non-existent name, returns empty")
        void findByNameIgnoreCase_withNonExistentName_returnsEmpty() {
            // When
            Optional<Uom> result = repository.findByNameIgnoreCase("NonExistent");
            
            // Then
            assertThat(result).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("ExistsByNameIgnoreCase")
    class ExistsByNameIgnoreCaseTests {
        
        @Test
        @DisplayName("existsByNameIgnoreCase: with existing item, returns true")
        void existsByNameIgnoreCase_withExistingItem_returnsTrue() {
            // Given
            createAndPersistUom("Kilogram", "Mass unit", "kg", new BigDecimal("1.0"));
            
            // When
            boolean exists = repository.existsByNameIgnoreCase("kilogram");
            
            // Then
            assertThat(exists).isTrue();
        }
        
        @Test
        @DisplayName("existsByNameIgnoreCase: with non-existent item, returns false")
        void existsByNameIgnoreCase_withNonExistentItem_returnsFalse() {
            // When
            boolean exists = repository.existsByNameIgnoreCase("NonExistent");
            
            // Then
            assertThat(exists).isFalse();
        }
    }
}
```

### 3. Controller Tests - REST API Layer

Test HTTP endpoints, status codes, and response formats.

**Framework:** `@WebMvcTest`  
**Mocks:** Services, dependencies  
**Tools:** `MockMvc` for HTTP simulation  
**Scope:** Request handling, status codes, error responses

**Example:**
```java
@WebMvcTest(UomController.class)
@DisplayName("UomController REST API Tests")
class UomControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UomService service;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @AfterEach
    void tearDown() {
        reset(service);
    }
    
    @Nested
    @DisplayName("POST / - Create")
    class CreateOperations {
        
        @Test
        @DisplayName("create: with valid request, returns 201 Created")
        void create_withValidRequest_returns201Created() throws Exception {
            // Given
            UomRequest request = createStandardRequest();
            UomResponse response = createStandardResponse();
            
            when(service.save(request))
                .thenReturn(response);
            
            // When & Then
            mockMvc.perform(post("/")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.name").value(response.name()));
            
            verify(service).save(request);
        }
        
        @Test
        @DisplayName("create: with invalid request (blank name), returns 400 Bad Request")
        void create_withBlankName_returns400BadRequest() throws Exception {
            // Given
            UomRequest request = new UomRequest(
                "",  // blank name
                "Description",
                "m",
                new BigDecimal("1.0")
            );
            
            // When & Then
            mockMvc.perform(post("/")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
            
            verify(service, never()).save(any());
        }
        
        @Test
        @DisplayName("create: with duplicate name, returns 409 Conflict")
        void create_withDuplicateName_returns409Conflict() throws Exception {
            // Given
            UomRequest request = createStandardRequest();
            
            when(service.save(request))
                .thenThrow(new ResourceConflictException("Uom already exists"));
            
            // When & Then
            mockMvc.perform(post("/")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003));
            
            verify(service).save(request);
        }
    }
    
    @Nested
    @DisplayName("GET /{id}")
    class GetByIdOperations {
        
        @Test
        @DisplayName("getById: with existing ID, returns 200 OK with item")
        void getById_withExistingId_returns200Ok() throws Exception {
            // Given
            Long uomId = 1L;
            UomResponse response = createStandardResponse();
            
            when(service.findById(uomId))
                .thenReturn(response);
            
            // When & Then
            mockMvc.perform(get("/{id}", uomId)
                    .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uomId))
                .andExpect(jsonPath("$.name").value(response.name()));
            
            verify(service).findById(uomId);
        }
        
        @Test
        @DisplayName("getById: with non-existent ID, returns 404 Not Found")
        void getById_withNonExistentId_returns404NotFound() throws Exception {
            // Given
            Long uomId = 999L;
            
            when(service.findById(uomId))
                .thenThrow(new ResourceNotFoundException("Uom not found"));
            
            // When & Then
            mockMvc.perform(get("/{id}", uomId)
                    .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
            
            verify(service).findById(uomId);
        }
    }
}
```

### 4. Mapper Tests - DTO Transformation

Test data transformation between DTOs and entities.

**Framework:** `@SpringBootTest` with `@ActiveProfiles("test")`  
**Scope:** Correct field mapping, type conversion, null handling

**Example:**
```java
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UomMapper")
class UomMapperTest {
    
    @Autowired
    private UomMapper mapper;
    
    @Nested
    @DisplayName("ToEntity - Request to Entity Mapping")
    class ToEntityTests {
        
        @Test
        @DisplayName("toEntity: with valid request, maps all fields correctly")
        void toEntity_withValidRequest_mapsAllFieldsCorrectly() {
            // Given
            UomRequest request = new UomRequest(
                "Meter",
                "Length measurement unit",
                "m",
                new BigDecimal("1.0")
            );
            
            // When
            Uom entity = mapper.toEntity(request);
            
            // Then
            assertThat(entity)
                .isNotNull()
                .extracting("name", "description", "abbreviation")
                .containsExactly("Meter", "Length measurement unit", "m");
            assertThat(entity.getConversionFactor()).isEqualByComparingTo("1.0");
        }
        
        @Test
        @DisplayName("toEntity: with null description, handles correctly")
        void toEntity_withNullDescription_handlesCorrectly() {
            // Given
            UomRequest request = new UomRequest(
                "Kilogram",
                null,
                "kg",
                new BigDecimal("1.0")
            );
            
            // When
            Uom entity = mapper.toEntity(request);
            
            // Then
            assertThat(entity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("description", null);
        }
        
        @Test
        @DisplayName("toEntity: with BigDecimal precision, preserves decimal places")
        void toEntity_withBigDecimalPrecision_preservesDecimalPlaces() {
            // Given
            UomRequest request = new UomRequest(
                "Centimeter",
                "Small length unit",
                "cm",
                new BigDecimal("0.01")
            );
            
            // When
            Uom entity = mapper.toEntity(request);
            
            // Then
            assertThat(entity.getConversionFactor()).isEqualByComparingTo("0.01");
        }
    }
}
```

### 5. Exception Tests

Test custom exceptions handle edge cases correctly.

**Framework:** `@Test` with assertions  
**Scope:** Exception construction, message formatting, field initialization

```java
@DisplayName("Custom Exceptions")
class CustomExceptionsTest {
    
    @Nested
    @DisplayName("ResourceConflictException")
    class ResourceConflictExceptionTests {
        
        @Test
        @DisplayName("constructor: initializes with message")
        void constructor_initializesWithMessage() {
            // When
            ResourceConflictException exception = new ResourceConflictException("Uom already exists");
            
            // Then
            assertThat(exception)
                .hasMessageContaining("already exists");
        }
    }
}
```

---

## Common Patterns

### Given-When-Then Structure

All tests follow this structure:

```java
@Test
@DisplayName("service: operation, expected result")
void testName() {
    // Given - Setup test data and mocks
    Uom uom = new Uom("Meter", "m", new BigDecimal("1.0"));
    when(repository.findById(1L)).thenReturn(Optional.of(uom));
    
    // When - Execute the code under test
    UomResponse result = service.findById(1L);
    
    // Then - Verify the outcome
    assertThat(result).isNotNull();
    verify(repository).findById(1L);
}
```

### Assertion Helpers

Use AssertJ for expressive assertions:

```java
// Null checks
assertThat(value).isNull();
assertThat(value).isNotNull();

// Equality
assertThat(value).isEqualTo(expected);
assertThat(value).isNotEqualTo(unexpected);

// Collections
assertThat(list).isEmpty();
assertThat(list).hasSize(3);
assertThat(list).contains(item1, item2);
assertThat(list).doesNotContain(item3);

// BigDecimal (use comparison, not equality)
assertThat(factor).isEqualByComparingTo("1.00");
assertThat(factor).isGreaterThan(new BigDecimal("0.5"));

// Exceptions
assertThatThrownBy(() -> service.save(request))
    .isInstanceOf(ResourceConflictException.class)
    .hasMessageContaining("already exists");

// Objects with multiple fields
assertThat(response)
    .isNotNull()
    .extracting("id", "name", "abbreviation")
    .containsExactly(1L, "Meter", "m");
```

### Mockito Patterns

**Setup mocks:**
```java
// Return value
when(repository.save(any(Uom.class)))
    .thenReturn(savedUom);

// Throw exception
when(repository.findById(999L))
    .thenThrow(new ResourceNotFoundException("Uom not found"));

// Multiple calls
when(repository.existsByName(anyString()))
    .thenReturn(false)
    .thenReturn(true);
```

**Verify interactions:**
```java
// Called once with specific argument
verify(repository).save(uom);

// Called exactly N times
verify(repository, times(2)).findAll();

// Never called
verify(repository, never()).delete(any());

// Verify call order
InOrder inOrder = inOrder(repository, service);
inOrder.verify(service).validate(request);
inOrder.verify(repository).save(entity);
```

**Argument Matchers:**
```java
any()              // Any value
any(String.class)  // Any string
anyLong()          // Any long
argThat(x -> x > 0)  // Custom matcher
eq("exact")        // Exact match
```

---

## Mocking Strategies

### Service Layer Mocking

Mock external dependencies but test service logic:

```java
@ExtendWith(MockitoExtension.class)
class UomImpTest {
    
    @Mock private UomRepository repository;
    @Mock private UomMapper mapper;
    @Mock private MessageService messageService;
    
    @InjectMocks
    private UomImp service;
    
    @AfterEach
    void tearDown() {
        // Reset ALL mocks to ensure test isolation
        reset(repository, mapper, messageService);
    }
    
    @Test
    void save_withValidRequest_callsValidationThenSave() {
        // Mock the flow
        UomRequest request = new UomRequest(...);
        
        // When repository is asked about duplicates, say no
        when(repository.existsByNameIgnoreCase(request.name()))
            .thenReturn(false);
        
        // When mapper is asked to convert, return an entity
        Uom entity = new Uom(...);
        when(mapper.toEntity(request))
            .thenReturn(entity);
        
        // When repository saves, return the saved entity
        when(repository.save(entity))
            .thenReturn(entity);
        
        // Test executes normally
        service.save(request);
        
        // Verify the sequence of calls
        InOrder inOrder = inOrder(repository, mapper);
        inOrder.verify(repository).existsByNameIgnoreCase(request.name());
        inOrder.verify(mapper).toEntity(request);
        inOrder.verify(repository).save(entity);
    }
}
```

### Controller Layer Mocking

Mock services, test HTTP layer:

```java
@WebMvcTest(UomController.class)
class UomControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockitoBean private UomService service;
    
    @Test
    void create_withValidRequest_returns201() throws Exception {
        // Mock service response
        UomResponse response = new UomResponse(1L, "Meter", "m", ...);
        when(service.save(any())).thenReturn(response);
        
        // Execute HTTP request
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{...}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
        
        verify(service).save(any());
    }
}
```

### Avoiding Over-Mocking

❌ **Bad** - Mocks internal behavior:
```java
// Don't mock what you're testing
when(repository.findAll()).thenReturn(pageOfUoms);
Page<Uom> result = service.findAll(pageable);
```

✅ **Good** - Mock only external dependencies:
```java
// Mock the repository (external dependency)
when(repository.findAll(pageable)).thenReturn(pageOfUoms);
// Test service's transformation logic
Page<UomResponse> result = service.findAll(pageable);
```

---

## Test Fixtures & Data

### Test Data Builders

Create reusable test data helpers:

```java
private UomRequest createStandardRequest() {
    return new UomRequest(
        "Meter",
        "Length measurement unit",
        "m",
        new BigDecimal("1.0")
    );
}

private UomRequest createRequestWithName(String name) {
    return new UomRequest(
        name,
        "Description",
        "u",
        new BigDecimal("1.0")
    );
}

private Uom createStandardEntity() {
    return Uom.builder()
        .id(1L)
        .name("Meter")
        .description("Length measurement unit")
        .abbreviation("m")
        .conversionFactor(new BigDecimal("1.0"))
        .build();
}

private UomResponse createStandardResponse() {
    return new UomResponse(
        1L,
        "Meter",
        "Length measurement unit",
        "m",
        new BigDecimal("1.0")
    );
}
```

### Database Setup (Repository Tests)

```java
@DataJpaTest
class UomRepositoryTest {
    
    @Autowired private TestEntityManager entityManager;
    @Autowired private UomRepository repository;
    
    private Uom createAndPersistUom(String name, String description, String abbreviation, 
                                     BigDecimal conversionFactor) {
        Uom uom = Uom.builder()
            .name(name)
            .description(description)
            .abbreviation(abbreviation)
            .conversionFactor(conversionFactor)
            .build();
        entityManager.persist(uom);
        entityManager.flush();
        return uom;
    }
}
```

### Test Profiles

Use `@ActiveProfiles("test")` to load test-specific configuration:

```java
// application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.root=WARN
```

---

## Best Practices

### 1. Test Independence

Each test must be independent:

```java
❌ BAD - Depends on test execution order
class UomTests {
    static List<Uom> uoms = new ArrayList<>();
    
    @Test void createUom() { uoms.add(new Uom()); }
    @Test void findUom() { Uom u = uoms.get(0); }  // Fails if run alone!
}

✅ GOOD - Each test stands alone
class UomTests {
    @Test void createUom() {
        Uom uom = repository.save(new Uom());
        assertThat(uom).isNotNull();
    }
    
    @Test void findUom() {
        Uom saved = repository.save(new Uom());
        Uom found = repository.findById(saved.getId());
        assertThat(found).isEqualTo(saved);
    }
}
```

### 2. Use @AfterEach for Mock Reset

Always reset mocks after each test:

```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock private Repository repository;
    @InjectMocks private Service service;
    
    @AfterEach
    void tearDown() {
        reset(repository);  // Reset mock state
    }
    
    @Test void test1() { /* ... */ }
    @Test void test2() { /* ... */ }  // Mocks are clean
}
```

### 3. Meaningful Test Names and Descriptions

```java
// Use @DisplayName for readable test reports
@Test
@DisplayName("save: with duplicate name (case-insensitive), throws ResourceConflictException")
void save_withDuplicateName_throwsResourceConflictException() {
    // ...
}
```

### 4. One Assertion per Test (or Related Assertions)

Prefer focused tests:

```java
❌ BAD - Tests multiple unrelated behaviors
@Test
void saveLongTest() {
    service.save(request);
    service.update(updated);
    service.delete(id);
    // Multiple assertions on unrelated operations
}

✅ GOOD - One logical behavior per test
@Test void save_withValidRequest_returnsSavedUom() { }
@Test void update_withValidRequest_updatesUom() { }
@Test void delete_withValidId_removesUom() { }
```

### 5. Avoid Testing Implementation Details

```java
❌ BAD - Tests how it works, not what it does
@Test
void save_callsMapperBeforeRepository() {
    InOrder inOrder = inOrder(mapper, repository);
    inOrder.verify(mapper).toEntity(request);
    inOrder.verify(repository).save(entity);
}

✅ GOOD - Tests the outcome
@Test
void save_withValidRequest_returnsResponse() {
    UomResponse result = service.save(request);
    assertThat(result).isNotNull().hasFieldOrPropertyWithValue("id", 1L);
}
```

### 6. Test Edge Cases and Boundaries

```java
@Nested
@DisplayName("Edge Cases")
class EdgeCases {
    
    @Test
    @DisplayName("toEntity: with maximum field lengths, handles correctly")
    void toEntity_withMaximumFieldLengths_handlesCorrectly() {
        String maxName = "A".repeat(50);      // Max 50 chars
        String maxDescription = "B".repeat(200);  // Max 200 chars
        
        UomRequest request = new UomRequest(
            maxName, maxDescription, "ab", new BigDecimal("1.0")
        );
        
        Uom result = mapper.toEntity(request);
        assertThat(result.getName()).hasSize(50);
        assertThat(result.getDescription()).hasSize(200);
    }
    
    @Test
    @DisplayName("toResponse: with minimum BigDecimal values, handles correctly")
    void toResponse_withMinimumBigDecimalValues_handlesCorrectly() {
        Uom entity = Uom.builder()
            .conversionFactor(new BigDecimal("0.0001"))  // Minimum
            .build();
        
        UomResponse result = mapper.toResponse(entity);
        assertThat(result.getConversionFactor()).isEqualByComparingTo("0.0001");
    }
}
```

### 7. Use Nested Classes for Organization

```java
@DisplayName("UomService")
class UomServiceTest {
    
    @Nested
    @DisplayName("Save Operations")
    class SaveTests { }
    
    @Nested
    @DisplayName("Update Operations")
    class UpdateTests { }
    
    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests { }
    
    @Nested
    @DisplayName("Error Cases")
    class ErrorCases { }
}
```

---

## Coverage Requirements

### Enforced by JaCoCo

```bash
# Generate coverage report
./mvnw clean verify

# Report location
target/site/jacoco/index.html
```

### Minimum Standards

| Metric | Minimum | Type |
|--------|---------|------|
| Line Coverage | 80% | Required (BUNDLE) |
| Branch Coverage | 70% | Required (BUNDLE) |
| Uncovered Classes | 0 | Required (BUNDLE) |
| Package Line Coverage | 70% | Required |

### Coverage Exclusions

The following are excluded from coverage requirements:
- `**/*.dto/**` - All DTO classes (records)
- `**/config/**` - Configuration classes
- `UnitOfMeasureServiceApplication` - Main application class

Add `@jakarta.annotation.Generated` to exclude generated code:

```java
@Generated  // Excluded from coverage
public class GeneratedClass {
    // ...
}
```

### Improving Coverage

**Identify gaps:**
```bash
# View coverage report
open target/site/jacoco/index.html
```

**Add missing tests for:**
- Error paths (exceptions, validation failures)
- Boundary conditions (null, empty, max values)
- Alternative branches (if/else conditions)
- Integration scenarios

---

## Troubleshooting

### Issue: Mock Not Injected

**Problem:** `@InjectMocks` field is null

**Solution:** Use `@ExtendWith(MockitoExtension.class)` on test class:
```java
@ExtendWith(MockitoExtension.class)  // Required for @InjectMocks
class ServiceTest {
    @Mock private Repository repository;
    @InjectMocks private Service service;  // Now service.repository is injected
}
```

### Issue: Test Isolation Failure

**Problem:** One test's data affects another test

**Solution:** Use `@AfterEach` with `reset()`:
```java
@AfterEach
void tearDown() {
    reset(repository, mapper, messageService);  // Reset ALL mocks
}
```

### Issue: Flaky BigDecimal Assertions

**Problem:** `isEqualTo()` fails due to scale differences

**Solution:** Use `isEqualByComparingTo()`:
```java
✅ CORRECT - Compares numeric value
assertThat(new BigDecimal("1.00"))
    .isEqualByComparingTo(new BigDecimal("1"));

❌ WRONG - Fails because scale differs
assertThat(new BigDecimal("1.00"))
    .isEqualTo(new BigDecimal("1"));
```

### Issue: Pagination Test Issues

**Problem:** PageImpl constructor confusion

**Solution:** Use correct constructor:
```java
List<Uom> uoms = Arrays.asList(uom1, uom2);
Pageable pageable = PageRequest.of(0, 20);
Page<Uom> page = new PageImpl<>(uoms, pageable, uoms.size());
```

### Issue: MockMvc Request/Response Format

**Problem:** JSON serialization/deserialization errors

**Solution:** Use ObjectMapper correctly:
```java
@Autowired
private ObjectMapper objectMapper;

// Serialize object to JSON
String json = objectMapper.writeValueAsString(request);

// Use in request
mockMvc.perform(post("/")
    .contentType(MediaType.APPLICATION_JSON)
    .content(json))
    ...

// Verify response
.andExpect(jsonPath("$.id").value(1L))
```

### Issue: Test Fails Locally but Passes in CI

**Problem:** Likely database profile or timing issue

**Solutions:**
- Ensure `@ActiveProfiles("test")` is set
- Add explicit waits if async operations exist
- Check database transaction isolation level
- Verify test data cleanup (use `@AfterEach` or `@DataJpaTest` auto-rollback)

### Issue: Coverage Report Not Generated

**Problem:** `target/site/jacoco/index.html` missing

**Solution:** Run with verify phase:
```bash
./mvnw clean verify
```

Not just `mvnw test` — you need the `verify` phase for JaCoCo reports.

---

## Quick Reference

### Running Tests

```bash
# All tests
./mvnw test

# Single test class
./mvnw test -Dtest=UomServiceTest

# Single test method
./mvnw test -Dtest=UomServiceTest#save_withUniqueName_createsAndReturnsResponse

# With coverage report
./mvnw clean verify

# Skip tests
./mvnw clean install -DskipTests
```

### Test Annotations

| Annotation | Usage |
|------------|-------|
| `@Test` | Marks method as test |
| `@DisplayName("...")` | Readable name in reports |
| `@Nested` | Groups related tests |
| `@BeforeEach` | Runs before each test |
| `@AfterEach` | Runs after each test (cleanup) |
| `@ParameterizedTest` | Multiple test cases |
| `@ExtendWith(...)` | Extension (e.g., Mockito) |
| `@Mock` | Creates mock object |
| `@InjectMocks` | Injects mocks into object |
| `@WebMvcTest(...)` | Load minimal Spring context for controllers |
| `@DataJpaTest` | Load Spring context for repositories |
| `@SpringBootTest` | Full Spring application context |
| `@ActiveProfiles("test")` | Use test configuration |

---

**Last Updated:** April 10, 2026  
**Version:** 1.0  
**Related:** See [`AGENTS.md`](AGENTS.md) for code style and architecture guidelines.

