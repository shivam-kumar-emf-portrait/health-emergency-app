package com.lifeline.emergency.enums;

public enum DoctorSpecialization {
    CARDIOLOGY("Cardiologist / Heart Specialist"),
    NEUROLOGY("Neurologist / Brain Specialist"),
    TRAUMA_SURGERY("Trauma Surgeon"),
    ORTHOPEDICS("Orthopedic Surgeon"),
    PULMONOLOGY("Pulmonologist / Chest Specialist"),
    PEDIATRICS("Pediatrician"),
    GYNECOLOGY("Obstetrician / Gynecologist"),
    GENERAL_EMERGENCY("Emergency Medicine Physician"),
    ANESTHESIOLOGY("Critical Care / Anesthesiologist");

    private final String description;

    DoctorSpecialization(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

