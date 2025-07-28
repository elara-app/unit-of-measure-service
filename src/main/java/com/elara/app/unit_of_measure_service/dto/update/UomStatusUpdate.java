package com.elara.app.unit_of_measure_service.dto.update;

import jakarta.validation.constraints.Size;

public record UomStatusUpdate(

        @Size(max = 50, message = "validation.size.max")
        String name,

        @Size(max = 200, message = "validation.size.max")
        String description

) {
}
