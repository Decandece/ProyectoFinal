package com.web.service.parcel;

import com.web.dto.parcel.ParcelCreateRequest;
import com.web.dto.parcel.ParcelResponse;
import com.web.entity.Parcel;

import java.time.LocalDate;
import java.util.List;

public interface ParcelService {
    
    ParcelResponse createParcel(ParcelCreateRequest request);
    
    List<ParcelResponse> getAllParcels();
    
    ParcelResponse trackParcel(String code);
    
    ParcelResponse updateStatus(Long parcelId, Parcel.ParcelStatus status);
    
    ParcelResponse deliverWithOtp(Long parcelId, String otp, String photoUrl);
    
    List<ParcelResponse> getParcelsInTransit(Long tripId);
    
    List<ParcelResponse> getParcelsByDateRange(LocalDate startDate, LocalDate endDate);
}

