package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
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
    private final MessageService messageService;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UomResponse> create(
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

    @GetMapping("/{id}")
    public ResponseEntity<UomResponse> getById(
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
    public ResponseEntity<Page<UomResponse>> getAll(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getAll";
        log.info("[{}] Request to retrieve all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAll(pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UomResponse>> getByNameContaining(
        @RequestParam @NotBlank String name,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getByNameContaining";
        log.info("[{}] Request to retrieve {} records with content in their name.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAllByName(name, pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/{uomStatusId}")
    public ResponseEntity<Page<UomResponse>> filterByStateId(
        @PathVariable @NotNull @Positive Long uomStatusId,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-filterUomStatusesByUsability";
        log.info("[{}] Request to filter all {} records by status id.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> response = service.findAllByUomStatusId(uomStatusId, pageable);
        log.info("[{}] {} records filtered.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> isNameTaken(
        @RequestParam @NotBlank() String name
    ) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Request to check if name is taken: '{}'", methodNomenclature, name);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[{}] Validation completed", methodNomenclature);
        return ResponseEntity.ok(isTaken);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    @PutMapping("/{id}")
    public ResponseEntity<UomResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UomUpdate update
    ) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Request to update {} record.", methodNomenclature, ENTITY_NAME);
        UomResponse response = service.update(id, update);
        String msg = messageService.getMessage("crud.update.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeStateId(
        @PathVariable @NotNull @Positive Long id,
        @RequestParam @NotNull @Positive Long newUomStatusId
    ) {
        final String methodNomenclature = NOMENCLATURE + "-changeStateId";
        log.info("[{}] Request to change status for {} record.", methodNomenclature, ENTITY_NAME);
        service.changeStatus(id, newUomStatusId);
        log.info("[{}] Usability changed for {} record.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
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
