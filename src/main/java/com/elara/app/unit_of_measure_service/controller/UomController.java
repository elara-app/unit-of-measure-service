package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Uom Management",
    description = "Complete API for managing Units of Measure. Includes CRUD operations, pagination, search, " +
                  "filtering by status, uniqueness validation, and dedicated status-change operation."
)
public class UomController {

    private static final String ENTITY_NAME = "Uom";
    private static final String NOMENCLATURE = ENTITY_NAME + "-controller";
    private final UomService service;
    private final MessageService messageService;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new UOM", description = """
            Creates a new Unit of Measure record.
            
            **Validation Rules:**
            - `name`: Required, 1-50 characters, must be unique
            - `description`: Optional, max 200 characters
            - `conversionFactorToBase`: Required, positive number
            - `uomStatusId`: Required, positive ID of an existing UOM status""")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created successfully - Returns the newly created unit",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomCreated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Validation Error", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "404", description = "Not Found - Referenced UOM status does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Status Not Found", ref = "#/components/examples/ErrorUomStatusNotFound"))),
        @ApiResponse(responseCode = "409", description = "Conflict - Unit with same name already exists",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Name Conflict", ref = "#/components/examples/ErrorUomConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Payload for creating a UOM",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomRequest"),
                examples = @ExampleObject(name = "Create Request",
                    value = "{\"name\":\"Kilogram\",\"description\":\"Base unit of mass in SI\",\"conversionFactorToBase\":1.000,\"uomStatusId\":1}"))
        )
        @Valid @RequestBody UomRequest request
    ) {
        final String methodNomenclature = NOMENCLATURE + "-create";
        log.info("[{}] Request to create a new {} record.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.save(request);
        String msg = messageService.getMessage("crud.save.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    @GetMapping("{id}")
    @Operation(summary = "Get UOM by ID", description = "Retrieves a specific Unit of Measure by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Found successfully",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomCreated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid ID",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid ID", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "404", description = "Not Found - UOM with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorUomNotFound"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomResponse> getById(
        @Parameter(description = "UOM ID", example = "1", required = true)
        @PathVariable @NotNull @Positive Long id
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getById";
        log.info("[{}] Request to retrieve {} record by id.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.findById(id);
        String msg = messageService.getMessage("crud.retrieve.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all UOMs", description = "Retrieves all Units of Measure with pagination and sorting support.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Retrieved successfully - Returns paginated list",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomPage"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomResponse>> getAll(
        @Parameter(description = "Pagination parameters: page, size, sort")
        @PageableDefault(size = 20) Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getAll";
        log.info("[{}] Request to retrieve all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAll(pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("search")
    @Operation(summary = "Search UOMs by name", description = "Case-insensitive partial name search with pagination support.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomPage"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Name parameter cannot be blank",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Missing Name", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomResponse>> getByNameContaining(
        @Parameter(description = "Search term", example = "gram", required = true)
        @RequestParam @NotBlank String name,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20) Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getByNameContaining";
        log.info("[{}] Request to retrieve {} records with content in their name.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAllByName(name, pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("filter/status/{uomStatusId}")
    @Operation(summary = "Filter UOMs by status ID", description = "Filters Units of Measure by `uomStatusId` with pagination and sorting support.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Filtering completed successfully",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomPage"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid status ID",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid Parameter", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomResponse>> filterByStateId(
        @Parameter(description = "Status ID to filter by", example = "1", required = true)
        @PathVariable @NotNull @Positive Long uomStatusId,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20) Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-filterUomStatusesByUsability";
        log.info("[{}] Request to filter all {} records by status id.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAllByUomStatusId(uomStatusId, pageable);
        log.info("[{}] {} records filtered.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("check-name")
    @Operation(summary = "Check UOM name availability", description = "Returns whether a UOM name is already taken. `true` means taken, `false` means available.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed successfully",
            content = @Content(schema = @Schema(type = "boolean"), examples = {
                @ExampleObject(name = "Name Taken", value = "true"),
                @ExampleObject(name = "Name Available", value = "false")
            })),
        @ApiResponse(responseCode = "400", description = "Bad Request - Name parameter cannot be blank",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Missing Name", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Boolean> isNameTaken(
        @Parameter(description = "Name to validate", example = "Kilogram", required = true)
        @RequestParam @NotBlank String name
    ) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Request to check if name is taken.", methodNomenclature);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[{}] Validation completed.", methodNomenclature);
        return ResponseEntity.ok(isTaken);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    @PutMapping("{id}")
    @Operation(summary = "Update UOM", description = """
            Updates an existing Unit of Measure.
            
            **Important:** `uomStatusId` is not updatable in this endpoint. Use `PATCH /{id}/change-state` to change status.""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated successfully - Returns updated unit",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomUpdated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Validation Error", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "404", description = "Not Found - UOM with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorUomNotFound"))),
        @ApiResponse(responseCode = "409", description = "Conflict - New name already exists",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Name Conflict", ref = "#/components/examples/ErrorUomConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomResponse> update(
        @Parameter(description = "UOM ID", example = "1", required = true)
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Payload for updating UOM core data",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomUpdate"),
                examples = @ExampleObject(name = "Update Request",
                    value = "{\"name\":\"Gram\",\"description\":\"Derived mass unit equal to one thousandth of a kilogram\",\"conversionFactorToBase\":0.001}"))
        )
        @Valid @RequestBody UomUpdate update
    ) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Request to update {} record.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.update(id, update);
        String msg = messageService.getMessage("crud.update.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{id}/status/{newUomStatusId}")
    @Operation(summary = "Change UOM status", description = "Changes only the status association (`uomStatusId`) of a UOM record.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status changed successfully - No content returned"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid IDs or missing parameter",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid Parameter", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "404", description = "Not Found - UOM or target status not found",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Related Resource Not Found", ref = "#/components/examples/ErrorUomStatusNotFound"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomResponse> changeStateId(
        @Parameter(description = "UOM ID", example = "1", required = true)
        @PathVariable @NotNull @Positive Long id,
        @Parameter(description = "New target status ID", example = "2", required = true)
        @PathVariable @NotNull @Positive Long newUomStatusId
    ) {
        final String methodNomenclature = NOMENCLATURE + "-changeStateId";
        log.info("[{}] Request to change status for {} record.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.changeStatus(id, newUomStatusId);
        log.info("[{}] Usability changed for {} record.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    @DeleteMapping("{id}")
    @Operation(summary = "Delete UOM", description = "Permanently deletes a Unit of Measure record.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted successfully - No content returned"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid ID",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid ID", ref = "#/components/examples/ErrorBadRequestUom"))),
        @ApiResponse(responseCode = "404", description = "Not Found - UOM with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorUomNotFound"))),
        @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete due to integrity constraints",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Has Dependencies", ref = "#/components/examples/ErrorDeleteConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "UOM ID", example = "1", required = true)
        @PathVariable @NotNull @Positive Long id
    ) {
        final String methodNomenclature = NOMENCLATURE + "-delete";
        log.info("[{}] Request to delete a {} record.", methodNomenclature, ENTITY_NAME);
        service.deleteById(id);
        String msg = messageService.getMessage("crud.delete.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.noContent().build();
    }

}
