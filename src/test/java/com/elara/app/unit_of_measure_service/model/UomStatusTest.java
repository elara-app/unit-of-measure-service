package com.elara.app.unit_of_measure_service.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class UomStatusTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void whenInvalidName_thenValidationFails(String name) {
        // Test that invalid names (null, empty, or too long) fail validation
        UomStatus status = UomStatus.builder()
                .name(name)
                .isUsable(true)
                .build();
        Set<ConstraintViolation<UomStatus>> violations = validator.validate(status);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Name")));
    }

    static Stream<String> invalidNames() {
        // Provides invalid name values for parameterized test
        return Stream.of(
                null,
                "",
                " ",
                "a".repeat(51)
        );
    }

    @Test
    void whenDescriptionExceedsMaxLength_thenValidationFails() {
        // Test that description exceeding max length fails validation
        UomStatus status = UomStatus.builder()
                .name("Valid Name")
                .isUsable(true)
                .description("a".repeat(201))
                .build();
        Set<ConstraintViolation<UomStatus>> violations = validator.validate(status);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Description cannot exceed 200 characters")));
    }

    @ParameterizedTest
    @MethodSource("validDescriptions")
    void whenValidDescription_thenValidationPasses(String description) {
        // Test that valid descriptions (null, empty, or non-empty) pass validation
        UomStatus status = UomStatus.builder()
                .name("Valid Name")
                .isUsable(true)
                .description(description)
                .build();
        Set<ConstraintViolation<UomStatus>> violations = validator.validate(status);
        assertTrue(violations.isEmpty());
    }

    static Stream<String> validDescriptions() {
        // Provides valid description values for parameterized test
        return Stream.of(
                null,
                "",
                "Some description"
        );
    }

    @Test
    void whenBuilderWithoutIsUsable_thenDefaultIsTrue() {
        // Test that the builder defaults isUsable to true if not set
        UomStatus status = UomStatus.builder()
                .name("Valid Name")
                .build();
        assertEquals(Boolean.TRUE, status.getIsUsable());
    }

    @Test
    void whenIsUsableIsNull_thenValidationFails() {
        // Test that isUsable cannot be null
        UomStatus status = UomStatus.builder()
                .name("Valid Name")
                .isUsable(null)
                .build();
        Set<ConstraintViolation<UomStatus>> violations = validator.validate(status);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Is usable cannot be null")));
    }

    @Test
    void whenAllFieldsAreValid_thenValidationPasses() {
        // Test that all valid fields pass validation
        UomStatus status = UomStatus.builder()
                .name("Valid Name")
                .isUsable(true)
                .description("Valid description")
                .build();
        Set<ConstraintViolation<UomStatus>> violations = validator.validate(status);
        assertTrue(violations.isEmpty());
    }

}