package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND h.isVerified = true AND h.isOpen = true AND h.hasEmergency = true AND (h.isClosedPermanently = false OR h.isClosedPermanently IS NULL)")
    List<Hospital> findActiveVerifiedOpenEmergencyHospitals();

    default List<Hospital> findByIsActiveTrueAndIsVerifiedTrueAndIsOpenTrueAndHasEmergencyTrue() {
        return findActiveVerifiedOpenEmergencyHospitals();
    }
}


