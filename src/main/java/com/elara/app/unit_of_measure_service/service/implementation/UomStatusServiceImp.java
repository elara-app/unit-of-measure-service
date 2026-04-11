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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Service implementation for managing UomStatus entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UomStatusServiceImp implements UomStatusService {

    /**
     * Name of the entity managed by this service.
     */
    private static final String ENTITY_NAME = "UomStatus";
    private static final String NOMENCLATURE = ENTITY_NAME + "-service";
    private final UomStatusRepository repository;
    private final UomStatusMapper mapper;
    private final MessageService messageService;

    /**
     * Saves a new UomStatus entity.
     * Logs the attempt, warns if a conflict exists, and logs success or error.
     *
     * @param request the UomStatusRequest DTO
     * @return the saved UomStatusResponse
     * @throws ResourceConflictException if a UomStatus with the same name exists
     */
    @Override
    @Transactional
    public UomStatusResponse save(UomStatusRequest request) {
        final String methodNomenclature = NOMENCLATURE + "-save";
        log.info("[{}] {} record to save: {}", methodNomenclature, ENTITY_NAME, request);
        if (isNameTaken(Objects.requireNonNull(request).name())) {
            String alreadyExistsMsg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
            String saveErrorMsg = messageService.getMessage("crud.save.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, alreadyExistsMsg);
            log.warn("[{}] {}", methodNomenclature, saveErrorMsg);
            throw new ResourceConflictException(alreadyExistsMsg);
        }
        UomStatus entity = mapper.toEntity(request);
        UomStatus saved = repository.save(entity);
        log.info("[{}] {} record created with id: {}.", methodNomenclature, ENTITY_NAME, saved.getId());
        return mapper.toResponse(saved);
    }

    /**
     * Updates an existing UomStatus entity.
     * <p>
     * <b>Transactional:</b> This method is transactional. All changes are committed atomically.
     * <b>Edge Cases:</b> If the entity does not exist, a ResourceNotFoundException is thrown. If the new name is already taken, a ResourceConflictException is thrown.
     * <b>Hibernate Behavior:</b> The entity is managed by the persistence context. Changes are flushed automatically at transaction commit. Explicit save is not required unless using a detached entity.
     *
     * @param id      the id of the UomStatus to update (must not be null)
     * @param request the UomStatusUpdate DTO (must not be null)
     * @return the updated UomStatusResponse
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     */
    @Override
    @Transactional
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        log.info("[{}] Update {} record with id: {} and request: {}", methodNomenclature, ENTITY_NAME, id, request);
        try {
            UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    String notFoundMsg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    String updateErrorMsg = messageService.getMessage("crud.update.error", ENTITY_NAME);
                    log.warn("[{}] {}", methodNomenclature, notFoundMsg);
                    log.warn("[{}] {}", methodNomenclature, updateErrorMsg);
                    return new ResourceNotFoundException(notFoundMsg);
                });
            if (!existing.getName().equals(request.name()) && isNameTaken(request.name())) {
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

    /**
     * Deletes a UomStatus entity by its id.
     * Logs the attempt and success or error.
     *
     * @param id the id of the UomStatus to delete
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-deleteById";
        log.info("[{}] Delete {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        if (!repository.existsById(id)) {
            String notFoundMsg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
            String deleteErrorMsg = messageService.getMessage("crud.delete.error", ENTITY_NAME);
            log.warn("[{}] {}", methodNomenclature, notFoundMsg);
            log.warn("[{}] {}", methodNomenclature, deleteErrorMsg);
            throw new ResourceNotFoundException(notFoundMsg);
        }
        repository.deleteById(id);
        log.info("[{}] {} record with id: {}, deleted.", methodNomenclature, ENTITY_NAME, id);
    }

    /**
     * Finds a UomStatus entity by its id.
     * Logs the search and result or error.
     *
     * @param id the id of the UomStatus to find
     * @return an Optional containing the found UomStatusResponse, or empty if not found
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     */
    @Override @Transactional(readOnly = true)
    public UomStatusResponse findById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findById";
        log.info("[{}] Fetch {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        Optional<UomStatus> entity = repository.findById(id);
        if (entity.isEmpty()) {
            String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id.toString());
            log.warn("[{}] {}", methodNomenclature, msg);
            throw new ResourceNotFoundException(msg);
        }
        log.info("[{}] Fetched {} record with id: {}: {}", methodNomenclature, ENTITY_NAME, id, entity.get());
        return mapper.toResponse(entity.get());
    }

    @Override
    @Transactional
    public UomStatus findEntityById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findEntityById";
        log.info("[{}] Fetch {} record with id: {}", methodNomenclature, ENTITY_NAME, id);
        UomStatus entity = repository.findById(id)
            .orElseThrow(() -> {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                return new ResourceNotFoundException(msg);
            });
        log.info("[{}] Fetched {} record with id: {}: {}", methodNomenclature, ENTITY_NAME, id, entity);
        return entity;
    }

    /**
     * Finds all UomStatus entities with pagination.
     *
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAll";
        log.info("[{}] Fetch all {} records.", methodNomenclature, ENTITY_NAME);
        Page<UomStatusResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME);
        return page;
    }

    /**
     * Finds all UomStatus entities by name with pagination.
     *
     * @param name     the name to search for
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByName";
        log.info("[{}] Fetch all {} records that contain in their name: '{}'", methodNomenclature, ENTITY_NAME, name);
        Page<UomStatusResponse> page = repository.findAllByNameContainingIgnoreCase(name, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} entities with name like '{}'.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, name);
        return page;
    }

    /**
     * Finds all UomStatus entities by usability status with pagination.
     *
     * @param isUsable the usability status to filter by
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByIsUsable";
        log.info("[{}] Fetch all {} records with isUsable: {}.", methodNomenclature, ENTITY_NAME, isUsable);
        Page<UomStatusResponse> page = repository.findAllByIsUsable(isUsable, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} {} records with isUsable: {}.", methodNomenclature, page.getNumberOfElements(), ENTITY_NAME, isUsable);
        return page;
    }

    /**
     * Checks if a UomStatus entity exists by its name.
     *
     * @param name the name of the UomStatus to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean isNameTaken(String name) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.info("[{}] Check if name '{}' is taken.", methodNomenclature, name);
        boolean exists = repository.existsByName(name);
        log.info("[{}] Name '{}' {} taken.", methodNomenclature, name, exists ? "is" : "is not");
        return exists;
    }

    /**
     * Changes the usability status of a UomStatus entity.
     * Logs the attempt and result.
     *
     * @param id       the id of the UomStatus to update
     * @param isUsable the new usability status
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     */
    @Override
    @Transactional
    public void changeStatus(Long id, Boolean isUsable) {
        final String methodNomenclature = NOMENCLATURE + "-changeStatus";
        log.info("[{}] Change status of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, isUsable);
        UomStatus existing = repository.findById(id)
            .orElseThrow(() -> {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                return new ResourceNotFoundException(msg);
            });
        existing.setIsUsable(isUsable);
        log.info("[{}] Changed status of {} record with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, isUsable);
    }
}
