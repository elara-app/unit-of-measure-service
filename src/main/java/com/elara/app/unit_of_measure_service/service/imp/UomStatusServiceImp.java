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

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UomStatusServiceImp implements UomStatusService {

    private static final String ENTITY_NAME = "UomStatus";
    private final UomStatusRepository repository;
    private final UomStatusMapper mapper;
    private final MessageService messageService;

    @Override
    @Transactional
    public UomStatusResponse save(UomStatusRequest request) {
        if (request == null) {
            String errorMessage = messageService.getMessage("validation.not.null", ENTITY_NAME + "Request");
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        log.debug("Creating {} with name: {}", ENTITY_NAME, request.name());
        if (Boolean.TRUE.equals(existsByName(request.name()))) {
            String errorMessage = messageService.getMessage("crud.already.exists", ENTITY_NAME, request.name());
            log.error(errorMessage);
            throw new ResourceConflictException(errorMessage);
        }
        try {
            UomStatus entity = mapper.updateEntityFromDto(request);
            UomStatus saved = repository.save(entity);
            log.info("Successfully created {} with id: {}", ENTITY_NAME, saved.getId());
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = messageService.getMessage("repository.save.error", ENTITY_NAME, e.getMessage());
            log.error(errorMessage, e);
            throw new UnexpectedErrorException(errorMessage);
        }
    }

    @Override
    @Transactional // Important to use without explicit save () | automatic flush() and commit
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        if (request == null) {
            String errorMessage = messageService.getMessage("validation.not.null", ENTITY_NAME + "Update request");
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Updating {} with id: {}", ENTITY_NAME, id);
        UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = messageService.getMessage("crud.not.found", ENTITY_NAME, id);
                    log.error(errorMessage);
                    return new ResourceNotFoundException(id);
                });

        if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(existsByName(request.name()))) {
            String errorMessage = messageService.getMessage("crud.already.exists", ENTITY_NAME, request.name());
            log.error(errorMessage);
            throw new ResourceConflictException(id);
        }

        try {
            mapper.updateEntityFromDto(existing, request);
//            repository.save() is not necessary as hibernate detects changes automatically
            log.info("Successfully updated {} with id: {}", ENTITY_NAME, id);
            return mapper.toResponse(existing);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = messageService.getMessage("repository.update.error", ENTITY_NAME, e.getMessage());
            log.error(errorMessage, e);
            throw new UnexpectedErrorException(errorMessage);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting {} with id: {}", ENTITY_NAME, id);
        if (!repository.existsById(id)) {
            String errorMessage = messageService.getMessage("crud.not.found", ENTITY_NAME, id);
            log.error(errorMessage);
            throw new ResourceNotFoundException(id);
        }
        try {
            repository.deleteById(id);
            log.info("Successfully deleted {} with id: {}", ENTITY_NAME, id);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = messageService.getMessage("repository.delete.error", ENTITY_NAME, e.getMessage());
            log.error(errorMessage, e);
            throw new UnexpectedErrorException(errorMessage);
        }
    }

    @Override
    public Optional<UomStatusResponse> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        if (pageable == null) {
            String errorCode = messageService.getMessage("validation.not.null", "Pageable");
            log.error(errorCode);
            throw new IllegalArgumentException(errorCode);
        }
        log.debug("Fetching all {} entities with pagination", ENTITY_NAME);
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        if (pageable == null) {
            String errorCode = messageService.getMessage("validation.not.null", "Pageable");
            log.error(errorCode);
            throw new IllegalArgumentException(errorCode);
        }
        log.debug("Fetching all {} entities with name containing: '{}' and pagination", ENTITY_NAME, name, pageable);
        return repository.findAllByNameContainingIgnoreCase(name, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        if (pageable == null) {
            String errorCode = messageService.getMessage("validation.not.null", "Pageable");
            log.error(errorCode);
            throw new IllegalArgumentException(errorCode);
        }
        log.debug("Fetching all {entities} with isUsable: {} and pagination", ENTITY_NAME, isUsable);
        return repository.findAllByIsUsable(isUsable, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public Boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public void changeStatus(Long id, Boolean isUsable) {

    }
}
