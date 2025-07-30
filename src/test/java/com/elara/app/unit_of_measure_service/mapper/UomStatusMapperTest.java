package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UomStatusMapperTest {

    @Autowired
    private UomStatusMapper mapper;

    @Test
    @DisplayName("toEntity() should map UomStatusRequest to UomStatus")
    void shouldMapUomStatusRequestToUomStatus() {
        UomStatusRequest request = new UomStatusRequest(
                "Active",
                "Status is active",
                true
        );
        UomStatus entity = mapper.updateEntityFromDto(request);
        assertNotNull(entity);
        assertEquals("Active", entity.getName());
        assertEquals("Status is active", entity.getDescription());
        assertTrue(entity.getIsUsable());
    }

    @Test
    @DisplayName("toResponse() should map UomStatus to UomStatusResponse")
    void shouldMapUomStatusToUomStatusResponse() {
        UomStatus entity = UomStatus.builder()
                .name("Active")
                .description("Status is active")
                .isUsable(true)
                .build();
        UomStatusResponse response = mapper.toResponse(entity);
        assertNotNull(response);
        assertNull(response.id());
        assertEquals("Active", response.name());
        assertEquals("Status is active", response.description());
        assertTrue(response.isUsable());
    }

    @Test
    @DisplayName("toResponse() should handle null entity gracefully")
    void shouldHandleNullEntityGracefully() {
        assertNull(mapper.updateEntityFromDto(null));
        assertNull(mapper.toResponse(null));
    }

    @Test
    @DisplayName("updateEntityFromDto() should update fields except id and isUsable")
    void updateEntityFromDto_shouldUpdateFieldsExceptIdAndIsUsable() {
        UomStatus existing = UomStatus.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Description")
                .isUsable(true)
                .build();
        UomStatusUpdate update = new UomStatusUpdate("New Name", "New Description");

        mapper.updateEntityFromDto(existing, update);

        assertEquals(1L, existing.getId()); // id should not change
        assertEquals("New Name", existing.getName());
        assertEquals("New Description", existing.getDescription());
        assertTrue(existing.getIsUsable()); // isUsable should not change
    }

    @Test
    @DisplayName("updateEntityFromDto() should do nothing if update is null")
    void updateEntityFromDto_shouldDoNothingIfUpdateIsNull() {
        UomStatus existing = UomStatus.builder()
                .id(2L)
                .name("Name")
                .description("Description")
                .isUsable(false)
                .build();
        mapper.updateEntityFromDto(existing, null);
        assertEquals("Name", existing.getName());
        assertEquals("Description", existing.getDescription());
        assertFalse(existing.getIsUsable());
    }

}