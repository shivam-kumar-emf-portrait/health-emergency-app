package com.lifeline.emergency.dto;

import com.lifeline.emergency.enums.EmergencyType;
import java.util.List;

public class EmergencySearchResponse {
    private EmergencyType emergencyType;
    private String emergencyTitle;
    private String requiredSpecialty;
    private double userLatitude;
    private double userLongitude;
    private String locationName;
    private int totalHospitalsFound;
    private List<HospitalScoreDto> topNearestHospitals;
    private List<HospitalScoreDto> topBestHospitals;

    public EmergencySearchResponse() {}

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) { this.emergencyType = emergencyType; }

    public String getEmergencyTitle() { return emergencyTitle; }
    public void setEmergencyTitle(String emergencyTitle) { this.emergencyTitle = emergencyTitle; }

    public String getRequiredSpecialty() { return requiredSpecialty; }
    public void setRequiredSpecialty(String requiredSpecialty) { this.requiredSpecialty = requiredSpecialty; }

    public double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(double userLatitude) { this.userLatitude = userLatitude; }

    public double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(double userLongitude) { this.userLongitude = userLongitude; }

    public int getTotalHospitalsFound() { return totalHospitalsFound; }
    public void setTotalHospitalsFound(int totalHospitalsFound) { this.totalHospitalsFound = totalHospitalsFound; }

    public List<HospitalScoreDto> getTopNearestHospitals() { return topNearestHospitals; }
    public void setTopNearestHospitals(List<HospitalScoreDto> topNearestHospitals) { this.topNearestHospitals = topNearestHospitals; }

    public List<HospitalScoreDto> getTopBestHospitals() { return topBestHospitals; }
    public void setTopBestHospitals(List<HospitalScoreDto> topBestHospitals) { this.topBestHospitals = topBestHospitals; }
}

