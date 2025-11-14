package com.web.dto.catalog.Stop.mapper;

import com.web.dto.catalog.Stop.StopCreateRequest;
import com.web.dto.catalog.Stop.StopResponse;
import com.web.entity.Stop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StopMapper {
    
    // Entity → Response
    StopResponse toResponse(Stop stop);
    
    List<StopResponse> toResponseList(List<Stop> stops);
    
    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "ticketsFrom", ignore = true)
    @Mapping(target = "ticketsTo", ignore = true)
    @Mapping(target = "fareRulesFrom", ignore = true)
    @Mapping(target = "fareRulesTo", ignore = true)
    @Mapping(target = "parcelsFrom", ignore = true)
    @Mapping(target = "parcelsTo", ignore = true)
    Stop toEntity(StopCreateRequest request);
}

