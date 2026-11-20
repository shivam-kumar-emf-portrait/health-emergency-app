package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.PatientBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientBillRepository extends JpaRepository<PatientBill, Long> {
    List<PatientBill> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
    List<PatientBill> findByPatientId(Long patientId);
    Optional<PatientBill> findByInvoiceNumber(String invoiceNumber);
}
