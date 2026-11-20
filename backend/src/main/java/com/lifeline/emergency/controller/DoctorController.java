package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.DoctorDto;
import com.lifeline.emergency.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors(Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<DoctorDto> doctors = doctorService.getHospitalDoctors(hospitalId);

        long onDutyCount = doctors.stream().filter(DoctorDto::isOnDuty).count();

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        response.put("stats", Map.of(
                "totalDoctors", doctors.size(),
                "onDutyDoctors", onDutyCount,
                "offDutyDoctors", doctors.size() - onDutyCount
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addDoctor(@RequestBody DoctorDto dto, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        DoctorDto created = doctorService.addDoctor(hospitalId, dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor registered successfully");
        response.put("doctor", created);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-duty")
    public ResponseEntity<Map<String, Object>> toggleDuty(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        DoctorDto updated = doctorService.toggleDoctorDuty(hospitalId, id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor duty status updated");
        response.put("doctor", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        doctorService.deleteDoctor(hospitalId, id);
        return ResponseEntity.ok(Map.of("message", "Doctor removed successfully"));
    }
}

