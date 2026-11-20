package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.CreateBillRequest;
import com.lifeline.emergency.entity.BillItem;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.entity.HospitalPatient;
import com.lifeline.emergency.entity.PatientBill;
import com.lifeline.emergency.repository.HospitalPatientRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import com.lifeline.emergency.repository.PatientBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class HospitalBillingService {

    private final PatientBillRepository billRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalPatientRepository patientRepository;

    public HospitalBillingService(PatientBillRepository billRepository,
                                  HospitalRepository hospitalRepository,
                                  HospitalPatientRepository patientRepository) {
        this.billRepository = billRepository;
        this.hospitalRepository = hospitalRepository;
        this.patientRepository = patientRepository;
    }

    public List<PatientBill> getHospitalBills(Long hospitalId) {
        return billRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId);
    }

    public PatientBill getBillById(Long hospitalId, Long billId) {
        PatientBill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
        if (!bill.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized bill access.");
        }
        return bill;
    }

    @Transactional
    public PatientBill createBill(Long hospitalId, CreateBillRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));

        HospitalPatient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + request.getPatientId()));

        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Patient does not belong to this hospital.");
        }

        List<BillItem> items = request.getItems() != null ? request.getItems() : new ArrayList<>();
        double subtotal = 0.0;
        for (BillItem item : items) {
            item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
            subtotal += item.getTotalPrice();
        }

        double tax = request.getTaxAmount() > 0 ? request.getTaxAmount() : Math.round(subtotal * 0.05 * 100.0) / 100.0; // 5% GST
        double discount = request.getDiscountAmount();
        double grandTotal = Math.max(0.0, subtotal + tax - discount);

        int randomSuffix = ThreadLocalRandom.current().nextInt(10000, 99999);
        String invoiceNumber = "INV-2026-" + randomSuffix;

        PatientBill bill = new PatientBill();
        bill.setInvoiceNumber(invoiceNumber);
        bill.setHospital(hospital);
        bill.setPatient(patient);
        bill.setItems(items);
        bill.setSubtotal(subtotal);
        bill.setTaxAmount(tax);
        bill.setDiscountAmount(discount);
        bill.setGrandTotal(grandTotal);
        bill.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus().toUpperCase() : "PENDING");
        bill.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH");
        bill.setCreatedAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    @Transactional
    public PatientBill updatePaymentStatus(Long hospitalId, Long billId, String paymentStatus, String paymentMethod) {
        PatientBill bill = getBillById(hospitalId, billId);
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            bill.setPaymentStatus(paymentStatus.toUpperCase());
        }
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            bill.setPaymentMethod(paymentMethod.toUpperCase());
        }
        return billRepository.save(bill);
    }
}
