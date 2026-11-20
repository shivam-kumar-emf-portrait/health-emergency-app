package com.lifeline.emergency.dto;

import com.lifeline.emergency.enums.EmergencyType;

public class EmergencySearchRequest {
    private Double latitude;
    private Double longitude;
    private String locationName;
    private EmergencyType emergencyType;

    public EmergencySearchRequest() {}

    public EmergencySearchRequest(Double latitude, Double longitude, EmergencyType emergencyType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyType = emergencyType;
    }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) { this.emergencyType = emergencyType; }
}

