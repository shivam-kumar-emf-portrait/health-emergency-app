package com.lifeline.emergency.dto;

public class SubmitHospitalReviewRequest {
    private Long hospitalId;
    private Integer rating; // 1 to 5
    private String category; // e.g. Care Quality, Doctor Attention, Bed Availability, Response Speed
    private String description; // User's detailed review / complaint
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    public SubmitHospitalReviewRequest() {}

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}

