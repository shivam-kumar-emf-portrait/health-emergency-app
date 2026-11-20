package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.NotifyHospitalRequest;
import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.enums.CaseStatus;
import com.lifeline.emergency.repository.DoctorRepository;
import com.lifeline.emergency.repository.EmergencyCaseRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyCaseService {

    private final EmergencyCaseRepository emergencyCaseRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public EmergencyCaseService(EmergencyCaseRepository emergencyCaseRepository,
                                HospitalRepository hospitalRepository,
                                DoctorRepository doctorRepository) {
        this.emergencyCaseRepository = emergencyCaseRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public EmergencyCase notifyHospital(NotifyHospitalRequest request) {
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + request.getHospitalId()));

        EmergencyCase emergencyCase = new EmergencyCase();
        emergencyCase.setHospital(hospital);
        emergencyCase.setEmergencyType(request.getEmergencyType());
        emergencyCase.setSeverity(request.getSeverity() != null ? request.getSeverity() : "EMERGENCY");
        emergencyCase.setPatientName(request.getPatientName() != null ? request.getPatientName() : "Emergency Patient");
        emergencyCase.setPatientPhone(request.getPatientPhone() != null ? request.getPatientPhone() : "Not Provided");
        emergencyCase.setPatientEmail(request.getPatientEmail());
        emergencyCase.setUserLatitude(request.getUserLatitude());
        emergencyCase.setUserLongitude(request.getUserLongitude());
        emergencyCase.setNotes(request.getNotes());
        emergencyCase.setStatus(CaseStatus.ACTIVE);

        return emergencyCaseRepository.save(emergencyCase);
    }

    public List<EmergencyCase> getHospitalEmergencies(Long hospitalId) {
        return emergencyCaseRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId);
    }

    @Transactional
    public EmergencyCase updateCaseStatus(Long hospitalId, Long caseId, CaseStatus newStatus) {
        EmergencyCase emergencyCase = emergencyCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency case not found: " + caseId));

        if (!emergencyCase.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized emergency case update.");
        }

        // When emergency moves to IN_PROGRESS, automatically assign an available on-duty doctor if not already assigned
        if (newStatus == CaseStatus.IN_PROGRESS && emergencyCase.getAssignedDoctor() == null) {
            List<Doctor> onDutyDoctors = doctorRepository.findByHospitalIdAndIsOnDutyTrue(hospitalId);
            if (!onDutyDoctors.isEmpty()) {
                // Try to find matching specialist first, or any on-duty doctor
                Doctor doctorToAssign = onDutyDoctors.stream()
                        .filter(d -> d.getSpecialization() == emergencyCase.getEmergencyType().getRequiredSpecialization())
                        .findFirst()
                        .orElse(onDutyDoctors.get(0));

                emergencyCase.setAssignedDoctor(doctorToAssign);
            }
        }

        emergencyCase.setStatus(newStatus);
        return emergencyCaseRepository.save(emergencyCase);
    }
}

