package com.lifeline.emergency.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String contactNumber;
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String licenseNumber = "WB-MOH-2024-REG";
    private String directorName = "Medical Director";
    private boolean isVerified = false;
    private boolean isActive = true;
    private boolean isOpen = true;
    private boolean hasEmergency = true;
    private boolean isClosedPermanently = false;
    private String verificationNotes;

    // Bed Management
    private int totalBeds = 50;
    private int availableBeds = 15;
    private int totalICUBeds = 10;
    private int availableICUBeds = 3;

    // Equipment
    private int ventilators = 5;
    private int oxygenCylinders = 20;
    private int ambulances = 2;
    private int cardiacMonitors = 8;

    // Blood Inventory (Blood Group -> Units Available)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_blood_inventory", joinColumns = @JoinColumn(name = "hospital_id"))
    @MapKeyColumn(name = "blood_group")
    @Column(name = "units")
    private Map<String, Integer> bloodInventory = new HashMap<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Doctor> doctors = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bloodInventory.isEmpty()) {
            bloodInventory.put("O+", 10);
            bloodInventory.put("O-", 4);
            bloodInventory.put("A+", 8);
            bloodInventory.put("A-", 3);
            bloodInventory.put("B+", 9);
            bloodInventory.put("B-", 2);
            bloodInventory.put("AB+", 5);
            bloodInventory.put("AB-", 1);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Hospital() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }

    public boolean isClosedPermanently() { return isClosedPermanently; }
    public void setClosedPermanently(boolean closedPermanently) { isClosedPermanently = closedPermanently; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    public boolean isHasEmergency() { return hasEmergency; }
    public void setHasEmergency(boolean hasEmergency) { this.hasEmergency = hasEmergency; }

    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public int getTotalICUBeds() { return totalICUBeds; }
    public void setTotalICUBeds(int totalICUBeds) { this.totalICUBeds = totalICUBeds; }

    public int getAvailableICUBeds() { return availableICUBeds; }
    public void setAvailableICUBeds(int availableICUBeds) { this.availableICUBeds = availableICUBeds; }

    public int getVentilators() { return ventilators; }
    public void setVentilators(int ventilators) { this.ventilators = ventilators; }

    public int getOxygenCylinders() { return oxygenCylinders; }
    public void setOxygenCylinders(int oxygenCylinders) { this.oxygenCylinders = oxygenCylinders; }

    public int getAmbulances() { return ambulances; }
    public void setAmbulances(int ambulances) { this.ambulances = ambulances; }

    public int getCardiacMonitors() { return cardiacMonitors; }
    public void setCardiacMonitors(int cardiacMonitors) { this.cardiacMonitors = cardiacMonitors; }

    public Map<String, Integer> getBloodInventory() { return bloodInventory; }
    public void setBloodInventory(Map<String, Integer> bloodInventory) { this.bloodInventory = bloodInventory; }

    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> doctors) { this.doctors = doctors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

