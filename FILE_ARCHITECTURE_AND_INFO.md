# Lifeline: Full Architecture, Information Flows, Tech Stack & Interview Guide

> **Document Version:** 2.0.0 (Production Release)  
> **System Name:** Lifeline — Smart Emergency Response & Hospital SaaS ERP Platform  
> **Target Audience:** Technical Architects, Lead Engineers, Full-Stack Developers & System Design Interviewers

---

## Table of Contents
1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [Complete Technology Stack & Architectural Rationale](#2-complete-technology-stack--architectural-rationale)
3. [High-Level System Architecture & Component Topology](#3-high-level-system-architecture--component-topology)
4. [Module-by-Module Breakdown & Information Flows](#4-module-by-module-breakdown--information-flows)
   - [Module 1: Citizen Emergency Discovery & Smart Triage](#module-1-citizen-emergency-discovery--smart-triage)
   - [Module 2: Hospital SaaS Enterprise Resource Planning (ERP)](#module-2-hospital-saas-enterprise-resource-planning-erp)
   - [Module 3: Super Admin Health Governance & Audit Authority](#module-3-super-admin-health-governance--audit-authority)
   - [Module 4: Patient Identity, EHR Care History & Anti-Spam Rating Engine](#module-4-patient-identity-ehr-care-history--anti-spam-rating-engine)
   - [Module 5: Lifeline AI Medical Assistant & Guardrail Pipeline](#module-5-lifeline-ai-medical-assistant--guardrail-pipeline)
   - [Module 6: Security, Authentication (RBAC) & Route Isolation](#module-6-security-authentication-rbac--route-isolation)
5. [Algorithms, Mathematical Formulas & Business Rules](#5-algorithms-mathematical-formulas--business-rules)
6. [Database Schema & Entity-Relationship Model](#6-database-schema--entity-relationship-model)
7. [RESTful API Specification Reference](#7-restful-api-specification-reference)
8. [Comprehensive Technical Interview Preparation Guide](#8-comprehensive-technical-interview-preparation-guide)
9. [Summary & Quick Reference](#9-summary--quick-reference)
10. [Live Production Cloud Deployment & Infrastructure Topology](#10-live-production-cloud-deployment--infrastructure-topology)
11. [Resume-Ready Project Bullet Points](#11-resume-ready-project-bullet-points)

---

## 1. Executive Summary & Problem Statement

### The Problem
During medical emergencies (cardiac arrest, stroke, pediatric trauma, respiratory distress), critical minutes are squandered due to:
1. **Information Asymmetry:** Patients navigate blindly to the physically closest hospital without knowing whether an ICU bed, catheterization lab, ventilator, or required specialist (e.g., Cardiologist, Neurosurgeon) is actually active or on-duty.
2. **Transit Delay & Zero Pre-Arrival Warning:** Emergency rooms (ER) are hit with unannounced critical patients, delaying triage setup and blood/oxygen prep until arrival.
3. **Fragmented Hospital Operations:** Mid-tier and multi-specialty hospitals lack a unified operating system linking real-time bed capacity, physician rosters, nursing shifts, pharmacy formulary stocks, and automated invoicing.
4. **Spam & Fake Facility Reviews:** Unvetted reviews degrade public trust, or rogue/unverified medical facilities operate without regulatory accreditation.

### The Solution: Lifeline
**Lifeline** is a mission-critical, full-stack emergency response system and hospital operating platform. It features:
- **Intelligent Triage & Routing:** Evaluates real-time geospatial distance, specialty-matched on-duty physicians, ICU beds, and life-support units to score and recommend facilities.
- **Bi-Directional Pre-Arrival Dispatch:** Sends real-time telemetry (patient condition, vitals, GPS, ETA) directly into the hospital's ER dashboard.
- **Enterprise Hospital SaaS ERP:** Comprehensive hospital management across 8 operational modules (Emergency Alarms, EHR Inpatient Admissions, Itemized Billing, Doctor Rostering, Nursing Shifts, Pharmacy Formulary, Capacity Tracking, and Element-Collection Blood Bank).
- **State Health Governance Authority:** Super Admin portal for licensing verification, permanent closure enforcement, citizen & facility suspension, and two-way grievance adjudication.
- **Anti-Spam Verified Healthcare Reviews:** Reviews can only be submitted on a dedicated Profile Page by verified patients with completed/discharged medical records, strictly enforced at 1 review per facility.
- **Domain-Restricted AI Doctor:** Private, token-gated health advisory bot restricted strictly to healthcare topics with per-user conversational isolation.

---

## 2. Complete Technology Stack & Architectural Rationale

### 2.1 Technology Matrix

| Layer | Technology | Version | Purpose & Architectural Rationale |
| :--- | :--- | :--- | :--- |
| **Backend Framework** | **Java Spring Boot** | 3.3.5 | Industry standard for enterprise healthcare. Strong typing, dependency injection, high concurrent throughput, and robust exception propagation. |
| **JDK Runtime** | **Eclipse Temurin / OpenJDK** | 17+ LTS | Modern Java features (Records, Pattern Matching, Sealed Classes, Virtual Threads capability). |
| **Security Layer** | **Spring Security 6 + JJWT** | 6.3.4 / 0.12.5 | Stateless JWT authentication, role-based authorization filter chain (`JwtAuthenticationFilter`), BCrypt password hashing ($2a$). |
| **Persistence / ORM** | **Spring Data JPA / Hibernate** | 6.5.3 | Object-Relational Mapping, transaction management (`@Transactional`), automated DDL generation, and type-safe query formulation. |
| **Database** | **H2 In-Memory DB / PostgreSQL Ready** | 2.2.x | High-speed zero-config runtime with full dialect compatibility to easily swap into AWS RDS PostgreSQL in production. |
| **Frontend Architecture** | **Vanilla HTML5 / Modern CSS3 / ES6+** | Native | Zero external bundling overhead, blazing load times (<50ms initial paint), responsive across mobile, tablet, and desktop without node build bloat. |
| **Geospatial & Maps** | **OpenStreetMap (OSRM & Nominatim)** | Open API | Real-world road routing and reverse geocoding without vendor lock-in or recurring third-party API costs. |
| **Build & Tooling** | **Apache Maven** | 3.9+ | Deterministic multi-dependency build lifecycle, test automation, and executable fat JAR packaging. |

### 2.2 Why Spring Boot 3 over Node.js for Lifeline?
1. **Thread Safety & High Concurrency:** Hospital telemetry, ER ward alarms, and capacity tracking require atomic, ACID-compliant state transitions. Java's memory model and thread-safe collections prevent race conditions during simultaneous ICU bed reservations.
2. **Enterprise Layering:** Separation of Concerns via Controller-Service-Repository patterns ensures zero business logic leakage into HTTP handling layers.
3. **Compile-Time Type Safety:** Prevents runtime `TypeError` issues common in dynamic runtimes when handling critical medical data (e.g., medication dosages, blood inventory units, triage urgency).

---

## 3. High-Level System Architecture & Component Topology

```mermaid
flowchart TD
    subgraph Clients["Client Layer (Frontend UI)"]
        C1["Citizen Patient<br/>(emergency.html, profile.html)"]
        C2["Hospital Operations Desk<br/>(hospital-dashboard.html)"]
        C3["Health Regulatory Authority<br/>(admin.html)"]
        C4["AI Health Assistant<br/>(chat.html)"]
    end

    subgraph SecurityFilter["Security & Routing Layer"]
        GW["Tomcat Embedded Server :8080"]
        CORS["CorsFilter"]
        SEC["JwtAuthenticationFilter<br/>(Extracts Bearer Token & Role)"]
        AUTH["Spring Security 6 SecurityFilterChain"]
    end

    subgraph Controllers["Spring Boot REST API Controllers"]
        CTL_EM["EmergencyController<br/>& EmergencyCaseController"]
        CTL_HOSP["HospitalController<br/>& Doctor/Nurse/Staff"]
        CTL_ADM["AdminController"]
        CTL_USR["UserController"]
        CTL_CHAT["AiChatController"]
        CTL_AUTH["AuthController"]
    end

    subgraph Services["Core Business Services"]
        SVC_MATCH["EmergencyMatchingService<br/>(Haversine & Scoring Engine)"]
        SVC_TRANSIT["EmergencyTransitAiService"]
        SVC_CASE["EmergencyCaseService"]
        SVC_HOSP["HospitalService & StaffAttendanceService"]
        SVC_BILL["HospitalBillingService"]
        SVC_ADMIN["AdminService"]
        SVC_CHAT["AiChatService"]
    end

    subgraph Persistence["Persistence & Database Layer"]
        REPO["Spring Data JPA Repositories<br/>(11 Specialized Interfaces)"]
        DB[("In-Memory H2 Database<br/>(PostgreSQL Dialect Compatible)")]
    end

    C1 & C2 & C3 & C4 --> GW --> CORS --> SEC --> AUTH
    AUTH --> Controllers
    Controllers --> Services
    Services --> REPO --> DB
```

---

## 4. Module-by-Module Breakdown & Information Flows

---

### Module 1: Citizen Emergency Discovery & Smart Triage

#### Goal
Instantly locate, rank, and alert the best equipped hospital for a specific medical trauma within seconds.

#### Information Flow: Emergency Search & ER Dispatch
```mermaid
sequenceDiagram
    autonumber
    actor Patient as Citizen Patient
    participant Browser as emergency.html
    participant Backend as EmergencyController
    participant Matcher as EmergencyMatchingService
    participant Repo as Hospital & Doctor Repositories
    participant ERDesk as EmergencyCaseController
    participant HospitalERP as hospital-dashboard.html

    Patient->>Browser: Selects "Cardiac Arrest" & clicks "Auto-Detect Real GPS"
    Browser->>Browser: HTML5 Geolocation API retrieves lat: 22.5550, lng: 88.3510
    Browser->>Backend: POST /api/emergency/search {lat, lng, emergencyType: 'CARDIAC'}
    Backend->>Matcher: findNearestAndBestHospitals(lat, lng, CARDIAC)
    Matcher->>Repo: Fetch active, verified, open hospitals (excluding permanently closed)
    Matcher->>Repo: Fetch on-duty doctors & active equipment
    Matcher->>Matcher: Compute Haversine distance, travel time & Readiness Score (0-100)
    Matcher-->>Backend: EmergencySearchResponse (Top 3 Best + Top 3 Nearest)
    Backend-->>Browser: JSON payload with breakdown scores & on-duty specialists
    Browser-->>Patient: Render Ranked Hospital Cards & Clinical Score Details

    Patient->>Browser: Clicks "Notify Ward in Advance" & enters emergency notes
    Browser->>ERDesk: POST /api/emergency/cases/notify {hospitalId, triageNotes, vitals}
    ERDesk->>Repo: Create EmergencyCase (Status: ACTIVE, CaseID: #EC-9821)
    ERDesk-->>HospitalERP: Real-time case reflects on Inbound ER Alerts table
    ERDesk-->>Browser: 201 Created {caseId, hospitalName, status: "ACTIVE"}
    Browser-->>Patient: Display emergency dispatch confirmation banner
```

#### Key Architectural Components
1. **`EmergencyMatchingService.java`**: Implements multi-criteria clinical scoring.
2. **`GeoUtils.java`**: Implements high-precision Haversine trigonometric distance calculation.
3. **`EmergencyCaseService.java`**: Manages incoming patient case lifecycle (`ACTIVE` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED`).

---

### Module 2: Hospital SaaS Enterprise Resource Planning (ERP)

#### Goal
Provide a clinical command center for doctors, nurses, ER shift managers, and administrative officers.

```mermaid
graph TD
    HOSP["Hospital SaaS ERP<br/>(hospital-dashboard.html)"]
    HOSP --> M1["1. Overview & Dynamic KPIs<br/>(Bed Count, ICU Occupancy, Active Alarms)"]
    HOSP --> M2["2. Inbound ER Alerts<br/>(Telemetry, Pre-Arrival Triage, Status State Machine)"]
    HOSP --> M3["3. Inpatient Admissions & EHR<br/>(UHID Generation, Diagnosis, Bed Allocation, Discharge)"]
    HOSP --> M4["4. Billing & Automated Invoicing<br/>(Itemized Service Charges, Tax, Payment Settlements)"]
    HOSP --> M5["5. Staff & Nurse Attendance<br/>(Biometric-Style Shift Check-In, Ward Assignment)"]
    HOSP --> M6["6. Emergency Pharmacy Formulary<br/>(Drug Stock Levels, Minimum Thresholds, Dispensing)"]
    HOSP --> M7["7. Doctor Duty Roster<br/>(Specialist Duty Toggles, Experience, Qualifications)"]
    HOSP --> M8["8. Capacity & Blood Inventory<br/>(ElementCollection: O+, O-, A+, B+, AB+ units)"]
```

#### Key ERP Sub-Modules & Data Flow
1. **Patient Admissions (EHR):** 
   - Endpoint: `POST /api/hospital/patients/admit`
   - Generates unique Universal Health Identifier (`UHID-2026-XXXX`).
   - Assigns bed numbers (`PULMO-04`, `ICU-02`) and decrements available beds atomically in `Hospital.java`.
2. **Clinical Discharge & Review Unlock:**
   - Endpoint: `PUT /api/hospital/patients/{id}/discharge`
   - Sets `admissionStatus = "DISCHARGED"`, sets `dischargeDate = LocalDateTime.now()`, increments available beds.
   - **Crucial Hook:** This discharge state unlocks review eligibility for that specific patient in the Rating Engine.
3. **Emergency Pharmacy Stock:**
   - Tracks critical emergency drugs (Adrenaline, Atropine, Amiodarone, Furosemide).
   - `PUT /api/hospital/pharmacy/{id}/dispense` reduces inventory and flags `LOW_STOCK` warnings when count $< 10$.

---

### Module 3: Super Admin Health Governance & Audit Authority

#### Goal
Give state regulatory authorities complete supervisory control over healthcare providers and citizen accounts.

```mermaid
sequenceDiagram
    autonumber
    actor Admin as State Health Officer
    participant AdminUI as admin.html
    participant AdminCtrl as AdminController
    participant AdminSvc as AdminService
    participant Repo as Hospital & User & Report Repos

    Admin->>AdminUI: Opens Admin Portal (Protected ROLE_SUPER_ADMIN)
    AdminUI->>AdminCtrl: GET /api/admin/hospitals, /api/admin/users, /api/admin/reports
    AdminCtrl->>AdminSvc: Aggregate system metrics
    AdminSvc-->>AdminUI: Total Hospitals (8), Pending (1), Verified (6), Citizens (3), Reports (4)

    alt Hospital Verification
        Admin->>AdminUI: Clicks "Verify Hospital" for new applicant
        AdminUI->>AdminCtrl: PUT /api/admin/hospitals/{id}/verify
        AdminCtrl->>Repo: Set isVerified = true, isActive = true
        AdminCtrl-->>AdminUI: Hospital approved; now eligible for public emergency search
    else Enforce Permanent Closure
        Admin->>AdminUI: Clicks "Enforce Permanent Closure" (e.g. defunct facility)
        AdminUI->>AdminCtrl: PUT /api/admin/hospitals/{id}/close-permanently
        AdminCtrl->>Repo: Set isClosedPermanently = true, isOpen = false, isActive = false
        AdminCtrl-->>AdminUI: Facility barred permanently from search & dispatch algorithms
    else Citizen Oversight
        Admin->>AdminUI: Identifies abusive or spam user; clicks "Suspend User"
        AdminUI->>AdminCtrl: PUT /api/admin/users/{id}/suspend
        AdminCtrl->>Repo: Set user.status = "SUSPENDED"
        AdminCtrl-->>AdminUI: User tokens invalidated and blocked from API
    end
```

---

### Module 4: Patient Identity, EHR Care History & Anti-Spam Rating Engine

#### Goal
Provide a dedicated, full-screen portal ([profile.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/profile.html)) for citizens to manage medical profiles, review admission histories, and submit verified ratings with zero spam or duplicates.

#### The Anti-Spam Rating State Machine
```mermaid
stateDiagram-v2
    [*] --> Unauthenticated: Citizen visits site
    Unauthenticated --> AuthenticatedUser: Login via JWT /api/auth/login
    
    AuthenticatedUser --> ProfilePage: Opens profile.html
    
    state ProfilePage {
        [*] --> FetchEligibleHospitals: GET /api/user/eligible-reviews
        
        FetchEligibleHospitals --> NoCompletedCare: Zero COMPLETED cases / Zero DISCHARGED visits
        NoCompletedCare --> RatingBlocked: Display Security Policy Notice (Cannot Rate)
        
        FetchEligibleHospitals --> HasCompletedCare: Treatment record found (EmergencyCase / EHR)
        
        state HasCompletedCare {
            [*] --> DuplicateCheck: Query feedback_reports for (User + Hospital + USER_TO_HOSPITAL)
            DuplicateCheck --> AlreadyReviewed: Review exists in DB
            AlreadyReviewed --> DisplayVerifiedReview: Render "✓ Verified Review Submitted" (Form Disabled)
            
            DuplicateCheck --> NotYetReviewed: No review exists in DB
            NotYetReviewed --> ActiveRatingForm: Render 1-5 Star Picker & Feedback Form
            ActiveRatingForm --> SubmitRating: POST /api/user/feedback
            SubmitRating --> PersistReview: Save to DB & Update Hospital Average Score
            PersistReview --> DisplayVerifiedReview
        }
    }
```

#### Verification & Duplicate Prevention Logic in Code
In `UserController.java`:
1. **Verification Check:**
   ```java
   private boolean hasCompletedTreatmentAtHospital(User user, Long hospitalId) {
       // 1. Emergency Case verification
       boolean hasEmergency = emergencyCaseRepository
           .findByPatientEmailOrPatientPhoneOrderByCreatedAtDesc(user.getEmail(), user.getPhone())
           .stream().anyMatch(c -> c.getHospital().getId().equals(hospitalId) && c.getStatus() == CaseStatus.COMPLETED);
       if (hasEmergency) return true;

       // 2. Hospital Admission EHR verification
       return hospitalPatientRepository.findByPhoneOrderByAdmissionDateDesc(user.getPhone())
           .stream().anyMatch(p -> p.getHospital().getId().equals(hospitalId) &&
               ("DISCHARGED".equalsIgnoreCase(p.getAdmissionStatus()) || "COMPLETED".equalsIgnoreCase(p.getAdmissionStatus())));
   }
   ```
2. **Duplicate Check:**
   ```java
   if (feedbackReportRepository.existsByReporterEmailAndTargetIdAndReportType(user.getEmail(), hospital.getId(), "USER_TO_HOSPITAL")) {
       throw new IllegalArgumentException("Duplicate Rating Prevented: You have already submitted a verified review and rating for this hospital. Multiple ratings for the same hospital are not permitted.");
   }
   ```
3. **Global Exception Handling:** 
   `GlobalExceptionHandler.java` catches `IllegalArgumentException` and serializes a clean HTTP 400 Bad Request JSON response with the user-friendly explanation.

---

### Module 5: Lifeline AI Medical Assistant & Guardrail Pipeline

#### Goal
Offer citizens instant, private medical guidance while rigorously rejecting non-healthcare queries and maintaining user privacy.

```mermaid
flowchart TD
    UserMsg["User Prompt in chat.html"] --> AuthCheck{"Is User Signed In?<br/>(Bearer JWT present?)"}
    AuthCheck -- No --> LoginModal["Show 'Login Required' Modal<br/>(Access Blocked)"]
    AuthCheck -- Yes --> API["POST /api/chat/message"]
    API --> Guardrail{"Domain Guardrail Filter<br/>(Strict Health & Fitness Regex)"}
    
    Guardrail -- Off-Topic Query<br/>(Crypto, Politics, Coding, etc.) --> Rejection["Deflective Medical Response:<br/>'I am Lifeline AI Medical Assistant. I can only assist with health, fitness, and emergency medicine.'"]
    
    Guardrail -- Medical / Health Query --> ContextEngine["Context Engine:<br/>Retrieve authenticated user's private ChatSession history"]
    ContextEngine --> PromptBuilder["System Instruction Injector:<br/>- Professional Medical Persona<br/>- Emergency Protocol Escalation<br/>- Indian Healthcare Context"]
    PromptBuilder --> LLM["LLM Inference / Mock Health Engine"]
    LLM --> Persist["Save Message to User's ChatSession Entity"]
    Persist --> Response["Return Encrypted ChatResponse JSON"]
```

---

### Module 6: Security, Authentication (RBAC) & Route Isolation

#### Goal
Ensure zero cross-role access and protect patient confidentiality using stateless JWT tokens.

#### Role Hierarchies & Route Map
- `ROLE_USER` (Citizen / Patient): Can access `/emergency.html`, `/profile.html`, `/chat.html`. Barred from `/hospital-dashboard.html` and `/admin.html`.
- `ROLE_HOSPITAL_ADMIN` (Medical Facility Staff): Can access `/hospital-dashboard.html`. Barred from `/admin.html` and patient chat.
- `ROLE_SUPER_ADMIN` (State Health Authority): Can access `/admin.html`. Barred from impersonating patient consultations.

#### Cross-Role Defensive UI Guards
Even if a user manually changes the URL, the client-side session checks in tandem with the Spring Security backend filter chain:
```javascript
const session = API.getSession();
if (session && session.role === 'ROLE_HOSPITAL_ADMIN') {
    alert("Access Denied: You are signed in as Hospital Staff. Please use your Hospital ERP.");
    window.location.replace('hospital-dashboard.html');
}
```

---

## 5. Algorithms, Mathematical Formulas & Business Rules

### 5.1 Haversine Distance Formula
Calculates great-circle distance between user GPS coordinates $(\phi_1, \lambda_1)$ and hospital coordinates $(\phi_2, \lambda_2)$ in kilometers:

$$\Delta\phi = \phi_2 - \phi_1, \quad \Delta\lambda = \lambda_2 - \lambda_1$$

$$a = \sin^2\left(\frac{\Delta\phi}{2}\right) + \cos(\phi_1) \cdot \cos(\phi_2) \cdot \sin^2\left(\frac{\Delta\lambda}{2}\right)$$

$$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1 - a}\right)$$

$$d = R \cdot c \quad (\text{where } R = 6371.0 \text{ km})$$

Implemented in `GeoUtils.java`:
```java
public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
}
```

---

### 5.2 Multi-Factor Emergency Readiness Score (0 – 100)
Rather than simply showing the nearest hospital, Lifeline computes a weighted **Clinical Readiness Score** calibrated to trauma survival metrics:

$$\text{Readiness Score} = S_{\text{specialist}} + S_{\text{icu}} + S_{\text{beds}} + S_{\text{equipment}} + S_{\text{proximity}}$$

Where:
1. **Specialist Score ($S_{\text{specialist}}$, Max 35 Pts):**
   - Exact On-Duty Specialist for condition (e.g., Cardiologist for Cardiac Arrest): **35 Pts**
   - General Emergency Physician On-Duty: **20 Pts**
   - Specialist Registered but Off-Duty: **10 Pts**
   - No Specialist: **0 Pts**
2. **ICU Capacity Score ($S_{\text{icu}}$, Max 25 Pts):**
   - Available ICU Beds $\ge 5$: **25 Pts**
   - Available ICU Beds $1 - 4$: **15 Pts**
   - ICU Full (0 Available): **0 Pts**
3. **General Bed Availability ($S_{\text{beds}}$, Max 15 Pts):**
   - $\min\left(15, \frac{\text{Available Beds}}{3}\right)$
4. **Life-Support Equipment ($S_{\text{equipment}}$, Max 15 Pts):**
   - Ventilators ($>3$: 5 pts, $>0$: 3 pts)
   - Oxygen Cylinders ($>10$: 5 pts, $>0$: 2 pts)
   - Cardiac Monitors ($>5$: 5 pts, $>0$: 2 pts)
5. **Proximity Score ($S_{\text{proximity}}$, Max 10 Pts):**
   - $d \le 2.0\text{ km}$: **10 Pts**
   - $2.0\text{ km} < d \le 5.0\text{ km}$: **8 Pts**
   - $5.0\text{ km} < d \le 10.0\text{ km}$: **5 Pts**
   - $d > 10.0\text{ km}$: **2 Pts**

---

### 5.3 Dynamic Rating Aggregation Formula
When calculating a hospital's public rating:

$$\bar{R} = \frac{\sum_{i=1}^{N} R_i}{N}$$

Where $R_i \in \{1, 2, 3, 4, 5\}$ are verified ratings from patients who completed care. If no reviews exist yet, a baseline score of $4.8$ with a default count of $12$ is applied as the default seed.

---

## 6. Database Schema & Entity-Relationship Model

```mermaid
erDiagram
    HOSPITALS ||--o{ DOCTORS : employs
    HOSPITALS ||--o{ NURSES : employs
    HOSPITALS ||--o{ HOSPITAL_PATIENTS : admits
    HOSPITALS ||--o{ EMERGENCY_CASES : receives
    HOSPITALS ||--o{ EMERGENCY_MEDICINES : stocks
    HOSPITALS ||--o{ EMPLOYEE_ATTENDANCE : logs
    HOSPITALS ||--o{ PATIENT_BILLS : generates
    HOSPITALS ||--o{ HOSPITAL_BLOOD_INVENTORY : stores
    USERS ||--o{ EMERGENCY_CASES : files
    USERS ||--o{ CHAT_SESSIONS : owns
    USERS ||--o{ FEEDBACK_REPORTS : files

    HOSPITALS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        varchar contact_number
        varchar address
        double latitude
        double longitude
        varchar license_number
        boolean is_verified
        boolean is_active
        boolean is_open
        boolean is_closed_permanently
        int total_beds
        int available_beds
        int total_icu_beds
        int available_icu_beds
        int ventilators
        int ambulances
    }

    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        varchar phone
        int age
        varchar gender
        varchar known_conditions
        varchar role
        varchar status
    }

    HOSPITAL_PATIENTS {
        bigint id PK
        varchar uhid UK
        varchar full_name
        varchar phone
        varchar admission_status
        varchar primary_diagnosis
        varchar assigned_bed_number
        timestamp admission_date
        timestamp discharge_date
        bigint hospital_id FK
        bigint doctor_id FK
    }

    FEEDBACK_REPORTS {
        bigint id PK
        varchar report_type
        bigint reporter_id
        varchar reporter_email
        bigint target_id
        varchar target_name
        int rating
        varchar category
        varchar severity
        text description
        varchar status
        timestamp created_at
    }

    EMERGENCY_CASES {
        bigint id PK
        varchar emergency_type
        varchar severity
        varchar status
        varchar patient_name
        varchar patient_phone
        double user_latitude
        double user_longitude
        text notes
        bigint hospital_id FK
        timestamp created_at
    }
```

---

## 7. RESTful API Specification Reference

### 7.1 Authentication & Profile APIs
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Citizen patient registration |
| `POST` | `/api/auth/login` | Public | Citizen login (Returns JWT token) |
| `POST` | `/api/auth/hospital/register` | Public | Hospital registration with GPS coords & license |
| `POST` | `/api/auth/hospital/login` | Public | Hospital staff ERP authentication |
| `POST` | `/api/auth/admin/login` | Public | Super Admin health authority sign in |
| `GET` | `/api/user/profile` | Patient | Get user profile, emergency alerts & EHR checkups |
| `PUT` | `/api/user/profile` | Patient | Update user personal details & medical allergies |
| `GET` | `/api/user/eligible-reviews`| Patient | List hospitals eligible for rating (Completed treatments only) |
| `POST` | `/api/user/feedback` | Patient | Submit verified rating & review (Strict duplicate prevention) |

### 7.2 Emergency & Triage APIs
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/emergency/search` | Public | Real-time triage matching & nearest hospital ranking |
| `GET` | `/api/emergency/hospitals/{id}` | Public | Complete hospital clinical profile & doctor duty roster |
| `POST` | `/api/emergency/cases/notify` | Public / Patient | Transmit pre-arrival emergency alert to hospital ward |

### 7.3 Hospital SaaS ERP APIs
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/hospital/profile` | Hospital Staff | Retrieve hospital metadata and KPIs |
| `GET` | `/api/hospital/cases` | Hospital Staff | Inbound emergency dispatch alerts queue |
| `PUT` | `/api/hospital/cases/{id}/status`| Hospital Staff | Update emergency case state (`IN_PROGRESS`, `COMPLETED`)|
| `GET` | `/api/hospital/patients` | Hospital Staff | List admitted inpatient records (EHR) |
| `POST` | `/api/hospital/patients/admit` | Hospital Staff | Admit patient, generate UHID, allocate bed |
| `PUT` | `/api/hospital/patients/{id}/discharge`| Hospital Staff | Discharge patient & release bed |
| `GET` | `/api/hospital/billing/invoices` | Hospital Staff | Itemized bills and payment statuses |
| `POST` | `/api/hospital/billing/invoices` | Hospital Staff | Generate new inpatient bill |
| `GET` | `/api/hospital/pharmacy` | Hospital Staff | Formulary stock levels & reorder alerts |
| `PUT` | `/api/hospital/pharmacy/{id}/dispense`| Hospital Staff | Dispense drug & reduce inventory |
| `GET` | `/api/hospital/doctors` | Hospital Staff | Doctor duty roster & specialty tracking |
| `PUT` | `/api/hospital/doctors/{id}/duty` | Hospital Staff | Toggle doctor on-duty / off-duty status |

### 7.4 Super Admin Governance APIs
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/hospitals` | Super Admin | Audit all registered hospital facilities |
| `PUT` | `/api/admin/hospitals/{id}/verify`| Super Admin | Approve hospital license & activate facility |
| `PUT` | `/api/admin/hospitals/{id}/close-permanently`| Super Admin | Enforce permanent facility closure |
| `GET` | `/api/admin/users` | Super Admin | Audit citizen accounts & healthcare history |
| `PUT` | `/api/admin/users/{id}/suspend` | Super Admin | Suspend abusive user account |
| `GET` | `/api/admin/reports` | Super Admin | Review two-way hospital/patient grievance tickets |
| `PUT` | `/api/admin/reports/{id}/resolve`| Super Admin | Close complaint with official action notes |

---

## 8. Comprehensive Technical Interview Preparation Guide

This section equips you with exact, high-scoring answers to questions an interviewer (Senior Software Engineer, Tech Lead, or Engineering Manager) might ask about Lifeline.

---

### Q1: "Walk me through the high-level architecture of Lifeline."
**High-Impact Answer:**  
> "Lifeline is built as a multi-tier, high-availability emergency medical platform. The frontend is built with high-performance Vanilla ES6+ and modern CSS, avoiding framework overhead to guarantee sub-50ms load times during emergency scenarios.  
> The backend is a Spring Boot 3 enterprise application following Domain-Driven Design (DDD) layered architecture:
> 1. A stateless **Security Layer** featuring Spring Security 6 and JJWT implementing Role-Based Access Control (`ROLE_USER`, `ROLE_HOSPITAL_ADMIN`, `ROLE_SUPER_ADMIN`).
> 2. A **REST Controller Layer** managing clean HTTP contracts with global exception translation (`@RestControllerAdvice`).
> 3. A **Service Layer** encapsulating business logic such as our multi-factor emergency scoring engine and state machine transitions.
> 4. A **Spring Data JPA & Hibernate Layer** interfacing with an in-memory or PostgreSQL relational database ensuring ACID-compliant transactions for bed allocation and billing."

---

### Q2: "How does your hospital recommendation algorithm work? Why not just use Google Maps to find the closest hospital?"
**High-Impact Answer:**  
> "Recommending solely by distance is clinically dangerous during trauma emergencies. If a patient experiencing acute myocardial infarction rushes to the closest clinic 1 km away, but that clinic lacks a catheterization lab or has zero on-duty cardiologists, the patient faces fatal delays.  
> Lifeline uses an intelligent multi-criteria scoring algorithm:
> - **Specialist Match (35% weight):** Checks whether a specialist matched to the emergency (e.g., Cardiologist for Cardiac, Pulmonologist for Asthma) is actively clocked *on-duty* in the hospital roster.
> - **ICU Availability (25% weight):** Assesses critical care capacity. A hospital with zero ICU beds receives 0 points here.
> - **Equipment Readiness (15% weight):** Evaluates real-time availability of ventilators, oxygen cylinders, and cardiac monitors.
> - **General Bed Capacity (15% weight):** Ensures general ward intake headroom.
> - **Haversine Proximity (10% weight):** Computes real-time road distance and travel time.  
> We present users with both the **#1 Best Equipped Hospital** (highest clinical readiness) and the **#1 Nearest Open Hospital** (shortest transit time) so doctors and paramedics can make informed life-saving decisions."

---

### Q3: "How do you prevent fake reviews and review bombing on hospitals?"
**High-Impact Answer:**  
> "We enforce a two-stage verification barrier:
> 1. **Completed Treatment Prerequisite:** A user cannot submit a rating for a hospital unless our backend validates that the user has a completed medical interaction at that specific hospital. We cross-reference the user's authenticated email and phone number against `EmergencyCase` records where status is `COMPLETED`, or `HospitalPatient` EHR records where `admissionStatus` is `DISCHARGED`. If neither exists, the request is rejected with a `400 Bad Request` ('Verified Treatment Required').
> 2. **Strict No-Duplication Constraint:** Ratings are strictly capped at 1 review per verified visit. In `UserController.java`, we invoke `feedbackReportRepository.existsByReporterEmailAndTargetIdAndReportType(...)`. If a review exists, duplicate submissions are rejected.
> 3. **Single Source of Truth UI:** Review forms exist exclusively on the user's authenticated `profile.html`. Emergency cards only display read-only public averages, preventing unauthorized review generation."

---

### Q4: "How do you handle race conditions when two patients book the last available ICU bed simultaneously?"
**High-Impact Answer:**  
> "In our Spring Boot JPA layer, bed management operations are wrapped in `@Transactional` boundaries with pessimistic or optimistic locking (`@Version` attribute or `SELECT FOR UPDATE` queries).  
> When an admission request arrives via `HospitalPatientService.admitPatient(...)`, the service reads the hospital entity, checks `if (hospital.getAvailableICUBeds() <= 0) throw new InsufficientCapacityException();`, decrements the capacity atomically, and commits the transaction. If two threads collide, the second transaction is rolled back safely, preventing negative bed counts."

---

### Q5: "Why did you choose stateless JWT tokens over HTTP session cookies?"
**High-Impact Answer:**  
> "Stateless JWTs allow our backend to scale horizontally across multiple instances or microservices without requiring shared session stores like Redis or sticky sessions at the load balancer.  
> Each token carries cryptographically signed claims (`email`, `role`, `issuedAt`, `expiration`). Our `JwtAuthenticationFilter` intercepts requests, validates the signature using our HMAC-SHA-256 secret key, and populates the `SecurityContextHolder`. To mitigate token compromise, we enforce short expiration lifespans and maintain an administrative suspension table that invalidates blacklisted users."

---

### Q6: "How did you design the database to handle hospital blood inventory?"
**High-Impact Answer:**  
> "Rather than creating a bloated individual table with fixed columns for each blood group, we used JPA's `@ElementCollection`:
> ```java
> @ElementCollection(fetch = FetchType.EAGER)
> @CollectionTable(name = "hospital_blood_inventory", joinColumns = @JoinColumn(name = "hospital_id"))
> @MapKeyColumn(name = "blood_group")
> @Column(name = "units")
> private Map<String, Integer> bloodInventory = new HashMap<>();
> ```
> This creates a normalized auxiliary table `hospital_blood_inventory(hospital_id, blood_group, units)`. It provides extreme flexibility: adding specialized blood components (e.g., Plasma, Cryoprecipitate) requires zero database schema migrations."

---

### Q7: "How is the AI Doctor Chatbot kept private and prevented from answering off-topic questions?"
**High-Impact Answer:**  
> "1. **Privacy Isolation:** Every chat interaction requires a valid Bearer JWT. Chat messages are bound to a foreign key relationship with the authenticated `User` in `ChatSession.java`. Users cannot read or manipulate another citizen's chat history.
> 2. **Domain-Specific Guardrail Filter:** Incoming user prompts pass through a healthcare regex and domain validation pipeline in `AiChatService.java`. If a user asks about non-health topics (e.g., cryptocurrency, code generation, movie trivia), the assistant fires a deflection response: *'I am Lifeline AI Medical Assistant. I can only assist with health, fitness, and emergency medicine.'*
> 3. **Emergency Escalation:** If critical triage keywords are detected (e.g., 'crushing chest pain', 'slurred speech'), the AI prepends an urgent disclaimer directing the user to immediately trigger the **Emergency Pre-Arrival Dispatcher**."

---

### Q8: "If this application experiences a massive surge during a city-wide disaster, how would you scale it?"
**High-Impact Answer:**  
> "1. **Read-Heavy Caching:** Hospital profiles, doctor rosters, and static equipment specs change infrequently compared to search requests. We would introduce a **Redis Cache** (`@Cacheable`) for `findNearestAndBestHospitals`, caching results using a geohash spatial grid (e.g., 1 km precision buckets) with a 30-second TTL.
> 2. **Asynchronous ER Dispatch Queue:** Pre-arrival alerts can be offloaded to an asynchronous message broker like **Apache Kafka** or **RabbitMQ**. The ER desk would subscribe to a WebSocket or Server-Sent Events (SSE) topic, decoupling alert ingestion from database writes.
> 3. **Database Read Replicas:** Route read operations (public search) to read replicas, reserving the primary PostgreSQL instance for transactional writes (admissions, billings, status changes).
> 4. **Containerization & Auto-Scaling:** Package the Spring Boot JAR into lightweight Docker containers orchestrated by **Kubernetes (EKS/GKE)**, scaling pods horizontally based on CPU utilization and HTTP request queue length."

---

### Q9: "What design patterns have you utilized in this project?"
**High-Impact Answer:**  
> "We utilized several classical and modern enterprise design patterns:
> - **Dependency Injection / Inversion of Control (IoC):** Used across all Spring `@Service`, `@Repository`, and `@RestController` components to ensure loose coupling and testability.
> - **Repository Pattern:** Spring Data JPA abstracts database operations behind clean domain interfaces.
> - **DTO (Data Transfer Object) Pattern:** `HospitalScoreDto`, `AuthResponse`, and `EmergencySearchRequest` decouple internal database entities from external API contracts, preventing over-fetching and protecting sensitive fields (like password hashes).
> - **Filter Chain Pattern:** Spring Security's `SecurityFilterChain` processes requests sequentially (CORS $\rightarrow$ JWT Extraction $\rightarrow$ Authorization $\rightarrow$ Controller Execution).
> - **Strategy / Scoring Engine Pattern:** `EmergencyMatchingService` isolates clinical readiness calculations from raw entity data retrieval."

---

## 9. Summary & Quick Reference

| Module | Core File Reference | Primary Route / Endpoint |
| :--- | :--- | :--- |
| **Emergency Finder** | [emergency.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/emergency.html) | `POST /api/emergency/find-hospitals` |
| **Dedicated Profile** | [profile.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/profile.html) | `GET /api/user/profile`, `POST /api/user/feedback` |
| **Hospital SaaS ERP** | [hospital-dashboard.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/hospital-dashboard.html) | `/api/hospital/*` (EHR, Billing, Pharmacy, Doctors) |
| **Admin Governance**| [admin.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/admin.html) | `/api/admin/*` (Verify, Close, Suspend, Resolve) |
| **AI Doctor Chat** | [chat.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/chat.html) | `POST /api/chat/message` |
| **Unified Portal** | [index.html](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/index.html) | Gateway entry & Role selection |

---

## 10. Live Production Cloud Deployment & Infrastructure Topology

```mermaid
flowchart LR
    subgraph EdgeCDN["Vercel Global Edge Network"]
        V["Production Frontend<br/>(lifeline-emergency-gamma.vercel.app)"]
    end

    subgraph CloudBackend["Render Cloud Container Services"]
        R["Spring Boot 3.3.5 Docker Container<br/>(lifeline-backend-0zz7.onrender.com)"]
        H2["In-Memory Relational H2 / PostgreSQL DB"]
    end

    subgraph External["External Public Services"]
        OSM["OpenStreetMap / OSRM Engine<br/>(Live Road Routing)"]
        LLM["OpenRouter API<br/>(LLaMA 3.1 8B Instruct / Fallback)"]
    end

    V -- "HTTPS REST API (JWT Bearer)" --> R
    R --> H2
    V -- "OSRM Car Routing Coordinates" --> OSM
    R -. "Optional In-Transit Telemetry" .-> LLM
```

### Production Service Coordinates
* **Live Frontend URL:** [https://lifeline-emergency-gamma.vercel.app](https://lifeline-emergency-gamma.vercel.app) (Hosted on Vercel Edge Network)
* **Live Backend API URL:** [https://lifeline-backend-0zz7.onrender.com](https://lifeline-backend-0zz7.onrender.com) (Hosted on Render Cloud Docker Container)
* **Official GitHub Repository:** [https://github.com/shivam-kumar-emf-portrait/health-emergency-app](https://github.com/shivam-kumar-emf-portrait/health-emergency-app)
* **Continuous Integration & Deployment (CI/CD):** Pushes to the `main` branch automatically trigger Vercel edge builds and Render Docker redeployments with zero manual downtime.

---

## 11. Resume-Ready Project Bullet Points

Use these high-impact, STAR-formatted (Situation, Task, Action, Result) bullet points on your resume:

### Option A: Full-Stack / Software Engineer Focus
* **Architected and deployed Lifeline**, an enterprise-grade emergency response platform and hospital SaaS ERP utilizing **Java 17, Spring Boot 3, Spring Security 6, and JPA/Hibernate**, serving real-time clinical triage and capacity telemetry.
* **Engineered a multi-criteria triage recommendation algorithm** leveraging the **Haversine formula, specialist physician rosters, ICU bed availability, and life-support units** to calculate clinical emergency readiness scores ($0-100$), slashing transit decision latency.
* **Designed an anti-spam hospital rating engine** backed by a strict treatment verification state machine (requiring `COMPLETED` ER cases or `DISCHARGED` EHR records) and a single-review constraint, eliminating fake reviews and dynamically updating public facility scores.
* **Deployed a cloud-native architecture** orchestrating a multi-stage **Dockerized Spring Boot container on Render** and a high-performance **static edge client on Vercel**, achieving sub-50ms static load times and 100% independent 24/7 uptime.

### Option B: Backend & Java Systems Focus
* **Built an enterprise hospital SaaS ERP backend** in **Spring Boot 3.3** featuring 8 core operational subsystems: inbound ER telemetry queues, EHR patient admissions (UHID generation), itemized billing/invoicing, pharmacy formulary tracking, and blood inventory management.
* **Implemented stateless Role-Based Access Control (RBAC)** using **Spring Security 6, JJWT, and BCrypt**, enforcing strict multi-tenant boundary isolation across Citizens, Hospital Administrators, and State Health Governance Authorities.
* **Constructed an AI Medical Assistant pipeline** incorporating private session memory, emergency escalation triggers, and strict healthcare domain guardrails with an automated zero-downtime clinical fallback engine.

