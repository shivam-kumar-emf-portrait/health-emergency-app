package com.lifeline.emergency.controller;

import com.lifeline.emergency.entity.EmergencyMedicine;
import com.lifeline.emergency.service.PharmacyStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/pharmacy")
public class PharmacyStockController {

    private final PharmacyStockService pharmacyStockService;

    public PharmacyStockController(PharmacyStockService pharmacyStockService) {
        this.pharmacyStockService = pharmacyStockService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMedicines(Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<EmergencyMedicine> medicines = pharmacyStockService.getHospitalMedicines(hospitalId);

        long lowStockCount = medicines.stream()
                .filter(m -> m.getUnitsInStock() <= m.getLowStockThreshold())
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("medicines", medicines);
        response.put("totalItems", medicines.size());
        response.put("lowStockCount", lowStockCount);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(@PathVariable Long id,
                                                          @RequestBody Map<String, Integer> body,
                                                          Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        Integer units = body.get("unitsInStock");
        if (units == null) {
            throw new IllegalArgumentException("unitsInStock is required");
        }

        EmergencyMedicine updated = pharmacyStockService.updateStock(hospitalId, id, units);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Stock updated for " + updated.getMedicineName());
        response.put("medicine", updated);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addMedicine(@RequestBody EmergencyMedicine medicine, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        EmergencyMedicine added = pharmacyStockService.addMedicine(hospitalId, medicine);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "New emergency medicine added to formulary: " + added.getMedicineName());
        response.put("medicine", added);
        return ResponseEntity.ok(response);
    }
}
