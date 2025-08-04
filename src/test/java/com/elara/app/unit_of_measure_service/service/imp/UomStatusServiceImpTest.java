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
import com.elara.app.unit_of_measure_service.utils.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UomStatusServiceImpTest {

    @Mock
    private UomStatusRepository repository;
    @Mock
    private UomStatusMapper mapper;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private UomStatusServiceImp service;

    // --- Save method tests ---

    @Test
    @DisplayName("save() should create and return UomStatusResponse when name is not taken")
    void save_shouldCreateAndReturnResponse() {
        UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
        UomStatus entity = UomStatus.builder().name("Active").description("desc").isUsable(true).build();
        UomStatus saved = UomStatus.builder().id(1L).name("Active").description("desc").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(1L, "Active", "desc", true);

        when(service.isNameTaken("Active")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        UomStatusResponse result = service.save(request);

        assertThat(result).isEqualTo(response);
        verify(repository).save(entity);
        verify(mapper).toResponse(saved);
        verify(messageService, never()).getMessage(eq("crud.already.exists"), any(), any());
    }

    @Test
    @DisplayName("save() should throw ResourceConflictException if name is taken")
    void save_shouldThrowResourceConflictIfNameTaken() {
        UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
        when(service.isNameTaken("Active")).thenReturn(true);
        when(messageService.getMessage("crud.already.exists", "UomStatus", "name", "Active"))
                .thenReturn("UomStatus with name 'Active' already exists");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(ResourceConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save() should throw UnexpectedErrorException on DataIntegrityViolationException")
    void save_shouldThrowUnexpectedErrorOnDataIntegrityViolation() {
        UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
        UomStatus entity = UomStatus.builder().name("Active").description("desc").isUsable(true).build();
        when(service.isNameTaken("Active")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("db error"));
        when(messageService.getMessage("repository.save.error", "UomStatus", "db error"))
                .thenReturn("Database error while saving UomStatus");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(UnexpectedErrorException.class)
                .hasMessageContaining("db error");
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("save() should handle null request gracefully")
    void save_shouldHandleNullRequest() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("save() should handle null name in request")
    void save_shouldHandleNullName() {
        UomStatusRequest request = new UomStatusRequest(null, "desc", true);
        when(service.isNameTaken(null)).thenReturn(false);
        UomStatus entity = UomStatus.builder().name(null).description("desc").isUsable(true).build();
        UomStatus saved = UomStatus.builder().id(1L).name(null).description("desc").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(1L, null, "desc", true);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);
        UomStatusResponse result = service.save(request);
        assertThat(result).isEqualTo(response);
    }

    /**
     * Test that save() handles the case where isNameTaken returns null (should proceed as not taken).
     */
    @Test
    @DisplayName("save() should handle isNameTaken returning null")
    void save_shouldHandleIsNameTakenReturningNull() {
        UomStatusRequest request = new UomStatusRequest("Test", "desc", true);
        when(service.isNameTaken("Test")).thenReturn(null);
        UomStatus entity = UomStatus.builder().name("Test").description("desc").isUsable(true).build();
        UomStatus saved = UomStatus.builder().id(1L).name("Test").description("desc").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(1L, "Test", "desc", true);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);
        UomStatusResponse result = service.save(request);
        assertThat(result).isEqualTo(response);
    }

    // --- Update method tests ---

    /**
     * Test that update() successfully updates and returns a response when the update is valid.
     */
    @Test
    @DisplayName("update() should update and return UomStatusResponse when valid")
    void update_shouldUpdateAndReturnResponse() {
        Long id = 1L;
        UomStatusUpdate update = new UomStatusUpdate("New Name", "New Desc");
        UomStatus existing = UomStatus.builder().id(id).name("Old Name").description("Old Desc").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(id, "New Name", "New Desc", true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(service.isNameTaken("New Name")).thenReturn(false);
        doAnswer(invocation -> {
            existing.setName(update.name());
            existing.setDescription(update.description());
            return null;
        }).when(mapper).updateEntityFromDto(existing, update);
        when(mapper.toResponse(existing)).thenReturn(response);

        UomStatusResponse result = service.update(id, update);
        assertThat(result).isEqualTo(response);
        verify(mapper).updateEntityFromDto(existing, update);
        verify(mapper).toResponse(existing);
    }

    @Test
    @DisplayName("update() should throw ResourceNotFoundException if entity not found")
    void update_shouldThrowResourceNotFoundIfNotFound() {
        Long id = 2L;
        UomStatusUpdate update = new UomStatusUpdate("Name", "Desc");
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(messageService.getMessage("crud.not.found", "UomStatus", "id", id)).thenReturn("Not found");

        assertThatThrownBy(() -> service.update(id, update))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(mapper, never()).updateEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("update() should throw ResourceConflictException if name is taken")
    void update_shouldThrowResourceConflictIfNameTaken() {
        Long id = 3L;
        UomStatusUpdate update = new UomStatusUpdate("Taken Name", "Desc");
        UomStatus existing = UomStatus.builder().id(id).name("Old Name").description("Old Desc").isUsable(true).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(service.isNameTaken("Taken Name")).thenReturn(true);
        when(messageService.getMessage("crud.already.exists", "UomStatus", "name", "Taken Name"))
                .thenReturn("UomStatus with name 'Taken Name' already exists");

        assertThatThrownBy(() -> service.update(id, update))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    @DisplayName("update() should throw UnexpectedErrorException on DataIntegrityViolationException")
    void update_shouldThrowUnexpectedErrorOnDataIntegrityViolation() {
        Long id = 4L;
        var update = new UomStatusUpdate("Name", "Desc");
        var existing = UomStatus.builder().id(id).name("Old Name").description("Old Desc").isUsable(true).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(service.isNameTaken("Name")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("db error")).when(mapper).updateEntityFromDto(existing, update);
        when(messageService.getMessage("repository.update.error", "UomStatus", "db error"))
                .thenReturn("Database error while updating UomStatus");

        assertThatThrownBy(() -> service.update(id, update))
                .isInstanceOf(UnexpectedErrorException.class)
                .hasMessageContaining("db error");
    }

    @Test
    @DisplayName("update() should skip name conflict check if name is unchanged")
    void update_shouldSkipNameConflictIfNameUnchanged() {
        Long id = 100L;
        UomStatusUpdate update = new UomStatusUpdate("Same Name", "desc");
        UomStatus existing = UomStatus.builder().id(id).name("Same Name").description("old").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(id, "Same Name", "desc", true);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            existing.setDescription(update.description());
            return null;
        }).when(mapper).updateEntityFromDto(existing, update);
        when(mapper.toResponse(existing)).thenReturn(response);
        UomStatusResponse result = service.update(id, update);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("update() should handle null name in update request")
    void update_shouldHandleNullNameInUpdate() {
        Long id = 101L;
        UomStatusUpdate update = new UomStatusUpdate(null, "desc");
        UomStatus existing = UomStatus.builder().id(id).name("Old Name").description("old").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(id, null, "desc", true);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(service.isNameTaken(null)).thenReturn(false);
        doAnswer(invocation -> {
            existing.setName(null);
            existing.setDescription(update.description());
            return null;
        }).when(mapper).updateEntityFromDto(existing, update);
        when(mapper.toResponse(existing)).thenReturn(response);
        UomStatusResponse result = service.update(id, update);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("update() should handle isNameTaken returning null")
    void update_shouldHandleIsNameTakenReturningNull() {
        Long id = 102L;
        UomStatusUpdate update = new UomStatusUpdate("New Name", "desc");
        UomStatus existing = UomStatus.builder().id(id).name("Old Name").description("old").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(id, "New Name", "desc", true);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(service.isNameTaken("New Name")).thenReturn(null);
        doAnswer(invocation -> {
            existing.setName(update.name());
            existing.setDescription(update.description());
            return null;
        }).when(mapper).updateEntityFromDto(existing, update);
        when(mapper.toResponse(existing)).thenReturn(response);
        UomStatusResponse result = service.update(id, update);
        assertThat(result).isEqualTo(response);
    }

    // --- Delete method tests ---

    @Test
    @DisplayName("deleteById() should delete entity when it exists")
    void deleteById_shouldDeleteEntity() {
        Long id = 10L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository).deleteById(id);
        verify(messageService, never()).getMessage(eq("crud.not.found"), any(), any());
    }

    @Test
    @DisplayName("deleteById() should throw ResourceNotFoundException if entity does not exist")
    void deleteById_shouldThrowResourceNotFoundIfNotFound() {
        Long id = 11L;
        when(repository.existsById(id)).thenReturn(false);
        when(messageService.getMessage("crud.not.found", "UomStatus", "id", id)).thenReturn("Not found");

        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteById() should throw UnexpectedErrorException on DataIntegrityViolationException")
    void deleteById_shouldThrowUnexpectedErrorOnDataIntegrityViolation() {
        Long id = 12L;
        when(repository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("db error")).when(repository).deleteById(id);
        when(messageService.getMessage("repository.delete.error", "UomStatus", "db error"))
                .thenReturn("Database error while deleting UomStatus");

        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(UnexpectedErrorException.class)
                .hasMessageContaining("db error");
    }

    // --- Find methods tests ---

    @Test
    @DisplayName("findById() should return response when entity exists")
    void findById_shouldReturnResponse() {
        Long id = 20L;
        UomStatus entity = UomStatus.builder().id(id).name("Test").description("desc").isUsable(true).build();
        UomStatusResponse response = new UomStatusResponse(id, "Test", "desc", true);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        Optional<UomStatusResponse> result = service.findById(id);
        assertThat(result).isPresent().contains(response);
    }

    @Test
    @DisplayName("findById() should throw ResourceNotFoundException if not found")
    void findById_shouldThrowResourceNotFoundIfNotFound() {
        Long id = 21L;
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(messageService.getMessage("crud.not.found", "UomStatus", "id", id)).thenReturn("Not found");

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll() should return page of responses")
    void findAll_shouldReturnPage() {
        Pageable pageable = mock(Pageable.class);
        Page<UomStatus> page = mock(Page.class);
        when(repository.findAll(pageable)).thenReturn(page);
        when(page.map(any())).thenReturn(Page.empty());

        Page<UomStatusResponse> result = service.findAll(pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("findAllByName() should return page of responses")
    void findAllByName_shouldReturnPage() {
        Pageable pageable = mock(Pageable.class);
        String name = "Test";
        Page<UomStatus> page = mock(Page.class);
        when(repository.findAllByNameContainingIgnoreCase(name, pageable)).thenReturn(page);
        when(page.map(any())).thenReturn(Page.empty());

        Page<UomStatusResponse> result = service.findAllByName(name, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("findAllByIsUsable() should return page of responses")
    void findAllByIsUsable_shouldReturnPage() {
        Pageable pageable = mock(Pageable.class);
        Boolean isUsable = true;
        Page<UomStatus> page = mock(Page.class);
        when(repository.findAllByIsUsable(isUsable, pageable)).thenReturn(page);
        when(page.map(any())).thenReturn(Page.empty());

        Page<UomStatusResponse> result = service.findAllByIsUsable(isUsable, pageable);
        assertThat(result).isNotNull();
    }

    // --- isNameTaken method tests ---

    @Test
    @DisplayName("isNameTaken() should return true if name exists")
    void isNameTaken_shouldReturnTrueIfExists() {
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(true);
        assertThat(service.isNameTaken(name)).isTrue();
    }

    @Test
    @DisplayName("isNameTaken() should return false if name does not exist")
    void isNameTaken_shouldReturnFalseIfNotExists() {
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(false);
        assertThat(service.isNameTaken(name)).isFalse();
    }

    // --- changeStatus method tests ---

    @Test
    @DisplayName("changeStatus() should update isUsable when entity exists")
    void changeStatus_shouldUpdateIsUsable() {
        Long id = 30L;
        UomStatus entity = UomStatus.builder().id(id).name("Test").description("desc").isUsable(false).build();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.changeStatus(id, true);
        assertThat(entity.getIsUsable()).isTrue();
    }

    @Test
    @DisplayName("changeStatus() should throw ResourceNotFoundException if entity not found")
    void changeStatus_shouldThrowResourceNotFoundIfNotFound() {
        Long id = 31L;
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(messageService.getMessage("crud.not.found", "UomStatus", "id", id)).thenReturn("Not found");

        assertThatThrownBy(() -> service.changeStatus(id, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
