package com.elara.app.unit_of_measure_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "uom")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Uom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id", updatable = false)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    @NotNull
    @Positive
    @Column(name = "conversion_factor_to_base", nullable = false, precision = 10, scale = 3)
    private BigDecimal conversionFactorToBase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_status_id")
    private UomStatus uomStatus;

}
