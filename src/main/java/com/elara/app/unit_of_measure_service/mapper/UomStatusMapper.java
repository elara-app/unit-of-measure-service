package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomStatusUpdate;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UomStatusMapper {

    UomStatus updateEntityFromDto(UomStatusRequest request);

    UomStatusResponse toResponse(UomStatus entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isUsable", ignore = true)
    UomStatus updateEntityFromDto(@MappingTarget UomStatus existing, UomStatusUpdate update);

}
