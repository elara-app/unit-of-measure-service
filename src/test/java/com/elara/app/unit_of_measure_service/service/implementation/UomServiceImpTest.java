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
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UomServiceImpTest {

    @Mock
    private UomRepository repository;
    
    @Mock
    private UomMapper mapper;
    
    @Mock
    private MessageService messageService;
    
    @Mock
    private UomStatusService statusService;

    @InjectMocks
    private UomServiceImp service;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("save() should create and return UomResponse when valid request")
        void save_shouldCreateAndReturnResponse() {
            // Given
            UomRequest request = new UomRequest("Kilogram", "Base unit of mass", new BigDecimal("1.000"), 1L);
            UomStatus status = UomStatus.builder().id(1L).name("Active").build();
            Uom entity = Uom.builder().name("Kilogram").description("Base unit of mass").conversionFactorToBase(new BigDecimal("1.000")).build();
            Uom saved = Uom.builder().id(1L).name("Kilogram").description("Base unit of mass").conversionFactorToBase(new BigDecimal("1.000")).uomStatus(status).build();
            UomResponse response = new UomResponse(1L, "Kilogram", "Base unit of mass", new BigDecimal("1.000"), null);

            when(service.isNameTaken("Kilogram")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(statusService.findByIdService(1L)).thenReturn(status);
            when(repository.save(entity)).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            // When
            UomResponse result = service.save(request);

            // Then
            assertThat(result).isEqualTo(response);
            verify(repository).save(entity);
            verify(statusService).findByIdService(1L);
            verify(mapper).toResponse(saved);
        }

        @Test
        @DisplayName("save() should throw ResourceConflictException when name already exists")
        void save_shouldThrowResourceConflictWhenNameTaken() {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("1.000"), 1L);
            when(service.isNameTaken("Kilogram")).thenReturn(true);
            when(messageService.getMessage("crud.already.exists", "Uom", "name", "Kilogram"))
                    .thenReturn("Uom with name 'Kilogram' already exists");

            // When & Then
            assertThatThrownBy(() -> service.save(request))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("Kilogram");
            
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("save() should throw ResourceNotFoundException when UomStatus not exists")
        void save_shouldThrowNotFoundWhenStatusNotExists() {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("1.000"), 999L);
            Uom entity = Uom.builder().name("Kilogram").build();
            
            when(service.isNameTaken("Kilogram")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(statusService.findByIdService(999L)).thenThrow(new ResourceNotFoundException("UomStatus", "id", "999"));

            // When & Then
            assertThatThrownBy(() -> service.save(request))
                    .isInstanceOf(ResourceNotFoundException.class);
            
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("save() should set UomStatus relationship correctly")
        void save_shouldSetStatusRelationship() {
            // Given
            UomRequest request = new UomRequest("Gram", "desc", new BigDecimal("0.001"), 1L);
            UomStatus status = UomStatus.builder().id(1L).name("Active").build();
            Uom entity = Uom.builder().name("Gram").build();
            Uom saved = Uom.builder().id(1L).name("Gram").uomStatus(status).build();
            UomResponse response = new UomResponse(1L, "Gram", "desc", new BigDecimal("0.001"), null);

            when(service.isNameTaken("Gram")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(statusService.findByIdService(1L)).thenReturn(status);
            when(repository.save(entity)).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            // When
            service.save(request);

            // Then
            verify(statusService).findByIdService(1L);
            verify(repository).save(argThat(uom -> uom.getUomStatus() != null && uom.getUomStatus().getId().equals(1L)));
        }

        @Test
        @DisplayName("save() should handle null request")
        void save_shouldHandleNullRequest() {
            // When & Then
            assertThatThrownBy(() -> service.save(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        @DisplayName("update() should update and return UomResponse when valid")
        void update_shouldUpdateAndReturnResponse() {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Kilogram Updated", "New description", new BigDecimal("1.500"));
            UomStatus status = UomStatus.builder().id(1L).name("Active").build();
            Uom existing = Uom.builder().id(1L).name("Kilogram").description("Old").conversionFactorToBase(new BigDecimal("1.000")).uomStatus(status).build();
            UomResponse response = new UomResponse(1L, "Kilogram Updated", "New description", new BigDecimal("1.500"), null);

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(service.isNameTaken("Kilogram Updated")).thenReturn(false);
            doNothing().when(mapper).updateEntityFromDto(existing, updateRequest);
            when(mapper.toResponse(existing)).thenReturn(response);

            // When
            UomResponse result = service.update(id, updateRequest);

            // Then
            assertThat(result).isEqualTo(response);
            verify(repository).findById(id);
            verify(mapper).updateEntityFromDto(existing, updateRequest);
            verify(mapper).toResponse(existing);
        }

        @Test
        @DisplayName("update() should throw ResourceNotFoundException when ID not exists")
        void update_shouldThrowNotFoundWhenIdNotExists() {
            // Given
            Long id = 999L;
            UomUpdate updateRequest = new UomUpdate("Name", "desc", new BigDecimal("1.0"));
            
            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage("crud.not.found", "Uom", "id", id))
                    .thenReturn("Uom not found");

            // When & Then
            assertThatThrownBy(() -> service.update(id, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
            
            verify(mapper, never()).updateEntityFromDto(any(), any());
        }

        @Test
        @DisplayName("update() should throw ResourceConflictException when new name already exists")
        void update_shouldThrowConflictWhenNewNameExists() {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Gram", "desc", new BigDecimal("1.0"));
            UomStatus status = UomStatus.builder().id(1L).build();
            Uom existing = Uom.builder().id(1L).name("Kilogram").uomStatus(status).build();

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(service.isNameTaken("Gram")).thenReturn(true);
            when(messageService.getMessage("crud.already.exists", "Uom", "name", "Gram"))
                    .thenReturn("Name already exists");

            // When & Then
            assertThatThrownBy(() -> service.update(id, updateRequest))
                    .isInstanceOf(ResourceConflictException.class);
            
            verify(mapper, never()).updateEntityFromDto(any(), any());
        }

        @Test
        @DisplayName("update() should allow same name when updating existing record")
        void update_shouldAllowSameNameForExistingRecord() {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Kilogram", "Updated description", new BigDecimal("1.0"));
            UomStatus status = UomStatus.builder().id(1L).build();
            Uom existing = Uom.builder().id(1L).name("Kilogram").uomStatus(status).build();
            UomResponse response = new UomResponse(1L, "Kilogram", "Updated description", new BigDecimal("1.0"), null);

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            doNothing().when(mapper).updateEntityFromDto(existing, updateRequest);
            when(mapper.toResponse(existing)).thenReturn(response);

            // When
            UomResponse result = service.update(id, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(service, never()).isNameTaken(any());
            verify(mapper).updateEntityFromDto(existing, updateRequest);
        }

        @Test
        @DisplayName("update() should preserve UomStatus after update")
        void update_shouldPreserveUomStatus() {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Updated", "desc", new BigDecimal("1.0"));
            UomStatus originalStatus = UomStatus.builder().id(1L).name("Active").build();
            Uom existing = Uom.builder().id(1L).name("Original").uomStatus(originalStatus).build();
            UomResponse response = new UomResponse(1L, "Updated", "desc", new BigDecimal("1.0"), null);

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(service.isNameTaken("Updated")).thenReturn(false);
            doNothing().when(mapper).updateEntityFromDto(existing, updateRequest);
            when(mapper.toResponse(existing)).thenReturn(response);

            // When
            service.update(id, updateRequest);

            // Then
            verify(repository).findById(id);
            assertThat(existing.getUomStatus()).isEqualTo(originalStatus);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("deleteById() should delete UOM when exists")
        void deleteById_shouldDeleteWhenExists() {
            // Given
            Long id = 1L;
            when(repository.existsById(id)).thenReturn(true);
            doNothing().when(repository).deleteById(id);

            // When
            service.deleteById(id);

            // Then
            verify(repository).existsById(id);
            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("deleteById() should throw ResourceNotFoundException when ID not exists")
        void deleteById_shouldThrowNotFoundWhenIdNotExists() {
            // Given
            Long id = 999L;
            when(repository.existsById(id)).thenReturn(false);
            when(messageService.getMessage("crud.not.found", "Uom", "id", id))
                    .thenReturn("Uom not found");

            // When & Then
            assertThatThrownBy(() -> service.deleteById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
            
            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Read Operations - Single Record")
    class ReadSingleOperations {

        @Test
        @DisplayName("findById() should return UomResponse when exists")
        void findById_shouldReturnResponseWhenExists() {
            // Given
            Long id = 1L;
            Uom entity = Uom.builder().id(1L).name("Kilogram").build();
            UomResponse response = new UomResponse(1L, "Kilogram", "desc", new BigDecimal("1.0"), null);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            // When
            UomResponse result = service.findById(id);

            // Then
            assertThat(result).isEqualTo(response);
            verify(repository).findById(id);
            verify(mapper).toResponse(entity);
        }

        @Test
        @DisplayName("findById() should throw ResourceNotFoundException when ID not exists")
        void findById_shouldThrowNotFoundWhenIdNotExists() {
            // Given
            Long id = 999L;
            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage("crud.not.found", "Uom", "id", id))
                    .thenReturn("Uom not found");

            // When & Then
            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Read Operations - Multiple Records")
    class ReadMultipleOperations {

        @Test
        @DisplayName("findAll() should return paged results")
        void findAll_shouldReturnPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Uom entity1 = Uom.builder().id(1L).name("Kilogram").build();
            Uom entity2 = Uom.builder().id(2L).name("Gram").build();
            Page<Uom> entityPage = new PageImpl<>(List.of(entity1, entity2), pageable, 2);
            UomResponse response1 = new UomResponse(1L, "Kilogram", "desc1", new BigDecimal("1.0"), null);
            UomResponse response2 = new UomResponse(2L, "Gram", "desc2", new BigDecimal("0.001"), null);

            when(repository.findAll(pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity1)).thenReturn(response1);
            when(mapper.toResponse(entity2)).thenReturn(response2);

            // When
            Page<UomResponse> result = service.findAll(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("findAll() should return empty page when no data")
        void findAll_shouldReturnEmptyPageWhenNoData() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Uom> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<UomResponse> result = service.findAll(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("findAllByName() should return filtered results")
        void findAllByName_shouldReturnFilteredResults() {
            // Given
            String name = "kilo";
            Pageable pageable = PageRequest.of(0, 10);
            Uom entity = Uom.builder().id(1L).name("Kilogram").build();
            Page<Uom> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            UomResponse response = new UomResponse(1L, "Kilogram", "desc", new BigDecimal("1.0"), null);

            when(repository.findAllByNameContainingIgnoreCase(name, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            // When
            Page<UomResponse> result = service.findAllByName(name, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findAllByNameContainingIgnoreCase(name, pageable);
        }

        @Test
        @DisplayName("findAllByName() should be case insensitive")
        void findAllByName_shouldBeCaseInsensitive() {
            // Given
            String name = "KILO";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Uom> entityPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAllByNameContainingIgnoreCase(name, pageable)).thenReturn(entityPage);

            // When
            service.findAllByName(name, pageable);

            // Then
            verify(repository).findAllByNameContainingIgnoreCase(name, pageable);
        }

        @Test
        @DisplayName("findAllByUomStatusId() should filter by status")
        void findAllByUomStatusId_shouldFilterByStatus() {
            // Given
            Long statusId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Uom entity = Uom.builder().id(1L).name("Kilogram").build();
            Page<Uom> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            UomResponse response = new UomResponse(1L, "Kilogram", "desc", new BigDecimal("1.0"), null);

            when(repository.findAllByUomStatusId(statusId, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            // When
            Page<UomResponse> result = service.findAllByUomStatusId(statusId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findAllByUomStatusId(statusId, pageable);
        }

        @Test
        @DisplayName("findAllByUomStatusId() should return empty when no match")
        void findAllByUomStatusId_shouldReturnEmptyWhenNoMatch() {
            // Given
            Long statusId = 999L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Uom> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAllByUomStatusId(statusId, pageable)).thenReturn(emptyPage);

            // When
            Page<UomResponse> result = service.findAllByUomStatusId(statusId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Business Logic Operations")
    class BusinessLogicOperations {

        @Test
        @DisplayName("isNameTaken() should return true when name exists")
        void isNameTaken_shouldReturnTrueWhenExists() {
            // Given
            String name = "Kilogram";
            when(repository.existsByNameIgnoreCase(name)).thenReturn(true);

            // When
            Boolean result = service.isNameTaken(name);

            // Then
            assertThat(result).isTrue();
            verify(repository).existsByNameIgnoreCase(name);
        }

        @Test
        @DisplayName("isNameTaken() should return false when name not exists")
        void isNameTaken_shouldReturnFalseWhenNotExists() {
            // Given
            String name = "NonExistent";
            when(repository.existsByNameIgnoreCase(name)).thenReturn(false);

            // When
            Boolean result = service.isNameTaken(name);

            // Then
            assertThat(result).isFalse();
            verify(repository).existsByNameIgnoreCase(name);
        }

        @Test
        @DisplayName("isNameTaken() should be case insensitive")
        void isNameTaken_shouldBeCaseInsensitive() {
            // Given
            String name = "KILOGRAM";
            when(repository.existsByNameIgnoreCase(name)).thenReturn(true);

            // When
            service.isNameTaken(name);

            // Then
            verify(repository).existsByNameIgnoreCase(name);
        }

        @Test
        @DisplayName("changeStatus() should update status when both exist")
        void changeStatus_shouldUpdateStatusWhenBothExist() {
            // Given
            Long uomId = 1L;
            Long statusId = 2L;
            UomStatus oldStatus = UomStatus.builder().id(1L).name("Active").build();
            UomStatus newStatus = UomStatus.builder().id(2L).name("Inactive").build();
            Uom existing = Uom.builder().id(uomId).name("Kilogram").uomStatus(oldStatus).build();

            when(repository.findById(uomId)).thenReturn(Optional.of(existing));
            when(statusService.findByIdService(statusId)).thenReturn(newStatus);

            // When
            service.changeStatus(uomId, statusId);

            // Then
            assertThat(existing.getUomStatus()).isEqualTo(newStatus);
            verify(repository).findById(uomId);
            verify(statusService).findByIdService(statusId);
        }

        @Test
        @DisplayName("changeStatus() should throw ResourceNotFoundException when UOM not exists")
        void changeStatus_shouldThrowNotFoundWhenUomNotExists() {
            // Given
            Long uomId = 999L;
            Long statusId = 1L;
            
            when(repository.findById(uomId)).thenReturn(Optional.empty());
            when(messageService.getMessage("crud.not.found", "Uom", "id", uomId))
                    .thenReturn("Uom not found");

            // When & Then
            assertThatThrownBy(() -> service.changeStatus(uomId, statusId))
                    .isInstanceOf(ResourceNotFoundException.class);
            
            verify(statusService, never()).findByIdService(any());
        }

        @Test
        @DisplayName("changeStatus() should throw ResourceNotFoundException when Status not exists")
        void changeStatus_shouldThrowNotFoundWhenStatusNotExists() {
            // Given
            Long uomId = 1L;
            Long statusId = 999L;
            UomStatus oldStatus = UomStatus.builder().id(1L).build();
            Uom existing = Uom.builder().id(uomId).name("Kilogram").uomStatus(oldStatus).build();

            when(repository.findById(uomId)).thenReturn(Optional.of(existing));
            when(statusService.findByIdService(statusId)).thenThrow(new ResourceNotFoundException("UomStatus", "id", "999"));

            // When & Then
            assertThatThrownBy(() -> service.changeStatus(uomId, statusId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
