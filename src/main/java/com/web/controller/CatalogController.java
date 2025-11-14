package com.web.controller;

import com.web.dto.catalog.Route.RouteCreateRequest;
import com.web.dto.catalog.Route.RouteDetailResponse;
import com.web.dto.catalog.Route.RouteResponse;
import com.web.dto.catalog.Route.RouteUpdateRequest;
import com.web.dto.catalog.Stop.StopCreateRequest;
import com.web.service.catalog.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class CatalogController {

    private final RouteService routeService;

    // Endpoints públicos (consulta)

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDetailResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<RouteDetailResponse> getRouteWithStops(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    // Endpoints protegidos (solo ADMIN)

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request) {
        RouteResponse response = routeService.updateRoute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{routeId}/stops")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDetailResponse> addStop(
            @PathVariable Long routeId,
            @Valid @RequestBody StopCreateRequest request) {
        RouteDetailResponse response = routeService.addStop(routeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{routeId}/stops/{stopId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeStop(
            @PathVariable Long routeId,
            @PathVariable Long stopId) {
        routeService.removeStop(routeId, stopId);
        return ResponseEntity.noContent().build();
    }
}
