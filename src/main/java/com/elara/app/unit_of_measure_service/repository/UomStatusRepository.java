package com.elara.app.unit_of_measure_service.repository;

import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UomStatusRepository extends JpaRepository<UomStatus, Long> {

    Optional<UomStatus> findByNameContainingIgnoreCase(String name);

    Page<UomStatus> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<UomStatus> findAllByIsUsable(Boolean isUsable, Pageable pageable);

    Boolean existsByName(String name);

    @Modifying
    @Query("UPDATE uom_status u SET u.isUsable = :isUsable WHERE u.id = :id")
    void changeStatus(@Param("id") Long id, @Param("isUsable") Boolean isUsable);

}
