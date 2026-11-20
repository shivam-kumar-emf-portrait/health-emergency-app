package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {
    List<Nurse> findByHospitalId(Long hospitalId);
    List<Nurse> findByHospitalIdAndIsOnDutyTrue(Long hospitalId);
}

