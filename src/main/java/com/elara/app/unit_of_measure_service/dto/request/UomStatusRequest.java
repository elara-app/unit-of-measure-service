package com.elara.app.unit_of_measure_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UomStatusRequest(

        @NotBlank(message = "Name cannot be blank")
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description,

        @NotNull(message = "Is usable cannot be null")
        Boolean isUsable

) {
}
