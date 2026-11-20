package com.lifeline.emergency.service;

import com.lifeline.emergency.entity.*;
import com.lifeline.emergency.enums.Role;
import com.lifeline.emergency.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final EmergencyCaseRepository emergencyCaseRepository;

    public AdminService(HospitalRepository hospitalRepository,
                        UserRepository userRepository,
                        DoctorRepository doctorRepository,
                        NurseRepository nurseRepository,
                        FeedbackReportRepository feedbackReportRepository,
                        EmergencyCaseRepository emergencyCaseRepository) {
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.feedbackReportRepository = feedbackReportRepository;
        this.emergencyCaseRepository = emergencyCaseRepository;
    }

    /**
     * Get Real-Time Platform System Statistics
     */
    public Map<String, Object> getAdminStats() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        List<User> users = userRepository.findAll();
        List<FeedbackReport> reports = feedbackReportRepository.findAll();

        long pendingHospitals = hospitals.stream()
                .filter(h -> !h.isVerified() && !h.isClosedPermanently())
                .count();

        long verifiedHospitals = hospitals.stream()
                .filter(Hospital::isVerified)
                .count();

        long closedPermanently = hospitals.stream()
                .filter(Hospital::isClosedPermanently)
                .count();

        long suspendedHospitals = hospitals.stream()
                .filter(h -> !h.isActive() && !h.isClosedPermanently())
                .count();

        long blockedUsers = users.stream()
                .filter(User::isBlocked)
                .count();

        long openReports = reports.stream()
                .filter(r -> "PENDING_REVIEW".equalsIgnoreCase(r.getStatus()) || "INVESTIGATING".equalsIgnoreCase(r.getStatus()))
                .count();

        long userToHospitalReports = reports.stream()
                .filter(r -> "USER_TO_HOSPITAL".equalsIgnoreCase(r.getReportType()))
                .count();

        long hospitalToUserReports = reports.stream()
                .filter(r -> "HOSPITAL_TO_USER".equalsIgnoreCase(r.getReportType()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHospitals", hospitals.size());
        stats.put("pendingHospitals", pendingHospitals);
        stats.put("verifiedHospitals", verifiedHospitals);
        stats.put("suspendedHospitals", suspendedHospitals);
        stats.put("closedPermanentlyHospitals", closedPermanently);
        stats.put("totalUsers", users.size());
        stats.put("blockedUsers", blockedUsers);
        stats.put("totalDoctors", doctorRepository.count());
        stats.put("totalNurses", nurseRepository.count());
        stats.put("totalEmergencyCases", emergencyCaseRepository.count());
        stats.put("totalReports", reports.size());
        stats.put("openReports", openReports);
        stats.put("userToHospitalReports", userToHospitalReports);
        stats.put("hospitalToUserReports", hospitalToUserReports);

        return stats;
    }

    /**
     * Get Hospitals with Dynamic Search and Status Filtering
     */
    public List<Map<String, Object>> getHospitals(String search, String status) {
        List<Hospital> hospitals = hospitalRepository.findAll();
        String q = search != null ? search.trim().toLowerCase() : "";

        return hospitals.stream()
                .filter(h -> {
                    if (q.isEmpty()) return true;
                    return (h.getName() != null && h.getName().toLowerCase().contains(q)) ||
                           (h.getEmail() != null && h.getEmail().toLowerCase().contains(q)) ||
                           (h.getAddress() != null && h.getAddress().toLowerCase().contains(q)) ||
                           (h.getLicenseNumber() != null && h.getLicenseNumber().toLowerCase().contains(q));
                })
                .filter(h -> {
                    if (status == null || status.equalsIgnoreCase("ALL")) return true;
                    if (status.equalsIgnoreCase("PENDING")) return !h.isVerified() && !h.isClosedPermanently();
                    if (status.equalsIgnoreCase("VERIFIED")) return h.isVerified() && h.isActive() && !h.isClosedPermanently();
                    if (status.equalsIgnoreCase("SUSPENDED")) return !h.isActive() && !h.isClosedPermanently();
                    if (status.equalsIgnoreCase("CLOSED_PERMANENTLY")) return h.isClosedPermanently();
                    return true;
                })
                .map(this::mapHospitalAdminDto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapHospitalAdminDto(Hospital h) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", h.getId());
        map.put("name", h.getName());
        map.put("email", h.getEmail());
        map.put("contactNumber", h.getContactNumber());
        map.put("address", h.getAddress());
        map.put("latitude", h.getLatitude());
        map.put("longitude", h.getLongitude());
        map.put("licenseNumber", h.getLicenseNumber());
        map.put("directorName", h.getDirectorName());
        map.put("isVerified", h.isVerified());
        map.put("isActive", h.isActive());
        map.put("isOpen", h.isOpen());
        map.put("isClosedPermanently", h.isClosedPermanently());
        map.put("verificationNotes", h.getVerificationNotes());
        map.put("totalBeds", h.getTotalBeds());
        map.put("availableBeds", h.getAvailableBeds());
        map.put("totalICUBeds", h.getTotalICUBeds());
        map.put("availableICUBeds", h.getAvailableICUBeds());
        map.put("ventilators", h.getVentilators());
        map.put("ambulances", h.getAmbulances());
        map.put("oxygenCylinders", h.getOxygenCylinders());
        map.put("doctorCount", h.getDoctors() != null ? h.getDoctors().size() : 0);
        map.put("createdAt", h.getCreatedAt());

        // Derived status label
        String statusLabel = "PENDING_VERIFICATION";
        if (h.isClosedPermanently()) {
            statusLabel = "CLOSED_PERMANENTLY";
        } else if (!h.isActive()) {
            statusLabel = "SUSPENDED";
        } else if (h.isVerified()) {
            statusLabel = "VERIFIED_OPERATIONAL";
        }
        map.put("statusLabel", statusLabel);

        return map;
    }

    /**
     * Admin Action: Verify Hospital
     */
    @Transactional
    public Hospital verifyHospital(Long id, String notes) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        hospital.setVerified(true);
        hospital.setActive(true);
        hospital.setOpen(true);
        hospital.setClosedPermanently(false);
        hospital.setVerificationNotes(notes != null ? notes : "Verified by Health Regulator Administrator on " + LocalDateTime.now());

        return hospitalRepository.save(hospital);
    }

    /**
     * Admin Action: Undo / Revoke Hospital Verification
     */
    @Transactional
    public Hospital unverifyHospital(Long id, String reason) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        hospital.setVerified(false);
        hospital.setOpen(false);
        hospital.setVerificationNotes("Verification revoked: " + (reason != null ? reason : "Administrative compliance check."));

        return hospitalRepository.save(hospital);
    }

    /**
     * Admin Action: Suspend or Unsuspend Hospital
     */
    @Transactional
    public Hospital toggleHospitalSuspend(Long id, String reason) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        boolean newActive = !hospital.isActive();
        hospital.setActive(newActive);
        if (!newActive) {
            hospital.setOpen(false);
            hospital.setVerificationNotes("Suspended: " + (reason != null ? reason : "Operational violation."));
        } else {
            hospital.setVerificationNotes("Reactivated by administrator on " + LocalDateTime.now());
        }

        return hospitalRepository.save(hospital);
    }

    /**
     * Admin Action: Close Hospital Permanently (Undoable if mistake)
     */
    @Transactional
    public Hospital closeHospitalPermanently(Long id, String reason) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        hospital.setClosedPermanently(true);
        hospital.setVerified(false);
        hospital.setActive(false);
        hospital.setOpen(false);
        hospital.setVerificationNotes("Permanently decommissioned: " + (reason != null ? reason : "Permanent closure order."));

        return hospitalRepository.save(hospital);
    }

    /**
     * Admin Action: Reopen Permanently Closed Hospital (Undo action)
     */
    @Transactional
    public Hospital undoPermanentClosure(Long id, String reason) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + id));

        hospital.setClosedPermanently(false);
        hospital.setVerified(false); // requires re-verification
        hospital.setActive(true);
        hospital.setVerificationNotes("Permanent closure undone. Pending formal re-verification. " + (reason != null ? reason : ""));

        return hospitalRepository.save(hospital);
    }

    /**
     * Get Users with Search and Status Filtering
     */
    public List<Map<String, Object>> getUsers(String search, String status) {
        List<User> users = userRepository.findAll();
        String q = search != null ? search.trim().toLowerCase() : "";

        return users.stream()
                .filter(u -> {
                    if (q.isEmpty()) return true;
                    return (u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) ||
                           (u.getPhone() != null && u.getPhone().toLowerCase().contains(q));
                })
                .filter(u -> {
                    if (status == null || status.equalsIgnoreCase("ALL")) return true;
                    if (status.equalsIgnoreCase("ACTIVE")) return !u.isBlocked();
                    if (status.equalsIgnoreCase("BLOCKED")) return u.isBlocked();
                    return true;
                })
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());
                    map.put("phone", u.getPhone());
                    map.put("age", u.getAge());
                    map.put("gender", u.getGender());
                    map.put("knownConditions", u.getKnownConditions());
                    map.put("role", u.getRole().name());
                    map.put("isBlocked", u.isBlocked());
                    map.put("blockedReason", u.getBlockedReason());
                    map.put("createdAt", u.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Admin Action: Block or Unblock Citizen User
     */
    @Transactional
    public User toggleBlockUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        boolean newBlocked = !user.isBlocked();
        user.setBlocked(newBlocked);
        user.setBlockedReason(newBlocked ? (reason != null ? reason : "Violation of emergency response terms.") : null);

        return userRepository.save(user);
    }

    /**
     * Get Two-Way Reports with Filtering
     */
    public List<FeedbackReport> getReports(String type, String status, String severity) {
        List<FeedbackReport> list = feedbackReportRepository.findAllByOrderByCreatedAtDesc();

        return list.stream()
                .filter(r -> {
                    if (type == null || type.equalsIgnoreCase("ALL")) return true;
                    return r.getReportType() != null && r.getReportType().equalsIgnoreCase(type);
                })
                .filter(r -> {
                    if (status == null || status.equalsIgnoreCase("ALL")) return true;
                    return r.getStatus() != null && r.getStatus().equalsIgnoreCase(status);
                })
                .filter(r -> {
                    if (severity == null || severity.equalsIgnoreCase("ALL")) return true;
                    return r.getSeverity() != null && r.getSeverity().equalsIgnoreCase(severity);
                })
                .collect(Collectors.toList());
    }

    /**
     * Admin Action: Resolve or Enforce Penalty on a Report
     */
    @Transactional
    public FeedbackReport actionReport(Long reportId, String newStatus, String adminAction, String adminNotes, boolean blockTarget) {
        FeedbackReport report = feedbackReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        report.setStatus(newStatus != null ? newStatus : "ACTION_TAKEN");
        report.setAdminAction(adminAction);
        report.setAdminNotes(adminNotes);

        // Automatic target penalty enforcement if requested
        if (blockTarget && report.getTargetId() != null) {
            if ("HOSPITAL_TO_USER".equalsIgnoreCase(report.getReportType())) {
                // Block the reported user
                userRepository.findById(report.getTargetId()).ifPresent(u -> {
                    u.setBlocked(true);
                    u.setBlockedReason("Blocked following verified hospital report: " + report.getCategory());
                    userRepository.save(u);
                });
                report.setAdminAction(adminAction != null ? adminAction : "Reported User Blocked & Disciplinary Notice Issued");
            } else if ("USER_TO_HOSPITAL".equalsIgnoreCase(report.getReportType())) {
                // Suspend the reported hospital
                hospitalRepository.findById(report.getTargetId()).ifPresent(h -> {
                    h.setActive(false);
                    h.setOpen(false);
                    h.setVerificationNotes("Suspended pending investigation: " + report.getCategory());
                    hospitalRepository.save(h);
                });
                report.setAdminAction(adminAction != null ? adminAction : "Hospital Suspended & Investigation Bay Dispatched");
            }
        }

        return feedbackReportRepository.save(report);
    }

    /**
     * Submit Two-Way Feedback or Incident Report
     */
    @Transactional
    public FeedbackReport submitFeedbackReport(FeedbackReport report) {
        if (report.getReportType() == null) {
            report.setReportType("USER_TO_HOSPITAL");
        }
        if (report.getStatus() == null) {
            report.setStatus("PENDING_REVIEW");
        }
        if (report.getSeverity() == null) {
            report.setSeverity("MEDIUM");
        }

        return feedbackReportRepository.save(report);
    }

    /**
     * Public / Patient Hospital Reviews
     */
    public List<FeedbackReport> getHospitalReviews(Long hospitalId) {
        return feedbackReportRepository.findByTargetIdAndReportTypeOrderByCreatedAtDesc(hospitalId, "USER_TO_HOSPITAL");
    }

    /**
     * Dynamic Workforce Directory (Doctors & Nurses across all hospitals)
     */
    public List<Map<String, Object>> getWorkforce(Long hospitalId, String roleFilter, String departmentFilter) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. Doctors
        if (roleFilter == null || roleFilter.equalsIgnoreCase("ALL") || roleFilter.equalsIgnoreCase("DOCTOR")) {
            List<Doctor> doctors = hospitalId != null ?
                    doctorRepository.findByHospitalId(hospitalId) : doctorRepository.findAll();

            for (Doctor d : doctors) {
                if (departmentFilter != null && !departmentFilter.isEmpty() &&
                        !d.getSpecialization().name().equalsIgnoreCase(departmentFilter) &&
                        !d.getSpecialization().getDescription().toLowerCase().contains(departmentFilter.toLowerCase())) {
                    continue;
                }
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", d.getId());
                map.put("name", d.getName());
                map.put("role", "Doctor");
                map.put("roleCode", "DOCTOR");
                map.put("specialization", d.getSpecialization().getDescription());
                map.put("specializationCode", d.getSpecialization().name());
                map.put("experienceYears", d.getExperienceYears());
                map.put("qualification", d.getQualification());
                map.put("isOnDuty", d.isOnDuty());
                map.put("hospitalId", d.getHospital().getId());
                map.put("hospitalName", d.getHospital().getName());
                result.add(map);
            }
        }

        // 2. Nurses
        if (roleFilter == null || roleFilter.equalsIgnoreCase("ALL") || roleFilter.equalsIgnoreCase("NURSE")) {
            List<Nurse> nurses = hospitalId != null ?
                    nurseRepository.findByHospitalId(hospitalId) : nurseRepository.findAll();

            for (Nurse n : nurses) {
                if (departmentFilter != null && !departmentFilter.isEmpty() &&
                        !n.getDepartment().toLowerCase().contains(departmentFilter.toLowerCase())) {
                    continue;
                }
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", n.getId());
                map.put("name", n.getName());
                map.put("role", "Nurse");
                map.put("roleCode", "NURSE");
                map.put("specialization", n.getDepartment());
                map.put("specializationCode", "NURSING_" + n.getDepartment().toUpperCase().replace(" ", "_"));
                map.put("experienceYears", n.getExperienceYears());
                map.put("shift", n.getShift());
                map.put("isOnDuty", n.isOnDuty());
                map.put("hospitalId", n.getHospital().getId());
                map.put("hospitalName", n.getHospital().getName());
                result.add(map);
            }
        }

        return result;
    }

    /**
     * Toggle On-Duty Status for any Doctor or Nurse
     */
    @Transactional
    public Map<String, Object> toggleStaffOnDuty(String roleCode, Long id) {
        Map<String, Object> response = new HashMap<>();

        if ("DOCTOR".equalsIgnoreCase(roleCode)) {
            Doctor d = doctorRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
            d.setOnDuty(!d.isOnDuty());
            doctorRepository.save(d);
            response.put("success", true);
            response.put("isOnDuty", d.isOnDuty());
            response.put("name", d.getName());
            response.put("role", "Doctor");
        } else if ("NURSE".equalsIgnoreCase(roleCode)) {
            Nurse n = nurseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Nurse not found: " + id));
            n.setOnDuty(!n.isOnDuty());
            nurseRepository.save(n);
            response.put("success", true);
            response.put("isOnDuty", n.isOnDuty());
            response.put("name", n.getName());
            response.put("role", "Nurse");
        } else {
            throw new IllegalArgumentException("Unknown staff role: " + roleCode);
        }

        return response;
    }
}
