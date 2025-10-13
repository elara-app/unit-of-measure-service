package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
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
    description = ""
)
public class UomController {

    private static final String ENTITY_NAME = "Uom";
    private static final String NOMENCLATURE = "Uom-controller";
    private final UomService service;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UomResponse> createUom(
        @Valid @RequestBody UomRequest request
    ) {
        final String methodNomenclature = NOMENCLATURE + "-create";
        log.info("[{}] Request to create a new {} record.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.save(request);
        log.info("[{}] {} record created with id: {}.", methodNomenclature, ENTITY_NAME, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    @GetMapping("/{id}")
    public ResponseEntity<UomResponse> getUomStatusById(
        @PathVariable @NotNull @Positive Long id
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getUomStatusById";
        log.info("[{}] Request to get Uom by id: {}", methodNomenclature, id);
        UomResponse response = service.findById(id);
        log.info("[{}] Uom found: {}", methodNomenclature, response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UomResponse>> getAllUomStatuses(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getAllUomStatuses";
        log.info("[{}] Request to get all Uom.", methodNomenclature);
        Page<UomResponse> response = service.findAll(pageable);
        log.info("[{}] Fetched {} Uom.", methodNomenclature, response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UomResponse>> searchUomStatusesByName(
        @RequestParam @NotBlank String name,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-searchUomStatusesByName";
        log.info("[{}] Request to search Uoms by name: '{}'", methodNomenclature, name);
        Page<UomResponse> response = service.findAllByName(name, pageable);
        log.info("[{}] Fetched {} Uoms for name: '{}'", methodNomenclature,
            response.getNumberOfElements(), name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/{uomStatusId}")
    public ResponseEntity<Page<UomResponse>> filterUomStatusesByUsability(
        @PathVariable @NotNull @Positive Long uomStatusId,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-filterUomStatusesByUsability";
        log.info("[{}] Request to filter all Uoms by status id: {}", methodNomenclature, uomStatusId);
        Page<UomResponse> response = service.findAllByUomStatusId(uomStatusId, pageable);
        log.info("[{}] Fetched {} Uoms for uom status id: {}", methodNomenclature,
            response.getNumberOfElements(), uomStatusId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> isNameTaken(
        @RequestParam @NotBlank() String name
    ) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Request to check if name is taken: '{}'", methodNomenclature, name);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[{}] Name '{}' taken: {}", methodNomenclature, name, isTaken);
        return ResponseEntity.ok(isTaken);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    @PutMapping("/{id}")
    public ResponseEntity<UomResponse> updateUom(
        @PathVariable Long id,
        @Valid @RequestBody UomUpdate update
    ) {
        final String methodNomenclature = NOMENCLATURE + "-updateUom";
        log.info("[{}] Request to update Uom id: {} with data: {}", methodNomenclature, id, update);
        UomResponse response = service.update(id, update);
        log.info("[{}] Uom updated: {}", methodNomenclature, response);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeUomStatusUsability(
        @PathVariable @NotNull @Positive Long id,
        @RequestParam @NotNull @Positive Long newUomStatusId
    ) {
        final String methodNomenclature = NOMENCLATURE + "-changeUomStatusUsability";
        log.info("[{}] Request to change status for UOm with id: {} to: {}", methodNomenclature, id, newUomStatusId);
        service.changeStatus(id, newUomStatusId);
        log.info("[{}] Usability changed for Uom with id: {}", methodNomenclature, id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUomStatus(
        @PathVariable @NotNull @Positive Long id
    ) {
        final String methodNomenclature = NOMENCLATURE + "-deleteUomStatus";
        log.info("[{}] Request to delete Uom id: {}", methodNomenclature, id);
        service.deleteById(id);
        log.info("[{}] Uom deleted: {}", methodNomenclature, id);
        return ResponseEntity.noContent().build();
    }

}
