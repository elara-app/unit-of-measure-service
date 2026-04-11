package com.elara.app.unit_of_measure_service.service.implementation;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.mapper.UomMapper;
import com.elara.app.unit_of_measure_service.model.Uom;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import com.elara.app.unit_of_measure_service.repository.UomRepository;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UomServiceImp implements UomService {

    private static final String ENTITY_NAME = "Uom";
    private static final String NOMENCLATURE = ENTITY_NAME + "-service";
    private final UomRepository repository;
    private final UomMapper mapper;
    private final MessageService messageService;
    private final UomStatusService statusService;

    @Override
    @Transactional
    public UomResponse save(UomRequest request) {
        final String methodNomenclature = NOMENCLATURE + "-save";
        log.info("[{}] {} record to save: {}", methodNomenclature, ENTITY_NAME, request);
        try {
            if (isNameTaken(Objects.requireNonNull(request).name())) {
                String alreadyExistsMsg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, alreadyExistsMsg);
                throw new ResourceConflictException(alreadyExistsMsg);
            }
            Uom entity = mapper.toEntity(request);
            UomStatus status = statusService.findEntityById(request.uomStatusId());
            entity.setUomStatus(status); // This can be avoided using @Context in the mapper and receive the request and UomStatus as parameters
            Uom saved = repository.save(entity);
            log.info("[{}] {} record created with id: {}.", methodNomenclature, ENTITY_NAME, saved.getId());
            return mapper.toResponse(saved);
        } catch (ResourceConflictException | ResourceNotFoundException e) {
            String saveErrorMsg = messageService.getMessage("crud.save.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, saveErrorMsg);
            throw e;
        }
    }

    @Override
    @Transactional
    public UomResponse update(Long id, UomUpdate request) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Update {} record with id: {} and request: {}", methodNomenclature, ENTITY_NAME, id, request);
        try {
            Uom existing = repository.findById(id)
                .orElseThrow(() -> {
                    String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[{}] {}", methodNomenclature, msg);
                    return new ResourceNotFoundException(msg);
                });
            if (!existing.getName().equals(request.name()) && isNameTaken(request.name())) {
                String alreadyExistsMsg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, alreadyExistsMsg);
                throw new ResourceConflictException(alreadyExistsMsg);
            }
            UomStatus status = existing.getUomStatus();
            mapper.updateEntityFromDto(existing, request);
            existing.setUomStatus(status);
            log.info("[{}] {} record updated with data: {}", methodNomenclature, ENTITY_NAME, existing);
            return mapper.toResponse(existing);
        } catch (ResourceNotFoundException | ResourceConflictException e) {
            String updateErrorMsg = messageService.getMessage("crud.update.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, updateErrorMsg);
            throw e;
        }
    }

    @Override
    public void deleteById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-deleteById";
        log.info("[{}] Delete {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        try {
            if (!repository.existsById(id)) {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceNotFoundException(msg);
            }
            repository.deleteById(id);
            log.info("[{}] {} record with id: {}, deleted.", methodNomenclature, ENTITY_NAME, id);
        } catch (ResourceNotFoundException e) {
            String deleteErrorMsg = messageService.getMessage("crud.delete.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, deleteErrorMsg);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UomResponse findById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findById";
        log.info("[{}] Fetch {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        try {
            Optional<Uom> entity = repository.findById(id);
            if (entity.isEmpty()) {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceNotFoundException(msg);
            }
            log.info("[{}] Fetched {} record with id: {}: {}", methodNomenclature, ENTITY_NAME, id, entity.get());
            return mapper.toResponse(entity.get());
        } catch (ResourceNotFoundException e) {
            String retrieveErrorMsg = messageService.getMessage("crud.retrieve.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, retrieveErrorMsg);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UomResponse> findAll(Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAll";
        log.info("[{}] Fetch all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME);
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UomResponse> findAllByName(String name, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByName";
        log.info("[{}] Fetch all {} records that contain in their name: '{}'", methodNomenclature, ENTITY_NAME, name);
        Page<UomResponse> page = repository.findAllByNameContainingIgnoreCase(name, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} entities with name like '{}'.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, name);
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UomResponse> findAllByUomStatusId(Long uomStatusId, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByUomStatusId";
        log.info("[{}] Fetch all {} records with status id: '{}'", methodNomenclature, ENTITY_NAME, uomStatusId);
        Page<UomResponse> page = repository.findAllByUomStatusId(uomStatusId, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records with status id: '{}'", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, uomStatusId);
        return page;
    }

    @Override
    public boolean isNameTaken(String name) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Check if name '{}' is taken.", methodNomenclature, name);
        boolean exists = repository.existsByNameIgnoreCase(name);
        log.info("[{}] Name '{}' {} taken.", methodNomenclature, name, exists ? "is" : "is not");
        return exists;
    }

    @Override
    @Transactional
    public UomResponse changeStatus(Long id, Long uomStatusId) {
        final String methodNomenclature = NOMENCLATURE + "-changeStatus";
        log.info("[{}] Change status id of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, uomStatusId);
        try {
            Uom existing = repository.findById(id)
                .orElseThrow(() -> {
                    String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[{}] {}", methodNomenclature, msg);
                    return new ResourceNotFoundException(msg);
                });
            UomStatus newStatus = statusService.findEntityById(uomStatusId);
            existing.setUomStatus(newStatus);
            log.info("[{}] Changed status id of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, newStatus.getId());
            return mapper.toResponse(existing);
        } catch (ResourceNotFoundException e) {
            String updateErrorMsg = messageService.getMessage("crud.update.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, updateErrorMsg);
            throw e;
        }
    }

}
