package com.elara.app.unit_of_measure_service.service.imp;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.exceptions.UnexpectedErrorException;
import com.elara.app.unit_of_measure_service.mapper.UomStatusMapper;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import com.elara.app.unit_of_measure_service.repository.UomStatusRepository;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
     * @throws UnexpectedErrorException  if a database error occurs
     */
    @Override
    @Transactional
    public UomStatusResponse save(UomStatusRequest request) {
        log.debug("[save] Attempting to create {} with name: {}", ENTITY_NAME, request != null ? request.name() : null);
        if (Boolean.TRUE.equals(isNameTaken(Objects.requireNonNull(request).name()))) {
            log.warn("[save] {}", messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name()));
            throw new ResourceConflictException((Object[]) new Object[]{"name", request.name()});
        }
        try {
            UomStatus entity = mapper.toEntity(request);
            UomStatus saved = repository.save(entity);
            log.info("[save] {}", messageService.getMessage("crud.create.success", ENTITY_NAME));
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("[save] {}", messageService.getMessage("repository.save.error", ENTITY_NAME, e.getMessage()));
            throw new UnexpectedErrorException(e.getMessage());
        }
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
     * @throws ResourceConflictException if a conflict exists with the new data
     * @throws UnexpectedErrorException  if a database error occurs
     */
    @Override
    @Transactional
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        log.debug("[update] Attempting to update {} with id: {}", ENTITY_NAME, id);
        UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[update] {}", messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id));
                    return new ResourceNotFoundException((Object[]) new Object[]{"id", id.toString()});
                });

        if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(isNameTaken(request.name()))) {
            log.warn("[update] {}", messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name()));
            throw new ResourceConflictException("name", request.name());
        }

        try {
            mapper.updateEntityFromDto(existing, request);
            // Hibernate will flush changes automatically at transaction commit.
            log.info("[update] {}", messageService.getMessage("crud.update.success", ENTITY_NAME));
            return mapper.toResponse(existing);
        } catch (DataIntegrityViolationException e) {
            log.error("[update] {}", messageService.getMessage("repository.update.error", ENTITY_NAME, e.getMessage()));
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    /**
     * Deletes a UomStatus entity by its id.
     * Logs the attempt and success or error.
     *
     * @param id the id of the UomStatus to delete
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     * @throws UnexpectedErrorException  if a database error occurs
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("[deleteById] Attempting to delete {} with id: {}", ENTITY_NAME, id);
        if (!repository.existsById(id)) {
            log.warn("[deleteById] {}", messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id));
            throw new ResourceNotFoundException((Object[]) new Object[]{"id", id.toString()});
        }
        try {
            repository.deleteById(id);
            log.info("[deleteById] {}", messageService.getMessage("crud.delete.success", ENTITY_NAME));
        } catch (DataIntegrityViolationException e) {
            log.error("[deleteById] {}", messageService.getMessage("repository.delete.error", ENTITY_NAME, e.getMessage()));
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    /**
     * Finds a UomStatus entity by its id.
     * Logs the search and result or error.
     *
     * @param id the id of the UomStatus to find
     * @return an Optional containing the found UomStatusResponse, or empty if not found
     * @throws ResourceNotFoundException if no UomStatus found with the given id
     */
    @Override
    public Optional<UomStatusResponse> findById(Long id) {
        log.debug("[findById] Searching {} with id: {}", ENTITY_NAME, id);
        Optional<UomStatusResponse> response = repository.findById(id)
                .map(mapper::toResponse);
        if (response.isEmpty()) {
            log.warn("[findById] {}", messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id));
            throw new ResourceNotFoundException((Object[]) new Object[]{"id", id.toString()});
        }
        log.info("[findById] {}", messageService.getMessage("crud.read.success", ENTITY_NAME));
        return response;
    }

    /**
     * Finds all UomStatus entities with pagination.
     *
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        log.debug("[findAll] Fetching all {} entities with pagination: {}", ENTITY_NAME, pageable);
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Finds all UomStatus entities by name with pagination.
     *
     * @param name     the name to search for
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        log.debug("[findAllByName] Fetching all {} entities with name containing: '{}' and pagination: {}", ENTITY_NAME, name, pageable);
        return repository.findAllByNameContainingIgnoreCase(name, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Finds all UomStatus entities by usability status with pagination.
     *
     * @param isUsable the usability status to filter by
     * @param pageable the pagination information
     * @return a page of UomStatusResponse
     */
    @Override
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        log.debug("[findAllByIsUsable] Fetching all {} with isUsable: {} and pagination: {}", ENTITY_NAME, isUsable, pageable);
        return repository.findAllByIsUsable(isUsable, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Checks if a UomStatus entity exists by its name.
     *
     * @param name the name of the UomStatus to check
     * @return true if exists, false otherwise
     */
    public Boolean isNameTaken(String name) {
        log.debug("[isNameTaken] Checking if {} name is taken: {}", ENTITY_NAME, name);
        return repository.existsByName(name);
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
        log.debug("[changeStatus] Attempting to change status of {} with id: {} to isUsable: {}", ENTITY_NAME, id, isUsable);
        UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[changeStatus] {}", messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id));
                    return new ResourceNotFoundException((Object[]) new Object[]{"id", id.toString()});
                });
        existing.setIsUsable(isUsable);
        log.info("[changeStatus] Changed status of {} with id: {} to isUsable: {}", ENTITY_NAME, id, isUsable);
    }
}
