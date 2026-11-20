package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.AttendanceLogRequest;
import com.lifeline.emergency.entity.EmployeeAttendance;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.repository.EmployeeAttendanceRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class StaffAttendanceService {

    private final EmployeeAttendanceRepository attendanceRepository;
    private final HospitalRepository hospitalRepository;

    public StaffAttendanceService(EmployeeAttendanceRepository attendanceRepository,
                                  HospitalRepository hospitalRepository) {
        this.attendanceRepository = attendanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public List<EmployeeAttendance> getAttendance(Long hospitalId, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return attendanceRepository.findByHospitalIdAndDate(hospitalId, queryDate);
    }

    public List<EmployeeAttendance> getAllRecentAttendance(Long hospitalId) {
        return attendanceRepository.findByHospitalIdOrderByDateDesc(hospitalId);
    }

    @Transactional
    public EmployeeAttendance logAttendance(Long hospitalId, AttendanceLogRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));

        EmployeeAttendance attendance = new EmployeeAttendance();
        attendance.setHospital(hospital);
        attendance.setEmployeeName(request.getEmployeeName());
        attendance.setEmployeeRole(request.getEmployeeRole());
        attendance.setShift(request.getShift() != null ? request.getShift().toUpperCase() : "MORNING");
        attendance.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "PRESENT");
        attendance.setDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public EmployeeAttendance checkOut(Long hospitalId, Long attendanceId) {
        EmployeeAttendance record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + attendanceId));

        if (!record.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized attendance access.");
        }

        record.setCheckOutTime(LocalTime.now());
        return attendanceRepository.save(record);
    }
}
