package com.lifeline.emergency.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lifeline.emergency.enums.DoctorSpecialization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorSpecialization specialization;

    private String qualification;
    private int experienceYears = 0;
    private boolean isOnDuty = true;
    private boolean isPublicProfile = true;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Doctor() {}

    public Doctor(String name, DoctorSpecialization specialization, String qualification, int experienceYears, boolean isOnDuty, boolean isPublicProfile, Hospital hospital) {
        this.name = name;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.isOnDuty = isOnDuty;
        this.isPublicProfile = isPublicProfile;
        this.hospital = hospital;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DoctorSpecialization getSpecialization() { return specialization; }
    public void setSpecialization(DoctorSpecialization specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public boolean isOnDuty() { return isOnDuty; }
    public void setOnDuty(boolean onDuty) { isOnDuty = onDuty; }

    public boolean isPublicProfile() { return isPublicProfile; }
    public void setPublicProfile(boolean publicProfile) { isPublicProfile = publicProfile; }

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

