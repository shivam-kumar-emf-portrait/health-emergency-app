package com.lifeline.emergency.config;

import com.lifeline.emergency.entity.*;
import com.lifeline.emergency.enums.CaseStatus;
import com.lifeline.emergency.enums.DoctorSpecialization;
import com.lifeline.emergency.enums.EmergencyType;
import com.lifeline.emergency.enums.Role;
import com.lifeline.emergency.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final EmergencyCaseRepository emergencyCaseRepository;
    private final HospitalPatientRepository patientRepository;
    private final PatientBillRepository billRepository;
    private final EmployeeAttendanceRepository attendanceRepository;
    private final EmergencyMedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           HospitalRepository hospitalRepository,
                           DoctorRepository doctorRepository,
                           NurseRepository nurseRepository,
                           FeedbackReportRepository feedbackReportRepository,
                           EmergencyCaseRepository emergencyCaseRepository,
                           HospitalPatientRepository patientRepository,
                           PatientBillRepository billRepository,
                           EmployeeAttendanceRepository attendanceRepository,
                           EmergencyMedicineRepository medicineRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.feedbackReportRepository = feedbackReportRepository;
        this.emergencyCaseRepository = emergencyCaseRepository;
        this.patientRepository = patientRepository;
        this.billRepository = billRepository;
        this.attendanceRepository = attendanceRepository;
        this.medicineRepository = medicineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (hospitalRepository.count() > 0) {
            return; // Already initialized
        }

        // ==========================================
        // 1. SEED SUPER ADMIN & USERS
        // ==========================================
        User admin = new User(
                "Health Authority Administrator",
                "admin@lifeline.org",
                passwordEncoder.encode("admin123"),
                "+91 33 2222 0000",
                46,
                "Official",
                "State Health & Emergency Commission",
                Role.ROLE_SUPER_ADMIN
        );
        userRepository.save(admin);

        User patient = new User(
                "Shivam Kumar",
                "patient@lifeline.com",
                passwordEncoder.encode("password123"),
                "+91 98765 43210",
                38,
                "Male",
                "Mild Hypertension, Asthma",
                Role.ROLE_USER
        );
        userRepository.save(patient);

        User blockedUser = new User(
                "Ramesh Verma",
                "ramesh@citizen.org",
                passwordEncoder.encode("password123"),
                "+91 98111 22334",
                29,
                "Male",
                "None",
                Role.ROLE_USER
        );
        blockedUser.setBlocked(true);
        blockedUser.setBlockedReason("Disciplinary hold: Multiple fraudulent emergency alerts reported by hospital staff.");
        userRepository.save(blockedUser);

        // ==========================================
        // 2. SEED HOSPITAL 1: Metro Heart & Emergency (Cardiology Leader)
        // ==========================================
        Hospital h1 = new Hospital();
        h1.setName("Metro Heart & Emergency Institute");
        h1.setEmail("metro@hospital.com");
        h1.setPassword(passwordEncoder.encode("admin123"));
        h1.setContactNumber("+91 33 2456 7890");
        h1.setAddress("45 Park Street, Central District, Kolkata");
        h1.setLatitude(22.5550);
        h1.setLongitude(22.5550 > 80 ? 22.5550 : 88.3510);
        h1.setVerified(true);
        h1.setActive(true);
        h1.setOpen(true);
        h1.setHasEmergency(true);
        h1.setTotalBeds(65);
        h1.setAvailableBeds(18);
        h1.setTotalICUBeds(15);
        h1.setAvailableICUBeds(6);
        h1.setVentilators(8);
        h1.setOxygenCylinders(25);
        h1.setAmbulances(4);
        h1.setCardiacMonitors(12);

        Map<String, Integer> blood1 = new HashMap<>();
        blood1.put("O+", 15); blood1.put("O-", 6);
        blood1.put("A+", 12); blood1.put("A-", 4);
        blood1.put("B+", 14); blood1.put("B-", 3);
        blood1.put("AB+", 8); blood1.put("AB-", 2);
        h1.setBloodInventory(blood1);
        Hospital savedH1 = hospitalRepository.save(h1);

        // Doctors for Metro Heart
        Doctor d1 = new Doctor("Dr. Arvind Mehta", DoctorSpecialization.CARDIOLOGY, "MD, DM (Cardiology), FACC", 16, true, true, savedH1);
        Doctor d2 = new Doctor("Dr. Pooja Sen", DoctorSpecialization.TRAUMA_SURGERY, "MS (Ortho), Fellowship in Trauma", 9, true, true, savedH1);
        Doctor d3 = new Doctor("Dr. Subhash Bose", DoctorSpecialization.ANESTHESIOLOGY, "MD (Critical Care)", 12, true, true, savedH1);
        doctorRepository.save(d1);
        doctorRepository.save(d2);
        doctorRepository.save(d3);

        // ==========================================
        // 3. SEED HOSPITAL 2: City Care Multi-Specialty (Closest General)
        // ==========================================
        Hospital h2 = new Hospital();
        h2.setName("City Care General Hospital");
        h2.setEmail("citycare@hospital.com");
        h2.setPassword(passwordEncoder.encode("admin123"));
        h2.setContactNumber("+91 33 2289 1122");
        h2.setAddress("12 Esplanade Ave, Kolkata");
        h2.setLatitude(22.5680);
        h2.setLongitude(88.3520);
        h2.setVerified(true);
        h2.setActive(true);
        h2.setOpen(true);
        h2.setHasEmergency(true);
        h2.setTotalBeds(40);
        h2.setAvailableBeds(14);
        h2.setTotalICUBeds(6);
        h2.setAvailableICUBeds(2);
        h2.setVentilators(3);
        h2.setOxygenCylinders(18);
        h2.setAmbulances(2);
        h2.setCardiacMonitors(4);

        Map<String, Integer> blood2 = new HashMap<>();
        blood2.put("O+", 8); blood2.put("O-", 2);
        blood2.put("A+", 6); blood2.put("A-", 1);
        blood2.put("B+", 7); blood2.put("B-", 2);
        blood2.put("AB+", 4); blood2.put("AB-", 1);
        h2.setBloodInventory(blood2);
        Hospital savedH2 = hospitalRepository.save(h2);

        Doctor d4 = new Doctor("Dr. Kabir Das", DoctorSpecialization.PULMONOLOGY, "MD (Chest & Respiratory)", 11, true, true, savedH2);
        Doctor d5 = new Doctor("Dr. Sunita Rao", DoctorSpecialization.GENERAL_EMERGENCY, "MBBS, MEM (Emergency Medicine)", 7, true, true, savedH2);
        doctorRepository.save(d4);
        doctorRepository.save(d5);

        // ==========================================
        // 4. SEED HOSPITAL 3: Apex Trauma & Neuro Center (Stroke & Trauma Leader)
        // ==========================================
        Hospital h3 = new Hospital();
        h3.setName("Apex Trauma & Neuro Institute");
        h3.setEmail("apex@hospital.com");
        h3.setPassword(passwordEncoder.encode("admin123"));
        h3.setContactNumber("+91 33 2555 4321");
        h3.setAddress("88 Bidhan Sarani, Shyambazar, Kolkata");
        h3.setLatitude(22.5990);
        h3.setLongitude(88.3710);
        h3.setVerified(true);
        h3.setActive(true);
        h3.setOpen(true);
        h3.setHasEmergency(true);
        h3.setTotalBeds(80);
        h3.setAvailableBeds(22);
        h3.setTotalICUBeds(20);
        h3.setAvailableICUBeds(8);
        h3.setVentilators(10);
        h3.setOxygenCylinders(30);
        h3.setAmbulances(5);
        h3.setCardiacMonitors(10);

        Map<String, Integer> blood3 = new HashMap<>();
        blood3.put("O+", 20); blood3.put("O-", 8);
        blood3.put("A+", 15); blood3.put("A-", 6);
        blood3.put("B+", 18); blood3.put("B-", 5);
        blood3.put("AB+", 10); blood3.put("AB-", 3);
        h3.setBloodInventory(blood3);
        Hospital savedH3 = hospitalRepository.save(h3);

        Doctor d6 = new Doctor("Dr. Vikram Banerjee", DoctorSpecialization.NEUROLOGY, "MD, DM (Neurology), Stroke Specialist", 18, true, true, savedH3);
        Doctor d7 = new Doctor("Dr. R. K. Gupta", DoctorSpecialization.ORTHOPEDICS, "MS (Ortho), MCh, Complex Trauma", 14, true, true, savedH3);
        Doctor d8 = new Doctor("Dr. Anita Desai", DoctorSpecialization.GENERAL_EMERGENCY, "MD (Emergency Medicine)", 8, true, true, savedH3);
        doctorRepository.save(d6);
        doctorRepository.save(d7);
        doctorRepository.save(d8);

        // ==========================================
        // 5. SEED HOSPITAL 4: Mother & Child Care Hospital (Pregnancy & Pediatrics)
        // ==========================================
        Hospital h4 = new Hospital();
        h4.setName("Mother & Child Emergency Hospital");
        h4.setEmail("motherchild@hospital.com");
        h4.setPassword(passwordEncoder.encode("admin123"));
        h4.setContactNumber("+91 33 2475 9900");
        h4.setAddress("24 Southern Ave, Kolkata");
        h4.setLatitude(22.5180);
        h4.setLongitude(88.3580);
        h4.setVerified(true);
        h4.setActive(true);
        h4.setOpen(true);
        h4.setHasEmergency(true);
        h4.setTotalBeds(50);
        h4.setAvailableBeds(16);
        h4.setTotalICUBeds(10);
        h4.setAvailableICUBeds(5);
        h4.setVentilators(6);
        h4.setOxygenCylinders(20);
        h4.setAmbulances(3);
        h4.setCardiacMonitors(5);

        Map<String, Integer> blood4 = new HashMap<>();
        blood4.put("O+", 10); blood4.put("O-", 5);
        blood4.put("A+", 8); blood4.put("A-", 3);
        blood4.put("B+", 10); blood4.put("B-", 2);
        blood4.put("AB+", 5); blood4.put("AB-", 2);
        h4.setBloodInventory(blood4);
        Hospital savedH4 = hospitalRepository.save(h4);

        Doctor d9 = new Doctor("Dr. Meera Roy", DoctorSpecialization.GYNECOLOGY, "MD, DGO, High Risk Obstetrics", 15, true, true, savedH4);
        Doctor d10 = new Doctor("Dr. Alok Nath", DoctorSpecialization.PEDIATRICS, "MD (Pediatrics), Neonatologist", 12, true, true, savedH4);
        doctorRepository.save(d9);
        doctorRepository.save(d10);

        // ==========================================
        // 6. SEED HOSPITAL 5: LifeLine Global Super-Specialty
        // ==========================================
        Hospital h5 = new Hospital();
        h5.setName("LifeLine Global Super-Specialty Hospital");
        h5.setEmail("lifeline.super@hospital.com");
        h5.setPassword(passwordEncoder.encode("admin123"));
        h5.setContactNumber("+91 33 2345 6700");
        h5.setAddress("100 EM Bypass, Salt Lake, Kolkata");
        h5.setLatitude(22.5690);
        h5.setLongitude(88.4050);
        h5.setVerified(true);
        h5.setActive(true);
        h5.setOpen(true);
        h5.setHasEmergency(true);
        h5.setTotalBeds(120);
        h5.setAvailableBeds(35);
        h5.setTotalICUBeds(25);
        h5.setAvailableICUBeds(9);
        h5.setVentilators(14);
        h5.setOxygenCylinders(40);
        h5.setAmbulances(6);
        h5.setCardiacMonitors(16);

        Map<String, Integer> blood5 = new HashMap<>();
        blood5.put("O+", 25); blood5.put("O-", 10);
        blood5.put("A+", 20); blood5.put("A-", 8);
        blood5.put("B+", 22); blood5.put("B-", 7);
        blood5.put("AB+", 12); blood5.put("AB-", 5);
        h5.setBloodInventory(blood5);
        Hospital savedH5 = hospitalRepository.save(h5);

        Doctor d11 = new Doctor("Dr. Sneha Chatterjee", DoctorSpecialization.CARDIOLOGY, "MD, DM (Cardiology), Interventional", 13, true, true, savedH5);
        Doctor d12 = new Doctor("Dr. Debashis Mukherjee", DoctorSpecialization.PULMONOLOGY, "MD (Pulmonology)", 10, true, true, savedH5);
        Doctor d13 = new Doctor("Dr. Sanjay Varma", DoctorSpecialization.GENERAL_EMERGENCY, "MEM, Critical Care", 9, true, true, savedH5);
        doctorRepository.save(d11);
        doctorRepository.save(d12);
        doctorRepository.save(d13);

        // ==========================================
        // 7. SEED HOSPITAL 6: GreenLife Community Care (Nearby, but Small / No ICU)
        // ==========================================
        Hospital h6 = new Hospital();
        h6.setName("GreenLife Community Clinic & Day Care");
        h6.setEmail("greenlife@hospital.com");
        h6.setPassword(passwordEncoder.encode("admin123"));
        h6.setContactNumber("+91 33 2211 4455");
        h6.setAddress("7 College Square, Kolkata");
        h6.setLatitude(22.5740);
        h6.setLongitude(88.3630);
        h6.setVerified(true);
        h6.setActive(true);
        h6.setOpen(true);
        h6.setHasEmergency(true);
        h6.setTotalBeds(10);
        h6.setAvailableBeds(4);
        h6.setTotalICUBeds(0);
        h6.setAvailableICUBeds(0); // NO ICU BEDS
        h6.setVentilators(0);      // NO VENTILATORS
        h6.setOxygenCylinders(4);
        h6.setAmbulances(1);
        h6.setCardiacMonitors(1);

        Map<String, Integer> blood6 = new HashMap<>();
        blood6.put("O+", 3); blood6.put("A+", 2);
        h6.setBloodInventory(blood6);
        Hospital savedH6 = hospitalRepository.save(h6);

        Doctor d14 = new Doctor("Dr. Manoj Saha", DoctorSpecialization.GENERAL_EMERGENCY, "MBBS", 4, true, true, savedH6);
        doctorRepository.save(d14);

        // ==========================================
        // 8. SEED INITIAL EMERGENCY CASES (FOR HOSPITAL SAAS LIVE BOARD)
        // ==========================================
        EmergencyCase case1 = new EmergencyCase();
        case1.setHospital(savedH1);
        case1.setEmergencyType(EmergencyType.CARDIAC);
        case1.setSeverity("CRITICAL");
        case1.setStatus(CaseStatus.ACTIVE);
        case1.setPatientName("Ramesh Kumar");
        case1.setPatientPhone("+91 98301 23456");
        case1.setPatientEmail("ramesh@gmail.com");
        case1.setUserLatitude(22.5600);
        case1.setUserLongitude(88.3530);
        case1.setNotes("Severe radiating chest pain and sweating for past 30 minutes.");
        emergencyCaseRepository.save(case1);

        EmergencyCase case2 = new EmergencyCase();
        case2.setHospital(savedH1);
        case2.setEmergencyType(EmergencyType.TRAUMA);
        case2.setSeverity("EMERGENCY");
        case2.setStatus(CaseStatus.IN_PROGRESS);
        case2.setPatientName("Priya Sharma");
        case2.setPatientPhone("+91 98311 98765");
        case2.setUserLatitude(22.5530);
        case2.setUserLongitude(88.3490);
        case2.setAssignedDoctor(d2);
        case2.setNotes("Two-wheeler road collision, suspected right femur fracture.");
        emergencyCaseRepository.save(case2);

        EmergencyCase caseShivam = new EmergencyCase();
        caseShivam.setHospital(savedH1);
        caseShivam.setEmergencyType(EmergencyType.RESPIRATORY);
        caseShivam.setSeverity("EMERGENCY");
        caseShivam.setStatus(CaseStatus.COMPLETED);
        caseShivam.setPatientName(patient.getName());
        caseShivam.setPatientPhone(patient.getPhone());
        caseShivam.setPatientEmail(patient.getEmail());
        caseShivam.setUserLatitude(22.5600);
        caseShivam.setUserLongitude(88.3530);
        caseShivam.setAssignedDoctor(d1);
        caseShivam.setNotes("Acute respiratory wheezing episode; treated with bronchodilator & nebulizer at ER bay.");
        emergencyCaseRepository.save(caseShivam);

        // ==========================================
        // 9. SEED ENTERPRISE HOSPITAL SAAS PATIENTS (EHR)
        // ==========================================
        HospitalPatient p1 = new HospitalPatient();
        p1.setUhid("UHID-2026-1042");
        p1.setFullName("Ramesh Kumar");
        p1.setPhone("+91 98301 23456");
        p1.setAge(54);
        p1.setGender("Male");
        p1.setBloodGroup("O+");
        p1.setAddress("Flat 4B, Camac Street, Kolkata");
        p1.setEmergencyContactName("Sunita Kumar (Wife)");
        p1.setEmergencyContactPhone("+91 98301 23457");
        p1.setPrimaryDiagnosis("Acute Anterior STEMI (Myocardial Infarction)");
        p1.setAssignedBedNumber("ICU-BAY-03");
        p1.setAdmissionStatus("ICU");
        p1.setAdmissionDate(LocalDateTime.now().minusDays(1).minusHours(4));
        p1.setAssignedDoctor(d1);
        p1.setHospital(savedH1);
        HospitalPatient savedP1 = patientRepository.save(p1);

        HospitalPatient p2 = new HospitalPatient();
        p2.setUhid("UHID-2026-2189");
        p2.setFullName("Priya Sharma");
        p2.setPhone("+91 98311 98765");
        p2.setAge(29);
        p2.setGender("Female");
        p2.setBloodGroup("B+");
        p2.setAddress("78 Rashbehari Ave, Kolkata");
        p2.setEmergencyContactName("Rohit Sharma (Brother)");
        p2.setEmergencyContactPhone("+91 98311 98766");
        p2.setPrimaryDiagnosis("Right Distal Femur Fracture & Laceration");
        p2.setAssignedBedNumber("TRAUMA-WARD-07");
        p2.setAdmissionStatus("ADMITTED");
        p2.setAdmissionDate(LocalDateTime.now().minusHours(8));
        p2.setAssignedDoctor(d2);
        p2.setHospital(savedH1);
        HospitalPatient savedP2 = patientRepository.save(p2);

        HospitalPatient p3 = new HospitalPatient();
        p3.setUhid("UHID-2026-3401");
        p3.setFullName("Amit Banerjee");
        p3.setPhone("+91 98302 55443");
        p3.setAge(66);
        p3.setGender("Male");
        p3.setBloodGroup("A+");
        p3.setAddress("14 Southern Ave, Kolkata");
        p3.setEmergencyContactName("Sharmila Banerjee (Daughter)");
        p3.setEmergencyContactPhone("+91 98302 55444");
        p3.setPrimaryDiagnosis("Atrial Fibrillation with Rapid Ventricular Response");
        p3.setAssignedBedNumber("CARD-CCU-02");
        p3.setAdmissionStatus("OBSERVATION");
        p3.setAdmissionDate(LocalDateTime.now().minusHours(3));
        p3.setAssignedDoctor(d1);
        p3.setHospital(savedH1);
        HospitalPatient savedP3 = patientRepository.save(p3);

        HospitalPatient p4 = new HospitalPatient();
        p4.setUhid("UHID-2026-0911");
        p4.setFullName("Sunil Verma");
        p4.setPhone("+91 98200 11223");
        p4.setAge(45);
        p4.setGender("Male");
        p4.setBloodGroup("AB+");
        p4.setAddress("9 Park Circus, Kolkata");
        p4.setEmergencyContactName("Neha Verma (Spouse)");
        p4.setEmergencyContactPhone("+91 98200 11224");
        p4.setPrimaryDiagnosis("Hypertensive Urgency (Controlled)");
        p4.setAssignedBedNumber("WARD-12B");
        p4.setAdmissionStatus("DISCHARGED");
        p4.setAdmissionDate(LocalDateTime.now().minusDays(2));
        p4.setDischargeDate(LocalDateTime.now().minusHours(1));
        p4.setAssignedDoctor(d3);
        p4.setHospital(savedH1);
        HospitalPatient savedP4 = patientRepository.save(p4);

        HospitalPatient pShivam = new HospitalPatient();
        pShivam.setUhid("UHID-2026-8802");
        pShivam.setFullName(patient.getName());
        pShivam.setPhone(patient.getPhone());
        pShivam.setAge(patient.getAge());
        pShivam.setGender(patient.getGender());
        pShivam.setBloodGroup("B+");
        pShivam.setAddress("Flat 3A, Salt Lake Sector 2, Kolkata");
        pShivam.setEmergencyContactName("Meera Kumar (Sister)");
        pShivam.setEmergencyContactPhone("+91 98765 43211");
        pShivam.setPrimaryDiagnosis("Bronchial Asthma Flare & Routine Blood Pressure Checkup");
        pShivam.setAssignedBedNumber("PULMO-04");
        pShivam.setAdmissionStatus("DISCHARGED");
        pShivam.setAdmissionDate(LocalDateTime.now().minusDays(5));
        pShivam.setDischargeDate(LocalDateTime.now().minusDays(4));
        pShivam.setAssignedDoctor(d1);
        pShivam.setHospital(savedH1);
        patientRepository.save(pShivam);

        // ==========================================
        // 10. SEED HOSPITAL BILLING & INVOICES
        // ==========================================
        PatientBill b1 = new PatientBill();
        b1.setInvoiceNumber("INV-2026-8801");
        b1.setHospital(savedH1);
        b1.setPatient(savedP1);
        List<BillItem> items1 = new ArrayList<>();
        items1.add(new BillItem("Cardiac ICU Monitoring & Care (2 Days)", "ICU", 2, 12000.0));
        items1.add(new BillItem("Emergency Coronary Angiogram & Cath Lab Support", "SURGERY", 1, 45000.0));
        items1.add(new BillItem("Senior Interventional Cardiologist Consult", "DOCTOR_FEE", 1, 8000.0));
        items1.add(new BillItem("Heparin & Nitroglycerin Continuous Infusion", "PHARMACY", 1, 4800.0));
        items1.add(new BillItem("High-Sensitivity Troponin-T Lab Assay & ABG", "LAB", 1, 3500.0));
        b1.setItems(items1);
        double sub1 = items1.stream().mapToDouble(BillItem::getTotalPrice).sum();
        b1.setSubtotal(sub1);
        b1.setTaxAmount(Math.round(sub1 * 0.05 * 100.0) / 100.0);
        b1.setDiscountAmount(2500.0);
        b1.setGrandTotal(sub1 + b1.getTaxAmount() - b1.getDiscountAmount());
        b1.setPaymentStatus("PENDING");
        b1.setPaymentMethod("INSURANCE");
        billRepository.save(b1);

        PatientBill b2 = new PatientBill();
        b2.setInvoiceNumber("INV-2026-8802");
        b2.setHospital(savedH1);
        b2.setPatient(savedP2);
        List<BillItem> items2 = new ArrayList<>();
        items2.add(new BillItem("Emergency Trauma Bay Stabilization & Splinting", "EQUIPMENT", 1, 9500.0));
        items2.add(new BillItem("Femur Reduction & Wound Debridement", "SURGERY", 1, 32000.0));
        items2.add(new BillItem("Trauma Surgeon Procedure & Consult", "DOCTOR_FEE", 1, 7500.0));
        items2.add(new BillItem("Digital Radiography (X-Ray Full Leg & Pelvis)", "LAB", 1, 4200.0));
        b2.setItems(items2);
        double sub2 = items2.stream().mapToDouble(BillItem::getTotalPrice).sum();
        b2.setSubtotal(sub2);
        b2.setTaxAmount(Math.round(sub2 * 0.05 * 100.0) / 100.0);
        b2.setDiscountAmount(0.0);
        b2.setGrandTotal(sub2 + b2.getTaxAmount() - b2.getDiscountAmount());
        b2.setPaymentStatus("INSURANCE_CLAIM");
        b2.setPaymentMethod("INSURANCE");
        billRepository.save(b2);

        PatientBill b3 = new PatientBill();
        b3.setInvoiceNumber("INV-2026-8799");
        b3.setHospital(savedH1);
        b3.setPatient(savedP4);
        List<BillItem> items3 = new ArrayList<>();
        items3.add(new BillItem("Emergency Observation Bay (12 Hours)", "BED", 1, 3500.0));
        items3.add(new BillItem("Emergency Physician Comprehensive Exam", "DOCTOR_FEE", 1, 2000.0));
        items3.add(new BillItem("IV Labetalol Infusion & Telemetry", "PHARMACY", 1, 2200.0));
        b3.setItems(items3);
        double sub3 = items3.stream().mapToDouble(BillItem::getTotalPrice).sum();
        b3.setSubtotal(sub3);
        b3.setTaxAmount(Math.round(sub3 * 0.05 * 100.0) / 100.0);
        b3.setDiscountAmount(500.0);
        b3.setGrandTotal(sub3 + b3.getTaxAmount() - b3.getDiscountAmount());
        b3.setPaymentStatus("PAID");
        b3.setPaymentMethod("UPI");
        billRepository.save(b3);

        // ==========================================
        // 11. SEED STAFF ATTENDANCE
        // ==========================================
        List<EmployeeAttendance> attendanceList = List.of(
                createAttendance(savedH1, "Dr. Arvind Mehta", "CARDIOLOGIST", "MORNING", "ON_DUTY", LocalTime.of(8, 0)),
                createAttendance(savedH1, "Dr. Pooja Sen", "TRAUMA_SURGEON", "MORNING", "ON_DUTY", LocalTime.of(8, 15)),
                createAttendance(savedH1, "Dr. Subhash Bose", "ANESTHESIOLOGIST", "EVENING", "ON_DUTY", LocalTime.of(13, 50)),
                createAttendance(savedH1, "Sister Anjali Nair", "HEAD_NURSE", "MORNING", "PRESENT", LocalTime.of(7, 45)),
                createAttendance(savedH1, "Rajesh Mondal", "PARAMEDIC", "MORNING", "ON_DUTY", LocalTime.of(8, 0)),
                createAttendance(savedH1, "Tanmoy Ghosh", "PHARMACIST", "MORNING", "PRESENT", LocalTime.of(8, 30)),
                createAttendance(savedH1, "Sunita Soren", "DUTY_NURSE", "MORNING", "PRESENT", LocalTime.of(7, 55)),
                createAttendance(savedH1, "Bikram Roy", "RADIOLOGY_TECH", "MORNING", "PRESENT", LocalTime.of(8, 10))
        );
        attendanceRepository.saveAll(attendanceList);

        // ==========================================
        // 12. SEED EMERGENCY MEDICINES (PHARMACY FORMULARY)
        // ==========================================
        List<EmergencyMedicine> medicines = List.of(
                new EmergencyMedicine("Adrenaline (Epinephrine) 1mg/ml", "Resuscitation", 120, "Ampoules", 25, savedH1),
                new EmergencyMedicine("Atropine Sulfate 0.6mg/ml", "Cardiovascular", 85, "Ampoules", 20, savedH1),
                new EmergencyMedicine("Nitroglycerin Infusion 5mg/ml", "Cardiovascular", 45, "Vials", 15, savedH1),
                new EmergencyMedicine("Heparin Sodium 5000 IU/ml", "Anticoagulant", 60, "Vials", 20, savedH1),
                new EmergencyMedicine("0.9% Normal Saline 500ml", "IV Fluids", 240, "Bottles", 50, savedH1),
                new EmergencyMedicine("Amiodarone 150mg/3ml", "Antiarrhythmic", 40, "Ampoules", 15, savedH1),
                new EmergencyMedicine("Dopamine HCl 200mg/5ml", "Vasopressor", 35, "Ampoules", 10, savedH1),
                new EmergencyMedicine("Furosemide (Lasix) 20mg/2ml", "Diuretic", 90, "Ampoules", 25, savedH1),
                new EmergencyMedicine("Tramadol HCl 50mg/ml", "Analgesic", 55, "Ampoules", 15, savedH1),
                new EmergencyMedicine("Diazepam 10mg/2ml", "Anticonvulsant", 30, "Ampoules", 10, savedH1)
        );
        medicineRepository.saveAll(medicines);

        // ==========================================
        // 13. SEED PENDING & PERMANENTLY CLOSED HOSPITALS (ADMIN VERIFICATION TEST DATA)
        // ==========================================
        Hospital hPending = new Hospital();
        hPending.setName("Shree Krishna Multi-Specialty & Trauma Hospital");
        hPending.setEmail("shreekrishna@hospital.com");
        hPending.setPassword(passwordEncoder.encode("admin123"));
        hPending.setContactNumber("+91 33 2490 5500");
        hPending.setAddress("78 Diamond Harbour Road, Behala, Kolkata");
        hPending.setLatitude(22.4988);
        hPending.setLongitude(88.3184);
        hPending.setLicenseNumber("WB-MOH-2026-PENDING-9941");
        hPending.setDirectorName("Dr. Rohan Verma, MS");
        hPending.setVerified(false); // MUST BE VERIFIED BY SUPER ADMIN
        hPending.setActive(true);
        hPending.setOpen(false);
        hPending.setHasEmergency(true);
        hPending.setClosedPermanently(false);
        hPending.setVerificationNotes("Registration submitted. Awaiting medical board credentialing & onsite audit.");
        hPending.setTotalBeds(45);
        hPending.setAvailableBeds(20);
        hPending.setTotalICUBeds(8);
        hPending.setAvailableICUBeds(4);
        hPending.setVentilators(4);
        hPending.setOxygenCylinders(18);
        hPending.setAmbulances(2);
        hPending.setCardiacMonitors(6);
        hospitalRepository.save(hPending);

        Hospital hClosed = new Hospital();
        hClosed.setName("Heritage Charitable Clinic & Infirmary");
        hClosed.setEmail("heritage@hospital.com");
        hClosed.setPassword(passwordEncoder.encode("admin123"));
        hClosed.setContactNumber("+91 33 2210 9900");
        hClosed.setAddress("14 College Street Old Lane, Kolkata");
        hClosed.setLatitude(22.5740);
        hClosed.setLongitude(88.3630);
        hClosed.setLicenseNumber("WB-MOH-OLD-1988-DEC");
        hClosed.setDirectorName("Dr. B. K. Banerjee (Retired)");
        hClosed.setVerified(false);
        hClosed.setActive(false);
        hClosed.setOpen(false);
        hClosed.setHasEmergency(false);
        hClosed.setClosedPermanently(true); // CLOSED PERMANENTLY
        hClosed.setVerificationNotes("Decommissioned permanently under State Health structural order #8841.");
        hospitalRepository.save(hClosed);

        // ==========================================
        // 14. SEED DYNAMIC NURSING WORKFORCE
        // ==========================================
        List<Nurse> nurses = List.of(
                new Nurse("Sister Anjali Nair", "Emergency ICU", 11, "morning", true, savedH1),
                new Nurse("Sunita Soren", "Cardiac Care Unit", 6, "morning", true, savedH1),
                new Nurse("Deepa Mukherjee", "Trauma Bay", 8, "evening", true, savedH1),
                new Nurse("Rina Mondal", "Operating Theater", 5, "night", true, savedH1),
                new Nurse("Priyanka Das", "Emergency Triage", 7, "morning", true, savedH2),
                new Nurse("Kavita Biswas", "High Dependency Unit", 9, "evening", true, savedH2),
                new Nurse("Sharmila Roy", "Surgical Intensive Care", 10, "morning", true, savedH3),
                new Nurse("Archana Paul", "Neuro Intensive Care", 8, "evening", true, savedH3),
                new Nurse("Swati Ghosh", "Level-1 Trauma & Burn", 12, "morning", true, savedH4),
                new Nurse("Bratati Sen", "Emergency Pediatric Bay", 6, "morning", true, savedH4)
        );
        nurseRepository.saveAll(nurses);

        // ==========================================
        // 15. SEED TWO-WAY FEEDBACK & INCIDENT REPORTS
        // ==========================================
        FeedbackReport r1 = new FeedbackReport();
        r1.setReportType("USER_TO_HOSPITAL");
        r1.setReporterId(patient.getId());
        r1.setReporterName(patient.getName());
        r1.setReporterEmail(patient.getEmail());
        r1.setReporterRole("CITIZEN_PATIENT");
        r1.setTargetId(savedH1.getId());
        r1.setTargetName(savedH1.getName());
        r1.setRating(5);
        r1.setCategory("Clinical Care Quality");
        r1.setSeverity("LOW");
        r1.setDescription("Exceptional ER coordination. Cardiologist Dr. Mehta received me at the ambulance bay within 4 minutes.");
        r1.setStatus("RESOLVED");
        r1.setAdminAction("Commendation logged in hospital accreditation dossier");
        r1.setAdminNotes("Verified with ER telemetry timestamps.");

        FeedbackReport r2 = new FeedbackReport();
        r2.setReportType("USER_TO_HOSPITAL");
        r2.setReporterName("Ananya Roy");
        r2.setReporterEmail("ananya.roy@example.com");
        r2.setReporterRole("CITIZEN_PATIENT");
        r2.setTargetId(savedH6.getId());
        r2.setTargetName(savedH6.getName());
        r2.setRating(1);
        r2.setCategory("Bed Availability Dispute");
        r2.setSeverity("HIGH");
        r2.setDescription("Online telemetry showed 2 emergency beds available, but security at reception turned away my elderly uncle saying beds were booked.");
        r2.setStatus("PENDING_REVIEW");
        r2.setAdminNotes("Notice sent to GreenLife administration for bed audit.");

        FeedbackReport r3 = new FeedbackReport();
        r3.setReportType("HOSPITAL_TO_USER");
        r3.setReporterId(savedH1.getId());
        r3.setReporterName("Metro Heart ER Administration");
        r3.setReporterEmail("metro@hospital.com");
        r3.setReporterRole("HOSPITAL_STAFF");
        r3.setTargetId(blockedUser.getId());
        r3.setTargetName(blockedUser.getName());
        r3.setCategory("Fraudulent Emergency Dispatch");
        r3.setSeverity("CRITICAL");
        r3.setDescription("Dispatched Level-1 Cardiac life-support ambulance to address; team found empty park and caller disconnected with false laughter.");
        r3.setStatus("ACTION_TAKEN");
        r3.setAdminAction("Citizen Account Blocked & Police Grievance Filed");
        r3.setAdminNotes("Enforced user block on 2026-11-19.");

        FeedbackReport r4 = new FeedbackReport();
        r4.setReportType("HOSPITAL_TO_USER");
        r4.setReporterId(savedH3.getId());
        r4.setReporterName("Apex Neuro ER Shift Manager");
        r4.setReporterEmail("apex@hospital.com");
        r4.setReporterRole("HOSPITAL_STAFF");
        r4.setTargetId(patient.getId());
        r4.setTargetName(patient.getName());
        r4.setCategory("Billing Dispute Mediation");
        r4.setSeverity("LOW");
        r4.setDescription("Patient requested invoice line-item re-audit for emergency CT scan contrast material.");
        r4.setStatus("RESOLVED");
        r4.setAdminAction("Pharmacy adjusted invoice difference");
        r4.setAdminNotes("Resolved mutually with hospital accounts officer.");

        feedbackReportRepository.saveAll(List.of(r1, r2, r3, r4));

        System.out.println(">>> [LIFELINE SAAS] Initialized 8 Hospitals (Verified, Pending, Closed), Super Admin, Nurses, Staff Attendance, Formulary Stocks, and Two-Way Grievance Reports.");
    }

    private EmployeeAttendance createAttendance(Hospital hospital, String name, String role, String shift, String status, LocalTime checkIn) {
        EmployeeAttendance att = new EmployeeAttendance();
        att.setHospital(hospital);
        att.setEmployeeName(name);
        att.setEmployeeRole(role);
        att.setShift(shift);
        att.setStatus(status);
        att.setDate(LocalDate.now());
        att.setCheckInTime(checkIn);
        return att;
    }
}

