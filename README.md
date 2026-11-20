#  Lifeline — Health Emergency & Hospital SaaS System
> **Enterprise Java Full Stack Project** built with **Java 17/21/25**, **Spring Boot 3.3**, **OpenRouter AI Clinical Triage**, **Clerk Dual Authentication**, and an aesthetic **Charcoal, Mint Green, Desert Sun & Teal Blue** UI featuring **Interactive 60fps Gravitational Physics**.

---

##  What's New & Key Innovations

1. **OpenRouter AI Emergency Route & Transit Navigator (In-Transit Co-Pilot)**:
   - Powered by OpenRouter (`meta-llama/llama-3.1-8b-instruct`), providing instant, medically precise in-transit life support advice while rushing to the hospital:
     - **Exact Vehicle Posture**: e.g., Semi-Fowlers 45° angle with knees bent for cardiac distress, or supine with neck alignment for trauma.
     - **Airway & Vitals Monitoring Protocol**: Emergency oxygenation guidance, pulse and respiratory rate tracking.
     - **Driving Strategy & Speed Shock Tactics**: Recommends smooth acceleration/braking and controllable speeds (e.g. max 40-50 km/h) to prevent sudden intracranial pressure surges, secondary trauma dislocation, or acute hypotension.
     - **Automated Pre-Arrival Alert**: Dispatches a priority alert directly to the receiving hospital ER team so the Cath Lab or Trauma Bay preps before wheels touch the hospital.
     - **Turn-by-Turn GPS Navigation**: Integrated with OpenStreetMap live route navigation.
     - **Direct ER Phone Hotline**: 1-click click-to-call button to speak with the hospital triage desk in advance.

2. **Enterprise Hospital SaaS ERP (Reputable Hospital Grade)**:
   - Independent SaaS console built for hospitals (e.g. Metro Heart Institute, Apex Trauma Center):
     - **Patient Admissions (EHR)**: Generates UHID (`UHID-2026-XXXX`), tracks admission status (`ICU`, `ADMITTED`, `OBSERVATION`, `DISCHARGED`), assigned bed, and primary diagnosis.
     - **Itemized Billing & Invoicing**: Tracks invoices (`INV-2026-XXXX`), line items (ICU care, surgeon fees, oxygen, medicines, labs), taxes, discounts, payment status (`PAID`, `PENDING`, `INSURANCE_CLAIM`), and generates printable hospital invoice receipts.
     - **Staff Attendance & Shift Management**: Daily clock-in and clock-out for doctors, head nurses, paramedics, pharmacists, and radiology technicians across Morning, Evening, and Night shifts.
     - **Critical Pharmacy Formulary & Stocks**: Live unit counters for life-saving drugs (Adrenaline, Atropine, Nitroglycerin, Heparin, Saline, Amiodarone, Dopamine, Furosemide) with automated low-stock warnings.
     - **Live Emergency Command Center**: Real-time inbound emergency cases with click-to-call, triage bay prep, and doctor assignment.

3. **Dual Emergency Matching (Top 3 Nearest vs Top 3 Best Equipped)**:
   - User side evaluates real-time hospital stocks, on-duty specialist doctors, ICU capacity, and equipment to compute:
     - **Top 3 Nearest Hospitals** (strictly by Haversine distance).
     - **Top 3 Best Equipped Hospitals** (evaluated for the specific emergency, e.g. Heart Emergency -> checks on-duty Cardiologist, ICU beds, cardiac monitors, ventilators).

4. **Guest-Friendly Zero-Barrier Access (No Login Required)**:
   - Any citizen in a crisis can immediately find hospitals, launch OpenRouter in-transit co-pilot, and chat with the AI Clinical Triage without logging in.
   - **The Distinction**: For guests, AI chats are processed in memory and are **zero-trace (not saved to servers)**. When signed in, conversations and medical histories are permanently synced and saved to the database.

5. **Clerk Dual-Role Authentication**:
   - Distinct portals for Patients vs Hospital SaaS Admins.
   - Configure your Clerk publishable key in `js/clerk-config.js` or use 1-click test credentials.

6. **Aesthetic Color Palette (Subtle, Rich, Matte & Editorial)**:
   - Charcoal (`#121619`), Mint Green (`#10b981`), Desert Sun (`#e76f51`), Teal Blue (`#0d9488`), and hand-drawn marker highlighter effects (`.marker-highlight`). No cheap neon glows.

7. **High-Level Physics Simulation ("WOW Factor")**:
   - Interactive 60fps Gravitational Resource Radar on HTML5 Canvas with Hooke's Law spring linkages, orbital resource particles, and magnetic cursor force fields.

---

##  Where to Paste Your Clerk Keys

You can connect your own Clerk project in **one simple step**:

1. Open file: [clerk-config.js](file:///c:/Users/asksh/Desktop/resume_project/health-emergency-app/frontend/js/clerk-config.js)
2. Replace the placeholder on line 9:
   ```javascript
   window.CLERK_PUBLISHABLE_KEY = "pk_test_YOUR_CLERK_KEY_HERE";
   ```
3. That's it! The frontend automatically loads the official Clerk JavaScript SDK (`@clerk/clerk-js`) and handles:
   - Separate Patient Sign-In / Sign-Up
   - Separate Hospital Admin Sign-In / Sign-Up
   - If the key is left as the placeholder, the application runs smoothly in **Demo Mode** with 1-click test credentials so testing is never blocked.

---

## ️ Clean Project Architecture

```
health-emergency-app/
├── pom.xml                                      # Root Maven Multi-Module aggregator
│
├── backend/                                     # Spring Boot 3 Java Backend
│   ├── pom.xml                                  # Java 17/21/25, Spring Boot 3.3, JPA, Security, OpenRouter
│   └── src/
│       ├── main/
│       │   ├── java/com/lifeline/emergency/
│       │   │   ├── EmergencyApplication.java    # Spring Boot Main Entry Point
│       │   │   ├── config/                      # SecurityConfig, JwtFilter, DataInitializer
│       │   │   ├── controller/                  # AiChatController, EmergencyController, HospitalController, etc.
│       │   │   ├── dto/                         # ChatRequest, ChatResponse, EmergencySearchRequest, etc.
│       │   │   ├── entity/                      # ChatSession, Hospital, Doctor, EmergencyCase, User
│       │   │   ├── enums/                       # EmergencyType, DoctorSpecialization, CaseStatus, Role
│       │   │   ├── repository/                  # ChatSessionRepository, HospitalRepository, DoctorRepository, etc.
│       │   │   ├── service/                     # AiChatService (OpenRouter), EmergencyMatchingService, etc.
│       │   │   └── util/                        # GeoUtils (Haversine), JwtUtil
│       │   └── resources/
│       │       ├── application.yml              # OpenRouter API key & H2 Database configuration
│       │       └── static/                      # Bundled Web Frontend (Served on port 8080)
│       └── test/
│           └── java/com/lifeline/emergency/
│               └── EmergencyMatchingServiceTest.java # Automated JUnit 5 Integration Test Suite
│
├── frontend/                                    # Clean Web Client & SaaS ERP Portal
│   ├── index.html                               # Modern Landing Page (Interactive Physics Radar)
│   ├── chat.html                                # AI Clinical Triage Room (Guest Mode vs Saved History)
│   ├── emergency.html                           # Patient Emergency Portal (Top 3 Nearest vs Best)
│   ├── hospital-dashboard.html                  # Hospital SaaS ERP Management Console
│   ├── login.html                               # Clerk & Lifeline Dual Authentication
│   ├── css/                                     # style.css, emergency.css, hospital.css
│   └── js/                                      # api.js, clerk-config.js, physics-radar.js
│
├── legacy-node-backend/                         # Archived previous Node.js files
└── README.md                                    # Documentation
```

---

##  The Core Matching Logic (Top 3 Nearest vs. Top 3 Best)

When an emergency request is initiated (e.g. for `CARDIAC`):

### 1. Top 3 Nearest Hospitals
- Uses the **Haversine formula** (`GeoUtils.calculateDistanceKm`) to calculate the great-circle distance between the user's GPS coordinates and each hospital.
- Filters for active, verified, open hospitals with at least 1 available emergency bed.
- Sorts strictly by `distanceKm ASC` and returns the top 3.

### 2. Top 3 Best Equipped Hospitals
Evaluated dynamically using a **100-point multi-factor readiness score**:

$$\text{Score} = \text{SpecialistPts} + \text{ICUPts} + \text{BedPts} + \text{EquipmentPts} + \text{ProximityPts}$$

| Criteria | Max Points | Logic & Domain Rule |
| :--- | :---: | :--- |
| **Specialist Doctor Match** | **35 pts** | **+35 pts** if matching specialist is on duty (e.g. **Cardiologist** for `CARDIAC`, **Neurologist** for `STROKE`, **Trauma Surgeon** for `TRAUMA`, **Pulmonologist** for `RESPIRATORY`). <br>**+15 pts** if only general emergency doctors are present. |
| **ICU Bed Availability** | **20 pts** | **+5 pts per ICU bed** (up to 20 pts) for critical types like Cardiac, Stroke, Trauma. |
| **General Emergency Beds** | **15 pts** | **+1 pt per free bed** (up to 15 pts) for general emergency capacity. |
| **Equipment Readiness** | **18 pts** | **Cardiac:** Cardiac monitors (+8), Ventilators (+6), Oxygen (+4).<br>**Respiratory:** Ventilators (+10), Oxygen cylinders (+8).<br>**Trauma/Bleeding:** Ambulances (+8), Blood units (+10). |
| **Proximity Bonus** | **12 pts** | Formula: $\max(0, 12 - \text{distanceKm})$. Closer hospitals are favored when capability is equal. |

> **Real Test Verification Output**:
> - **Top 3 Nearest for Heart Emergency**:
>   1. Metro Heart & Emergency Institute -> `0.15 km`
>   2. City Care General Hospital -> `1.56 km`
>   3. GreenLife Community Clinic -> `2.49 km` (Has 0 ICU beds & NO Cardiologist)
> - **Top 3 Best Equipped for Heart Emergency**:
>   1. **Metro Heart & Emergency Institute** -> Score: `99.9` *(Cardiologist Dr. Arvind Mehta On-Duty, 6 ICU Beds, 8 Ventilators)*
>   2. **LifeLine Global Super-Specialty** -> Score: `94.3` *(Cardiologist Dr. Sneha Chatterjee On-Duty, 9 ICU Beds, 14 Ventilators)*
>   3. **Mother & Child Emergency Hospital** -> Score: `75.5` *(General Emergency Doctors, 5 ICU Beds, 6 Ventilators)*
> *(GreenLife Clinic was dropped from Top 3 Best because it lacks ICU beds and cardiologists!)*

---

##  Quick Run Guide

### 1. Start the Full Stack (Single Command)
```powershell
cd backend
mvn spring-boot:run
```

Once started, open **`http://localhost:8080`** in any browser.

- **Landing Page & Physics Radar**: `http://localhost:8080/index.html`
- **AI Clinical Triage (Guest or Saved)**: `http://localhost:8080/chat.html`
- **Patient Emergency Matcher**: `http://localhost:8080/emergency.html`
- **Hospital SaaS ERP**: `http://localhost:8080/hospital-dashboard.html`
- **Login / Register Portal**: `http://localhost:8080/login.html`
- **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:health_emergency_db`, User: `sa`, Password: *(blank)*)

### 2. Preloaded Demo Accounts
- **Hospital Admin 1 (Cardiology Leader)**: `metro@hospital.com` / `admin123`
- **Hospital Admin 2 (Neuro & Trauma)**: `apex@hospital.com` / `admin123`
- **Patient Profile**: Shivam Kumar (`patient@lifeline.com` / `password123`)
*(1-Click login buttons are also provided on the Login page.)*
