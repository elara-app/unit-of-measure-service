package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PutMapping("/{id}")
    public ResponseEntity<UomResponse> updateUom(@PathVariable Long id, @Valid @RequestBody UomUpdate update) {
        log.info("[updateUom] Request to update Uom id: {} with data: {}", id, update);
        UomResponse response = service.update(id, update);
        log.info("[updateUom] Uom updated: {}", response);
        return ResponseEntity.ok(response);
    }

}
