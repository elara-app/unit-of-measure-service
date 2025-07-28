package com.elara.app.unit_of_measure_service.service.imp;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.service.interfaces.UomStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class UomStatusServiceImp implements UomStatusService {
    @Override
    public UomStatusResponse save(UomStatusRequest request) {
        return null;
    }

    @Override
    public UomStatusResponse update(Long id, UomStatusUpdate request) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public Optional<UomStatusResponse> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Page<UomStatusResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomStatusResponse> findAllByName(String name, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomStatusResponse> findAllByIsUsable(Boolean isUsable, Pageable pageable) {
        return null;
    }

    @Override
    public Boolean existsByName(String name) {
        return null;
    }

    @Override
    public void changeStatus(Long id, Boolean isUsable) {

    }
}
