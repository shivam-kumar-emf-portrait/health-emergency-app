package com.lifeline.emergency.service;

import com.lifeline.emergency.entity.EmergencyMedicine;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.repository.EmergencyMedicineRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PharmacyStockService {

    private final EmergencyMedicineRepository medicineRepository;
    private final HospitalRepository hospitalRepository;

    public PharmacyStockService(EmergencyMedicineRepository medicineRepository,
                                HospitalRepository hospitalRepository) {
        this.medicineRepository = medicineRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public List<EmergencyMedicine> getHospitalMedicines(Long hospitalId) {
        return medicineRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public EmergencyMedicine updateStock(Long hospitalId, Long medicineId, int newUnits) {
        EmergencyMedicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + medicineId));

        if (!medicine.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized medicine stock access.");
        }

        medicine.setUnitsInStock(Math.max(0, newUnits));
        return medicineRepository.save(medicine);
    }

    @Transactional
    public EmergencyMedicine addMedicine(Long hospitalId, EmergencyMedicine medicine) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));

        medicine.setHospital(hospital);
        return medicineRepository.save(medicine);
    }
}
