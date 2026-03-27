package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.config.GlobalExceptionHandler;
import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UomController.class)
@Import({GlobalExceptionHandler.class, UomControllerTest.TestConfig.class})
class UomControllerTest {

    @AfterEach
    void tearDown() {
        reset(service, messageService);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("uomService")
    private UomService service;

    @Autowired
    private MessageService messageService;

    private static final String BASE_URL = "/";

    @TestConfiguration
    static class TestConfig {
        @Bean
        UomService uomService() {
            return mock(UomService.class);
        }
        @Bean
        MessageService messageService() {
            return mock(MessageService.class);
        }
    }

    @Nested
    @DisplayName("POST / - Create UOM")
    class CreateUomTests {

        @Test
        @DisplayName("should return 201 with created UOM when valid request")
        void create_shouldReturn201WithValidRequest() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "Base unit of mass", new BigDecimal("1.000"), 1L);
            UomResponse response = new UomResponse(1L, "Kilogram", "Base unit of mass", new BigDecimal("1.000"), null);

            given(service.save(any(UomRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kilogram"))
                .andExpect(jsonPath("$.description").value("Base unit of mass"))
                .andExpect(jsonPath("$.conversionFactorToBase").value(1.000));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void create_shouldReturn400WhenNameIsBlank() throws Exception {
            // Given
            UomRequest request = new UomRequest("", "desc", new BigDecimal("1.0"), 1L);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"));
        }

        @Test
        @DisplayName("should return 400 when name is too long")
        void create_shouldReturn400WhenNameTooLong() throws Exception {
            // Given
            String longName = "a".repeat(51);
            UomRequest request = new UomRequest(longName, "desc", new BigDecimal("1.0"), 1L);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 400 when conversionFactorToBase is null")
        void create_shouldReturn400WhenConversionFactorNull() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", null, 1L);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 400 when conversionFactorToBase is negative")
        void create_shouldReturn400WhenConversionFactorNegative() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("-1.0"), 1L);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 400 when conversionFactorToBase is zero")
        void create_shouldReturn400WhenConversionFactorZero() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("0"), 1L);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 400 when uomStatusId is null")
        void create_shouldReturn400WhenUomStatusIdNull() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("1.0"), null);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 409 when name already exists")
        void create_shouldReturn409WhenNameAlreadyExists() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("1.0"), 1L);
            given(service.save(any(UomRequest.class)))
                .willThrow(new ResourceConflictException("Uom with name 'Kilogram' already exists"));

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003));
        }

        @Test
        @DisplayName("should return 404 when UomStatus not exists")
        void create_shouldReturn404WhenUomStatusNotExists() throws Exception {
            // Given
            UomRequest request = new UomRequest("Kilogram", "desc", new BigDecimal("1.0"), 999L);
            given(service.save(any(UomRequest.class)))
                .willThrow(new ResourceNotFoundException("UomStatus", "id", "999"));

            // When & Then
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }
    }

    @Nested
    @DisplayName("GET /{id} - Get UOM by ID")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 with UOM when exists")
        void findById_shouldReturn200WhenExists() throws Exception {
            // Given
            Long id = 1L;
            UomResponse response = new UomResponse(1L, "Kilogram", "Base unit", new BigDecimal("1.0"), null);
            given(service.findById(id)).willReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL + "{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kilogram"));
        }

        @Test
        @DisplayName("should return 404 when UOM not exists")
        void findById_shouldReturn404WhenNotExists() throws Exception {
            // Given
            Long id = 999L;
            given(service.findById(id))
                .willThrow(new ResourceNotFoundException("Uom", "id", "999"));

            // When & Then
            mockMvc.perform(get(BASE_URL + "{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 400 when ID is negative")
        void findById_shouldReturn400WhenIdNegative() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should return 400 when ID is zero")
        void findById_shouldReturn400WhenIdZero() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }

    @Nested
    @DisplayName("GET / - Get All UOMs")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 with paged results")
        void findAll_shouldReturn200WithPagedResults() throws Exception {
            // Given
            UomResponse response1 = new UomResponse(1L, "Kilogram", "desc1", new BigDecimal("1.0"), null);
            UomResponse response2 = new UomResponse(2L, "Gram", "desc2", new BigDecimal("0.001"), null);
            Page<UomResponse> page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 20), 2);
            
            given(service.findAll(any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements").value(2));
        }

        @Test
        @DisplayName("should return 200 with empty page when no data")
        void findAll_shouldReturn200WithEmptyPageWhenNoData() throws Exception {
            // Given
            Page<UomResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            given(service.findAll(any())).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0));
        }

        @Test
        @DisplayName("should handle pagination parameters")
        void findAll_shouldHandlePaginationParameters() throws Exception {
            // Given
            Page<UomResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 10), 0);
            given(service.findAll(any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page", allOf(
                            hasEntry("number", 1),
                            hasEntry("size", 10)
                    )));
        }

        @Test
        @DisplayName("should handle sorting parameters")
        void findAll_shouldHandleSortingParameters() throws Exception {
            // Given
            Page<UomResponse> page = new PageImpl<>(Collections.emptyList());
            given(service.findAll(any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("sort", "name,asc"))
                .andExpect(status().isOk());
            
            verify(service).findAll(any());
        }
    }

    @Nested
    @DisplayName("GET /search - Search by Name")
    class SearchByNameTests {

        @Test
        @DisplayName("should return 200 with matching results")
        void searchByName_shouldReturn200WithResults() throws Exception {
            // Given
            UomResponse response = new UomResponse(1L, "Kilogram", "desc", new BigDecimal("1.0"), null);
            Page<UomResponse> page = new PageImpl<>(List.of(response));
            
            given(service.findAllByName(eq("kilo"), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL + "search")
                    .param("name", "kilo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("should return 200 with empty results when no match")
        void searchByName_shouldReturn200WithEmptyResults() throws Exception {
            // Given
            Page<UomResponse> emptyPage = new PageImpl<>(Collections.emptyList());
            given(service.findAllByName(eq("nonexistent"), any())).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get(BASE_URL + "search")
                    .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("should return 400 when name parameter is blank")
        void searchByName_shouldReturn400WhenNameBlank() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "search")
                    .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }

        @Test
        @DisplayName("should be case insensitive")
        void searchByName_shouldBeCaseInsensitive() throws Exception {
            // Given
            Page<UomResponse> page = new PageImpl<>(Collections.emptyList());
            given(service.findAllByName(eq("KILO"), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL + "search")
                    .param("name", "KILO"))
                .andExpect(status().isOk());
            
            verify(service).findAllByName(eq("KILO"), any());
        }
    }

    @Nested
    @DisplayName("GET /filter/status/{uomStatusId} - Filter by Status")
    class FilterByStatusTests {

        @Test
        @DisplayName("should return 200 with filtered results")
        void filterByStatus_shouldReturn200WithResults() throws Exception {
            // Given
            Long statusId = 1L;
            UomResponse response = new UomResponse(1L, "Kilogram", "desc", new BigDecimal("1.0"), null);
            Page<UomResponse> page = new PageImpl<>(List.of(response));
            
            given(service.findAllByUomStatusId(eq(statusId), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get(BASE_URL + "filter/status/{statusId}", statusId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("should return 200 with empty results when no match")
        void filterByStatus_shouldReturn200WithEmptyResults() throws Exception {
            // Given
            Long statusId = 999L;
            Page<UomResponse> emptyPage = new PageImpl<>(Collections.emptyList());
            given(service.findAllByUomStatusId(eq(statusId), any())).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get(BASE_URL + "filter/status/{statusId}", statusId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("should return 400 when statusId is invalid")
        void filterByStatus_shouldReturn400WhenStatusIdInvalid() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "filter/status/{statusId}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }

    @Nested
    @DisplayName("PUT /{id} - Update UOM")
    class UpdateUomTests {

        @Test
        @DisplayName("should return 200 with updated UOM when valid")
        void update_shouldReturn200WithValidRequest() throws Exception {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Kilogram Updated", "New description", new BigDecimal("1.500"));
            UomResponse response = new UomResponse(1L, "Kilogram Updated", "New description", new BigDecimal("1.500"), null);
            
            given(service.update(eq(id), any(UomUpdate.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put(BASE_URL + "{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kilogram Updated"))
                .andExpect(jsonPath("$.description").value("New description"));
        }

        @Test
        @DisplayName("should return 404 when UOM not exists")
        void update_shouldReturn404WhenIdNotExists() throws Exception {
            // Given
            Long id = 999L;
            UomUpdate updateRequest = new UomUpdate("Name", "desc", new BigDecimal("1.0"));
            
            given(service.update(eq(id), any(UomUpdate.class)))
                .willThrow(new ResourceNotFoundException("Uom", "id", "999"));

            // When & Then
            mockMvc.perform(put(BASE_URL + "{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 409 when new name already exists")
        void update_shouldReturn409WhenNewNameExists() throws Exception {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("Gram", "desc", new BigDecimal("1.0"));
            
            given(service.update(eq(id), any(UomUpdate.class)))
                .willThrow(new ResourceConflictException("Name already exists"));

            // When & Then
            mockMvc.perform(put(BASE_URL + "{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003));
        }

        @Test
        @DisplayName("should return 400 on validation error")
        void update_shouldReturn400OnValidationError() throws Exception {
            // Given
            Long id = 1L;
            UomUpdate updateRequest = new UomUpdate("", "desc", new BigDecimal("-1.0"));

            // When & Then
            mockMvc.perform(put(BASE_URL + "{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }

    @Nested
    @DisplayName("DELETE /{id} - Delete UOM")
    class DeleteUomTests {

        @Test
        @DisplayName("should return 204 when UOM deleted successfully")
        void delete_shouldReturn204WhenExists() throws Exception {
            // Given
            Long id = 1L;
            doNothing().when(service).deleteById(id);

            // When & Then
            mockMvc.perform(delete(BASE_URL + "{id}", id))
                .andExpect(status().isNoContent());
            
            verify(service).deleteById(id);
        }

        @Test
        @DisplayName("should return 404 when UOM not exists")
        void delete_shouldReturn404WhenNotExists() throws Exception {
            // Given
            Long id = 999L;
            doThrow(new ResourceNotFoundException("Uom", "id", "999"))
                .when(service).deleteById(id);

            // When & Then
            mockMvc.perform(delete(BASE_URL + "{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 400 when ID is invalid")
        void delete_shouldReturn400WhenIdInvalid() throws Exception {
            // When & Then
            mockMvc.perform(delete(BASE_URL + "{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }

    @Nested
    @DisplayName("PATCH /{id}/status/{statusId} - Change Status")
    class ChangeStatusTests {

        @Test
        @DisplayName("should return 200 when status changed successfully")
        void changeStatus_shouldReturn200WhenBothExist() throws Exception {
            // Given
            Long uomId = 1L;
            Long statusId = 2L;

            when(service.changeStatus(uomId, statusId)).thenReturn(null);

            // When & Then
            mockMvc.perform(patch(BASE_URL + "{id}/status/{statusId}", uomId, statusId))
                .andExpect(status().isOk());
            
            verify(service).changeStatus(uomId, statusId);
        }

        @Test
        @DisplayName("should return 404 when UOM not exists")
        void changeStatus_shouldReturn404WhenUomNotExists() throws Exception {
            // Given
            Long uomId = 999L;
            Long statusId = 1L;
            doThrow(new ResourceNotFoundException("Uom", "id", "999"))
                .when(service).changeStatus(uomId, statusId);

            // When & Then
            mockMvc.perform(patch(BASE_URL + "{id}/status/{statusId}", uomId, statusId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 404 when Status not exists")
        void changeStatus_shouldReturn404WhenStatusNotExists() throws Exception {
            // Given
            Long uomId = 1L;
            Long statusId = 999L;
            doThrow(new ResourceNotFoundException("UomStatus", "id", "999"))
                .when(service).changeStatus(uomId, statusId);

            // When & Then
            mockMvc.perform(patch(BASE_URL + "{id}/status/{statusId}", uomId, statusId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 400 when IDs are invalid")
        void changeStatus_shouldReturn400WhenIdsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(patch(BASE_URL + "{id}/status/{statusId}", -1, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }

    @Nested
    @DisplayName("GET /check-name - Check Name Availability")
    class CheckNameTests {

        @Test
        @DisplayName("should return true when name is taken")
        void checkName_shouldReturnTrueWhenNameTaken() throws Exception {
            // Given
            given(service.isNameTaken("Kilogram")).willReturn(true);

            // When & Then
            mockMvc.perform(get(BASE_URL + "check-name")
                    .param("name", "Kilogram"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("should return false when name is available")
        void checkName_shouldReturnFalseWhenNameAvailable() throws Exception {
            // Given
            given(service.isNameTaken("NewName")).willReturn(false);

            // When & Then
            mockMvc.perform(get(BASE_URL + "check-name")
                    .param("name", "NewName"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("should return 400 when name parameter is blank")
        void checkName_shouldReturn400WhenNameBlank() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "check-name")
                    .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002));
        }
    }
}
