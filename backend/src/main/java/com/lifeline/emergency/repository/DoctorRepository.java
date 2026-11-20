package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.enums.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(Long hospitalId);
    List<Doctor> findByHospitalIdAndIsPublicProfileTrue(Long hospitalId);
    List<Doctor> findByHospitalIdAndIsOnDutyTrue(Long hospitalId);
    List<Doctor> findByHospitalIdAndSpecializationAndIsOnDutyTrue(Long hospitalId, DoctorSpecialization specialization);
}

