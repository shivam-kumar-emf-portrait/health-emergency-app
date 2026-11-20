package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.AttendanceLogRequest;
import com.lifeline.emergency.entity.EmployeeAttendance;
import com.lifeline.emergency.service.StaffAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/attendance")
public class StaffAttendanceController {

    private final StaffAttendanceService attendanceService;

    public StaffAttendanceController(StaffAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    private Long getHospitalId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        List<EmployeeAttendance> records = attendanceService.getAttendance(hospitalId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("totalPresent", records.stream().filter(r -> "PRESENT".equalsIgnoreCase(r.getStatus()) || "ON_DUTY".equalsIgnoreCase(r.getStatus())).count());
        response.put("totalOnDuty", records.stream().filter(r -> "ON_DUTY".equalsIgnoreCase(r.getStatus())).count());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> logAttendance(@RequestBody AttendanceLogRequest request, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        EmployeeAttendance record = attendanceService.logAttendance(hospitalId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Attendance logged for " + record.getEmployeeName());
        response.put("record", record);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long id, Authentication auth) {
        Long hospitalId = getHospitalId(auth);
        EmployeeAttendance record = attendanceService.checkOut(hospitalId, id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Checked out successfully: " + record.getEmployeeName());
        response.put("record", record);
        return ResponseEntity.ok(response);
    }
}
