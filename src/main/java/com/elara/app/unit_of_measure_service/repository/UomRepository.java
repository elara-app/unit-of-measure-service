package com.elara.app.unit_of_measure_service.repository;

import com.elara.app.unit_of_measure_service.model.Uom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UomRepository extends JpaRepository<Uom, Long> {

    Optional<Uom> findByNameContainingIgnoreCase(String name);

    Page<Uom> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Uom> findAllByUomStatusId(Long uomStatusId, Pageable pageable);

    Boolean existsByNameIgnoreCase(String name);

}
