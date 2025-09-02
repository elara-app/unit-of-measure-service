package com.elara.app.unit_of_measure_service.dto.response;

public record UomStatusResponse(

        Long id,
        String name,
        String description,
        Boolean isUsable

) {
}
