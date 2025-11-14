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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final RouteMapper routeMapper;
    private final StopMapper stopMapper;

    @Override
    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request) {
        if (routeRepository.existsByCode(request.code())) {
            throw new BusinessException("Ya existe una ruta con el código: " + request.code(), HttpStatus.CONFLICT, "ROUTE_CODE_EXISTS");
        }

        Route route = routeMapper.toEntity(request);
        Route savedRoute = routeRepository.save(route);



        return routeMapper.toResponse(savedRoute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();
        return routeMapper.toResponseList(routes);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDetailResponse getRouteById(Long id) {
        Route route = routeRepository.findByIdWithStops(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", id));
        return routeMapper.toDetailResponse(route);
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(Long id, RouteUpdateRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", id));

        // RouteUpdateRequest no permite cambiar el código, así que no validamos
        routeMapper.updateEntityFromRequest(request, route);

        Route updatedRoute = routeRepository.save(route);


        return routeMapper.toResponse(updatedRoute);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", id));

        if (tripRepository.countByRouteIdAndTripDateAfter(id, LocalDate.now()) > 0) {
            throw new BusinessException("No se puede eliminar la ruta porque tiene viajes programados", HttpStatus.BAD_REQUEST, "ROUTE_HAS_TRIPS");
        }

        routeRepository.delete(route);


    }

    @Override
    @Transactional
    public RouteDetailResponse addStop(Long routeId, StopCreateRequest request) {
        Route route = routeRepository.findByIdWithStops(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", routeId));

        if (stopRepository.existsByRouteIdAndOrder(routeId, request.order())) {
            throw new BusinessException("Ya existe una parada con el orden " + request.order() + " en esta ruta", HttpStatus.CONFLICT, "STOP_ORDER_EXISTS");
        }

        Stop stop = stopMapper.toEntity(request);
        stop.setRoute(route);

        Stop savedStop = stopRepository.save(stop);


        return getRouteById(routeId);
    }

    @Override
    @Transactional
    public void removeStop(Long routeId, Long stopId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", routeId));

        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Parada", stopId));

        if (!stop.getRoute().getId().equals(routeId)) {
            throw new BusinessException("La parada no pertenece a esta ruta", HttpStatus.BAD_REQUEST, "STOP_ROUTE_MISMATCH");
        }

        stopRepository.delete(stop);

    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> findRoutesConnecting(String origin, String destination) {
        List<Route> routes = routeRepository.findRoutesConnecting(origin, destination);
        return routeMapper.toResponseList(routes);
    }
}

