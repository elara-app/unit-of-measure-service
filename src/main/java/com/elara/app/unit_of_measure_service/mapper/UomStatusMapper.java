package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UomStatusMapper {

    UomStatus toEntity(UomStatusRequest request);

    UomStatusResponse toResponse(UomStatus entity);

}
