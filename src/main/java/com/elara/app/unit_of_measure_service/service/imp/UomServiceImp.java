package com.elara.app.unit_of_measure_service.service.imp;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.exceptions.DatabaseException;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UomServiceImp implements UomService {

    private static final String ENTITY_NAME = "Uom";
    private static final String NOMENCLATURE = "Uom-service";
    private final UomRepository repository;
    private final UomMapper mapper;
    private final MessageService messageService;
    private final UomStatusService statusService;

    @Override
    @Transactional
    public UomResponse save(UomRequest request) {
        final String methodNomenclature = NOMENCLATURE + "-save";
        try {
            log.debug("[{}] Attempting to create {} with name: {} and request: {}", methodNomenclature, ENTITY_NAME, request != null ? request.name() : null, request);
            if (Boolean.TRUE.equals(isNameTaken(Objects.requireNonNull(request).name()))) {
                String msg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceConflictException(new Object[]{"name", request.name()});
            }
            Uom entity = mapper.toEntity(request);
            log.debug("[{}] Mapped DTO to entity: {}", methodNomenclature, entity);
            UomStatus status = statusService.findByIdService(request.uomStatusId());
            entity.setUomStatus(status);
            Uom saved = repository.save(entity);
            String msg = messageService.getMessage("crud.create.success", ENTITY_NAME);
            log.debug("[{}] {}", methodNomenclature, msg);
            return mapper.toResponse(saved);
        } catch (ResourceConflictException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("[{}] Data integrity violation while saving {}: {}", methodNomenclature, ENTITY_NAME, e.getMessage(), e);
            String[] data = getDataFromDataIntegrityExceptionMessage(e.getMessage());
            throw new DatabaseException(new Object[]{data[0], data[1]});
        } catch (Exception e) {
            log.error("[{}] Unexpected error while saving {}: {}", methodNomenclature, ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public UomResponse update(Long id, UomUpdate request) {
        final String methodNomenclature = NOMENCLATURE + "-update";
        try {
            log.debug("[{}] Attempting to update {} with id: {} and request: {}", methodNomenclature, ENTITY_NAME, id, request);
            Uom existing = repository.findById(id)
                .orElseThrow(() -> {
                    String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[{}] {}", methodNomenclature, msg);
                    return new ResourceNotFoundException(new Object[]{ENTITY_NAME, "id", id.toString()});
                });
            if (!existing.getName().equals(request.name()) && Boolean.TRUE.equals(isNameTaken(request.name()))) {
                String msg = messageService.getMessage("crud.already.exists", ENTITY_NAME, "name", request.name());
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceConflictException(new Object[]{"name", request.name()});
            }
            UomStatus status = existing.getUomStatus();
            log.debug("[{}] Mapping update DTO to entity. Before: {}", methodNomenclature, existing);
            mapper.updateEntityFromDto(existing, request);
            existing.setUomStatus(status);
            String msg = messageService.getMessage("crud.update.success", ENTITY_NAME);
            log.debug("[{}] {}", methodNomenclature, msg);
            return mapper.toResponse(existing);
        } catch (ResourceNotFoundException | ResourceConflictException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String[] data = getDataFromDataIntegrityExceptionMessage(e.getMessage());
            String msg = messageService.getMessage("global.error.database", data[0], data[1]);
            log.error("[{}] {}", methodNomenclature, msg);
            throw new DatabaseException(new Object[]{data[0], data[1]});
        } catch (Exception e) {
            log.error("[{}] Unexpected error while updating {}: {}", methodNomenclature, ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-deleteById";
        try {
            log.debug("[{}] Attempting to delete {} with id: {}", methodNomenclature, ENTITY_NAME, id);
            if (!repository.existsById(id)) {
                String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                log.warn("[{}] {}", methodNomenclature, msg);
                throw new ResourceNotFoundException(new Object[]{ENTITY_NAME, "id", id.toString()});
            }
            repository.deleteById(id);
            String msg = messageService.getMessage("crud.delete.success", ENTITY_NAME);
            log.debug("[{}] {} with id: {}", methodNomenclature, msg, id);
        } catch (ResourceConflictException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String[] data = getDataFromDataIntegrityExceptionMessage(e.getMessage());
            String msg = messageService.getMessage("global.error.database", data[0], data[1]);
            log.error("[{}] {}", methodNomenclature, msg);
            throw new DatabaseException(new Object[]{data[0], data[1]});
        } catch (Exception e) {
            log.error("[{}] Unexpected error while deleting {}: {}", methodNomenclature, ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    public UomResponse findById(Long id) {
        final String methodNomenclature = NOMENCLATURE + "-findById";
        log.debug("[{}] Searching {} with id: {}", methodNomenclature, ENTITY_NAME, id);
        Optional<UomResponse> response = repository.findById(id)
            .map(mapper::toResponse);
        if (response.isEmpty()) {
            String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
            log.warn("[{}] {}", methodNomenclature, msg);
            throw new ResourceNotFoundException(new Object[]{ENTITY_NAME, "id", id.toString()});
        }
        log.debug("[{}] {}", methodNomenclature, messageService.getMessage("crud.read.success", ENTITY_NAME));
        return response.get();
    }

    @Override
    public Page<UomResponse> findAll(Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAll";
        log.debug("[{}] Fetching all {} entities with pagination: {}.", methodNomenclature, ENTITY_NAME, pageable);
        Page<UomResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        log.debug("[{}] Fetched {} entities, page size: {}.", methodNomenclature, ENTITY_NAME, page.getNumberOfElements());
        return page;
    }

    @Override
    public Page<UomResponse> findAllByName(String name, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByName";
        log.debug("[{}] Fetching all {} entities with name containing: '{}'", methodNomenclature, ENTITY_NAME, name);
        Page<UomResponse> page = repository.findAllByNameContainingIgnoreCase(name, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} entities with name like '{}', page size: {}", methodNomenclature, ENTITY_NAME, name, page.getNumberOfElements());
        return page;
    }

    @Override
    public Page<UomResponse> findAllByUomStatusId(Long uomStatusId, Pageable pageable) {
        final String methodNomenclature = NOMENCLATURE + "-findAllByUomStatusId";
        log.debug("[{}] Fetching all {} entities with status id: '{}'", methodNomenclature, ENTITY_NAME, uomStatusId);
        Page<UomResponse> page = repository.findAllByUomStatusId(uomStatusId, pageable).map(mapper::toResponse);
        log.info("[{}] Fetched {} entities with status id: '{}', page size: {}", methodNomenclature, ENTITY_NAME, uomStatusId, page.getNumberOfElements());
        return page;
    }

    @Override
    public Boolean isNameTaken(String name) {
        final String methodNomenclature = NOMENCLATURE + "-isNameTaken";
        log.debug("[{}] Checking if name '{}' is taken for {}", methodNomenclature, name, ENTITY_NAME);
        Boolean exists = repository.existsByNameIgnoreCase(name);
        log.debug("[{}] Name '{}' taken: {}", methodNomenclature, name, exists);
        return exists;
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Long uomStatusId) {
        final String methodNomenclature = NOMENCLATURE + "-changeStatus";
        try {
            log.debug("[{}] Attempting to change status of {} with id: {} to the new status id: {}", methodNomenclature, ENTITY_NAME, id, uomStatusId);
            Uom existing = repository.findById(id)
                .orElseThrow(() -> {
                    String msg = messageService.getMessage("crud.not.found", ENTITY_NAME, "id", id);
                    log.warn("[{}] {}", methodNomenclature, msg);
                    return new ResourceNotFoundException(new Object[]{ENTITY_NAME, "id", id});
                });
            Long oldStatusId = existing.getUomStatus().getId();
            UomStatus newStatus = statusService.findByIdService(uomStatusId);
            existing.setUomStatus(newStatus);
            log.debug("[{}] Changed status of {} with id: {} from uomStatus with id: {} to: {}", methodNomenclature, ENTITY_NAME, id, oldStatusId, newStatus.getId());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String[] data = getDataFromDataIntegrityExceptionMessage(e.getMessage());
            String msg = messageService.getMessage("global.error.database", data[0], data[1]);
            log.error("[{}] {}", methodNomenclature, msg);
            throw new DatabaseException(new Object[]{data[0], data[1]});
        } catch (Exception e) {
            log.error("[{}] Unexpected error while updating {}: {}", methodNomenclature, ENTITY_NAME, e.getMessage(), e);
            throw new UnexpectedErrorException(e.getMessage());
        }
    }

    private static String[] getDataFromDataIntegrityExceptionMessage(String exceptionMessage) {
        String notAvailable = "<>";
        Matcher errorMatcher = Pattern.compile("ERROR:\\s*([^\\n]+)").matcher(exceptionMessage);
        String error = errorMatcher.find() ? errorMatcher.group(1).trim() : notAvailable;
        Matcher detailMatcher = Pattern.compile("Detail:\\s*([^.]+)").matcher(exceptionMessage);
        String detail = detailMatcher.find() ? detailMatcher.group(1).trim() : notAvailable;
        return new String[]{error, detail};
    }

}
