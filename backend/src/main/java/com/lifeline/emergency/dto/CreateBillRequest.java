package com.lifeline.emergency.dto;

import com.lifeline.emergency.entity.BillItem;
import java.util.List;

public class CreateBillRequest {
    private Long patientId;
    private String paymentStatus = "PENDING";
    private String paymentMethod = "CASH";
    private double discountAmount = 0.0;
    private double taxAmount = 0.0;
    private List<BillItem> items;

    public CreateBillRequest() {}

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
}
