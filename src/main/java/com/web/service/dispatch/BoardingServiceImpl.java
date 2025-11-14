package com.web.service.dispatch;

import com.web.dto.trip.TripResponse;
import com.web.dto.trip.mapper.TripMapper;
import com.web.entity.Assignment;
import com.web.entity.Trip;
import com.web.exception.BusinessException;
import com.web.exception.InvalidStateTransitionException;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.AssignmentRepository;
import com.web.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BoardingServiceImpl implements BoardingService {

    private final TripRepository tripRepository;
    private final AssignmentRepository assignmentRepository;
    private final TripMapper tripMapper;

    @Override
    @Transactional
    public TripResponse openBoarding(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", tripId));

        if (trip.getStatus() != Trip.TripStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Solo se puede abrir abordaje desde estado SCHEDULED (actual: " + trip.getStatus() + ")");
        }

        trip.setStatus(Trip.TripStatus.BOARDING);
        Trip updatedTrip = tripRepository.save(trip);



        return tripMapper.toResponse(updatedTrip);
    }

    @Override
    @Transactional
    public TripResponse closeBoarding(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", tripId));

        if (trip.getStatus() != Trip.TripStatus.BOARDING) {
            throw new InvalidStateTransitionException(
                    "Solo se puede cerrar abordaje desde estado BOARDING (actual: " + trip.getStatus() + ")");
        }

        Trip updatedTrip = tripRepository.save(trip);



        return tripMapper.toResponse(updatedTrip);
    }

    @Override
    @Transactional
    public TripResponse departTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", tripId));

        if (trip.getStatus() != Trip.TripStatus.BOARDING) {
            throw new InvalidStateTransitionException(
                    "Solo se puede partir desde estado BOARDING (actual: " + trip.getStatus() + ")");
        }

        Assignment assignment = assignmentRepository.findByTripId(tripId)
                .orElseThrow(() -> new BusinessException("El viaje no tiene asignación", HttpStatus.BAD_REQUEST, "NO_ASSIGNMENT"));

        if (!assignment.getChecklistOk()) {
            throw new BusinessException("No se puede partir sin checklist aprobado", HttpStatus.BAD_REQUEST, "CHECKLIST_NOT_APPROVED");
        }

        trip.setStatus(Trip.TripStatus.DEPARTED);
        Trip updatedTrip = tripRepository.save(trip);



        return tripMapper.toResponse(updatedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripStatus(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", tripId));
        return tripMapper.toResponse(trip);
    }
}

