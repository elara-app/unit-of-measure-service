package com.elara.app.unit_of_measure_service.controller;

import com.elara.app.unit_of_measure_service.config.GlobalExceptionHandler;
import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.exceptions.UnexpectedErrorException;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UomStatusController.class)
@Import({GlobalExceptionHandler.class, UomStatusControllerTest.TestConfig.class})
class UomStatusControllerTest {

    @AfterEach
    void tearDown() {
        reset(service, messageService);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("uomStatusService")
    private UomStatusService service;

    // GlobalExceptionHandler dependency
    @Autowired
    private MessageService messageService;

    private static final String BASE_URL = "/api/v1/uom-status";

    @TestConfiguration
    static class TestConfig {
        @Bean
        UomStatusService uomStatusService() {
            return mock(UomStatusService.class);
        }
        @Bean
        MessageService messageService() {
            return mock(MessageService.class);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/uom-status - createUomStatus")
    class CreateUomStatusTests {
        @Test
        @DisplayName("should return 201 with created resource")
        void create_shouldReturn201() throws Exception {
            UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
            UomStatusResponse response = new UomStatusResponse(1L, "Active", "desc", true);

            given(service.save(any())).willReturn(response);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Active"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.isUsable").value(true));
        }

        @Test
        @DisplayName("should return 400 when validation fails (body)")
        void create_shouldReturn400_onValidationErrors() throws Exception {
            // name blank and isUsable null to trigger two field errors
            UomStatusRequest invalid = new UomStatusRequest("", "d", null);
            given(messageService.getMessage(anyString(), any())).willAnswer(invocation -> {
                String key = invocation.getArgument(0);
                Object field = invocation.getArgument(1);
                return key + ":" + field; // simple deterministic message
            });

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"))
                .andExpect(jsonPath("$.path", containsString(BASE_URL)));
        }

        @Test
        @DisplayName("should return 409 when service reports conflict")
        void create_shouldReturn409_onConflict() throws Exception {
            UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
            given(service.save(any())).willThrow(new ResourceConflictException("conflict"));

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.value").value("RESOURCE_CONFLICT"))
                .andExpect(jsonPath("$.path", containsString(BASE_URL)));
        }

        @Test
        @DisplayName("should return 500 when service throws unexpected error")
        void create_shouldReturn500_onUnexpected() throws Exception {
            UomStatusRequest request = new UomStatusRequest("Active", "desc", true);
            given(service.save(any())).willThrow(new UnexpectedErrorException("boom"));

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(1006))
                .andExpect(jsonPath("$.value").value("UNEXPECTED_ERROR"))
                .andExpect(jsonPath("$.path", containsString(BASE_URL)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/uom-status/{id} - getUomStatusById")
    class GetByIdTests {
        @Test
        @DisplayName("should return 200 with resource")
        void getById_shouldReturn200() throws Exception {
            UomStatusResponse response = new UomStatusResponse(5L, "Active", "desc", true);
            given(service.findById(5L)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Active"));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getById_shouldReturn404() throws Exception {
            given(service.findById(999L)).willThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(get(BASE_URL + "/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004))
                .andExpect(jsonPath("$.value").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/uom-status - getAllUomStatuses")
    class GetAllTests {
        @Test
        @DisplayName("should return 200 with paged content")
        void getAll_shouldReturn200() throws Exception {
            List<UomStatusResponse> content = List.of(
                new UomStatusResponse(1L, "Active", "desc", true),
                new UomStatusResponse(2L, "Inactive", "desc", false)
            );
            Pageable pageable = PageRequest.of(0, 20);
            Page<UomStatusResponse> page = new PageImpl<>(content, pageable, content.size());
            given(service.findAll(any())).willReturn(page);

            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("should return 500 when service fails")
        void getAll_shouldReturn500_onUnexpected() throws Exception {
            given(service.findAll(any())).willThrow(new UnexpectedErrorException("boom"));

            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(1006));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/uom-status/search - searchUomStatusesByName")
    class SearchByNameTests {
        @Test
        @DisplayName("should return 200 with results")
        void search_shouldReturn200() throws Exception {
            List<UomStatusResponse> content = List.of(new UomStatusResponse(1L, "Active", "desc", true));
            Page<UomStatusResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
            given(service.findAllByName(eq("act"), any())).willReturn(page);

            mockMvc.perform(get(BASE_URL + "/search").param("name", "act"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Active"));
        }

        @Test
        @DisplayName("should return 400 when missing name param (handled by GlobalExceptionHandler)")
        void search_shouldReturn400_onMissingParam() throws Exception {
            given(messageService.getMessage(eq("parameter.missing"), any())).willReturn("Missing param");

            mockMvc.perform(get(BASE_URL + "/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"))
                .andExpect(jsonPath("$.message").value("Missing param"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/uom-status/filter - filterUomStatusesByUsability")
    class FilterByUsabilityTests {
        @Test
        @DisplayName("should return 200 with filtered results")
        void filter_shouldReturn200() throws Exception {
            List<UomStatusResponse> content = List.of(new UomStatusResponse(1L, "Active", "desc", true));
            Page<UomStatusResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
            given(service.findAllByIsUsable(eq(true), any())).willReturn(page);

            mockMvc.perform(get(BASE_URL + "/filter").param("isUsable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].isUsable").value(true));
        }

        @Test
        @DisplayName("should return 400 when missing isUsable param (handled by GlobalExceptionHandler)")
        void filter_shouldReturn400_onMissingParam() throws Exception {
            given(messageService.getMessage(eq("parameter.missing"), any())).willReturn("Missing param");

            mockMvc.perform(get(BASE_URL + "/filter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/uom-status/check-name - isNameTaken")
    class CheckNameTests {
        @Test
        @DisplayName("should return 200 with true/false")
        void isNameTaken_shouldReturn200() throws Exception {
            given(service.isNameTaken("Active")).willReturn(true);

            mockMvc.perform(get(BASE_URL + "/check-name").param("name", "Active"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("should return 400 when missing name param (handled by GlobalExceptionHandler)")
        void isNameTaken_shouldReturn400_onMissingParam() throws Exception {
            given(messageService.getMessage(eq("parameter.missing"), any())).willReturn("Missing param");

            mockMvc.perform(get(BASE_URL + "/check-name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/uom-status/{id} - updateUomStatus")
    class UpdateTests {
        @Test
        @DisplayName("should return 200 with updated resource")
        void update_shouldReturn200() throws Exception {
            UomStatusUpdate update = new UomStatusUpdate("New", "d");
            UomStatusResponse response = new UomStatusResponse(1L, "New", "d", true);
            given(service.update(eq(1L), any())).willReturn(response);

            mockMvc.perform(put(BASE_URL + "/{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New"));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void update_shouldReturn404() throws Exception {
            UomStatusUpdate update = new UomStatusUpdate("New", "d");
            given(service.update(eq(9L), any())).willThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(put(BASE_URL + "/{id}", 9)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 409 when name conflict")
        void update_shouldReturn409_onConflict() throws Exception {
            UomStatusUpdate update = new UomStatusUpdate("Dup", "d");
            given(service.update(eq(2L), any())).willThrow(new ResourceConflictException("conflict"));

            mockMvc.perform(put(BASE_URL + "/{id}", 2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003));
        }

        @Test
        @DisplayName("should return 400 when validation fails (body)")
        void update_shouldReturn400_onValidationErrors() throws Exception {
            UomStatusUpdate invalid = new UomStatusUpdate("x".repeat(51), "ok");
            given(messageService.getMessage(anyString(), any())).willAnswer(invocation -> {
                String key = invocation.getArgument(0);
                Object field = invocation.getArgument(1);
                return key + ":" + field;
            });

            mockMvc.perform(put(BASE_URL + "/{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/uom-status/{id}/status - changeUomStatusUsability")
    class ChangeStatusTests {
        @Test
        @DisplayName("should return 204 on success")
        void changeStatus_shouldReturn204() throws Exception {
            doNothing().when(service).changeStatus(1L, true);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", 1)
                    .param("isUsable", "true"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when id not found")
        void changeStatus_shouldReturn404() throws Exception {
            org.mockito.Mockito.doThrow(new ResourceNotFoundException("not found"))
                .when(service).changeStatus(999L, false);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", 999)
                    .param("isUsable", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004));
        }

        @Test
        @DisplayName("should return 400 when missing isUsable param (handled by GlobalExceptionHandler)")
        void changeStatus_shouldReturn400_onMissingParam() throws Exception {
            given(messageService.getMessage(eq("parameter.missing"), any())).willReturn("Missing param");

            mockMvc.perform(patch(BASE_URL + "/{id}/status", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.value").value("INVALID_DATA"));
        }
    }
}
