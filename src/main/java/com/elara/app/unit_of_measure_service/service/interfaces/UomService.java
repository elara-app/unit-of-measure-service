package com.elara.app.unit_of_measure_service.service.interfaces;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UomService {

    UomResponse save(UomRequest request);

    UomResponse update(Long id, UomRequest request);

    void deleteById(Long id);

    UomResponse findById(Long id);

    Page<UomResponse> findAll(Pageable pageable);

    Page<UomResponse> findAllByName(String name, Pageable pageable);

    Page<UomResponse> findAllByUomStatusId(Long uomStatusId, Pageable pageable);

    Boolean isNameTaken(String name);

    void changeStatus(Long id, Long uomStatusId);

}
