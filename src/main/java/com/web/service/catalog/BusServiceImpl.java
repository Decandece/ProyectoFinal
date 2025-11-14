package com.web.service.catalog;

import com.web.dto.catalog.Bus.BusCreateRequest;
import com.web.dto.catalog.Bus.BusResponse;
import com.web.dto.catalog.Bus.BusUpdateRequest;
import com.web.dto.catalog.Bus.mapper.BusMapper;
import com.web.entity.Bus;
import com.web.exception.BusinessException;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final BusMapper busMapper;

    @Override
    @Transactional
    public BusResponse createBus(BusCreateRequest request) {
        if (busRepository.findByPlate(request.plate()).isPresent()) {
            throw new BusinessException("Ya existe un bus con la placa: " + request.plate(), HttpStatus.CONFLICT, "PLATE_EXISTS");
        }

        Bus bus = busMapper.toEntity(request);
        Bus savedBus = busRepository.save(bus);

        return busMapper.toResponse(savedBus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getAllBuses() {
        List<Bus> buses = busRepository.findAll();
        return busMapper.toResponseList(buses);
    }

    @Override
    @Transactional(readOnly = true)
    public BusResponse getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus", id));
        return busMapper.toResponse(bus);
    }

    @Override
    @Transactional(readOnly = true)
    public BusResponse getBusByPlate(String plate) {
        Bus bus = busRepository.findByPlate(plate)
                .orElseThrow(() -> new ResourceNotFoundException("Bus", plate));
        return busMapper.toResponse(bus);
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long id, BusUpdateRequest request) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus", id));

        // BusUpdateRequest no permite cambiar la placa, así que no validamos
        busMapper.updateEntityFromRequest(request, bus);

        Bus updatedBus = busRepository.save(bus);



        return busMapper.toResponse(updatedBus);
    }

    @Override
    @Transactional
    public void deleteBus(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus", id));

        bus.setStatus(Bus.BusStatus.MAINTENANCE);
        busRepository.save(bus);


    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getAvailableBuses(LocalDate date) {
        // Implementación simplificada: buses activos
        List<Bus> buses = busRepository.findAll().stream()
                .filter(b -> b.getStatus() == Bus.BusStatus.ACTIVE)
                .collect(Collectors.toList());
        return busMapper.toResponseList(buses);
    }
}

