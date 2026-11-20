package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.CreateBillRequest;
import com.lifeline.emergency.entity.PatientBill;
import com.lifeline.emergency.service.HospitalBillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/billing")
public class HospitalBillingController {

    private final HospitalBillingService billingService;

    public HospitalBillingController(HospitalBillingService billingService) {
        this.billingService = billingService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBills(Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<PatientBill> bills = billingService.getHospitalBills(hospitalId);

        Map<String, Object> response = new HashMap<>();
        response.put("bills", bills);
        response.put("totalInvoices", bills.size());
        double totalRevenue = bills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getPaymentStatus()))
                .mapToDouble(PatientBill::getGrandTotal)
                .sum();
        response.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientBill> getBill(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        return ResponseEntity.ok(billingService.getBillById(hospitalId, id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBill(@RequestBody CreateBillRequest request, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        PatientBill created = billingService.createBill(hospitalId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invoice generated: " + created.getInvoiceNumber());
        response.put("bill", created);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePayment(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body,
                                                            Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        String paymentStatus = body.get("paymentStatus");
        String paymentMethod = body.get("paymentMethod");

        PatientBill updated = billingService.updatePaymentStatus(hospitalId, id, paymentStatus, paymentMethod);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment status updated to " + updated.getPaymentStatus());
        response.put("bill", updated);
        return ResponseEntity.ok(response);
    }
}
