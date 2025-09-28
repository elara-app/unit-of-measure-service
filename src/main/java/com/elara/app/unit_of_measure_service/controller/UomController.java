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
@RequestMapping(value = "/api/v1/uom", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Uom Management",
    description = ""
)
public class UomController {

    private final UomService service;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UomResponse> createUom(@Valid @RequestBody UomRequest request) {
        log.info("[Uom-controller-create] Request to create Uom: {}.", request);
        UomResponse response = service.save(request);
        System.out.println(response);
        log.info("[Uom-controller-service] Uom created with id: {}.", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    @GetMapping("/{id}")
    public ResponseEntity<UomResponse> getUomStatusById(
        @PathVariable @NotNull @Positive Long id
    ) {
        log.info("[Uom-controller-getUomById] Request to get Uom by id: {}", id);
        UomResponse response = service.findById(id);
        log.info("[Uom-controller-getUomById] Uom found: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UomResponse>> getAllUomStatuses(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[Uom-controller-getAll] Request to get all Uom.");
        Page<UomResponse> response = service.findAll(pageable);
        log.info("[Uom-controller-getAll] Fetched {} Uom.", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UomResponse>> searchUomStatusesByName(
        @RequestParam @NotBlank String name,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[Uom-controller-searchByName] Request to search Uoms by name: '{}'", name);
        Page<UomResponse> response = service.findAllByName(name, pageable);
        log.info("[Uom-controller-searchByName] Fetched {} Uoms for name: '{}'",
            response.getNumberOfElements(), name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/{uomStatusId}")
    public ResponseEntity<Page<UomResponse>> filterUomStatusesByUsability(
        @PathVariable @NotNull @Positive Long uomStatusId,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[Uom-controller-filterByUomStatusId] Request to filter all Uoms by status id: {}", uomStatusId);
        Page<UomResponse> response = service.findAllByUomStatusId(uomStatusId, pageable);
        log.info("[Uom-controller-filterByUomStatusId] Fetched {} Uoms for uom status id: {}",
            response.getNumberOfElements(), uomStatusId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> isNameTaken(
        @RequestParam @NotBlank() String name
    ) {
        log.info("[Uom-controller-isNameTaken] Request to check if name is taken: '{}'", name);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[Uom-controller-isNameTaken] Name '{}' taken: {}", name, isTaken);
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
        log.info("[Uom-controller-update] Request to update Uom id: {} with data: {}", id, update);
        UomResponse response = service.update(id, update);
        log.info("[Uom-controller-update] Uom updated: {}", response);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeUomStatusUsability(
        @PathVariable @NotNull @Positive Long id,
        @RequestParam @NotNull @Positive Long newUomStatusId
    ) {
        log.info("[Uom-controller-changeStatus] Request to change status for UOm with id: {} to: {}", id, newUomStatusId);
        service.changeStatus(id, newUomStatusId);
        log.info("[Uom-controller-changeStatus] Usability changed for Uom with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUomStatus(
        @PathVariable @NotNull @Positive Long id
    ) {
        log.info("[Uom-controller-deleteById] Request to delete Uom id: {}", id);
        service.deleteById(id);
        log.info("[Uom-controller-deleteById] Uom deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

}
