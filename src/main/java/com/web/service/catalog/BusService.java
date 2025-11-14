package com.web.service.catalog;

import com.web.dto.catalog.Bus.BusCreateRequest;
import com.web.dto.catalog.Bus.BusResponse;
import com.web.dto.catalog.Bus.BusUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface BusService {
    
    BusResponse createBus(BusCreateRequest request);
    
    List<BusResponse> getAllBuses();
    
    BusResponse getBusById(Long id);

    BusResponse getBusByPlate(String plate);
    
    BusResponse updateBus(Long id, BusUpdateRequest request);
    
    void deleteBus(Long id);
    
    List<BusResponse> getAvailableBuses(LocalDate date);
}

