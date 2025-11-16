package com.web.service.catalog;

import com.web.dto.catalog.Route.RouteCreateRequest;
import com.web.dto.catalog.Route.RouteDetailResponse;
import com.web.dto.catalog.Route.RouteResponse;
import com.web.dto.catalog.Route.RouteUpdateRequest;
import com.web.dto.catalog.Route.mapper.RouteMapper;
import com.web.dto.catalog.Stop.StopCreateRequest;
import com.web.dto.catalog.Stop.mapper.StopMapper;
import com.web.entity.Route;
import com.web.entity.Stop;
import com.web.exception.BusinessException;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.RouteRepository;
import com.web.repository.StopRepository;
import com.web.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private StopMapper stopMapper;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Route route;
    private RouteResponse routeResponse;
    private RouteDetailResponse routeDetailResponse;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá - Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .build();

        routeResponse = new RouteResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín", 
                java.math.BigDecimal.valueOf(500.0), 360, true
        );

        routeDetailResponse = new RouteDetailResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín", 
                java.math.BigDecimal.valueOf(500.0), 360, true, List.of()
        );
    }

    @Test
    void shouldCreateRoute_WithValidRequest_ReturnRouteResponse() {
        // Given
        RouteCreateRequest request = new RouteCreateRequest(
                "R001", "Bogotá - Medellín", "Bogotá", "Medellín", 
                java.math.BigDecimal.valueOf(500.0), 360
        );

        when(routeRepository.existsByCode("R001")).thenReturn(false);
        when(routeMapper.toEntity(request)).thenReturn(route);
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(routeMapper.toResponse(any(Route.class))).thenReturn(routeResponse);

        // When
        RouteResponse result = routeService.createRoute(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(routeRepository).existsByCode("R001");
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldGetAllRoutes_ReturnRouteList() {
        // Given
        List<Route> routes = List.of(route);
        List<RouteResponse> responses = List.of(routeResponse);

        when(routeRepository.findAll()).thenReturn(routes);
        when(routeMapper.toResponseList(routes)).thenReturn(responses);

        // When
        List<RouteResponse> result = routeService.getAllRoutes();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(routeRepository).findAll();
    }

    @Test
    void shouldGetRouteById_WithValidId_ReturnRouteDetail() {
        // Given
        when(routeRepository.findByIdWithStops(1L)).thenReturn(Optional.of(route));
        when(routeMapper.toDetailResponse(route)).thenReturn(routeDetailResponse);

        // When
        RouteDetailResponse result = routeService.getRouteById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(routeRepository).findByIdWithStops(1L);
    }

    @Test
    void shouldUpdateRoute_WithValidRequest_UpdateRoute() {
        // Given
        RouteUpdateRequest request = new RouteUpdateRequest(
                "Bogotá - Cali", java.math.BigDecimal.valueOf(600.0), 420, null
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);
        when(routeMapper.toResponse(any(Route.class))).thenReturn(routeResponse);

        // When
        RouteResponse result = routeService.updateRoute(1L, request);

        // Then
        assertThat(result).isNotNull();
        verify(routeMapper).updateEntityFromRequest(request, route);
        verify(routeRepository).save(route);
    }

    @Test
    void shouldAddStop_WithValidRequest_AddStopToRoute() {
        // Given
        StopCreateRequest request = new StopCreateRequest(
                1L, "Pereira", 2, null, null
        );

        Stop stop = Stop.builder()
                .id(1L)
                .route(route)
                .name("Pereira")
                .order(2)
                .build();

        when(routeRepository.findByIdWithStops(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(false);
        when(stopMapper.toEntity(request)).thenReturn(stop);
        when(stopRepository.save(any(Stop.class))).thenReturn(stop);
        when(routeRepository.findByIdWithStops(1L)).thenReturn(Optional.of(route));
        when(routeMapper.toDetailResponse(route)).thenReturn(routeDetailResponse);

        // When
        RouteDetailResponse result = routeService.addStop(1L, request);

        // Then
        assertThat(result).isNotNull();
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldRemoveStop_WithValidStopId_RemoveStop() {
        // Given
        Stop stop = Stop.builder()
                .id(1L)
                .route(route)
                .build();

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));

        // When
        routeService.removeStop(1L, 1L);

        // Then
        verify(stopRepository).delete(stop);
    }
}

