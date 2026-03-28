package com.elara.app.unit_of_measure_service.service.implementation;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.mapper.UomStatusMapper;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import com.elara.app.unit_of_measure_service.repository.UomStatusRepository;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UomStatusServiceImp")
class UomStatusServiceImpTest {

    @Mock
    private UomStatusRepository repository;

    @Mock
    private UomStatusMapper mapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private UomStatusServiceImp service;

    @AfterEach
    void tearDown() {
        reset(repository, mapper, messageService);
    }

    @Nested
    @DisplayName("Save Operation")
    class SaveTests {

        @Test
        @DisplayName("Save with unique name, creates and returns UomStatusResponse")
        void save_withUniqueName_createsAndReturnsResponse() {
            UomStatusRequest request = new UomStatusRequest("Active", "Active status for measurements", true);
            UomStatus entity = UomStatus.builder()
                .name("Active")
                .description("Active status for measurements")
                .isUsable(true)
                .build();
            UomStatus savedEntity = UomStatus.builder()
                .id(1L)
                .name("Active")
                .description("Active status for measurements")
                .isUsable(true)
                .build();
            UomStatusResponse expectedResponse = new UomStatusResponse(1L, "Active", "Active status for measurements", true);

            when(repository.existsByName("Active")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

            UomStatusResponse result = service.save(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Active");
            assertThat(result.description()).isEqualTo("Active status for measurements");
            assertThat(result.isUsable()).isTrue();
            
            verify(repository).existsByName("Active");
            verify(mapper).toEntity(request);
            verify(repository).save(entity);
            verify(mapper).toResponse(savedEntity);
            verifyNoMoreInteractions(repository, mapper, messageService);
        }

        @Test
        @DisplayName("Save with duplicate name, throws ResourceConflictException")
        void save_withDuplicateName_throwsResourceConflictException() {
            UomStatusRequest request = new UomStatusRequest("Active", "Description", true);
            String errorMessage = "UomStatus with name 'Active' already exists";

            when(repository.existsByName("Active")).thenReturn(true);
            when(messageService.getMessage(eq("crud.already.exists"), eq("UomStatus"), eq("name"), eq("Active")))
                .thenReturn(errorMessage);
            when(messageService.getMessage(eq("crud.save.error"), eq("UomStatus")))
                .thenReturn("Error saving UomStatus");

            assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).existsByName("Active");
            verify(repository, never()).save(any());
            verify(mapper, never()).toEntity(any());
            verify(messageService).getMessage(eq("crud.already.exists"), eq("UomStatus"), eq("name"), eq("Active"));
            verify(messageService).getMessage(eq("crud.save.error"), eq("UomStatus"));
        }

        @Test
        @DisplayName("Save verifies name uniqueness before persisting")
        void save_verifiesNameUniquenessBeforePersisting() {
            UomStatusRequest request = new UomStatusRequest("Inactive", "Inactive status", false);
            UomStatus entity = UomStatus.builder().name("Inactive").description("Inactive status").isUsable(false).build();
            UomStatus savedEntity = UomStatus.builder().id(2L).name("Inactive").description("Inactive status").isUsable(false).build();
            UomStatusResponse response = new UomStatusResponse(2L, "Inactive", "Inactive status", false);

            when(repository.existsByName("Inactive")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toResponse(savedEntity)).thenReturn(response);

            service.save(request);

            verify(repository).existsByName("Inactive");
            verify(repository).save(entity);
        }
    }

    @Nested
    @DisplayName("Update Operation")
    class UpdateTests {

        @Test
        @DisplayName("Update with valid data and unique name, updates and returns response")
        void update_withValidDataAndUniqueName_updatesAndReturnsResponse() {
            Long id = 1L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("Updated Name", "Updated description");
            UomStatus existingEntity = UomStatus.builder()
                .id(id)
                .name("Original Name")
                .description("Original description")
                .isUsable(true)
                .build();
            UomStatusResponse expectedResponse = new UomStatusResponse(id, "Updated Name", "Updated description", true);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.existsByName("Updated Name")).thenReturn(false);
            doAnswer(invocation -> {
                existingEntity.setName(updateRequest.name());
                existingEntity.setDescription(updateRequest.description());
                return null;
            }).when(mapper).updateEntityFromDto(existingEntity, updateRequest);
            when(mapper.toResponse(existingEntity)).thenReturn(expectedResponse);

            UomStatusResponse result = service.update(id, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("Updated Name");
            assertThat(result.description()).isEqualTo("Updated description");
            
            verify(repository).findById(id);
            verify(repository).existsByName("Updated Name");
            verify(mapper).updateEntityFromDto(existingEntity, updateRequest);
            verify(mapper).toResponse(existingEntity);
            verifyNoMoreInteractions(repository, mapper);
        }

        @Test
        @DisplayName("Update with non-existent id, throws ResourceNotFoundException")
        void update_withNonExistentId_throwsResourceNotFoundException() {
            Long id = 999L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("Name", "Description");
            String errorMessage = "UomStatus with id '999' not found";

            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id)))
                .thenReturn(errorMessage);
            when(messageService.getMessage(eq("crud.update.error"), eq("UomStatus")))
                .thenReturn("Error updating UomStatus");

            assertThatThrownBy(() -> service.update(id, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).findById(id);
            verify(mapper, never()).updateEntityFromDto(any(), any());
            verify(messageService).getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id));
        }

        @Test
        @DisplayName("Update with duplicate name, throws ResourceConflictException")
        void update_withDuplicateName_throwsResourceConflictException() {
            Long id = 1L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("Taken Name", "Description");
            UomStatus existingEntity = UomStatus.builder()
                .id(id)
                .name("Original Name")
                .description("Original description")
                .isUsable(true)
                .build();
            String errorMessage = "UomStatus with name 'Taken Name' already exists";

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.existsByName("Taken Name")).thenReturn(true);
            when(messageService.getMessage(eq("crud.already.exists"), eq("UomStatus"), eq("name"), eq("Taken Name")))
                .thenReturn(errorMessage);
            when(messageService.getMessage(eq("crud.update.error"), eq("UomStatus")))
                .thenReturn("Error updating UomStatus");

            assertThatThrownBy(() -> service.update(id, updateRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).findById(id);
            verify(repository).existsByName("Taken Name");
            verify(mapper, never()).updateEntityFromDto(any(), any());
        }

        @Test
        @DisplayName("Update with same name as existing, skips name uniqueness check")
        void update_withSameName_skipsNameUniquenessCheck() {
            Long id = 1L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("Same Name", "New description");
            UomStatus existingEntity = UomStatus.builder()
                .id(id)
                .name("Same Name")
                .description("Old description")
                .isUsable(true)
                .build();
            UomStatusResponse expectedResponse = new UomStatusResponse(id, "Same Name", "New description", true);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            doAnswer(invocation -> {
                existingEntity.setDescription(updateRequest.description());
                return null;
            }).when(mapper).updateEntityFromDto(existingEntity, updateRequest);
            when(mapper.toResponse(existingEntity)).thenReturn(expectedResponse);

            UomStatusResponse result = service.update(id, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.description()).isEqualTo("New description");
            
            verify(repository).findById(id);
            verify(repository, never()).existsByName(anyString());
            verify(mapper).updateEntityFromDto(existingEntity, updateRequest);
        }

        @Test
        @DisplayName("Update preserves entity id after update")
        void update_preservesEntityIdAfterUpdate() {
            Long id = 5L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("New Name", "New description");
            UomStatus existingEntity = UomStatus.builder()
                .id(id)
                .name("Old Name")
                .description("Old description")
                .isUsable(false)
                .build();
            UomStatusResponse expectedResponse = new UomStatusResponse(id, "New Name", "New description", false);

            when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(repository.existsByName("New Name")).thenReturn(false);
            doNothing().when(mapper).updateEntityFromDto(existingEntity, updateRequest);
            when(mapper.toResponse(existingEntity)).thenReturn(expectedResponse);

            UomStatusResponse result = service.update(id, updateRequest);

            assertThat(result.id()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("Delete Operation")
    class DeleteTests {

        @Test
        @DisplayName("DeleteById with existing id, deletes entity successfully")
        void deleteById_withExistingId_deletesSuccessfully() {
            Long id = 10L;

            when(repository.existsById(id)).thenReturn(true);
            doNothing().when(repository).deleteById(id);

            service.deleteById(id);

            verify(repository).existsById(id);
            verify(repository).deleteById(id);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(messageService);
        }

        @Test
        @DisplayName("DeleteById with non-existent id, throws ResourceNotFoundException")
        void deleteById_withNonExistentId_throwsResourceNotFoundException() {
            Long id = 999L;
            String errorMessage = "UomStatus with id '999' not found";

            when(repository.existsById(id)).thenReturn(false);
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id)))
                .thenReturn(errorMessage);
            when(messageService.getMessage(eq("crud.delete.error"), eq("UomStatus")))
                .thenReturn("Error deleting UomStatus");

            assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).existsById(id);
            verify(repository, never()).deleteById(any());
            verify(messageService).getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id));
            verify(messageService).getMessage(eq("crud.delete.error"), eq("UomStatus"));
        }

        @Test
        @DisplayName("DeleteById verifies existence before deletion")
        void deleteById_verifiesExistenceBeforeDeletion() {
            Long id = 5L;

            when(repository.existsById(id)).thenReturn(true);
            doNothing().when(repository).deleteById(id);

            service.deleteById(id);

            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(repository).existsById(idCaptor.capture());
            verify(repository).deleteById(idCaptor.getValue());
            assertThat(idCaptor.getValue()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("FindById Operations")
    class FindByIdTests {

        @Test
        @DisplayName("FindById with existing id, returns UomStatusResponse")
        void findById_withExistingId_returnsResponse() {
            Long id = 1L;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Active")
                .description("Active status")
                .isUsable(true)
                .build();
            UomStatusResponse expectedResponse = new UomStatusResponse(id, "Active", "Active status", true);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(expectedResponse);

            UomStatusResponse result = service.findById(id);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("Active");
            assertThat(result.description()).isEqualTo("Active status");
            assertThat(result.isUsable()).isTrue();
            
            verify(repository).findById(id);
            verify(mapper).toResponse(entity);
            verifyNoMoreInteractions(repository, mapper);
            verifyNoInteractions(messageService);
        }

        @Test
        @DisplayName("FindById with non-existent id, throws ResourceNotFoundException")
        void findById_withNonExistentId_throwsResourceNotFoundException() {
            Long id = 999L;
            String errorMessage = "UomStatus with id '999' not found";

            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id.toString())))
                .thenReturn(errorMessage);

            assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).findById(id);
            verify(mapper, never()).toResponse(any());
            verify(messageService).getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id.toString()));
        }

        @Test
        @DisplayName("FindByIdService with existing id, returns UomStatus entity")
        void findByIdService_withExistingId_returnsEntity() {
            Long id = 1L;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Active")
                .description("Active status")
                .isUsable(true)
                .build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            UomStatus result = service.findByIdService(id);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("Active");
            assertThat(result.getDescription()).isEqualTo("Active status");
            assertThat(result.getIsUsable()).isTrue();
            
            verify(repository).findById(id);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("FindByIdService with non-existent id, throws ResourceNotFoundException")
        void findByIdService_withNonExistentId_throwsResourceNotFoundException() {
            Long id = 999L;
            String errorMessage = "UomStatus with id '999' not found";

            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id)))
                .thenReturn(errorMessage);

            assertThatThrownBy(() -> service.findByIdService(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).findById(id);
            verify(messageService).getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id));
        }
    }

    @Nested
    @DisplayName("FindAll Operations")
    class FindAllTests {

        @Test
        @DisplayName("FindAll with pageable, returns page of responses")
        void findAll_withPageable_returnsPageOfResponses() {
            Pageable pageable = PageRequest.of(0, 10);
            UomStatus entity1 = UomStatus.builder().id(1L).name("Active").description("Active status").isUsable(true).build();
            UomStatus entity2 = UomStatus.builder().id(2L).name("Inactive").description("Inactive status").isUsable(false).build();
            List<UomStatus> entities = List.of(entity1, entity2);
            Page<UomStatus> entityPage = new PageImpl<>(entities, pageable, entities.size());
            
            UomStatusResponse response1 = new UomStatusResponse(1L, "Active", "Active status", true);
            UomStatusResponse response2 = new UomStatusResponse(2L, "Inactive", "Inactive status", false);
            List<UomStatusResponse> responses = List.of(response1, response2);
            Page<UomStatusResponse> responsePage = new PageImpl<>(responses, pageable, responses.size());

            when(repository.findAll(pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity1)).thenReturn(response1);
            when(mapper.toResponse(entity2)).thenReturn(response2);

            Page<UomStatusResponse> result = service.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).name()).isEqualTo("Active");
            assertThat(result.getContent().get(1).name()).isEqualTo("Inactive");
            
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("FindAll with empty result, returns empty page")
        void findAll_withEmptyResult_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UomStatus> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            Page<UomStatusResponse> result = service.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("FindAllByName with matching name, returns filtered page")
        void findAllByName_withMatchingName_returnsFilteredPage() {
            String searchName = "act";
            Pageable pageable = PageRequest.of(0, 10);
            UomStatus entity = UomStatus.builder().id(1L).name("Active").description("Active status").isUsable(true).build();
            Page<UomStatus> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            UomStatusResponse response = new UomStatusResponse(1L, "Active", "Active status", true);

            when(repository.findAllByNameContainingIgnoreCase(searchName, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<UomStatusResponse> result = service.findAllByName(searchName, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().name()).containsIgnoringCase(searchName);
            
            verify(repository).findAllByNameContainingIgnoreCase(searchName, pageable);
        }

        @Test
        @DisplayName("FindAllByName case-insensitive search works correctly")
        void findAllByName_caseInsensitiveSearchWorksCorrectly() {
            String searchName = "ACT";
            Pageable pageable = PageRequest.of(0, 10);
            Page<UomStatus> entityPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAllByNameContainingIgnoreCase(searchName, pageable)).thenReturn(entityPage);

            Page<UomStatusResponse> result = service.findAllByName(searchName, pageable);

            assertThat(result).isNotNull();
            verify(repository).findAllByNameContainingIgnoreCase(searchName, pageable);
        }

        @Test
        @DisplayName("FindAllByIsUsable with true, returns only usable statuses")
        void findAllByIsUsable_withTrue_returnsOnlyUsableStatuses() {
            Pageable pageable = PageRequest.of(0, 10);
            UomStatus entity = UomStatus.builder().id(1L).name("Active").description("Active status").isUsable(true).build();
            Page<UomStatus> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            UomStatusResponse response = new UomStatusResponse(1L, "Active", "Active status", true);

            when(repository.findAllByIsUsable(true, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<UomStatusResponse> result = service.findAllByIsUsable(true, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().isUsable()).isTrue();
            
            verify(repository).findAllByIsUsable(true, pageable);
        }

        @Test
        @DisplayName("FindAllByIsUsable with false, returns only unusable statuses")
        void findAllByIsUsable_withFalse_returnsOnlyUnusableStatuses() {
            Pageable pageable = PageRequest.of(0, 10);
            UomStatus entity = UomStatus.builder().id(2L).name("Deprecated").description("Deprecated status").isUsable(false).build();
            Page<UomStatus> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            UomStatusResponse response = new UomStatusResponse(2L, "Deprecated", "Deprecated status", false);

            when(repository.findAllByIsUsable(false, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<UomStatusResponse> result = service.findAllByIsUsable(false, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().isUsable()).isFalse();
            
            verify(repository).findAllByIsUsable(false, pageable);
        }

        @Test
        @DisplayName("FindAllByIsUsable respects pagination")
        void findAllByIsUsable_respectsPagination() {
            Pageable pageable = PageRequest.of(1, 5);
            Page<UomStatus> entityPage = new PageImpl<>(Collections.emptyList(), pageable, 10);

            when(repository.findAllByIsUsable(true, pageable)).thenReturn(entityPage);

            Page<UomStatusResponse> result = service.findAllByIsUsable(true, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            
            verify(repository).findAllByIsUsable(true, pageable);
        }
    }

    @Nested
    @DisplayName("Name Uniqueness Check")
    class IsNameTakenTests {

        @Test
        @DisplayName("IsNameTaken with existing name, returns true")
        void isNameTaken_withExistingName_returnsTrue() {
            String name = "Active";

            when(repository.existsByName(name)).thenReturn(true);

            Boolean result = service.isNameTaken(name);

            assertThat(result).isTrue();
            verify(repository).existsByName(name);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("IsNameTaken with non-existent name, returns false")
        void isNameTaken_withNonExistentName_returnsFalse() {
            String name = "NonExistent";

            when(repository.existsByName(name)).thenReturn(false);

            Boolean result = service.isNameTaken(name);

            assertThat(result).isFalse();
            verify(repository).existsByName(name);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("IsNameTaken is case-sensitive")
        void isNameTaken_isCaseSensitive() {
            String name = "Active";

            when(repository.existsByName(name)).thenReturn(true);
            when(repository.existsByName("active")).thenReturn(false);

            assertThat(service.isNameTaken(name)).isTrue();
            assertThat(service.isNameTaken("active")).isFalse();
            
            verify(repository).existsByName(name);
            verify(repository).existsByName("active");
        }
    }

    @Nested
    @DisplayName("Change Status Operation")
    class ChangeStatusTests {

        @Test
        @DisplayName("ChangeStatus with existing id, updates isUsable to true")
        void changeStatus_withExistingId_updatesIsUsableToTrue() {
            Long id = 1L;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Active")
                .description("Active status")
                .isUsable(false)
                .build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.changeStatus(id, true);

            assertThat(entity.getIsUsable()).isTrue();
            verify(repository).findById(id);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("ChangeStatus with existing id, updates isUsable to false")
        void changeStatus_withExistingId_updatesIsUsableToFalse() {
            Long id = 1L;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Active")
                .description("Active status")
                .isUsable(true)
                .build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.changeStatus(id, false);

            assertThat(entity.getIsUsable()).isFalse();
            verify(repository).findById(id);
        }

        @Test
        @DisplayName("ChangeStatus with non-existent id, throws ResourceNotFoundException")
        void changeStatus_withNonExistentId_throwsResourceNotFoundException() {
            Long id = 999L;
            String errorMessage = "UomStatus with id '999' not found";

            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id)))
                .thenReturn(errorMessage);

            assertThatThrownBy(() -> service.changeStatus(id, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(errorMessage);

            verify(repository).findById(id);
            verify(messageService).getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id));
        }

        @Test
        @DisplayName("ChangeStatus modifies entity in-place without explicit save")
        void changeStatus_modifiesEntityInPlace() {
            Long id = 1L;
            Boolean newStatus = true;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Test")
                .description("Test status")
                .isUsable(false)
                .build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.changeStatus(id, newStatus);

            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(repository).findById(idCaptor.capture());
            verify(repository, never()).save(any());
            assertThat(idCaptor.getValue()).isEqualTo(id);
            assertThat(entity.getIsUsable()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("ChangeStatus with same status value, still updates entity")
        void changeStatus_withSameStatusValue_stillUpdatesEntity() {
            Long id = 1L;
            UomStatus entity = UomStatus.builder()
                .id(id)
                .name("Active")
                .description("Active status")
                .isUsable(true)
                .build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.changeStatus(id, true);

            assertThat(entity.getIsUsable()).isTrue();
            verify(repository).findById(id);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration")
    class EdgeCaseTests {

        @Test
        @DisplayName("Save and update operations call message service for error messages")
        void saveAndUpdate_callMessageServiceForErrors() {
            UomStatusRequest request = new UomStatusRequest("Duplicate", "desc", true);
            
            when(repository.existsByName("Duplicate")).thenReturn(true);
            when(messageService.getMessage(eq("crud.already.exists"), eq("UomStatus"), eq("name"), eq("Duplicate")))
                .thenReturn("Already exists");
            when(messageService.getMessage(eq("crud.save.error"), eq("UomStatus")))
                .thenReturn("Save error");

            assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(ResourceConflictException.class);

            verify(messageService).getMessage(eq("crud.already.exists"), eq("UomStatus"), eq("name"), eq("Duplicate"));
            verify(messageService).getMessage(eq("crud.save.error"), eq("UomStatus"));
        }

        @Test
        @DisplayName("Multiple findAll operations use mapper consistently")
        void multipleFindAllOperations_useMapperConsistently() {
            Pageable pageable = PageRequest.of(0, 10);
            UomStatus entity = UomStatus.builder().id(1L).name("Test").description("desc").isUsable(true).build();
            UomStatusResponse response = new UomStatusResponse(1L, "Test", "desc", true);
            Page<UomStatus> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(pageable)).thenReturn(entityPage);
            when(repository.findAllByNameContainingIgnoreCase("Test", pageable)).thenReturn(entityPage);
            when(repository.findAllByIsUsable(true, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            service.findAll(pageable);
            service.findAllByName("Test", pageable);
            service.findAllByIsUsable(true, pageable);

            verify(mapper, times(3)).toResponse(entity);
        }

        @Test
        @DisplayName("Update operation logs error messages when catching exceptions")
        void update_logsErrorMessageWhenCatchingExceptions() {
            Long id = 1L;
            UomStatusUpdate updateRequest = new UomStatusUpdate("Name", "Desc");
            
            when(repository.findById(id)).thenReturn(Optional.empty());
            when(messageService.getMessage(eq("crud.not.found"), eq("UomStatus"), eq("id"), eq(id)))
                .thenReturn("Not found");
            when(messageService.getMessage(eq("crud.update.error"), eq("UomStatus")))
                .thenReturn("Update error");

            assertThatThrownBy(() -> service.update(id, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(messageService, times(2)).getMessage(eq("crud.update.error"), eq("UomStatus"));
        }
    }

}
