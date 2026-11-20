package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.EmergencySearchRequest;
import com.lifeline.emergency.dto.EmergencySearchResponse;
import com.lifeline.emergency.dto.EmergencyTransitAiRequest;
import com.lifeline.emergency.dto.EmergencyTransitAiResponse;
import com.lifeline.emergency.dto.NotifyHospitalRequest;
import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.enums.EmergencyType;
import com.lifeline.emergency.service.EmergencyCaseService;
import com.lifeline.emergency.service.EmergencyMatchingService;
import com.lifeline.emergency.service.EmergencyTransitAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    private final EmergencyMatchingService emergencyMatchingService;
    private final EmergencyCaseService emergencyCaseService;
    private final EmergencyTransitAiService emergencyTransitAiService;

    public EmergencyController(EmergencyMatchingService emergencyMatchingService,
                               EmergencyCaseService emergencyCaseService,
                               EmergencyTransitAiService emergencyTransitAiService) {
        this.emergencyMatchingService = emergencyMatchingService;
        this.emergencyCaseService = emergencyCaseService;
        this.emergencyTransitAiService = emergencyTransitAiService;
    }

    /**
     * Find Top 3 Nearest and Top 3 Best Hospitals for any given emergency type
     */
    @PostMapping("/find-hospitals")
    public ResponseEntity<EmergencySearchResponse> findHospitals(@RequestBody EmergencySearchRequest request) {
        return ResponseEntity.ok(emergencyMatchingService.findHospitalsForEmergency(request));
    }

    /**
     * OpenRouter AI Emergency Transit Co-Pilot:
     * Provides instant patient in-transit posture, airway survival steps, speed tactics,
     * OpenStreetMap routing, and auto-dispatches a pre-arrival alert to the hospital ER ward.
     */
    @PostMapping("/transit-ai-plan")
    public ResponseEntity<EmergencyTransitAiResponse> getTransitAiPlan(@RequestBody EmergencyTransitAiRequest request) {
        return ResponseEntity.ok(emergencyTransitAiService.generateTransitPlan(request));
    }

    /**
     * Notify/Alert the chosen hospital in real time
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notifyHospital(@RequestBody NotifyHospitalRequest request) {
        EmergencyCase emergencyCase = emergencyCaseService.notifyHospital(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Emergency alert sent to hospital successfully.");
        response.put("caseId", emergencyCase.getId());
        response.put("hospitalName", emergencyCase.getHospital().getName());
        response.put("status", emergencyCase.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Returns list of supported emergency categories
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getEmergencyTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (EmergencyType type : EmergencyType.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.name());
            map.put("title", type.getDisplayName());
            map.put("requiredSpecialty", type.getRequiredSpecialization().getDescription());
            types.add(map);
        }
        return ResponseEntity.ok(types);
    }

    /**
     * Resolve area, landmark, or custom address query to geographic coordinates (lat, lng)
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocodeLocation(@RequestParam String query) {
        return ResponseEntity.ok(emergencyMatchingService.resolveLocationCoordinates(query));
    }

    /**
     * Provide real-time location suggestions for the user's search box
     */
    @GetMapping("/suggest-locations")
    public ResponseEntity<List<Map<String, Object>>> suggestLocations(@RequestParam String query) {
        return ResponseEntity.ok(emergencyMatchingService.suggestLocations(query));
    }
}

