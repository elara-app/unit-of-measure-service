package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
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
@RequestMapping(value = "states", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
public class UomStatusController {

    private static final String ENTITY_NAME = "State";
    private static final String NOMENCLATURE = ENTITY_NAME + "-controller";
    private final UomStatusService service;
    private final MessageService messageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UomStatusResponse> create(
        @Valid @RequestBody UomStatusRequest request
    ) {
        final String methodNomenclature = NOMENCLATURE + "-create";
        log.info("[{}] Request to create a new {} record.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.save(request);
        String msg = messageService.getMessage("crud.save.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UomStatusResponse> getById(
        @PathVariable @NotNull @Positive Long id
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getById";
        log.info("[{}] Request to retrieve {} record by id.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.findById(id);
        String msg = messageService.getMessage("crud.retrieve.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UomStatusResponse>> getAll(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getAll";
        log.info("[{}] Request to retrieve all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAll(pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UomStatusResponse>> getByNameContaining(
        @RequestParam @NotBlank String name,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-getByNameContaining";
        log.info("[{}] Request to retrieve {} records with content in their name.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAllByName(name, pageable);
        log.info("[{}] {} records retrieved.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<UomStatusResponse>> filterByIsUsable(
        @RequestParam @NotNull Boolean isUsable,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-filterByIsUsable";
        log.info("[{}] Request to filter all {} records by usability.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> response = service.findAllByIsUsable(isUsable, pageable);
        log.info("[{}] {} records filtered.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> isNameTaken(
        @RequestParam @NotBlank() String name
    ) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Request to check if name is taken.", methodNomenclature);
        Boolean isTaken = service.isNameTaken(name);
        log.info("[{}] Validation completed.", methodNomenclature);
        return ResponseEntity.ok(isTaken);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UomStatusResponse> update(
        @PathVariable @NotNull @Positive Long id,
        @Valid @RequestBody UomStatusUpdate request
    ) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Request to update {} record.", methodNomenclature, ENTITY_NAME);
        UomStatusResponse response = service.update(id, request);
        String msg = messageService.getMessage("crud.update.success", ENTITY_NAME);
        log.info("[{}] {}", methodNomenclature, msg);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeUsability(
        @PathVariable @NotNull @Positive Long id,
        @RequestParam @NotNull Boolean isUsable
    ) {
        final String methodNomenclature = NOMENCLATURE + "-changeUsability";
        log.info("[{}] Request to change status for {} record.", methodNomenclature, ENTITY_NAME);
        service.changeStatus(id, isUsable);
        log.info("[{}] Usability changed for {} record.", methodNomenclature, ENTITY_NAME);
        return ResponseEntity.noContent().build();
    }

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
