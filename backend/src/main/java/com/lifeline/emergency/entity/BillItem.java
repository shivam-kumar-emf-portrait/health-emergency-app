package com.lifeline.emergency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BillItem {

    @Column(nullable = false)
    private String description;

    private String category; // BED, ICU, DOCTOR_FEE, SURGERY, EQUIPMENT, PHARMACY, LAB
    private int quantity = 1;
    private double unitPrice = 0.0;
    private double totalPrice = 0.0;

    public BillItem() {}

    public BillItem(String description, String category, int quantity, double unitPrice) {
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
