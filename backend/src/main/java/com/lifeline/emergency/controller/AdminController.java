package com.lifeline.emergency.controller;

import com.lifeline.emergency.entity.FeedbackReport;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.entity.User;
import com.lifeline.emergency.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<Map<String, Object>>> getHospitals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(adminService.getHospitals(search, status));
    }

    @PostMapping("/hospitals/{id}/verify")
    public ResponseEntity<Hospital> verifyHospital(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(adminService.verifyHospital(id, notes));
    }

    @PostMapping("/hospitals/{id}/unverify")
    public ResponseEntity<Hospital> unverifyHospital(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.unverifyHospital(id, reason));
    }

    @PostMapping("/hospitals/{id}/toggle-suspend")
    public ResponseEntity<Hospital> toggleHospitalSuspend(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.toggleHospitalSuspend(id, reason));
    }

    @PostMapping("/hospitals/{id}/close-permanently")
    public ResponseEntity<Hospital> closeHospitalPermanently(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.closeHospitalPermanently(id, reason));
    }

    @PostMapping("/hospitals/{id}/undo-closure")
    public ResponseEntity<Hospital> undoClosure(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.undoPermanentClosure(id, reason));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(adminService.getUsers(search, status));
    }

    @PostMapping("/users/{id}/toggle-block")
    public ResponseEntity<User> toggleBlockUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.toggleBlockUser(id, reason));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<FeedbackReport>> getReports(
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL") String severity) {
        return ResponseEntity.ok(adminService.getReports(type, status, severity));
    }

    @PostMapping("/reports/{id}/action")
    public ResponseEntity<FeedbackReport> actionReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String newStatus = (String) body.get("status");
        String adminAction = (String) body.get("adminAction");
        String adminNotes = (String) body.get("adminNotes");
        boolean blockTarget = Boolean.TRUE.equals(body.get("blockTarget"));

        return ResponseEntity.ok(adminService.actionReport(id, newStatus, adminAction, adminNotes, blockTarget));
    }

    @GetMapping("/workforce")
    public ResponseEntity<List<Map<String, Object>>> getWorkforce(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(adminService.getWorkforce(hospitalId, role, department));
    }

    @PostMapping("/workforce/toggle-duty")
    public ResponseEntity<Map<String, Object>> toggleDuty(@RequestBody Map<String, Object> body) {
        String role = (String) body.get("role");
        Long id = Long.valueOf(body.get("id").toString());
        return ResponseEntity.ok(adminService.toggleStaffOnDuty(role, id));
    }
}

