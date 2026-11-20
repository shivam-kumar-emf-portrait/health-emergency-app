package com.lifeline.emergency.dto;

import java.util.Map;

public class HospitalOperationsRequest {
    private Integer totalBeds;
    private Integer availableBeds;
    private Integer totalICUBeds;
    private Integer availableICUBeds;
    private Integer ventilators;
    private Integer oxygenCylinders;
    private Integer ambulances;
    private Integer cardiacMonitors;
    private Boolean isOpen;
    private Map<String, Integer> bloodInventory;

    public HospitalOperationsRequest() {}

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }

    public Integer getTotalICUBeds() { return totalICUBeds; }
    public void setTotalICUBeds(Integer totalICUBeds) { this.totalICUBeds = totalICUBeds; }

    public Integer getAvailableICUBeds() { return availableICUBeds; }
    public void setAvailableICUBeds(Integer availableICUBeds) { this.availableICUBeds = availableICUBeds; }

    public Integer getVentilators() { return ventilators; }
    public void setVentilators(Integer ventilators) { this.ventilators = ventilators; }

    public Integer getOxygenCylinders() { return oxygenCylinders; }
    public void setOxygenCylinders(Integer oxygenCylinders) { this.oxygenCylinders = oxygenCylinders; }

    public Integer getAmbulances() { return ambulances; }
    public void setAmbulances(Integer ambulances) { this.ambulances = ambulances; }

    public Integer getCardiacMonitors() { return cardiacMonitors; }
    public void setCardiacMonitors(Integer cardiacMonitors) { this.cardiacMonitors = cardiacMonitors; }

    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean open) { isOpen = open; }

    public Map<String, Integer> getBloodInventory() { return bloodInventory; }
    public void setBloodInventory(Map<String, Integer> bloodInventory) { this.bloodInventory = bloodInventory; }
}

