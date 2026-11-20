package com.lifeline.emergency.dto;

import java.util.List;

public class EmergencyTransitAiResponse {
    private String emergencyType;
    private String hospitalName;
    private String hospitalPhone;
    private String etaFormatted;
    private String transitProtocols;
    private List<String> immediateDos;
    private List<String> immediateDonts;
    private String sbarBrief;
    private String navigationUrl;
    private boolean hospitalPreAlertActive;

    public EmergencyTransitAiResponse() {}

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalPhone() { return hospitalPhone; }
    public void setHospitalPhone(String hospitalPhone) { this.hospitalPhone = hospitalPhone; }

    public String getEtaFormatted() { return etaFormatted; }
    public void setEtaFormatted(String etaFormatted) { this.etaFormatted = etaFormatted; }

    public String getTransitProtocols() { return transitProtocols; }
    public void setTransitProtocols(String transitProtocols) { this.transitProtocols = transitProtocols; }

    public List<String> getImmediateDos() { return immediateDos; }
    public void setImmediateDos(List<String> immediateDos) { this.immediateDos = immediateDos; }

    public List<String> getImmediateDonts() { return immediateDonts; }
    public void setImmediateDonts(List<String> immediateDonts) { this.immediateDonts = immediateDonts; }

    public String getSbarBrief() { return sbarBrief; }
    public void setSbarBrief(String sbarBrief) { this.sbarBrief = sbarBrief; }

    public String getNavigationUrl() { return navigationUrl; }
    public void setNavigationUrl(String navigationUrl) { this.navigationUrl = navigationUrl; }

    public boolean isHospitalPreAlertActive() { return hospitalPreAlertActive; }
    public void setHospitalPreAlertActive(boolean hospitalPreAlertActive) { this.hospitalPreAlertActive = hospitalPreAlertActive; }
}
