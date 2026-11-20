package com.lifeline.emergency.dto;

import com.lifeline.emergency.enums.EmergencyType;

public class EmergencyTransitAiRequest {
    private EmergencyType emergencyType;
    private String patientSymptoms;
    private Integer patientAge;
    private String patientGender;
    private Double originLat;
    private Double originLng;
    private Long hospitalId;
    private String hospitalName;
    private Double hospitalLat;
    private Double hospitalLng;
    private Double distanceKm;
    private String estimatedMinutes;

    public EmergencyTransitAiRequest() {}

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) { this.emergencyType = emergencyType; }

    public String getPatientSymptoms() { return patientSymptoms; }
    public void setPatientSymptoms(String patientSymptoms) { this.patientSymptoms = patientSymptoms; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Double getOriginLat() { return originLat; }
    public void setOriginLat(Double originLat) { this.originLat = originLat; }

    public Double getOriginLng() { return originLng; }
    public void setOriginLng(Double originLng) { this.originLng = originLng; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public Double getHospitalLat() { return hospitalLat; }
    public void setHospitalLat(Double hospitalLat) { this.hospitalLat = hospitalLat; }

    public Double getHospitalLng() { return hospitalLng; }
    public void setHospitalLng(Double hospitalLng) { this.hospitalLng = hospitalLng; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(String estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
}
