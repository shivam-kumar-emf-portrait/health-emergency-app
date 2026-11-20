package com.lifeline.emergency.dto;

import com.lifeline.emergency.entity.EmergencyCase;
import com.lifeline.emergency.entity.FeedbackReport;
import com.lifeline.emergency.entity.HospitalPatient;
import com.lifeline.emergency.entity.User;

import java.util.List;

public class UserProfileResponse {
    private User user;
    private List<EmergencyCase> alertHistory;
    private List<HospitalPatient> checkupHistory;
    private List<FeedbackReport> feedbackHistory;

    public UserProfileResponse() {}

    public UserProfileResponse(User user,
                               List<EmergencyCase> alertHistory,
                               List<HospitalPatient> checkupHistory,
                               List<FeedbackReport> feedbackHistory) {
        this.user = user;
        this.alertHistory = alertHistory;
        this.checkupHistory = checkupHistory;
        this.feedbackHistory = feedbackHistory;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<EmergencyCase> getAlertHistory() { return alertHistory; }
    public void setAlertHistory(List<EmergencyCase> alertHistory) { this.alertHistory = alertHistory; }

    public List<HospitalPatient> getCheckupHistory() { return checkupHistory; }
    public void setCheckupHistory(List<HospitalPatient> checkupHistory) { this.checkupHistory = checkupHistory; }

    public List<FeedbackReport> getFeedbackHistory() { return feedbackHistory; }
    public void setFeedbackHistory(List<FeedbackReport> feedbackHistory) { this.feedbackHistory = feedbackHistory; }
}

