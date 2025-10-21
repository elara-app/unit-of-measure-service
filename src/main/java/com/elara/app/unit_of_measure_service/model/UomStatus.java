package com.elara.app.unit_of_measure_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity(name = "uom_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UomStatus {

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

    @Builder.Default
    @NotNull
    @Column(name = "is_usable", nullable = false)
    private Boolean isUsable = true;

}
