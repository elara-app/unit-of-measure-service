package com.elara.app.unit_of_measure_service.mapper;

import com.elara.app.unit_of_measure_service.dto.request.UomStatusRequest;
import com.elara.app.unit_of_measure_service.dto.response.UomStatusResponse;
import com.elara.app.unit_of_measure_service.model.UomStatus;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
//@Mapper(componentModel = "spring") // Uncomment if you want to use Spring's dependency injection
public interface UomStatusMapper {

    UomStatusMapper INSTANCE = Mappers.getMapper(UomStatusMapper.class);

    UomStatus toEntity(UomStatusRequest request);

    UomStatusResponse toResponse(UomStatus entity);

}
