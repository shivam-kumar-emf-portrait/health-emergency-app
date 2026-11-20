package com.lifeline.emergency.controller;

import com.lifeline.emergency.entity.FeedbackReport;
import com.lifeline.emergency.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final AdminService adminService;

    public FeedbackController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Submit a report or review (Citizen to Hospital OR Hospital to Citizen)
     */
    @PostMapping("/submit")
    public ResponseEntity<FeedbackReport> submitReport(@RequestBody FeedbackReport report) {
        return ResponseEntity.ok(adminService.submitFeedbackReport(report));
    }

    /**
     * Public reviews for a hospital
     */
    @GetMapping("/hospital/{id}")
    public ResponseEntity<List<FeedbackReport>> getHospitalReviews(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getHospitalReviews(id));
    }
}

