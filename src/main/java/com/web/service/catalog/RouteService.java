package com.web.service.catalog;

import com.web.dto.catalog.Route.RouteCreateRequest;
import com.web.dto.catalog.Route.RouteDetailResponse;
import com.web.dto.catalog.Route.RouteResponse;
import com.web.dto.catalog.Route.RouteUpdateRequest;
import com.web.dto.catalog.Stop.StopCreateRequest;

import java.util.List;

public interface RouteService {
    
    RouteResponse createRoute(RouteCreateRequest request);
    
    List<RouteResponse> getAllRoutes();
    
    RouteDetailResponse getRouteById(Long id);
    
    RouteResponse updateRoute(Long id, RouteUpdateRequest request);
    
    void deleteRoute(Long id);
    
    RouteDetailResponse addStop(Long routeId, StopCreateRequest request);
    
    void removeStop(Long routeId, Long stopId);
    
    List<RouteResponse> findRoutesConnecting(String origin, String destination);
}

