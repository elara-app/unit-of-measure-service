package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class UomStatusMapperTest {

    private final UomStatusMapper mapper = UomStatusMapper.INSTANCE;

    @Test
    @DisplayName("toEntity() should map UomStatusRequest to UomStatus")
    void shouldMapUomStatusRequestToUomStatus() {
        UomStatusRequest request = new UomStatusRequest(
                "Active",
                "Status is active",
                true
        );
        UomStatus entity = mapper.toEntity(request);
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
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toResponse(null));
    }

}