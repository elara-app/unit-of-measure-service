package com.elara.app.unit_of_measure_service.repository;

import com.elara.app.unit_of_measure_service.model.Uom;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UomRepositoryTest {

    @Autowired
    private UomRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UomStatus activeStatus;
    private UomStatus inactiveStatus;

    @BeforeEach
    void setUp() {
        // Create and persist test statuses
        activeStatus = UomStatus.builder()
                .name("Active")
                .description("Active status")
                .isUsable(true)
                .build();
        entityManager.persist(activeStatus);

        inactiveStatus = UomStatus.builder()
                .name("Inactive")
                .description("Inactive status")
                .isUsable(false)
                .build();
        entityManager.persist(inactiveStatus);

        entityManager.flush();
    }

    @Nested
    @DisplayName("findAllByNameContainingIgnoreCase Tests")
    class FindByNameTests {

        @Test
        @DisplayName("should return UOMs matching partial name")
        void shouldReturnMatchingUoms() {
            // Given
            Uom kilogram = createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);
            Uom kilopascal = createAndPersistUom("Kilopascal", "Pressure unit", new BigDecimal("0.001"), activeStatus);
            createAndPersistUom("Gram", "Small mass unit", new BigDecimal("0.001"), activeStatus);

            // When
            Page<Uom> result = repository.findAllByNameContainingIgnoreCase("kilo", PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(Uom::getName)
                    .containsExactlyInAnyOrder("Kilogram", "Kilopascal");
        }

        @Test
        @DisplayName("should be case insensitive")
        void shouldBeCaseInsensitive() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);
            createAndPersistUom("KILOPASCAL", "Pressure unit", new BigDecimal("0.001"), activeStatus);

            // When
            Page<Uom> resultLower = repository.findAllByNameContainingIgnoreCase("kilo", PageRequest.of(0, 10));
            Page<Uom> resultUpper = repository.findAllByNameContainingIgnoreCase("KILO", PageRequest.of(0, 10));
            Page<Uom> resultMixed = repository.findAllByNameContainingIgnoreCase("KiLo", PageRequest.of(0, 10));

            // Then
            assertThat(resultLower.getContent()).hasSize(2);
            assertThat(resultUpper.getContent()).hasSize(2);
            assertThat(resultMixed.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("should return empty page when no match")
        void shouldReturnEmptyWhenNoMatch() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);
            createAndPersistUom("Gram", "Small unit", new BigDecimal("0.001"), activeStatus);

            // When
            Page<Uom> result = repository.findAllByNameContainingIgnoreCase("liter", PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should handle partial match correctly")
        void shouldHandlePartialMatch() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);
            createAndPersistUom("Gram", "Small unit", new BigDecimal("0.001"), activeStatus);
            createAndPersistUom("Milligram", "Tiny unit", new BigDecimal("0.000001"), activeStatus);

            // When
            Page<Uom> result = repository.findAllByNameContainingIgnoreCase("gram", PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).extracting(Uom::getName)
                    .containsExactlyInAnyOrder("Kilogram", "Gram", "Milligram");
        }

        @Test
        @DisplayName("should support pagination")
        void shouldSupportPagination() {
            // Given
            for (int i = 1; i <= 15; i++) {
                createAndPersistUom("Unit" + i, "desc" + i, new BigDecimal("1.0"), activeStatus);
            }

            // When
            Page<Uom> page1 = repository.findAllByNameContainingIgnoreCase("unit", PageRequest.of(0, 10));
            Page<Uom> page2 = repository.findAllByNameContainingIgnoreCase("unit", PageRequest.of(1, 10));

            // Then
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(15);
            assertThat(page1.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findAllByUomStatusId Tests")
    class FindByStatusIdTests {

        @Test
        @DisplayName("should filter UOMs by status ID")
        void shouldFilterByStatusId() {
            // Given
            createAndPersistUom("Kilogram", "Active unit", new BigDecimal("1.000"), activeStatus);
            createAndPersistUom("Gram", "Active unit", new BigDecimal("0.001"), activeStatus);
            createAndPersistUom("Deprecated", "Old unit", new BigDecimal("1.0"), inactiveStatus);

            // When
            Page<Uom> result = repository.findAllByUomStatusId(activeStatus.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(uom -> uom.getUomStatus().getId().equals(activeStatus.getId()));
        }

        @Test
        @DisplayName("should return empty page when no UOMs with status")
        void shouldReturnEmptyWhenNoMatch() {
            // Given
            createAndPersistUom("Kilogram", "Active unit", new BigDecimal("1.000"), activeStatus);
            createAndPersistUom("Gram", "Active unit", new BigDecimal("0.001"), activeStatus);

            // When
            Page<Uom> result = repository.findAllByUomStatusId(999L, PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should support pagination")
        void shouldSupportPagination() {
            // Given
            for (int i = 1; i <= 12; i++) {
                createAndPersistUom("ActiveUnit" + i, "desc", new BigDecimal("1.0"), activeStatus);
            }

            // When
            Page<Uom> page1 = repository.findAllByUomStatusId(activeStatus.getId(), PageRequest.of(0, 5));
            Page<Uom> page2 = repository.findAllByUomStatusId(activeStatus.getId(), PageRequest.of(1, 5));

            // Then
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(12);
        }

        @Test
        @DisplayName("should return only UOMs with specified status")
        void shouldReturnOnlyMatchingStatus() {
            // Given
            createAndPersistUom("Active1", "desc", new BigDecimal("1.0"), activeStatus);
            createAndPersistUom("Active2", "desc", new BigDecimal("1.0"), activeStatus);
            createAndPersistUom("Inactive1", "desc", new BigDecimal("1.0"), inactiveStatus);
            createAndPersistUom("Inactive2", "desc", new BigDecimal("1.0"), inactiveStatus);

            // When
            Page<Uom> activeResult = repository.findAllByUomStatusId(activeStatus.getId(), PageRequest.of(0, 10));
            Page<Uom> inactiveResult = repository.findAllByUomStatusId(inactiveStatus.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(activeResult.getContent()).hasSize(2);
            assertThat(inactiveResult.getContent()).hasSize(2);
            assertThat(activeResult.getContent()).extracting(Uom::getName)
                    .containsExactlyInAnyOrder("Active1", "Active2");
            assertThat(inactiveResult.getContent()).extracting(Uom::getName)
                    .containsExactlyInAnyOrder("Inactive1", "Inactive2");
        }
    }

    @Nested
    @DisplayName("existsByNameIgnoreCase Tests")
    class ExistsByNameTests {

        @Test
        @DisplayName("should return true when name exists")
        void shouldReturnTrueWhenExists() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);

            // When
            boolean exists = repository.existsByNameIgnoreCase("Kilogram");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when name not exists")
        void shouldReturnFalseWhenNotExists() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);

            // When
            boolean exists = repository.existsByNameIgnoreCase("Liter");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("should be case insensitive")
        void shouldBeCaseInsensitive() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);

            // When
            boolean existsLower = repository.existsByNameIgnoreCase("kilogram");
            boolean existsUpper = repository.existsByNameIgnoreCase("KILOGRAM");
            boolean existsMixed = repository.existsByNameIgnoreCase("KiLoGrAm");

            // Then
            assertThat(existsLower).isTrue();
            assertThat(existsUpper).isTrue();
            assertThat(existsMixed).isTrue();
        }

        @Test
        @DisplayName("should be exact match only")
        void shouldBeExactMatchOnly() {
            // Given
            createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);

            // When
            Boolean partialMatch = repository.existsByNameIgnoreCase("Kilo");
            Boolean fullMatch = repository.existsByNameIgnoreCase("Kilogram");

            // Then
            assertThat(partialMatch).isFalse();
            assertThat(fullMatch).isTrue();
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("should persist UOM with UomStatus relationship")
        void shouldPersistWithStatusRelationship() {
            // Given
            Uom uom = Uom.builder()
                    .name("Kilogram")
                    .description("Base unit")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(activeStatus)
                    .build();

            // When
            Uom saved = repository.save(uom);
            entityManager.flush();
            entityManager.clear();

            // Then
            Uom found = repository.findById(saved.getId()).orElse(null);
            assertThat(found).isNotNull();
            assertThat(found.getUomStatus()).isNotNull();
            assertThat(found.getUomStatus().getId()).isEqualTo(activeStatus.getId());
            assertThat(found.getUomStatus().getName()).isEqualTo("Active");
        }

        @Test
        @DisplayName("should load UomStatus lazily")
        void shouldLoadStatusLazily() {
            // Given
            Uom uom = createAndPersistUom("Kilogram", "Base unit", new BigDecimal("1.000"), activeStatus);
            entityManager.flush();
            entityManager.clear();

            // When
            Uom found = repository.findById(uom.getId()).orElse(null);

            // Then
            assertThat(found).isNotNull();
            // Access the lazy-loaded relationship
            assertThat(found.getUomStatus()).isNotNull();
            assertThat(found.getUomStatus().getName()).isEqualTo("Active");
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("should save new UOM")
        void shouldSaveNewUom() {
            // Given
            Uom uom = Uom.builder()
                    .name("Meter")
                    .description("Length unit")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(activeStatus)
                    .build();

            // When
            Uom saved = repository.save(uom);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Meter");
        }

        @Test
        @DisplayName("should update existing UOM")
        void shouldUpdateExistingUom() {
            // Given
            Uom uom = createAndPersistUom("Kilogram", "Old description", new BigDecimal("1.000"), activeStatus);
            Long id = uom.getId();

            // When
            uom.setDescription("New description");
            uom.setConversionFactorToBase(new BigDecimal("1.500"));
            repository.save(uom);
            entityManager.flush();
            entityManager.clear();

            // Then
            Uom updated = repository.findById(id).orElse(null);
            assertThat(updated).isNotNull();
            assertThat(updated.getDescription()).isEqualTo("New description");
            assertThat(updated.getConversionFactorToBase()).isEqualByComparingTo(new BigDecimal("1.500"));
        }

        @Test
        @DisplayName("should delete UOM by ID")
        void shouldDeleteById() {
            // Given
            Uom uom = createAndPersistUom("Kilogram", "desc", new BigDecimal("1.000"), activeStatus);
            Long id = uom.getId();

            // When
            repository.deleteById(id);
            entityManager.flush();

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }
    }

    // Helper method
    private Uom createAndPersistUom(String name, String description, BigDecimal conversionFactor, UomStatus status) {
        Uom uom = Uom.builder()
                .name(name)
                .description(description)
                .conversionFactorToBase(conversionFactor)
                .uomStatus(status)
                .build();
        entityManager.persist(uom);
        entityManager.flush();
        return uom;
    }
}
