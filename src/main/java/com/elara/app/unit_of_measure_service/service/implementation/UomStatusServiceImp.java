package com.elara.app.unit_of_measure_service.service.implementation;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.mapper.UomStatusMapper;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import com.elara.app.unit_of_measure_service.repository.UomStatusRepository;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UomStatusServiceImp implements UomStatusService {

    private static final String ENTITY_NAME = "UomStatus";
    private static final String NOMENCLATURE = ENTITY_NAME + "-service";
    private final UomStatusRepository repository;
    private final UomStatusMapper mapper;
    private final MessageService messageService;

    @Override
    @Transactional
    public UomStatusResponse save(UomStatusRequest request) {
        final String methodNomenclature = NOMENCLATURE + "-save";
        log.info("[{}] {} record to save: {}", methodNomenclature, ENTITY_NAME, request);
        try {
            if (Boolean.TRUE.equals(isNameTaken(Objects.requireNonNull(request).name()))) {
                String alreadyExistsMsg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, alreadyExistsMsg);
                throw new ResourceConflictException(alreadyExistsMsg);
            }
            UomStatus entity = mapper.toEntity(request);
            UomStatus saved = repository.save(entity);
            log.info("[{}] {} record created with id: {}.", methodNomenclature, ENTITY_NAME, saved.getId());
            return mapper.toResponse(saved);
        } catch (ResourceConflictException e) {
            String saveErrorMsg = messageService.getMessage("crud.save.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, saveErrorMsg);
            throw e;
        }
    }

    @Override
    @Transactional
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Update {} record with id: {} and request: {}", methodNomenclature, ENTITY_NAME, id, request);
        try {
            UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    String notFoundMsg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[{}] {}", methodNomenclature, notFoundMsg);
                    return new ResourceNotFoundException(ENTITY_NAME, "id", id);
                });
            if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(isNameTaken(request.name()))) {
                String alreadyExistsMsg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, alreadyExistsMsg);
                throw new ResourceConflictException(alreadyExistsMsg);
            }
            mapper.updateEntityFromDto(existing, request);
            log.info("[{}] {} record updated with data: {}", methodNomenclature, ENTITY_NAME, existing);
            return mapper.toResponse(existing);
        } catch (ResourceNotFoundException | ResourceConflictException e) {
            String updateErrorMsg = messageService.getMessage("crud.update.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, updateErrorMsg);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-deleteById";
        log.info("[{}] Delete {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        try {
            if (!repository.existsById(id)) {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceNotFoundException(ENTITY_NAME, "id", id.toString());
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
    public UomStatusResponse findById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findById";
        log.info("[{}] Fetch {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        try {
            Optional<UomStatus> entity = repository.findById(id);
            if (entity.isEmpty()) {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceNotFoundException(ENTITY_NAME, "id", id.toString());
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
    @Transactional
    public UomStatus findByIdService(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findByIdService";
        log.info("[{}] Fetch {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        UomStatus entity = repository.findById(id)
            .orElseThrow(() -> {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                return new ResourceNotFoundException(ENTITY_NAME, "id", id.toString());
            });
        log.info("[{}] Fetched {} record with id: {}: {}", methodNomenclature, ENTITY_NAME, id, entity);
        return entity;
    }

    @Override
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAll";
        log.info("[{}] Fetch all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME);
        return page;
    }

    @Override
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByName";
        log.info("[{}] Fetch all {} records that contain in their name: '{}'", methodNomenclature, ENTITY_NAME, name);
        Page<UomStatusResponse> page = repository.findAllByNameContainingIgnoreCase(name, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} entities with name like '{}'.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, name);
        return page;
    }

    @Override
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByIsUsable";
        log.info("[{}] Fetch all {} records with isUsable: {}.", methodNomenclature, ENTITY_NAME, isUsable);
        Page<UomStatusResponse> page = repository.findAllByIsUsable(isUsable, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records with isUsable: {}.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, isUsable);
        return page;
    }

    public Boolean isNameTaken(String name) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Check if name '{}' is taken.", methodNomenclature, name);
        Boolean exists = repository.existsByName(name);
        log.info("[{}] Name '{}' {} taken.", methodNomenclature, name, exists ? "is" : "is not");
        return exists;
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Boolean isUsable) {
        final String methodNomenclature = NOMENCLATURE + "-changeStatus";
        log.info("[{}] Change status of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, isUsable);
        UomStatus existing = repository.findById(id)
            .orElseThrow(() -> {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                return new ResourceNotFoundException(ENTITY_NAME, "id", id);
            });
        existing.setIsUsable(isUsable);
        log.info("[{}] Changed status of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, isUsable);
    }
}
