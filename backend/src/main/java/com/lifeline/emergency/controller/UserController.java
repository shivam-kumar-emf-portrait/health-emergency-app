package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.SubmitHospitalReviewRequest;
import com.lifeline.emergency.dto.UpdateProfileRequest;
import com.lifeline.emergency.dto.UserProfileResponse;
import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.entity.FeedbackReport;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.entity.HospitalPatient;
import com.lifeline.emergency.entity.User;
import com.lifeline.emergency.enums.CaseStatus;
import com.lifeline.emergency.repository.EmergencyCaseRepository;
import com.lifeline.emergency.repository.FeedbackReportRepository;
import com.lifeline.emergency.repository.HospitalPatientRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import com.lifeline.emergency.repository.UserRepository;
import com.lifeline.emergency.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final EmergencyCaseRepository emergencyCaseRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final HospitalRepository hospitalRepository;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository,
                          EmergencyCaseRepository emergencyCaseRepository,
                          HospitalPatientRepository hospitalPatientRepository,
                          FeedbackReportRepository feedbackReportRepository,
                          HospitalRepository hospitalRepository,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.emergencyCaseRepository = emergencyCaseRepository;
        this.hospitalPatientRepository = hospitalPatientRepository;
        this.feedbackReportRepository = feedbackReportRepository;
        this.hospitalRepository = hospitalRepository;
        this.jwtUtil = jwtUtil;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User record not found for email: " + auth.getName()));
    }

    /**
     * Get citizen profile details, live alert history, hospital checkup history, and reviews
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication auth) {
        User user = getAuthenticatedUser(auth);

        // Fetch dynamic alert history
        List<EmergencyCase> alerts = emergencyCaseRepository.findByPatientEmailOrPatientPhoneOrderByCreatedAtDesc(
                user.getEmail(), user.getPhone() != null ? user.getPhone() : ""
        );
        if (alerts == null) alerts = new ArrayList<>();

        // Fetch hospital checkup / admission history by phone or name
        List<HospitalPatient> checkups = new ArrayList<>();
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            checkups = hospitalPatientRepository.findByPhoneOrderByAdmissionDateDesc(user.getPhone());
        }
        if (checkups.isEmpty() && user.getName() != null && !user.getName().isBlank()) {
            checkups = hospitalPatientRepository.findByFullNameIgnoreCaseOrderByAdmissionDateDesc(user.getName());
        }

        // Fetch reviews submitted by this user
        List<FeedbackReport> feedbacks = feedbackReportRepository.findByReporterEmailOrderByCreatedAtDesc(user.getEmail());
        if (feedbacks == null) feedbacks = new ArrayList<>();

        UserProfileResponse response = new UserProfileResponse(user, alerts, checkups, feedbacks);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile (Name, Phone number, Known Health Conditions)
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UpdateProfileRequest request, Authentication auth) {
        User user = getAuthenticatedUser(auth);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getKnownConditions() != null) {
            user.setKnownConditions(request.getKnownConditions().trim());
        }
        if (request.getAge() != null && request.getAge() > 0) {
            user.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender().trim());
        }

        User savedUser = userRepository.save(user);

        // Issue refreshed token reflecting updated name
        String newToken = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId(), savedUser.getName());

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Profile updated successfully");
        resp.put("user", savedUser);
        resp.put("token", newToken);
        return ResponseEntity.ok(resp);
    }

    /**
     * Retrieve hospitals where this authenticated user has COMPLETED treatment and is eligible to rate
     */
    @GetMapping("/eligible-reviews")
    public ResponseEntity<List<Map<String, Object>>> getEligibleReviewHospitals(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Map<Long, Map<String, Object>> eligibleMap = new HashMap<>();

        // 1. Completed emergency cases
        List<EmergencyCase> cases = emergencyCaseRepository.findByPatientEmailOrPatientPhoneOrderByCreatedAtDesc(
                user.getEmail(), user.getPhone() != null ? user.getPhone() : ""
        );
        for (EmergencyCase c : cases) {
            if (c.getHospital() != null && c.getStatus() == CaseStatus.COMPLETED) {
                Long hid = c.getHospital().getId();
                if (!eligibleMap.containsKey(hid)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("hospitalId", hid);
                    item.put("hospitalName", c.getHospital().getName());
                    item.put("hospitalAddress", c.getHospital().getAddress());
                    item.put("treatmentType", "Emergency Ward Care (" + (c.getEmergencyType() != null ? c.getEmergencyType().getDisplayName() : "General") + ")");
                    item.put("completedDate", c.getCreatedAt());
                    eligibleMap.put(hid, item);
                }
            }
        }

        // 2. Discharged hospital checkup/admission patients
        List<HospitalPatient> checkups = new ArrayList<>();
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            checkups.addAll(hospitalPatientRepository.findByPhoneOrderByAdmissionDateDesc(user.getPhone()));
        }
        if (checkups.isEmpty() && user.getName() != null && !user.getName().isBlank()) {
            checkups.addAll(hospitalPatientRepository.findByFullNameIgnoreCaseOrderByAdmissionDateDesc(user.getName()));
        }
        for (HospitalPatient p : checkups) {
            if (p.getHospital() != null && ("DISCHARGED".equalsIgnoreCase(p.getAdmissionStatus()) || "COMPLETED".equalsIgnoreCase(p.getAdmissionStatus()))) {
                Long hid = p.getHospital().getId();
                if (!eligibleMap.containsKey(hid)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("hospitalId", hid);
                    item.put("hospitalName", p.getHospital().getName());
                    item.put("hospitalAddress", p.getHospital().getAddress());
                    item.put("treatmentType", "Inpatient Discharge (" + (p.getPrimaryDiagnosis() != null ? p.getPrimaryDiagnosis() : "Checkup") + ")");
                    item.put("completedDate", p.getDischargeDate() != null ? p.getDischargeDate() : p.getAdmissionDate());
                    eligibleMap.put(hid, item);
                }
            }
        }

        // Check if user already submitted a review for each eligible hospital
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : eligibleMap.values()) {
            Long hid = (Long) item.get("hospitalId");
            Optional<FeedbackReport> existing = feedbackReportRepository.findByReporterEmailAndTargetIdAndReportType(user.getEmail(), hid, "USER_TO_HOSPITAL");
            if (existing.isPresent()) {
                item.put("hasReviewed", true);
                item.put("existingReview", existing.get());
            } else {
                item.put("hasReviewed", false);
                item.put("existingReview", null);
            }
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    private boolean hasCompletedTreatmentAtHospital(User user, Long hospitalId) {
        // 1. Check completed emergency cases
        List<EmergencyCase> cases = emergencyCaseRepository.findByPatientEmailOrPatientPhoneOrderByCreatedAtDesc(
                user.getEmail(), user.getPhone() != null ? user.getPhone() : ""
        );
        boolean hasCompletedEmergency = cases.stream()
                .anyMatch(c -> c.getHospital() != null &&
                        c.getHospital().getId().equals(hospitalId) &&
                        c.getStatus() == CaseStatus.COMPLETED);

        if (hasCompletedEmergency) return true;

        // 2. Check discharged hospital inpatient / checkup admissions
        List<HospitalPatient> patients = new ArrayList<>();
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            patients.addAll(hospitalPatientRepository.findByPhoneOrderByAdmissionDateDesc(user.getPhone()));
        }
        if (patients.isEmpty() && user.getName() != null && !user.getName().isBlank()) {
            patients.addAll(hospitalPatientRepository.findByFullNameIgnoreCaseOrderByAdmissionDateDesc(user.getName()));
        }

        return patients.stream()
                .anyMatch(p -> p.getHospital() != null &&
                        p.getHospital().getId().equals(hospitalId) &&
                        ("DISCHARGED".equalsIgnoreCase(p.getAdmissionStatus()) || "COMPLETED".equalsIgnoreCase(p.getAdmissionStatus())));
    }

    /**
     * Submit a hospital rating & review/complaint.
     * STRICT HEALTHCARE LOGIC:
     * 1) Only patients who have completed treatment or received discharge from that specific hospital can rate.
     * 2) Strictly one verified review per hospital (no duplicate ratings).
     */
    @PostMapping("/feedback")
    public ResponseEntity<FeedbackReport> submitHospitalReview(@RequestBody SubmitHospitalReviewRequest request, Authentication auth) {
        User user = getAuthenticatedUser(auth);

        if (request.getHospitalId() == null) {
            throw new IllegalArgumentException("Hospital ID is required for review submission");
        }

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found with ID: " + request.getHospitalId()));

        // 1. Strict Healthcare Logic: Must have completed treatment at this specific hospital
        if (!hasCompletedTreatmentAtHospital(user, hospital.getId())) {
            throw new IllegalArgumentException("Verified Treatment Required: You can only rate a hospital after receiving and completing emergency treatment or hospital discharge at that facility.");
        }

        // 2. Duplicate prevention: Check if already reviewed
        if (feedbackReportRepository.existsByReporterEmailAndTargetIdAndReportType(user.getEmail(), hospital.getId(), "USER_TO_HOSPITAL")) {
            throw new IllegalArgumentException("Duplicate Rating Prevented: You have already submitted a verified review and rating for this hospital. Multiple ratings for the same hospital are not permitted.");
        }

        FeedbackReport report = new FeedbackReport();
        report.setReportType("USER_TO_HOSPITAL");
        report.setReporterId(user.getId());
        report.setReporterName(user.getName());
        report.setReporterEmail(user.getEmail());
        report.setReporterRole("CITIZEN");
        report.setTargetId(hospital.getId());
        report.setTargetName(hospital.getName());
        report.setRating(request.getRating() != null ? Math.max(1, Math.min(5, request.getRating())) : 5);
        report.setCategory(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory() : "Care Quality");
        report.setSeverity(request.getSeverity() != null && !request.getSeverity().isBlank() ? request.getSeverity() : "LOW");
        report.setDescription(request.getDescription() != null && !request.getDescription().isBlank() ?
                request.getDescription() : "Verified patient emergency visit and review.");
        report.setStatus("PENDING_REVIEW");

        FeedbackReport savedReport = feedbackReportRepository.save(report);
        return ResponseEntity.ok(savedReport);
    }
}

