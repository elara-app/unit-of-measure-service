package com.elara.app.unit_of_measure_service.service.imp;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.exceptions.UnexpectedErrorException;
import com.elara.app.unit_of_measure_service.mapper.UomMapper;
import com.elara.app.unit_of_measure_service.model.Uom;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import com.elara.app.unit_of_measure_service.repository.UomRepository;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UomServiceImp implements UomService {

    private static final String ENTITY_NAME = "Uom";
    private final UomRepository repository;
    private final UomMapper mapper;
    private final MessageService messageService;
    private final UomStatusService statusService;

    @Override
    @Transactional
    public UomResponse save(UomRequest request) {
        log.debug("[Uom-service-save] Attempting to create {} with name: {} and request: {}", ENTITY_NAME, request != null ? request.name() : null, request);
        if (Boolean.TRUE.equals(isNameTaken(Objects.requireNonNull(request).name()))) {
            log.warn("[Uom-service-save] {}", messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name()));
            throw new ResourceConflictException(new Object[]{"namem", request.name()});
        }
        try {
            Uom entity = mapper.toEntity(request);
            log.debug("[Uom-service-save] Mapped DTO to entity: {}", entity);
            UomStatus status = statusService.findByIdService(request.uomStatusId());
            entity.setUomStatus(status);
            System.out.println(entity.getUomStatus().getName());
            Uom saved = repository.save(entity);
            log.debug("[UomStatus-service-save] {}", messageService.getMessage("crud.create.success", ENTITY_NAME));
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("[Uom-service-save] Data integrity violation while saving {}: {}", ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        } catch (Exception e) {
            log.error("[Uom-service-save] Unexpected error while saving {}: {}", ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public UomResponse update(Long id, UomUpdate request) {
        try {
            log.debug("[Uom-service-update] Attempting to update {} with id: {} and request: {}", ENTITY_NAME, id, request);
            Uom existing = repository.findById(id)
                .orElseThrow(() -> {
                    String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[Uom-service-update] {}", msg);
                    return new ResourceNotFoundException(new Object[]{"id", id.toString()});
                });
            if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(isNameTaken(request.name()))) {
                String msg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[Uom-service-update] {}", msg);
                throw new ResourceConflictException(new Object[]{"name", request.name()});
            }
            UomStatus status = existing.getUomStatus();
            log.debug("[Uom-service-update] Mapping update DTO to entity. Before: {}", existing);
            mapper.updateEntityFromDto(existing, request);
            existing.setUomStatus(status);
            log.debug("[Uom-service-update] {}", messageService.getMessage("crud.update.success", ENTITY_NAME));
            return mapper.toResponse(existing);
        } catch (ResourceNotFoundException | ResourceConflictException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("[Uom-service-update] Data integrity violation while updating {}: {}", ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        } catch (Exception e) {
            log.error("[Uom-service-update] Unexpected error while updating {}: {}", ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public UomResponse findById(Long id) {
        return null;
    }

    @Override
    public Page<UomResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomResponse> findAllByName(String name, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomResponse> findAllByUomStatusId(Long uomStatusId, Pageable pageable) {
        return null;
    }

    @Override
    public Boolean isNameTaken(String name) {
        log.debug("[Uom-service-isNameTaken] Checking if name '{}' is taken for {}", name, ENTITY_NAME);
        Boolean exists = repository.existsByNameIgnoreCase(name);
        log.debug("[Uom-service-isNameTaken] Name '{}' taken: {}", name, exists);
        return exists;
    }

    @Override
    public void changeStatus(Long id, Long uomStatusId) {

    }

}
