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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BoardingServiceImplTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private BoardingServiceImpl boardingService;

    private Trip trip;
    private Assignment assignment;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .id(1L)
                .status(Trip.TripStatus.SCHEDULED)
                .build();

        assignment = Assignment.builder()
                .id(1L)
                .trip(trip)
                .checklistOk(true)
                .soatValid(true)
                .revisionValid(true)
                .build();

        tripResponse = new TripResponse(
                1L, 1L, "Route Name", "Origin", "Destination",
                1L, "Bus Plate", 40,
                null, null, null,
                Trip.TripStatus.SCHEDULED, 0, 0.0
        );
    }

    @Test
    void shouldOpenBoarding_WithScheduledTrip_ChangeStatusToBoarding() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toResponse(any(Trip.class))).thenReturn(tripResponse);

        // When
        TripResponse result = boardingService.openBoarding(1L);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.BOARDING
        ));
    }

    @Test
    void shouldOpenBoarding_WithInvalidStatus_ThrowException() {
        // Given
        trip.setStatus(Trip.TripStatus.DEPARTED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When/Then
        assertThatThrownBy(() -> boardingService.openBoarding(1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("SCHEDULED");
    }

    @Test
    void shouldCloseBoarding_WithBoardingTrip_ChangeStatus() {
        // Given
        trip.setStatus(Trip.TripStatus.BOARDING);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toResponse(any(Trip.class))).thenReturn(tripResponse);

        // When
        TripResponse result = boardingService.closeBoarding(1L);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldDepartTrip_WithCompleteChecklist_ChangeStatusToDeparted() {
        // Given
        trip.setStatus(Trip.TripStatus.BOARDING);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findByTripId(1L)).thenReturn(Optional.of(assignment));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toResponse(any(Trip.class))).thenReturn(tripResponse);

        // When
        TripResponse result = boardingService.departTrip(1L);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.DEPARTED
        ));
    }

    @Test
    void shouldDepartTrip_WithoutChecklistOk_ThrowException() {
        // Given
        trip.setStatus(Trip.TripStatus.BOARDING);
        assignment.setChecklistOk(false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findByTripId(1L)).thenReturn(Optional.of(assignment));

        // When/Then
        assertThatThrownBy(() -> boardingService.departTrip(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("checklist");
    }

    @Test
    void shouldDepartTrip_WithoutSoatValid_ThrowException() {
        // Given
        trip.setStatus(Trip.TripStatus.BOARDING);
        assignment.setSoatValid(false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findByTripId(1L)).thenReturn(Optional.of(assignment));

        // When/Then
        assertThatThrownBy(() -> boardingService.departTrip(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SOAT");
    }

    @Test
    void shouldDepartTrip_WithoutRevisionValid_ThrowException() {
        // Given
        trip.setStatus(Trip.TripStatus.BOARDING);
        assignment.setRevisionValid(false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findByTripId(1L)).thenReturn(Optional.of(assignment));

        // When/Then
        assertThatThrownBy(() -> boardingService.departTrip(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("revisión");
    }
}

