package com.elara.app.unit_of_measure_service.repository;

import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UomStatusRepositoryTest {

    @Autowired
    private UomStatusRepository uomStatusRepository;

    @Test
    @DisplayName("Should save and find UomStatus by name")
    void shouldSaveAndFindUomStatusByNameIgnoreCase() {
        UomStatus uomStatus = UomStatus.builder()
                .name("Active")
                .description("Currently active status")
                .build();
        uomStatusRepository.save(uomStatus);
        // Note: After the entity is saved, the ID field is automatically populated.

        Optional<UomStatus> found = uomStatusRepository.findByNameContainingIgnoreCase("active");
        assertThat(found.isPresent(), is(Boolean.TRUE));
        assertThat(found.get(), equalTo(uomStatus));
    }

    @Test
    @DisplayName("Should return empty when name ignore case not found")
    void shouldReturnEmptyWhenNameIgnoreCaseNotFound() {
        uomStatusRepository.save(UomStatus.builder().name("Active").build());
        Optional<UomStatus> found = uomStatusRepository.findByNameContainingIgnoreCase("NonExistent");
        assertThat(found.isPresent(), is(Boolean.FALSE));
    }

    @Test
    @DisplayName("Should find all by name containing ignore case")
    void shouldFindAllByNameContainingIgnoreCase() {
        uomStatusRepository.save(UomStatus.builder().name("Active").build());
        uomStatusRepository.save(UomStatus.builder().name("Inactive").build());
        uomStatusRepository.save(UomStatus.builder().name("Archived").build());
        Page<UomStatus> page = uomStatusRepository.findAllByNameContainingIgnoreCase("act", PageRequest.of(0, 10));
        assertThat(page.getTotalElements(), is(2L));
        assertTrue(page.get().allMatch(uomStatus -> uomStatus.getName().toLowerCase().contains("act")));
    }

    @Test
    @DisplayName("Should return empty page when no name matches")
    void shouldReturnEmptyPageWhenNoNameMatches() {
        uomStatusRepository.save(UomStatus.builder().name("Active").build());
        uomStatusRepository.save(UomStatus.builder().name("Inactive").build());
        Page<UomStatus> page = uomStatusRepository.findAllByNameContainingIgnoreCase("xyz", PageRequest.of(0, 10));
        assertThat(page.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should find all by isUsable")
    void shouldFinAllByIsUsable() {
        uomStatusRepository.save(UomStatus.builder().name("Active").isUsable(false).build());
        uomStatusRepository.save(UomStatus.builder().name("Inactive").isUsable(true).build());
        uomStatusRepository.save(UomStatus.builder().name("Deprecated").isUsable(false).build());
        Page<UomStatus> page = uomStatusRepository.findAllByIsUsable(Boolean.FALSE, PageRequest.of(0, 10));
        assertThat(page.getTotalElements(), is(2L));
        assertTrue(page.get().allMatch(uomStatus -> uomStatus.getIsUsable().equals(Boolean.FALSE)));
    }

    @Test
    @DisplayName("Should return empty page when no isUsable matches")
    void shouldReturnEmptyPageWhenNoIsUsableMatches() {
        uomStatusRepository.save(UomStatus.builder().name("Active").isUsable(false).build());
        uomStatusRepository.save(UomStatus.builder().name("Inactive").isUsable(false).build());
        Page<UomStatus> page = uomStatusRepository.findAllByIsUsable(Boolean.TRUE, PageRequest.of(0, 10));
        assertThat(page.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should update existing status by id")
    void shouldUpdateExistingStatusById() {
        uomStatusRepository.save(UomStatus.builder().name("Active").build());

        Optional<UomStatus> found = uomStatusRepository.findByNameContainingIgnoreCase("active");
        assertTrue(found.isPresent());
        final Long foudId = found.get().getId();
        found.get().setDescription("Updated description");
        found.get().setIsUsable(false);
        uomStatusRepository.save(found.get());

        Optional<UomStatus> updated = uomStatusRepository.findById(foudId);
        assertTrue(updated.isPresent());
        assertThat(updated.get().getDescription(), equalTo("Updated description"));
        assertThat(updated.get().getIsUsable(), equalTo(Boolean.FALSE));
    }

    @Test
    @DisplayName("Should delete existing status by id")
    void shouldDeleteExistingStatusById() {
        uomStatusRepository.save(UomStatus.builder().name("Active").build());
        Optional<UomStatus> found = uomStatusRepository.findByNameContainingIgnoreCase("Active");
        assertTrue(found.isPresent());

        uomStatusRepository.deleteById(found.get().getId());

        Optional<UomStatus> deleted = uomStatusRepository.findById(found.get().getId());
        assertFalse(deleted.isPresent());
    }

}