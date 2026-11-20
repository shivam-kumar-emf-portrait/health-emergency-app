package com.lifeline.emergency.dto;

import java.util.List;
import java.util.Map;

public class HospitalScoreDto {
    private Long id;
    private String name;
    private String contactNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private double distanceKm;
    private String distanceFormatted;
    private String estimatedTravelTime;
    private int availableBeds;
    private int totalBeds;
    private int availableICUBeds;
    private int totalICUBeds;
    private int ventilators;
    private int oxygenCylinders;
    private int ambulances;
    private int cardiacMonitors;
    private boolean isOpen;
    private double overallScore;
    private double specialistScore;
    private double icuScore;
    private double bedScore;
    private double equipmentScore;
    private double proximityScore;
    private String scoreBreakdownSummary;
    private boolean hasMatchingSpecialist;
    private List<String> matchingSpecialists;
    private List<String> matchHighlights;
    private Map<String, Integer> bloodInventory;
    private Double averageRating;
    private Integer totalReviews;

    public HospitalScoreDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getDistanceFormatted() { return distanceFormatted; }
    public void setDistanceFormatted(String distanceFormatted) { this.distanceFormatted = distanceFormatted; }

    public String getEstimatedTravelTime() { return estimatedTravelTime; }
    public void setEstimatedTravelTime(String estimatedTravelTime) { this.estimatedTravelTime = estimatedTravelTime; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }

    public int getAvailableICUBeds() { return availableICUBeds; }
    public void setAvailableICUBeds(int availableICUBeds) { this.availableICUBeds = availableICUBeds; }

    public int getTotalICUBeds() { return totalICUBeds; }
    public void setTotalICUBeds(int totalICUBeds) { this.totalICUBeds = totalICUBeds; }

    public int getVentilators() { return ventilators; }
    public void setVentilators(int ventilators) { this.ventilators = ventilators; }

    public int getOxygenCylinders() { return oxygenCylinders; }
    public void setOxygenCylinders(int oxygenCylinders) { this.oxygenCylinders = oxygenCylinders; }

    public int getAmbulances() { return ambulances; }
    public void setAmbulances(int ambulances) { this.ambulances = ambulances; }

    public int getCardiacMonitors() { return cardiacMonitors; }
    public void setCardiacMonitors(int cardiacMonitors) { this.cardiacMonitors = cardiacMonitors; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public double getSpecialistScore() { return specialistScore; }
    public void setSpecialistScore(double specialistScore) { this.specialistScore = specialistScore; }

    public double getIcuScore() { return icuScore; }
    public void setIcuScore(double icuScore) { this.icuScore = icuScore; }

    public double getBedScore() { return bedScore; }
    public void setBedScore(double bedScore) { this.bedScore = bedScore; }

    public double getEquipmentScore() { return equipmentScore; }
    public void setEquipmentScore(double equipmentScore) { this.equipmentScore = equipmentScore; }

    public double getProximityScore() { return proximityScore; }
    public void setProximityScore(double proximityScore) { this.proximityScore = proximityScore; }

    public String getScoreBreakdownSummary() { return scoreBreakdownSummary; }
    public void setScoreBreakdownSummary(String scoreBreakdownSummary) { this.scoreBreakdownSummary = scoreBreakdownSummary; }

    public boolean isHasMatchingSpecialist() { return hasMatchingSpecialist; }
    public void setHasMatchingSpecialist(boolean hasMatchingSpecialist) { this.hasMatchingSpecialist = hasMatchingSpecialist; }

    public List<String> getMatchingSpecialists() { return matchingSpecialists; }
    public void setMatchingSpecialists(List<String> matchingSpecialists) { this.matchingSpecialists = matchingSpecialists; }

    public List<String> getMatchHighlights() { return matchHighlights; }
    public void setMatchHighlights(List<String> matchHighlights) { this.matchHighlights = matchHighlights; }

    public Map<String, Integer> getBloodInventory() { return bloodInventory; }
    public void setBloodInventory(Map<String, Integer> bloodInventory) { this.bloodInventory = bloodInventory; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}

