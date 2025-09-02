package com.elara.app.unit_of_measure_service.dto.update;

import jakarta.validation.constraints.Size;

public record UomStatusUpdate(

    @Size(max = 50)
    String name,

    @Size(max = 200)
    String description

    // Deliberately exclude isUsable to force use of changeStatus()

) {
}
