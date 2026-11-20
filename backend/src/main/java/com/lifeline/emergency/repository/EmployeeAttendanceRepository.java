package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {
    List<EmployeeAttendance> findByHospitalIdAndDate(Long hospitalId, LocalDate date);
    List<EmployeeAttendance> findByHospitalIdOrderByDateDesc(Long hospitalId);
}
