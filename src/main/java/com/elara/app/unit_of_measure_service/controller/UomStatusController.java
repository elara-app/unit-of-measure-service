package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.config.ErrorResponse;
import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
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

/**
 * REST Controller for managing Unit of Measure Status (UomStatus) entities.
 *
 * <p>This controller provides comprehensive CRUD operations for UOM Status management,
 * including creation, retrieval, updating, deletion, and specialized operations like
 * status changes and name availability checks. All operations include proper validation,
 * error handling, and comprehensive API documentation.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Full CRUD operations with validation</li>
 *   <li>Pagination support for list operations</li>
 *   <li>Search and filtering capabilities</li>
 *   <li>Dedicated status change endpoint for data integrity</li>
 *   <li>Name availability validation</li>
 *   <li>Comprehensive error handling with proper HTTP status codes</li>
 * </ul>
 *
 * <p><b>Error Handling:</b></p>
 * <ul>
 *   <li>400 Bad Request - Invalid input data or validation errors</li>
 *   <li>404 Not Found - Resource not found</li>
 *   <li>409 Conflict - Resource already exists or conflicts with existing data</li>
 *   <li>500 Internal Server Error - Unexpected server errors</li>
 * </ul>
 *
 * <p><b>Service Layer Integration:</b></p>
 * <p>This controller integrates with the UomStatusService layer and handles the following exceptions:</p>
 * <ul>
 *   <li>ResourceNotFoundException (404) - When ID does not find entities</li>
 *   <li>ResourceConflictException (409) - When duplicate names or conflicts occur</li>
 *   <li>UnexpectedErrorException (500) - For database and unexpected errors</li>
 *   <li>ValidationException (400) - For Bean Validation errors</li>
 * </ul>
 *
 * @author Elara Development Team
 * @version 1.0
 * @since 2025-08-06
 */
@RestController
@RequestMapping(value = "/api/v1/uom-status", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "UOM Status Management",
    description = "Complete API for managing Unit of Measure Status entities. " +
        "Provides operations for creating, reading, updating, and deleting UOM status records, " +
        "along with specialized functionality for status management and validation."
)
public class UomStatusController {

    private final UomStatusService service;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new UOM Status entity.
     *
     * <p>This endpoint creates a new Unit of Measure Status with the provided information.
     * The name must be unique across all UOM statuses. The isUsable flag determines whether
     * this status can be actively used in the system.</p>
     *
     * <p><b>Validation Rules:</b></p>
     * <ul>
     *   <li>Name: Required, non-blank, maximum 50 characters</li>
     *   <li>Description: Optional, maximum 200 characters</li>
     *   <li>IsUsable: Required, boolean value</li>
     * </ul>
     *
     * @param request the UOM Status creation request
     * @return ResponseEntity containing the created UOM Status
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create new UOM Status",
        description = "Creates a new Unit of Measure Status entity with the provided information. " +
            "The name must be unique and will be validated before creation.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "UOM Status created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UomStatusResponse.class),
                examples = @ExampleObject(
                    name = "Created UOM Status",
                    summary = "Example of a successfully created UOM Status",
                    value = """
                        {
                            "id": 1,
                            "name": "Active",
                            "description": "Status for active unit of measures",
                            "isUsable": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Example of validation error response",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "Name is required and cannot be blank",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "UOM Status with the same name already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Resource Conflict",
                    summary = "Example of name conflict error",
                    value = """
                        {
                            "code": 1003,
                            "value": "RESOURCE_CONFLICT",
                            "message": "UomStatus already exists with name: Active",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Example of server error response",
                    value = """
                        {
                            "code": 1006,
                            "value": "UNEXPECTED_ERROR",
                            "message": "An unexpected error occurred while processing the request",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<UomStatusResponse> createUomStatus(
        @Parameter(
            description = "UOM Status creation request with name, description, and usability flag",
            required = true,
            schema = @Schema(implementation = UomStatusRequest.class)
        )
        @Valid @RequestBody UomStatusRequest request
    ) {
        log.info("[createUomStatus] Request to create UomStatus: {}", request);
        UomStatusResponse response = service.save(request);
        log.info("[createUomStatus] UomStatus created with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Retrieves a UOM Status by its unique identifier.
     *
     * <p>This endpoint fetches a specific Unit of Measure Status using its ID.
     * If no status exists with the provided ID, a 404 Not Found response is returned.</p>
     *
     * @param id the unique identifier of the UOM Status (must be positive)
     * @return ResponseEntity containing the requested UOM Status
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get UOM Status by ID",
        description = "Retrieves a specific Unit of Measure Status by its unique identifier. " +
            "Returns detailed information about the status including its usability state.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "UOM Status found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UomStatusResponse.class),
                examples = @ExampleObject(
                    name = "Found UOM Status",
                    summary = "Example of a successfully retrieved UOM Status",
                    value = """
                        {
                            "id": 1,
                            "name": "Active",
                            "description": "Status for active unit of measures",
                            "isUsable": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid ID format (must be a positive number)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid ID",
                    summary = "Example of invalid ID error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "ID must be a positive number",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/invalid"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "UOM Status not found with the specified ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "Example of resource not found error",
                    value = """
                        {
                            "code": 1004,
                            "value": "RESOURCE_NOT_FOUND",
                            "message": "UomStatus not found with id: 999",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/999"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UomStatusResponse> getUomStatusById(
        @Parameter(
            description = "The unique identifier of the UOM Status to retrieve",
            required = true,
            example = "1",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id
    ) {
        log.info("[getUomStatusById] Request to get UomStatus by id: {}", id);
        UomStatusResponse response = service.findById(id);
        log.info("[getUomStatusById] UomStatus found: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all UOM Status entities with pagination support.
     *
     * <p>This endpoint returns a paginated list of all Unit of Measure Status entities.
     * Results can be sorted and paginated using standard Spring Data parameters.</p>
     *
     * @param pageable pagination and sorting parameters
     * @return ResponseEntity containing a paginated list of UOM Status responses
     */
    @GetMapping
    @Operation(
        summary = "Get all UOM Statuses",
        description = "Retrieves all Unit of Measure Status entities with pagination and sorting support. " +
            "Default sorting is by name with 20 items per page.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "UOM Statuses retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Paginated UOM Statuses",
                    summary = "Example of paginated UOM Status list",
                    value = """
                        {
                            "content": [
                                {
                                    "id": 1,
                                    "name": "Active",
                                    "description": "Active status",
                                    "isUsable": true
                                },
                                {
                                    "id": 2,
                                    "name": "Inactive",
                                    "description": "Inactive status",
                                    "isUsable": false
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "sorted": true,
                                    "unsorted": false
                                },
                                "pageNumber": 0,
                                "pageSize": 20
                            },
                            "totalElements": 2,
                            "totalPages": 1,
                            "last": true,
                            "first": true,
                            "numberOfElements": 2
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Page<UomStatusResponse>> getAllUomStatuses(
        @Parameter(
            description = "Pagination and sorting parameters. Supports 'page', 'size', and 'sort' parameters.",
            example = "page=0&size=20&sort=name,asc"
        )
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[getAllUomStatuses] Request to get all UomStatuses. Pageable: {}", pageable);
        Page<UomStatusResponse> response = service.findAll(pageable);
        log.info("[getAllUomStatuses] Fetched {} UomStatuses", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for UOM Status entities by name with case-insensitive partial matching.
     *
     * <p>This endpoint searches for Unit of Measure Status entities where the name
     * contains the provided search term (case-insensitive). Results are paginated.</p>
     *
     * @param name     the name to search for (partial match supported)
     * @param pageable pagination and sorting parameters
     * @return ResponseEntity containing a paginated list of matching UOM Status responses
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search UOM Statuses by name",
        description = "Searches for Unit of Measure Status entities by name with case-insensitive partial matching. " +
            "Useful for implementing autocomplete or search functionality.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully (may return empty results)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Search Results",
                    summary = "Example of search results for 'act'",
                    value = """
                        {
                            "content": [
                                {
                                    "id": 1,
                                    "name": "Active",
                                    "description": "Active status",
                                    "isUsable": true
                                },
                                {
                                    "id": 3,
                                    "name": "Inactive",
                                    "description": "Inactive status",
                                    "isUsable": false
                                }
                            ],
                            "totalElements": 2,
                            "totalPages": 1,
                            "numberOfElements": 2
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters (name cannot be blank)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Search Parameter",
                    summary = "Example of blank name error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "Name parameter cannot be blank",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/search"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Page<UomStatusResponse>> searchUomStatusesByName(
        @Parameter(
            description = "The name to search for (case-insensitive partial matching)",
            required = true,
            example = "active",
            schema = @Schema(type = "string", minLength = 1)
        )
        @RequestParam @NotBlank(message = "validation.not.blank") String name,
        @Parameter(
            description = "Pagination and sorting parameters",
            example = "page=0&size=10&sort=name,asc"
        )
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[searchUomStatusesByName] Request to search UomStatuses by name: '{}'", name);
        Page<UomStatusResponse> response = service.findAllByName(name, pageable);
        log.info("[searchUomStatusesByName] Fetched {} UomStatuses for name: '{}'",
            response.getNumberOfElements(), name);
        return ResponseEntity.ok(response);
    }

    /**
     * Filters UOM Status entities by their usability status.
     *
     * <p>This endpoint retrieves Unit of Measure Status entities filtered by their
     * usability flag. Useful for getting only active or inactive statuses.</p>
     *
     * @param isUsable the usability status to filter by
     * @param pageable pagination and sorting parameters
     * @return ResponseEntity containing a paginated list of filtered UOM Status responses
     */
    @GetMapping("/filter")
    @Operation(
        summary = "Filter UOM Statuses by usability",
        description = "Retrieves Unit of Measure Status entities filtered by their usability status. " +
            "Use true to get only usable statuses, false for unusable ones.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Filtering completed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Filtered Results",
                    summary = "Example of filtered results for usable=true",
                    value = """
                        {
                            "content": [
                                {
                                    "id": 1,
                                    "name": "Active",
                                    "description": "Active status",
                                    "isUsable": true
                                },
                                {
                                    "id": 4,
                                    "name": "Enabled",
                                    "description": "Enabled status",
                                    "isUsable": true
                                }
                            ],
                            "totalElements": 2,
                            "numberOfElements": 2
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid filter parameters (isUsable cannot be null)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Filter Parameter",
                    summary = "Example of null isUsable error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "isUsable parameter is required and cannot be null",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/filter"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Page<UomStatusResponse>> filterUomStatusesByUsability(
        @Parameter(
            description = "The usability status to filter by (true for usable, false for unusable)",
            required = true,
            example = "true",
            schema = @Schema(type = "boolean")
        )
        @RequestParam @NotNull(message = "validation.not.null") Boolean isUsable,
        @Parameter(
            description = "Pagination and sorting parameters",
            example = "page=0&size=10&sort=name,desc"
        )
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[filterUomStatusesByUsability] Request to filter UomStatuses by isUsable: {}", isUsable);
        Page<UomStatusResponse> response = service.findAllByIsUsable(isUsable, pageable);
        log.info("[filterUomStatusesByUsability] Fetched {} UomStatuses for isUsable: {}",
            response.getNumberOfElements(), isUsable);
        return ResponseEntity.ok(response);
    }

    /**
     * Checks if a UOM Status name is already taken.
     *
     * <p>This endpoint validates name availability for UOM Status entities.
     * Useful for client-side validation and preventing duplicate names.</p>
     *
     * @param name the name to check for availability
     * @return ResponseEntity containing boolean indicating if the name is taken
     */
    @GetMapping("/check-name")
    @Operation(
        summary = "Check name availability",
        description = "Checks if a Unit of Measure Status name is already taken. " +
            "Returns true if the name exists, false if it's available for use.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Name availability check completed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(type = "boolean"),
                examples = {
                    @ExampleObject(
                        name = "Name Taken",
                        summary = "Example when name is already taken",
                        value = "true"
                    ),
                    @ExampleObject(
                        name = "Name Available",
                        summary = "Example when name is available",
                        value = "false"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid name parameter (cannot be blank)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Name Parameter",
                    summary = "Example of blank name error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "Name parameter cannot be blank",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/check-name"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Boolean> isNameTaken(
        @Parameter(
            description = "The name to check for availability (case-sensitive)",
            required = true,
            example = "Active",
            schema = @Schema(type = "string", minLength = 1, maxLength = 50)
        )
        @RequestParam @NotBlank(message = "validation.not.blank") String name
    ) {
        log.info("[isNameTaken] Request to check if name is taken: '{}'", name);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[isNameTaken] Name '{}' taken: {}", name, isTaken);
        return ResponseEntity.ok(isTaken);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing UOM Status entity (excluding usability status).
     *
     * <p>This endpoint updates an existing Unit of Measure Status with the provided
     * information. The usability status cannot be changed through this endpoint -
     * use the dedicated status change endpoint for that purpose.</p>
     *
     * <p><b>Note:</b> The isUsable field is intentionally excluded from the update DTO
     * to force the use of the dedicated status change endpoint for better audit trails.</p>
     *
     * @param id      the unique identifier of the UOM Status to update
     * @param request the update request containing the fields to modify
     * @return ResponseEntity containing the updated UOM Status
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update UOM Status",
        description = "Updates an existing Unit of Measure Status entity. Note: usability status " +
            "cannot be changed through this endpoint - use the status change endpoint instead.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "UOM Status updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UomStatusResponse.class),
                examples = @ExampleObject(
                    name = "Updated UOM Status",
                    summary = "Example of successfully updated UOM Status",
                    value = """
                        {
                            "id": 1,
                            "name": "Updated Active",
                            "description": "Updated description for active status",
                            "isUsable": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data, validation errors, or invalid ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Example of validation error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "Name length must be between 1 and 50 characters",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/1"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "UOM Status not found with the specified ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "Example of resource not found error",
                    value = """
                        {
                            "code": 1004,
                            "value": "RESOURCE_NOT_FOUND",
                            "message": "UomStatus not found with id: 999",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/999"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Name conflict with existing UOM Status",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Name Conflict",
                    summary = "Example of name conflict during update",
                    value = """
                        {
                            "code": 1003,
                            "value": "RESOURCE_CONFLICT",
                            "message": "UomStatus already exists with name: Active",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/1"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UomStatusResponse> updateUomStatus(
        @Parameter(
            description = "The unique identifier of the UOM Status to update",
            required = true,
            example = "1",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id,
        @Parameter(
            description = "Update request containing the fields to modify (excludes isUsable)",
            required = true,
            schema = @Schema(implementation = UomStatusUpdate.class)
        )
        @Valid @RequestBody UomStatusUpdate request
    ) {
        log.info("[updateUomStatus] Request to update UomStatus id: {} with data: {}", id, request);
        UomStatusResponse response = service.update(id, request);
        log.info("[updateUomStatus] UomStatus updated: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the usability status of a UOM Status entity.
     *
     * <p>This is a dedicated endpoint for status management to maintain data integrity
     * and provide better audit trails. It allows changing only the usability flag
     * of an existing UOM Status.</p>
     *
     * @param id       the unique identifier of the UOM Status
     * @param isUsable the new usability status
     * @return ResponseEntity with no content indicating a successful status change
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Change UOM Status usability",
        description = "Changes the usability status of a Unit of Measure Status entity. " +
            "This dedicated endpoint ensures better data integrity and audit trails for status changes.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Status changed successfully (no content returned)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters (ID must be positive, isUsable cannot be null)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Parameters",
                    summary = "Example of invalid parameter error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "isUsable parameter is required and cannot be null",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/1/status"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "UOM Status not found with the specified ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "Example of resource not found error",
                    value = """
                        {
                            "code": 1004,
                            "value": "RESOURCE_NOT_FOUND",
                            "message": "UomStatus not found with id: 999",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/999/status"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> changeUomStatusUsability(
        @Parameter(
            description = "The unique identifier of the UOM Status to update",
            required = true,
            example = "1",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id,
        @Parameter(
            description = "The new usability status (true for usable, false for unusable)",
            required = true,
            example = "false",
            schema = @Schema(type = "boolean")
        )
        @RequestParam @NotNull(message = "validation.not.null") Boolean isUsable
    ) {
        log.info("[changeUomStatusUsability] Request to change usability for UomStatus id: {} to: {}", id, isUsable);
        service.changeStatus(id, isUsable);
        log.info("[changeUomStatusUsability] Usability changed for UomStatus id: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Deletes a UOM Status entity by its ID.
     *
     * <p>This is a hard delete operation that permanently removes the UOM Status
     * from the system. Use with caution as this operation cannot be undone.</p>
     *
     * <p><b>Warning:</b> This operation may fail if the UOM Status is referenced
     * by other entities (foreign key constraint violations).</p>
     *
     * @param id the unique identifier of the UOM Status to delete
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete UOM Status",
        description = "Permanently deletes a Unit of Measure Status entity. This operation cannot be undone. " +
            "May fail if the status is referenced by other entities.",
        tags = {"UOM Status Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "UOM Status deleted successfully (no content returned)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid ID format (must be a positive number)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid ID",
                    summary = "Example of invalid ID error",
                    value = """
                        {
                            "code": 1002,
                            "value": "INVALID_DATA",
                            "message": "ID must be a positive number",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/invalid"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "UOM Status not found with the specified ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "Example of resource not found error",
                    value = """
                        {
                            "code": 1004,
                            "value": "RESOURCE_NOT_FOUND",
                            "message": "UomStatus not found with id: 999",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/999"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Cannot delete due to existing dependencies (foreign key constraints)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Constraint Violation",
                    summary = "Example of foreign key constraint error",
                    value = """
                        {
                            "code": 1006,
                            "value": "UNEXPECTED_ERROR",
                            "message": "Cannot delete UomStatus as it is referenced by other entities",
                            "timestamp": "2025-08-06T10:30:00",
                            "path": "/api/v1/uom-status/1"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> deleteUomStatus(
        @Parameter(
            description = "The unique identifier of the UOM Status to delete",
            required = true,
            example = "1",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable @NotNull @Positive Long id
    ) {
        log.info("[deleteUomStatus] Request to delete UomStatus id: {}", id);
        service.deleteById(id);
        log.info("[deleteUomStatus] UomStatus deleted: {}", id);
        return ResponseEntity.noContent().build();
    }
}
