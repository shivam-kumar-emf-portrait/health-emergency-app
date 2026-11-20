package com.lifeline.emergency.controller;

import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.enums.CaseStatus;
import com.lifeline.emergency.service.EmergencyCaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/emergencies")
public class EmergencyCaseController {

    private final EmergencyCaseService emergencyCaseService;

    public EmergencyCaseController(EmergencyCaseService emergencyCaseService) {
        this.emergencyCaseService = emergencyCaseService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHospitalEmergencies(Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<EmergencyCase> emergencies = emergencyCaseService.getHospitalEmergencies(hospitalId);

        Map<String, Object> response = new HashMap<>();
        response.put("emergencies", emergencies);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body,
                                                           Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        String statusStr = body.get("status");
        CaseStatus status = CaseStatus.valueOf(statusStr.toUpperCase());

        EmergencyCase updated = emergencyCaseService.updateCaseStatus(hospitalId, id, status);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Emergency status updated to " + status);
        response.put("emergency", updated);
        return ResponseEntity.ok(response);
    }
}

