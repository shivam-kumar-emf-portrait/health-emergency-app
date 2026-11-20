package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.AdmitPatientRequest;
import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.entity.HospitalPatient;
import com.lifeline.emergency.repository.DoctorRepository;
import com.lifeline.emergency.repository.HospitalPatientRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class HospitalPatientService {

    private final HospitalPatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public HospitalPatientService(HospitalPatientRepository patientRepository,
                                  HospitalRepository hospitalRepository,
                                  DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<HospitalPatient> getHospitalPatients(Long hospitalId, String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return patientRepository.findByHospitalIdAndAdmissionStatus(hospitalId, status.toUpperCase());
        }
        return patientRepository.findByHospitalIdOrderByAdmissionDateDesc(hospitalId);
    }

    public HospitalPatient getPatientById(Long hospitalId, Long patientId) {
        HospitalPatient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized patient access.");
        }
        return patient;
    }

    @Transactional
    public HospitalPatient admitPatient(Long hospitalId, AdmitPatientRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));

        Doctor doctor = null;
        if (request.getAssignedDoctorId() != null) {
            doctor = doctorRepository.findById(request.getAssignedDoctorId()).orElse(null);
        }

        // Generate UHID: LIFELINE-2026-XXXX
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        String uhid = "UHID-2026-" + randomSuffix;

        HospitalPatient patient = new HospitalPatient();
        patient.setUhid(uhid);
        patient.setFullName(request.getFullName());
        patient.setPhone(request.getPhone());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setPrimaryDiagnosis(request.getPrimaryDiagnosis());
        patient.setAssignedBedNumber(request.getAssignedBedNumber() != null ? request.getAssignedBedNumber() : "ER-BAY-" + (randomSuffix % 20 + 1));
        patient.setAdmissionStatus(request.getAdmissionStatus() != null ? request.getAdmissionStatus().toUpperCase() : "ADMITTED");
        patient.setAdmissionDate(LocalDateTime.now());
        patient.setAssignedDoctor(doctor);
        patient.setHospital(hospital);

        // Deduct bed count if beds available
        if (hospital.getAvailableBeds() > 0) {
            hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);
            if ("ICU".equalsIgnoreCase(patient.getAdmissionStatus()) && hospital.getAvailableICUBeds() > 0) {
                hospital.setAvailableICUBeds(hospital.getAvailableICUBeds() - 1);
            }
            hospitalRepository.save(hospital);
        }

        return patientRepository.save(patient);
    }

    @Transactional
    public HospitalPatient updatePatientStatus(Long hospitalId, Long patientId, String newStatus, String bedNumber) {
        HospitalPatient patient = getPatientById(hospitalId, patientId);

        String previousStatus = patient.getAdmissionStatus();
        patient.setAdmissionStatus(newStatus.toUpperCase());
        if (bedNumber != null && !bedNumber.isBlank()) {
            patient.setAssignedBedNumber(bedNumber);
        }

        // Handle bed adjustment between ICU and general
        Hospital hospital = patient.getHospital();
        if ("ICU".equalsIgnoreCase(newStatus) && !"ICU".equalsIgnoreCase(previousStatus)) {
            if (hospital.getAvailableICUBeds() > 0) {
                hospital.setAvailableICUBeds(hospital.getAvailableICUBeds() - 1);
                hospitalRepository.save(hospital);
            }
        } else if (!"ICU".equalsIgnoreCase(newStatus) && "ICU".equalsIgnoreCase(previousStatus)) {
            hospital.setAvailableICUBeds(hospital.getAvailableICUBeds() + 1);
            hospitalRepository.save(hospital);
        }

        return patientRepository.save(patient);
    }

    @Transactional
    public HospitalPatient dischargePatient(Long hospitalId, Long patientId) {
        HospitalPatient patient = getPatientById(hospitalId, patientId);
        Hospital hospital = patient.getHospital();

        if (!"DISCHARGED".equalsIgnoreCase(patient.getAdmissionStatus())) {
            if ("ICU".equalsIgnoreCase(patient.getAdmissionStatus())) {
                hospital.setAvailableICUBeds(hospital.getAvailableICUBeds() + 1);
            }
            hospital.setAvailableBeds(hospital.getAvailableBeds() + 1);
            hospitalRepository.save(hospital);
        }

        patient.setAdmissionStatus("DISCHARGED");
        patient.setDischargeDate(LocalDateTime.now());
        return patientRepository.save(patient);
    }
}
