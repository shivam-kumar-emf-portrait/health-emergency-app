package com.lifeline.emergency.dto;

import com.lifeline.emergency.enums.EmergencyType;

public class NotifyHospitalRequest {
    private Long hospitalId;
    private EmergencyType emergencyType;
    private String severity = "EMERGENCY";
    private Double userLatitude;
    private Double userLongitude;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private String notes;

    public NotifyHospitalRequest() {}

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) { this.emergencyType = emergencyType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(Double userLatitude) { this.userLatitude = userLatitude; }

    public Double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(Double userLongitude) { this.userLongitude = userLongitude; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

