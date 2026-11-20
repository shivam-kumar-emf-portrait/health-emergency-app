package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyCaseRepository extends JpaRepository<EmergencyCase, Long> {
    @Query("SELECT e FROM EmergencyCase e WHERE e.hospital.id = :hospitalId ORDER BY e.createdAt DESC")
    List<EmergencyCase> findByHospitalIdOrderByCreatedAtDesc(@Param("hospitalId") Long hospitalId);

    @Query("SELECT e FROM EmergencyCase e WHERE e.hospital.id = :hospitalId AND e.status = :status ORDER BY e.createdAt DESC")
    List<EmergencyCase> findByHospitalIdAndStatusOrderByCreatedAtDesc(@Param("hospitalId") Long hospitalId, @Param("status") CaseStatus status);

    @Query("SELECT e FROM EmergencyCase e WHERE e.patientEmail = :patientEmail OR e.patientPhone = :patientPhone ORDER BY e.createdAt DESC")
    List<EmergencyCase> findByPatientEmailOrPatientPhoneOrderByCreatedAtDesc(@Param("patientEmail") String patientEmail, @Param("patientPhone") String patientPhone);

    List<EmergencyCase> findByPatientEmailOrderByCreatedAtDesc(String patientEmail);
    List<EmergencyCase> findByPatientPhoneOrderByCreatedAtDesc(String patientPhone);
}


