package com.elara.app.unit_of_measure_service.service.interfaces;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UomStatusService {

    UomStatusResponse save(UomStatusRequest request);

    UomStatusResponse update(Long id, UomStatusUpdate request);

    void deleteById(Long id);

    Optional<UomStatusResponse> findById(Long id);

    Page<UomStatusResponse> findAll(Pageable pageable);

    Page<UomStatusResponse> findAllByName(String name, Pageable pageable);

    Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable);

    Boolean isNameTaken(String name);

    void changeStatus(Long id, Boolean isUsable);

}
