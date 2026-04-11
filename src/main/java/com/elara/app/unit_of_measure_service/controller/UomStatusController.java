package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
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
@RequestMapping(value = "status/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "UOM Status Management",
    description = "Complete API for managing Unit of Measure Status entities. Provides full CRUD operations, " +
                  "pagination, search, filtering, and status lifecycle management with dedicated endpoints for " +
                  "ensuring audit integrity and data consistency."
)
public class UomStatusController {

    private static final String ENTITY_NAME = "UomStatus";
    private static final String NOMENCLATURE = ENTITY_NAME + "-controller";
    private final UomStatusService service;
    private final MessageService messageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new UOM Status", 
        description = """
                Creates a new Unit of Measure Status record.
                
                **Validation Rules:**
                - `name`: Required, 1-50 characters, must be unique
                - `description`: Optional, max 200 characters
                - `isUsable`: Required boolean field
                
                **Request Body:** `UomStatusRequest`""")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created successfully - Returns the newly created status",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusCreated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed (missing fields, name too long, etc.)", 
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Validation Error", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "409", description = "Conflict - A status with this name already exists",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Name Conflict", ref = "#/components/examples/ErrorConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected server error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomStatusResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                description = "Payload for creating a UOM status",
                content = @Content(
                    schema = @Schema(ref = "#/components/schemas/UomStatusRequest"),
                    examples = @ExampleObject(
                        name = "Create Request",
                        value = "{\"name\":\"Active\",\"description\":\"Unit of measure is currently active and can be used in transactions\",\"isUsable\":true}"
                    )
                )
            )
            @Valid @RequestBody UomStatusRequest request) {
        final String methodNomenclature = NOMENCLATURE + "-create";
        log.info("[{}] Request to create a new {} record.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.save(request);
        String msg = messageService.getMessage("crud.save.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get UOM Status by ID", 
        description = """
                Retrieves a specific Unit of Measure Status record by its unique identifier.
                
                **Parameters:**
                - `id`: Status identifier (positive integer)""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Found successfully - Returns the status details",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusCreated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid ID format or invalid parameters",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid ID", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "404", description = "Not Found - Status with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorNotFound"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomStatusResponse> getById(
            @Parameter(description = "UOM Status ID", example = "1", required = true)
            @PathVariable @NotNull @Positive Long id) {
        final String methodNomenclature = NOMENCLATURE + "-getById";
        log.info("[{}] Request to retrieve {} record by id.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.findById(id);
        String msg = messageService.getMessage("crud.retrieve.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all UOM Statuses", 
        description = """
                Retrieves all Unit of Measure Status records with pagination support.
                
                **Pagination Parameters:**
                - `page`: Page number (0-indexed, default: 0)
                - `size`: Page size (default: 20)
                - `sort`: Sort criteria (e.g., 'name', 'id,desc')""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Retrieved successfully - Returns paginated list of statuses",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusPage"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomStatusResponse>> getAll(
            @Parameter(description = "Pagination parameters: page, size, sort")
            @PageableDefault(size = 20) Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-getAll";
        log.info("[{}] Request to retrieve all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAll(pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("search")
    @Operation(summary = "Search UOM Statuses by name", 
        description = """
                Searches for Unit of Measure Status records using case-insensitive partial name matching.
                
                **Search Features:**
                - Case-insensitive matching
                - Partial string matching (e.g., 'act' finds 'Active')
                - Results support pagination and sorting
                
                **Example:** `/status/search?name=act&page=0&size=20`""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully - Returns matching statuses",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusPage"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Name parameter cannot be blank",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Missing Name", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomStatusResponse>> getByNameContaining(
            @Parameter(description = "Search term (case-insensitive)", example = "active", required = true)
            @RequestParam @NotBlank String name,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-getByNameContaining";
        log.info("[{}] Request to retrieve {} records with content in their name.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAllByName(name, pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("filter")
    @Operation(summary = "Filter UOM Statuses by usability", 
        description = """
                Filters Unit of Measure Status records by their usability status.
                
                **Filter Options:**
                - `isUsable=true`: Returns only usable statuses
                - `isUsable=false`: Returns only non-usable statuses
                - Results support pagination and sorting
                
                **Example:** `/status/filter?isUsable=true&page=0&size=20`""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Filtering completed successfully - Returns filtered statuses",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusPageResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusPage"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - isUsable parameter is required",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Missing Parameter", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Page<UomStatusResponse>> filterByIsUsable(
            @Parameter(description = "Filter by usability status", example = "true", required = true)
            @RequestParam @NotNull Boolean isUsable,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-filterByIsUsable";
        log.info("[{}] Request to filter all {} records by usability.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAllByIsUsable(isUsable, pageable);
        log.info("[{}] {} records filtered.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("check-name")
    @Operation(summary = "Check name availability", 
        description = """
                Validates whether a name is already in use by another status.
                
                **Response Values:**
                - `true`: Name is already taken (not available)
                - `false`: Name is available and can be used
                
                **Example:** `/status/check-name?name=Active`""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed successfully - Returns boolean (true=taken, false=available)",
            content = @Content(schema = @Schema(type = "boolean"),
                examples = {
                    @ExampleObject(name = "Name Taken", value = "true"),
                    @ExampleObject(name = "Name Available", value = "false")
                })),
        @ApiResponse(responseCode = "400", description = "Bad Request - Name parameter cannot be blank",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Missing Name", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Boolean> isNameTaken(
            @Parameter(description = "Name to check for availability", example = "Active", required = true)
            @RequestParam @NotBlank String name) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Request to check if name is taken.", methodNomenclature);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[{}] Validation completed.", methodNomenclature);
        return ResponseEntity.ok(isTaken);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update UOM Status", 
        description = """
                Updates an existing UOM Status record.
                
                **Important:** The `isUsable` field cannot be changed using this endpoint. Use the dedicated `/status/{id}/change-usability` endpoint to modify the usability status.
                
                **Updatable Fields:**
                - `name`: New status name (1-50 chars, optional)
                - `description`: New description (max 200 chars, optional)
                
                **Request Body:** `UomStatusUpdate`""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated successfully - Returns the updated status",
            content = @Content(schema = @Schema(ref = "#/components/schemas/UomStatusResponse"),
                examples = @ExampleObject(name = "Success", ref = "#/components/examples/UomStatusUpdated"))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or validation errors",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Validation Error", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "404", description = "Not Found - Status with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorNotFound"))),
        @ApiResponse(responseCode = "409", description = "Conflict - New name already exists",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Name Conflict", ref = "#/components/examples/ErrorConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<UomStatusResponse> update(
            @Parameter(description = "UOM Status ID", example = "1", required = true)
            @PathVariable @NotNull @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                description = "Payload for updating a UOM status (isUsable is not allowed here)",
                content = @Content(
                    schema = @Schema(ref = "#/components/schemas/UomStatusUpdate"),
                    examples = @ExampleObject(
                        name = "Update Request",
                        value = "{\"name\":\"Inactive\",\"description\":\"Unit of measure has been marked as inactive and is no longer used in transactions\"}"
                    )
                )
            )
            @Valid @RequestBody UomStatusUpdate request) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Request to update {} record.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.update(id, request);
        String msg = messageService.getMessage("crud.update.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{id}/change-usability")
    @Operation(summary = "Change UOM Status usability", 
        description = """
                Dedicated endpoint to change only the `isUsable` flag for a status.
                
                **Purpose:** This separate endpoint ensures audit integrity by dedicating status changes to their own operation, distinct from regular updates.
                
                **Parameters:**
                - `id`: Status identifier (positive integer)
                - `isUsable`: New usability status (required boolean)
                
                **Example:** `PATCH /status/1/change-usability/?isUsable=false`""")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status changed successfully - No content returned"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid ID format or missing isUsable parameter",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid Parameters", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "404", description = "Not Found - Status with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorNotFound"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Void> changeUsability(
            @Parameter(description = "UOM Status ID", example = "1", required = true)
            @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "New usability status", example = "false", required = true)
            @RequestParam @NotNull Boolean isUsable) {
        final String methodNomenclature = NOMENCLATURE + "-changeUsability";
        log.info("[{}] Request to change status for {} record.", methodNomenclature, ENTITY_NAME);
        service.changeStatus(id, isUsable);
        log.info("[{}] Usability changed for {} record.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete UOM Status", 
        description = """
                Permanently deletes a UOM Status record.
                
                **Warning:** This operation cannot be undone. Deletion may fail if the status is referenced by other entities.
                
                **Parameters:**
                - `id`: Status identifier (positive integer)""")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted successfully - No content returned"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid ID format",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Invalid ID", ref = "#/components/examples/ErrorBadRequest"))),
        @ApiResponse(responseCode = "404", description = "Not Found - Status with given ID does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Not Found", ref = "#/components/examples/ErrorNotFound"))),
        @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete due to dependencies or foreign key constraints",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Has Dependencies", ref = "#/components/examples/ErrorDeleteConflict"))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(name = "Server Error", ref = "#/components/examples/ErrorServer")))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "UOM Status ID", example = "1", required = true)
            @PathVariable @NotNull @Positive Long id) {
        final String methodNomenclature = NOMENCLATURE + "-delete";
        log.info("[{}] Request to delete a {} record.", methodNomenclature, ENTITY_NAME);
        service.deleteById(id);
        String msg = messageService.getMessage("crud.delete.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.noContent().build();
    }
}
