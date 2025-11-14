package com.web.dto.catalog.Bus.mapper;

import com.web.dto.catalog.Bus.BusCreateRequest;
import com.web.dto.catalog.Bus.BusResponse;
import com.web.dto.catalog.Bus.BusUpdateRequest;
import com.web.entity.Bus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusMapper {
    
    // Entity → Response
    BusResponse toResponse(Bus bus);
    
    List<BusResponse> toResponseList(List<Bus> buses);
    
    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "trips", ignore = true)
    Bus toEntity(BusCreateRequest request);
    
    // Update parcial
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "trips", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(BusUpdateRequest request, @MappingTarget Bus bus);
}

