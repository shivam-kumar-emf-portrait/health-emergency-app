package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.HospitalOperationsRequest;
import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.repository.DoctorRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public HospitalService(HospitalRepository hospitalRepository, DoctorRepository doctorRepository) {
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found with ID: " + id));
    }

    @Transactional
    public Hospital updateOperations(Long hospitalId, HospitalOperationsRequest request) {
        Hospital hospital = getHospitalById(hospitalId);

        if (request.getTotalBeds() != null) hospital.setTotalBeds(request.getTotalBeds());
        if (request.getAvailableBeds() != null) hospital.setAvailableBeds(request.getAvailableBeds());
        if (request.getTotalICUBeds() != null) hospital.setTotalICUBeds(request.getTotalICUBeds());
        if (request.getAvailableICUBeds() != null) hospital.setAvailableICUBeds(request.getAvailableICUBeds());

        if (request.getVentilators() != null) hospital.setVentilators(request.getVentilators());
        if (request.getOxygenCylinders() != null) hospital.setOxygenCylinders(request.getOxygenCylinders());
        if (request.getAmbulances() != null) hospital.setAmbulances(request.getAmbulances());
        if (request.getCardiacMonitors() != null) hospital.setCardiacMonitors(request.getCardiacMonitors());

        if (request.getIsOpen() != null) hospital.setOpen(request.getIsOpen());
        if (request.getBloodInventory() != null) {
            hospital.setBloodInventory(new HashMap<>(request.getBloodInventory()));
        }

        return hospitalRepository.save(hospital);
    }

    @Transactional
    public boolean toggleOpenStatus(Long hospitalId) {
        Hospital hospital = getHospitalById(hospitalId);
        hospital.setOpen(!hospital.isOpen());
        hospitalRepository.save(hospital);
        return hospital.isOpen();
    }

    public Map<String, Object> getPublicHospitalDetails(Long hospitalId) {
        Hospital hospital = getHospitalById(hospitalId);
        List<Doctor> publicDoctors = doctorRepository.findByHospitalIdAndIsPublicProfileTrue(hospitalId);

        Map<String, Object> response = new HashMap<>();
        response.put("hospital", hospital);
        response.put("doctors", publicDoctors);
        return response;
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }
}

