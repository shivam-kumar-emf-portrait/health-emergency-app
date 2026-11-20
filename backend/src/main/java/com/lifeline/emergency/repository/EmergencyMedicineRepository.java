package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.EmergencyMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyMedicineRepository extends JpaRepository<EmergencyMedicine, Long> {
    List<EmergencyMedicine> findByHospitalId(Long hospitalId);
}
