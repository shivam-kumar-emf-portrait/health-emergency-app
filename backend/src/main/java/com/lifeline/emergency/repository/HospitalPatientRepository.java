package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.HospitalPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalPatientRepository extends JpaRepository<HospitalPatient, Long> {
    @Query("SELECT p FROM HospitalPatient p WHERE p.hospital.id = :hospitalId ORDER BY p.admissionDate DESC")
    List<HospitalPatient> findByHospitalIdOrderByAdmissionDateDesc(@Param("hospitalId") Long hospitalId);

    Optional<HospitalPatient> findByUhid(String uhid);

    @Query("SELECT p FROM HospitalPatient p WHERE p.hospital.id = :hospitalId AND p.admissionStatus = :admissionStatus")
    List<HospitalPatient> findByHospitalIdAndAdmissionStatus(@Param("hospitalId") Long hospitalId, @Param("admissionStatus") String admissionStatus);

    List<HospitalPatient> findByPhoneOrderByAdmissionDateDesc(String phone);

    List<HospitalPatient> findByFullNameIgnoreCaseOrderByAdmissionDateDesc(String fullName);
}
