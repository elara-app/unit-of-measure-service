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

    private final UomStatusRepository repository;
    private final UomStatusMapper mapper;

    @Override
    @Transactional
    public UomStatusResponse save(UomStatusRequest request) {
        log.debug("Creating UomStatus with name: {}", request.name());
        if (Boolean.TRUE.equals(existsByName(request.name()))) {
            String errorMessage = String.format("UomStatus with name '%s' already exists", request.name());
            log.error(errorMessage);
            throw new ResourceConflictException(errorMessage);
        }
        try {
            UomStatus entity = mapper.updateEntityFromDto(request);
            UomStatus saved = repository.save(entity);
            log.info("Successfully created UomStatus with id: {}", saved.getId());
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Database error while saving UomStatus with name '%s'", request.name());
            log.error(errorMessage, e);
            throw new UnexpectedErrorException(errorMessage);
        }
    }

    @Override
    @Transactional // Important to use without explicit save () | automatic flush() and commit
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        if (request == null) {
            String errorMessage = "UomStatusUpdate request cannot be null";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Updating UomStatus with id: {}", id);
        UomStatus existing = repository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("UomStatus with id %d not found", id);
                    log.error(errorMessage);
                    return new ResourceNotFoundException(id);
                });

        if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(existsByName(request.name()))) {
            String errorMessage = String.format("UomStatus with name '%s' already exists", request.name());
            log.error(errorMessage);
            throw new ResourceConflictException(id);
        }

        try {
            mapper.updateEntityFromDto(existing, request);
//            repository.save() is not necessary as hibernate detects changes automatically
            log.info("Successfully updated UomStatus with id: {}", id);
            return mapper.toResponse(existing);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Database error while updating UomStatus with name '%s'", request.name());
            log.error(errorMessage, e);
            throw new UnexpectedErrorException(errorMessage);
        }
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public Optional<UomStatusResponse> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        return null;
    }

    @Override
    public Boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public void changeStatus(Long id, Boolean isUsable) {

    }
}
