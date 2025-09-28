package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomResponse;
import com.elara.app.unit_of_measure_service.dto.update.UomUpdate;
import com.elara.app.unit_of_measure_service.model.Uom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UomMapper {

    Uom toEntity(UomRequest request);

    @Mapping(target = "uomStatusId", source = "uomStatus.id")
    UomResponse toResponse(Uom entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uomStatus", ignore = true)
    void updateEntityFromDto(@MappingTarget Uom existing, UomUpdate update);

}
