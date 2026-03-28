package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.model.Uom;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UomMapperTest {

    @Autowired
    private UomMapper mapper;

    @Nested
    @DisplayName("toEntity() Tests - Request to Entity Mapping")
    class ToEntityTests {

        @Test
        @DisplayName("should map all fields from UomRequest to Uom entity")
        void shouldMapAllFieldsFromRequest() {
            // Given
            UomRequest request = new UomRequest(
                    "Kilogram",
                    "Base unit of mass in SI",
                    new BigDecimal("1.000"),
                    1L
            );

            // When
            Uom entity = mapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getName()).isEqualTo("Kilogram");
            assertThat(entity.getDescription()).isEqualTo("Base unit of mass in SI");
            assertThat(entity.getConversionFactorToBase()).isEqualByComparingTo(new BigDecimal("1.000"));
        }

        @Test
        @DisplayName("should handle null description")
        void shouldHandleNullDescription() {
            // Given
            UomRequest request = new UomRequest(
                    "Kilogram",
                    null,
                    new BigDecimal("1.000"),
                    1L
            );

            // When
            Uom entity = mapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getName()).isEqualTo("Kilogram");
            assertThat(entity.getDescription()).isNull();
        }

        @Test
        @DisplayName("should not map ID from request")
        void shouldNotMapIdFromRequest() {
            // Given
            UomRequest request = new UomRequest(
                    "Kilogram",
                    "desc",
                    new BigDecimal("1.000"),
                    1L
            );

            // When
            Uom entity = mapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull(); // ID should not be set from request
        }

        @Test
        @DisplayName("should not map UomStatus from request")
        void shouldNotMapStatusFromRequest() {
            // Given
            UomRequest request = new UomRequest(
                    "Kilogram",
                    "desc",
                    new BigDecimal("1.000"),
                    1L
            );

            // When
            Uom entity = mapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getUomStatus()).isNull(); // Status should be set separately in service
        }

        @Test
        @DisplayName("should handle null request")
        void shouldHandleNullRequest() {
            // When
            Uom entity = mapper.toEntity(null);

            // Then
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("should handle empty description")
        void shouldHandleEmptyDescription() {
            // Given
            UomRequest request = new UomRequest(
                    "Kilogram",
                    "",
                    new BigDecimal("1.000"),
                    1L
            );

            // When
            Uom entity = mapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getDescription()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toResponse() Tests - Entity to Response Mapping")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields from Uom entity to UomResponse")
        void shouldMapAllFieldsFromEntity() {
            // Given
            UomStatus status = UomStatus.builder()
                    .id(1L)
                    .name("Active")
                    .description("Active status")
                    .isUsable(true)
                    .build();

            Uom entity = Uom.builder()
                    .id(1L)
                    .name("Kilogram")
                    .description("Base unit of mass")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(status)
                    .build();

            // When
            UomResponse response = mapper.toResponse(entity);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Kilogram");
            assertThat(response.description()).isEqualTo("Base unit of mass");
            assertThat(response.conversionFactorToBase()).isEqualByComparingTo(new BigDecimal("1.000"));
        }

        @Test
        @DisplayName("should map nested UomStatus correctly")
        void shouldMapNestedUomStatusCorrectly() {
            // Given
            UomStatus status = UomStatus.builder()
                    .id(1L)
                    .name("Active")
                    .description("Active status")
                    .isUsable(true)
                    .build();

            Uom entity = Uom.builder()
                    .id(1L)
                    .name("Kilogram")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(status)
                    .build();

            // When
            UomResponse response = mapper.toResponse(entity);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.uomStatusId()).isNotNull();
            assertThat(response.uomStatusId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should handle null description")
        void shouldHandleNullDescription() {
            // Given
            Uom entity = Uom.builder()
                    .id(1L)
                    .name("Kilogram")
                    .description(null)
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            // When
            UomResponse response = mapper.toResponse(entity);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.description()).isNull();
        }

        @Test
        @DisplayName("should handle null UomStatus")
        void shouldHandleNullUomStatus() {
            // Given
            Uom entity = Uom.builder()
                    .id(1L)
                    .name("Kilogram")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(null)
                    .build();

            // When
            UomResponse response = mapper.toResponse(entity);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.uomStatusId()).isNull();
        }

        @Test
        @DisplayName("should handle null entity")
        void shouldHandleNullEntity() {
            // When
            UomResponse response = mapper.toResponse(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("should map entity without ID")
        void shouldMapEntityWithoutId() {
            // Given
            Uom entity = Uom.builder()
                    .name("Kilogram")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            // When
            UomResponse response = mapper.toResponse(entity);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isNull();
            assertThat(response.name()).isEqualTo("Kilogram");
        }
    }

    @Nested
    @DisplayName("updateEntityFromDto() Tests - Update DTO to Entity Mapping")
    class UpdateEntityTests {

        @Test
        @DisplayName("should update all fields from UomUpdate")
        void shouldUpdateAllFields() {
            // Given
            UomStatus status = UomStatus.builder().id(1L).name("Active").build();
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Old Name")
                    .description("Old Description")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(status)
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "New Name",
                    "New Description",
                    new BigDecimal("2.500")
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getDescription()).isEqualTo("New Description");
            assertThat(existing.getConversionFactorToBase()).isEqualByComparingTo(new BigDecimal("2.500"));
        }

        @Test
        @DisplayName("should not update ID")
        void shouldNotUpdateId() {
            // Given
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Old Name")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "New Name",
                    "New Description",
                    new BigDecimal("2.000")
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getId()).isEqualTo(1L); // ID should not change
        }

        @Test
        @DisplayName("should handle null description in update")
        void shouldHandleNullDescription() {
            // Given
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Old Name")
                    .description("Old Description")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "New Name",
                    null,
                    new BigDecimal("2.000")
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getDescription()).isNull();
            assertThat(existing.getConversionFactorToBase()).isEqualByComparingTo(new BigDecimal("2.000"));
        }

        @Test
        @DisplayName("should not update UomStatus")
        void shouldNotUpdateUomStatus() {
            // Given
            UomStatus originalStatus = UomStatus.builder().id(1L).name("Active").build();
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Old Name")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .uomStatus(originalStatus)
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "New Name",
                    "New Description",
                    new BigDecimal("2.000")
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getUomStatus()).isEqualTo(originalStatus); // Status should not change
            assertThat(existing.getUomStatus().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should handle null update DTO")
        void shouldHandleNullDto() {
            // Given
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Name")
                    .description("Description")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            String originalName = existing.getName();
            String originalDescription = existing.getDescription();
            BigDecimal originalFactor = existing.getConversionFactorToBase();

            // When
            mapper.updateEntityFromDto(existing, null);

            // Then - Nothing should change
            assertThat(existing.getName()).isEqualTo(originalName);
            assertThat(existing.getDescription()).isEqualTo(originalDescription);
            assertThat(existing.getConversionFactorToBase()).isEqualByComparingTo(originalFactor);
        }

        @Test
        @DisplayName("should update name to empty string if provided")
        void shouldUpdateNameToEmptyString() {
            // Given
            Uom existing = Uom.builder()
                    .id(1L)
                    .name("Old Name")
                    .description("desc")
                    .conversionFactorToBase(new BigDecimal("1.000"))
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "",
                    "desc",
                    new BigDecimal("1.000")
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getName()).isEmpty();
        }

        @Test
        @DisplayName("should preserve other fields when updating specific fields")
        void shouldPreserveOtherFields() {
            // Given
            UomStatus status = UomStatus.builder().id(1L).name("Active").build();
            Uom existing = Uom.builder()
                    .id(5L)
                    .name("Original Name")
                    .description("Original Description")
                    .conversionFactorToBase(new BigDecimal("3.500"))
                    .uomStatus(status)
                    .build();

            UomUpdate updateDto = new UomUpdate(
                    "Updated Name",
                    "Original Description", // Same as before
                    new BigDecimal("3.500") // Same as before
            );

            // When
            mapper.updateEntityFromDto(existing, updateDto);

            // Then
            assertThat(existing.getId()).isEqualTo(5L);
            assertThat(existing.getName()).isEqualTo("Updated Name");
            assertThat(existing.getDescription()).isEqualTo("Original Description");
            assertThat(existing.getConversionFactorToBase()).isEqualByComparingTo(new BigDecimal("3.500"));
            assertThat(existing.getUomStatus()).isEqualTo(status);
        }
    }
}
