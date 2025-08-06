package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
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
@RequestMapping("/api/v1/uom-status")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UomStatusController {

    private final UomStatusService service;

    // ---------------------------------------------------------------------
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UomStatusResponse> createUomStatus(
            @Valid @RequestBody UomStatusRequest request
    ) {
        log.info("[createUomStatus] Request to create UomStatus: {}", request);
        UomStatusResponse response = service.save(request);
        log.info("[createUomStatus] UomStatus created with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------------------------------------------------------------
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UomStatusResponse> getUomStatusById(
            @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id
    ) {
        log.info("[getUomStatusById] Request to get UomStatus by id: {}", id);
        UomStatusResponse response = service.findById(id);
        log.info("[getUomStatusById] UomStatus found: {}", response);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UomStatusResponse>> getAllUomStatuses(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[getAllUomStatuses] Request to get all UomStatuses. Pageable: {}", pageable);
        Page<UomStatusResponse> response = service.findAll(pageable);
        log.info("[getAllUomStatuses] Fetched {} UomStatuses", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @GetMapping(
            value = "/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<UomStatusResponse>> searchUomStatusesByName(
            @RequestParam @NotBlank(message = "validation.not.blank") String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[searchUomStatusesByName] Request to search UomStatuses by name: '{}'", name);
        Page<UomStatusResponse> response = service.findAllByName(name, pageable);
        log.info("[searchUomStatusesByName] Fetched {} UomStatuses for name: '{}'", response.getNumberOfElements(), name);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @GetMapping(
            value = "/filter",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<UomStatusResponse>> filterUomStatusesByUsability(
            @RequestParam @NotNull(message = "validation.not.null") Boolean isUsable,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("[filterUomStatusesByUsability] Request to filter UomStatuses by isUsable: {}", isUsable);
        Page<UomStatusResponse> response = service.findAllByIsUsable(isUsable, pageable);
        log.info("[filterUomStatusesByUsability] Fetched {} UomStatuses for isUsable: {}", response.getNumberOfElements(), isUsable);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UomStatusResponse> updateUomStatus(
            @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id,
            @Valid @RequestBody UomStatusUpdate request
    ) {
        log.info("[updateUomStatus] Request to update UomStatus id: {} with data: {}", id, request);
        UomStatusResponse response = service.update(id, request);
        log.info("[updateUomStatus] UomStatus updated: {}", response);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @PatchMapping(
            value = "/{id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> changeUomStatusUsability(
            @PathVariable @NotNull(message = "validation.not.null") @Positive(message = "validation.positive") Long id,
            @RequestParam @NotNull(message = "validation.not.null") Boolean isUsable
    ) {
        log.info("[changeUomStatusUsability] Request to change usability for UomStatus id: {} to: {}", id, isUsable);
        service.changeStatus(id, isUsable);
        log.info("[changeUomStatusUsability] Usability changed for UomStatus id: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------------
    @GetMapping(
            value = "/check-name",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> isNameTaken(
            @RequestParam @NotBlank(message = "validation.not.blank") String name
    ) {
        log.info("[isNameTaken] Request to check if name is taken: '{}'", name);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[isNameTaken] Name '{}' taken: {}", name, isTaken);
        return ResponseEntity.ok(isTaken);
    }

    // ---------------------------------------------------------------------
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUomStatus(
            @PathVariable @NotNull @Positive Long id
    ) {
        log.info("[deleteUomStatus] Request to delete UomStatus id: {}", id);
        service.deleteById(id);
        log.info("[deleteUomStatus] UomStatus deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

}
