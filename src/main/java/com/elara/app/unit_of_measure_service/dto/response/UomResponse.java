package com.elara.app.unit_of_measure_service.dto.response;

import java.math.BigDecimal;

public record UomResponse(

    Long id,
    String name,
    String description,
    BigDecimal conversionFactorToBase,
    Long uomStatusId

) {
}
