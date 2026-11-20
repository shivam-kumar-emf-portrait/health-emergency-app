package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.HospitalOperationsRequest;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    private Long getHospitalIdFromAuth(Authentication auth) {
        if (auth == null || auth.getCredentials() == null) {
            throw new SecurityException("Not authenticated");
        }
        return (Long) auth.getCredentials();
    }

    @GetMapping("/me")
    public ResponseEntity<Hospital> getProfile(Authentication auth) {
        Long hospitalId = getHospitalIdFromAuth(auth);
        return ResponseEntity.ok(hospitalService.getHospitalById(hospitalId));
    }

    @PatchMapping("/operations")
    public ResponseEntity<Map<String, Object>> updateOperations(@RequestBody HospitalOperationsRequest request,
                                                               Authentication auth) {
        Long hospitalId = getHospitalIdFromAuth(auth);
        Hospital updated = hospitalService.updateOperations(hospitalId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hospital operations updated successfully");
        response.put("hospital", updated);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/toggle-open")
    public ResponseEntity<Map<String, Object>> toggleOpenStatus(Authentication auth) {
        Long hospitalId = getHospitalIdFromAuth(auth);
        boolean isOpen = hospitalService.toggleOpenStatus(hospitalId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hospital operational status updated");
        response.put("isOpen", isOpen);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Map<String, Object>> getPublicDetails(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getPublicHospitalDetails(id));
    }

    @GetMapping("/public/all")
    public ResponseEntity<java.util.List<Hospital>> getAllPublicHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }
}

