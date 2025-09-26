package com.elara.app.unit_of_measure_service.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UomUpdate(

    @NotBlank
    @Size(max = 50)
    String name,

    @Size(max = 200)
    String description,

    @NotNull
    @Positive
    BigDecimal conversionFactorToBase

    // Deliberately exclude isUsable to force use of changeStatus()

) {
}
