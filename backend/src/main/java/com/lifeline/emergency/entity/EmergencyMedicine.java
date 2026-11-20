package com.lifeline.emergency.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "emergency_medicines")
public class EmergencyMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicineName;

    private String category; // Resuscitation, Cardiovascular, Anticoagulant, IV Fluids, Analgesic
    private int unitsInStock = 50;
    private String unitType = "Ampoules"; // Ampoules, Vials, Bottles, Strips
    private int lowStockThreshold = 15;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    public EmergencyMedicine() {}

    public EmergencyMedicine(String medicineName, String category, int unitsInStock, String unitType, int lowStockThreshold, Hospital hospital) {
        this.medicineName = medicineName;
        this.category = category;
        this.unitsInStock = unitsInStock;
        this.unitType = unitType;
        this.lowStockThreshold = lowStockThreshold;
        this.hospital = hospital;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getUnitsInStock() { return unitsInStock; }
    public void setUnitsInStock(int unitsInStock) { this.unitsInStock = unitsInStock; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
}
