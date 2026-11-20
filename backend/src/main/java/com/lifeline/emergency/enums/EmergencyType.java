package com.lifeline.emergency.enums;

public enum EmergencyType {
    CARDIAC("Cardiac Arrest / Acute Coronary Syndrome", DoctorSpecialization.CARDIOLOGY),
    RESPIRATORY("Severe Respiratory Distress / Asthma", DoctorSpecialization.PULMONOLOGY),
    TRAUMA("Major Trauma / Multi-Organ / Fracture", DoctorSpecialization.TRAUMA_SURGERY),
    STROKE("Acute Stroke / Neurological Emergency", DoctorSpecialization.NEUROLOGY),
    BLEEDING("Acute Hemorrhage / Active Bleeding", DoctorSpecialization.GENERAL_EMERGENCY),
    PREGNANCY("Obstetric / Labor & Delivery Emergency", DoctorSpecialization.GYNECOLOGY),
    PEDIATRIC("Pediatric Acute Emergency", DoctorSpecialization.PEDIATRICS),
    GENERAL("Acute Unspecified Emergency", DoctorSpecialization.GENERAL_EMERGENCY);

    private final String displayName;
    private final DoctorSpecialization requiredSpecialization;

    EmergencyType(String displayName, DoctorSpecialization requiredSpecialization) {
        this.displayName = displayName;
        this.requiredSpecialization = requiredSpecialization;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DoctorSpecialization getRequiredSpecialization() {
        return requiredSpecialization;
    }
}

