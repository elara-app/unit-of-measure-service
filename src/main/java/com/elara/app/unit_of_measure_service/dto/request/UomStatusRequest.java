package com.elara.app.unit_of_measure_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UomStatusRequest(

        @NotBlank
        @Size(max = 50, message = "validation.size.max")
        String name,

        @Size(max = 200, message = "validation.size.max")
        String description,

        @NotNull
        Boolean isUsable

) {
}
