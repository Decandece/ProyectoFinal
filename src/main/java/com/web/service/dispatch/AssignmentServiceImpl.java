package com.web.service.dispatch;

import com.web.dto.dispatch.Assignment.AssignmentCreateRequest;
import com.web.dto.dispatch.Assignment.AssignmentResponse;
import com.web.dto.dispatch.Assignment.AssignmentUpdateRequest;
import com.web.dto.dispatch.Assignment.mapper.AssignmentMapper;
import com.web.entity.Assignment;
import com.web.entity.Trip;
import com.web.entity.User;
import com.web.exception.BusinessException;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.AssignmentRepository;
import com.web.repository.TripRepository;
import com.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final AssignmentMapper assignmentMapper;

    @Override

    @Transactional
    public AssignmentResponse assignTrip(AssignmentCreateRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", request.tripId()));

        if (trip.getStatus() != Trip.TripStatus.SCHEDULED) {
            throw new BusinessException("Solo se pueden asignar viajes en estado SCHEDULED", HttpStatus.BAD_REQUEST, "INVALID_TRIP_STATUS");
        }

        // Verificar si ya existe una asignación
        if (assignmentRepository.findByTripId(request.tripId()).isPresent()) {
            throw new BusinessException("Este viaje ya tiene una asignación", HttpStatus.CONFLICT, "ASSIGNMENT_EXISTS");
        }

        User driver = userRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor", request.driverId()));

        if (driver.getRole() != User.Role.DRIVER) {
            throw new BusinessException("El usuario no es un conductor", HttpStatus.BAD_REQUEST, "INVALID_DRIVER_ROLE");
        }

        User dispatcher = userRepository.findById(request.dispatcherId())
                .orElseThrow(() -> new ResourceNotFoundException("Despachador", request.dispatcherId()));

        if (dispatcher.getRole() != User.Role.DISPATCHER) {
            throw new BusinessException("El usuario no es un despachador", HttpStatus.BAD_REQUEST, "INVALID_DISPATCHER_ROLE");
        }

        Assignment assignment = assignmentMapper.toEntity(request);
        // Establecer las relaciones manualmente
        assignment.setTrip(trip);
        assignment.setDriver(driver);
        assignment.setDispatcher(dispatcher);
        assignment.setAssignedAt(LocalDateTime.now());
        
        Assignment savedAssignment = assignmentRepository.save(assignment);


        return assignmentMapper.toResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentByTrip(Long tripId) {
        Assignment assignment = assignmentRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación para viaje: " + tripId));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateChecklist(Long assignmentId, AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));

        assignmentMapper.updateEntityFromRequest(request, assignment);

        Assignment updatedAssignment = assignmentRepository.save(assignment);


        return assignmentMapper.toResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getDriverAssignments(Long driverId, LocalDate date) {
        List<Assignment> assignments = assignmentRepository.findByDriverId(driverId);
        // Filtrar por fecha si es necesario
        return assignmentMapper.toResponseList(assignments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getDispatcherAssignments(Long dispatcherId) {
        List<Assignment> assignments = assignmentRepository.findByDispatcherId(dispatcherId, LocalDate.now());
        return assignmentMapper.toResponseList(assignments);
    }
}

