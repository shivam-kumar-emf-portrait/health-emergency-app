package com.lifeline.emergency.dto;

public class ChatResponse {
    private String reply;
    private boolean isSaved;
    private String severity; // "MILD", "MODERATE", "EMERGENCY"
    private String suggestedSpecialty;
    private String disclaimer;

    public ChatResponse() {}

    public ChatResponse(String reply, boolean isSaved, String severity, String suggestedSpecialty) {
        this.reply = reply;
        this.isSaved = isSaved;
        this.severity = severity;
        this.suggestedSpecialty = suggestedSpecialty;
        this.disclaimer = "Medical triage guidance only. If experiencing acute chest pain, shortness of breath, or stroke symptoms, activate Emergency Mode immediately.";
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSuggestedSpecialty() { return suggestedSpecialty; }
    public void setSuggestedSpecialty(String suggestedSpecialty) { this.suggestedSpecialty = suggestedSpecialty; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}

