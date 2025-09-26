package com.elara.app.unit_of_measure_service.service.imp;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.service.interfaces.UomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class UomServiceImp implements UomService {

    @Override
    public UomResponse save(UomRequest request) {
        return null;
    }

    @Override
    public UomResponse update(Long id, UomRequest request) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public UomResponse findById(Long id) {
        return null;
    }

    @Override
    public Page<UomResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomResponse> findAllByName(String name, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UomResponse> findAllByUomStatusId(Long uomStatusId, Pageable pageable) {
        return null;
    }

    @Override
    public Boolean isNameTaken(String name) {
        return null;
    }

    @Override
    public void changeStatus(Long id, Long uomStatusId) {

    }

}
