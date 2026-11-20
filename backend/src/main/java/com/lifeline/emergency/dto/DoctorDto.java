package com.lifeline.emergency.dto;

import com.lifeline.emergency.enums.DoctorSpecialization;

public class DoctorDto {
    private Long id;
    private String name;
    private DoctorSpecialization specialization;
    private String specializationDescription;
    private String qualification;
    private int experienceYears;
    private boolean isOnDuty;
    private boolean isPublicProfile;

    public DoctorDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DoctorSpecialization getSpecialization() { return specialization; }
    public void setSpecialization(DoctorSpecialization specialization) {
        this.specialization = specialization;
        if (specialization != null) {
            this.specializationDescription = specialization.getDescription();
        }
    }

    public String getSpecializationDescription() { return specializationDescription; }
    public void setSpecializationDescription(String specializationDescription) { this.specializationDescription = specializationDescription; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public boolean isOnDuty() { return isOnDuty; }
    public void setOnDuty(boolean onDuty) { isOnDuty = onDuty; }

    public boolean isPublicProfile() { return isPublicProfile; }
    public void setPublicProfile(boolean publicProfile) { isPublicProfile = publicProfile; }
}

