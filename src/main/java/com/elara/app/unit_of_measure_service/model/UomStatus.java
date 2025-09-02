package com.elara.app.unit_of_measure_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Represents the status of a Unit of Measure (UOM) in the system.
 * <p>
 * This entity is mapped to the <b>uom_status</b> table in the database and holds information
 * about the status name, description, and whether the status is usable.
 * </p>
 *
 * <p>Best practices:</p>
 * <ul>
 *   <li>Use this class to manage and persist UOM status information.</li>
 *   <li>Do not modify the ID after creation; it is auto-generated.</li>
 *   <li>Validation constraints are applied to ensure data integrity.</li>
 * </ul>
 *
 * @author Elara Team
 * @since 1.0
 */
@Entity(name = "uom_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UomStatus {

    /**
     * Unique identifier for the UOM status.
     * <p>Auto-generated and not updatable after creation.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id", updatable = false)
    private Long id;

    /**
     * Name of the UOM status.
     * <p>Must be unique, not blank, and up to 50 characters.</p>
     */
    @NotBlank
    @Size(max = 50, message = "validation.size.max")
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    /**
     * Optional description of the UOM status (up to 200 characters).
     */
    @Size(max = 200, message = "validation.size.max")
    @Column(name = "description", length = 200)
    private String description;

    /**
     * Indicates if the status is usable.
     * <p>Defaults to {@code true} and cannot be null.</p>
     */
    @Builder.Default
    @NotNull
    @Column(name = "is_usable", nullable = false)
    private Boolean isUsable = true;

}
