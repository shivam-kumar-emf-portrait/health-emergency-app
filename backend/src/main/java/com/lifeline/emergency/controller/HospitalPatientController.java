package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.AdmitPatientRequest;
import com.lifeline.emergency.entity.HospitalPatient;
import com.lifeline.emergency.service.HospitalPatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/patients")
public class HospitalPatientController {

    private final HospitalPatientService patientService;

    public HospitalPatientController(HospitalPatientService patientService) {
        this.patientService = patientService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPatients(@RequestParam(required = false) String status,
                                                          Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<HospitalPatient> patients = patientService.getHospitalPatients(hospitalId, status);

        Map<String, Object> response = new HashMap<>();
        response.put("patients", patients);
        response.put("count", patients.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalPatient> getPatient(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        return ResponseEntity.ok(patientService.getPatientById(hospitalId, id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> admitPatient(@RequestBody AdmitPatientRequest request,
                                                           Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        HospitalPatient admitted = patientService.admitPatient(hospitalId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient admitted successfully (UHID: " + admitted.getUhid() + ")");
        response.put("patient", admitted);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body,
                                                           Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        String status = body.get("status");
        String bedNumber = body.get("bedNumber");

        HospitalPatient updated = patientService.updatePatientStatus(hospitalId, id, status, bedNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient status updated to " + updated.getAdmissionStatus());
        response.put("patient", updated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<Map<String, Object>> dischargePatient(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        HospitalPatient discharged = patientService.dischargePatient(hospitalId, id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient successfully discharged.");
        response.put("patient", discharged);
        return ResponseEntity.ok(response);
    }
}
