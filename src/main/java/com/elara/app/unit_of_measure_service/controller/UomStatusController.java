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
        UomStatusResponse response = service.save(request);
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
        UomStatusResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UomStatusResponse>> getAllUomStatuses(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<UomStatusResponse> response = service.findAll(pageable);
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
        Page<UomStatusResponse> response = service.findAllByName(name, pageable);
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
        Page<UomStatusResponse> response = service.findAllByIsUsable(isUsable, pageable);
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
        UomStatusResponse response = service.update(id, request);
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
        service.changeStatus(id, isUsable);
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
        Boolean isTaken = service.isNameTaken(name);
        return ResponseEntity.ok(isTaken);
    }

    // ---------------------------------------------------------------------
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUomStatus(
            @PathVariable @NotNull @Positive Long id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
