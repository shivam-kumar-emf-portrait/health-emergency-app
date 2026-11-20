const BASE_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:5000"
    : "https://health-emergency-backend.onrender.com";


const hospitalToken = localStorage.getItem("hospitalToken");

if (!hospitalToken) {
  window.location.replace("login.html?role=hospital");
}
let currentCases = [];
let lastCaseIds = new Set();
let activeSection = "overview";

/* =========================================
   AUTH
========================================= */
function getToken() {
  return localStorage.getItem("hospitalToken");
}

function requireAuth() {

  const token = localStorage.getItem("hospitalToken");

  if (!token) {
    window.location.replace("login.html?role=hospital");
    
  }

  try {

    const payload = JSON.parse(atob(token.split(".")[1]));

    if (payload.role !== "hospital_admin") {
      localStorage.removeItem("hospitalToken");
      window.location.replace("login.html?role=hospital");
    }

  } catch (err) {
    localStorage.removeItem("hospitalToken");
    window.location.replace("login.html?role=hospital");
  }
}

/* =========================================
   SIDEBAR NAVIGATION (FIXED)
========================================= */
document.addEventListener("DOMContentLoaded", () => {
requireAuth();
loadHospitalStatus();
  const buttons = document.querySelectorAll("#sidebarNav button");

  buttons.forEach(btn => {
    btn.addEventListener("click", () => {

      buttons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");

      const section = btn.dataset.section;
      activeSection = section;

      document.getElementById("sectionTitle").innerText =
        btn.innerText;

      loadSection(section);
    });
  });

  loadSection("overview");
});


/* =========================================
   MAIN SECTION CONTROLLER
========================================= */

function loadSection(section) {

  const content = document.getElementById("dynamicContent");
  content.innerHTML = "";

  if (section === "overview") {
    activeSection = "overview";
    loadOverview();
    return;
  }

  if (section === "emergency") {
    activeSection = "emergency";
    loadEmergencyBoard();
    return;
  }

  if (section === "doctors") {
    activeSection = "doctors";
    loadDoctorsSection();
    return;
  }

  if (section === "nurses") {
    activeSection="nurses";
    loadNursesSection();
    return;
  }

  if (section === "resources") {
  activeSection = "resources";
  loadResourcesSection();
  return;
}

if (section === "blood") {
  activeSection = "blood";
  loadBloodBankSection();
  return;
}

if (section === "equipment") {
  activeSection = "equipment";
  loadEquipmentSection();
  return;
}


  content.innerHTML = `
    <div class="module-placeholder">
      <h2>${capitalize(section)}</h2>
      <p>This module UI will be here.</p>
    </div>
  `;
}




/* =========================================
   OVERVIEW
========================================= */
function loadOverview() {

  const content = document.getElementById("dynamicContent");

  content.innerHTML = `
    <div class="overview-grid">
      <div class="overview-card" onclick="filterByStatus('active')">
        <h3>Active</h3>
        <p id="activeCount">0</p>
      </div>
      <div class="overview-card" onclick="filterByStatus('in_progress')">
        <h3>In Progress</h3>
        <p id="progressCount">0</p>
      </div>
      <div class="overview-card" onclick="filterByStatus('completed')">
        <h3>Completed</h3>
        <p id="completedCount">0</p>
      </div>
    </div>

    <div class="search-wrapper">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" id="searchInput"
          placeholder="Search patient, phone, type..."
          oninput="renderCases(this.value)">
      </div>
    </div>

    <div id="casesList" class="cases-grid"></div>
  `;

  loadCases();
}


/* =========================================
   LOAD CASES
========================================= */
async function loadCases() {

  if (activeSection !== "overview") return; // 🔴 Only load for overview

  try {
    const res = await fetch(`${API_BASE}/api/hospital/emergencies`, {
      headers: { Authorization: "Bearer " + getToken() }
    });

    const data = await res.json();
    const cases = data.emergencies || [];

    currentCases = cases;

    updateCounts();
    renderCases();

    detectNewCases(cases);

  } catch (err) {
    console.error("Error loading cases:", err);
  }
}


/* =========================================
   DETECT NEW CASE (FIXED PROPERLY)
========================================= */
function detectNewCases(cases) {

  const newIds = new Set(cases.map(c => c._id));

  cases.forEach(c => {
    if (!lastCaseIds.has(c._id)) {

      showGlobalAlert(
        `🚨 NEW EMERGENCY: ${c.emergencyType} (${c.patient?.name || "Unknown"})`
      );

      playAlertSound();
    }
  });

  lastCaseIds = newIds;
}


/* =========================================
   UPDATE COUNTS
========================================= */
function updateCounts() {

  let active = 0, progress = 0, completed = 0;

  currentCases.forEach(c => {
    if (c.status === "active") active++;
    if (c.status === "in_progress") progress++;
    if (c.status === "completed") completed++;
  });

  document.getElementById("activeCount").innerText = active;
  document.getElementById("progressCount").innerText = progress;
  document.getElementById("completedCount").innerText = completed;
}


/* =========================================
   SEARCH + RENDER (IMPROVED)
========================================= */
function renderCases(searchValue = "") {

  const container = document.getElementById("casesList");
  if (!container) return;

  container.innerHTML = "";

  const value = searchValue.trim().toLowerCase();

  let filtered = currentCases;

  if (value) {
    filtered = currentCases.filter(c => {

      const patient = (c.patient?.name || "").toLowerCase();
      const phone = (c.patientPhone || "").toLowerCase();
      const type = (c.emergencyType || "").toLowerCase();

      return (
        patient.includes(value) ||
        phone.includes(value) ||
        type.includes(value)
      );
    });
  }

  if (filtered.length === 0) {
    container.innerHTML =
      `<p style="opacity:0.6">No matching cases</p>`;
    return;
  }

  filtered.forEach(c => {

    const created = new Date(c.createdAt);

    const formattedDate = created.toLocaleDateString("en-IN");
    const formattedTime = created.toLocaleTimeString("en-IN", {
      hour: "2-digit",
      minute: "2-digit"
    });

    const card = document.createElement("div");
    card.className = "case-card";

    card.innerHTML = `
      <h4>${c.emergencyType}</h4>
      <p><strong>Patient:</strong> ${c.patient?.name || "Unknown"}</p>
      <p><strong>Phone:</strong> ${c.patientPhone || "N/A"}</p>
      <p><strong>Status:</strong> ${formatStatus(c.status)}</p>
      <p><strong>Date:</strong> ${formattedDate}</p>
      <p><strong>Time:</strong> ${formattedTime}</p>
    `;

    container.appendChild(card);
  });
}


/* =========================================
   FILTER BY STATUS (FIXED)
========================================= */
function filterByStatus(status) {

  const filtered = currentCases.filter(c => c.status === status);
  renderFilteredList(filtered);
}

function renderFilteredList(list) {

  const container = document.getElementById("casesList");
  container.innerHTML = "";

  if (list.length === 0) {
    container.innerHTML =
      `<p style="opacity:0.6">No cases found</p>`;
    return;
  }

  list.forEach(c => {

    const created = new Date(c.createdAt);

    const card = document.createElement("div");
    card.className = "case-card";

    card.innerHTML = `
      <h4>${c.emergencyType}</h4>
      <p><strong>Patient:</strong> ${c.patient?.name || "Unknown"}</p>
      <p><strong>Status:</strong> ${formatStatus(c.status)}</p>
      <p><strong>Date:</strong> ${created.toLocaleDateString("en-IN")}</p>
      <p><strong>Time:</strong> ${created.toLocaleTimeString("en-IN")}</p>
    `;

    container.appendChild(card);
  });
}


/* =========================================
   FORMAT STATUS
========================================= */
function formatStatus(status) {
  if (status === "active") return "Active";
  if (status === "in_progress") return "In Progress";
  if (status === "completed") return "Completed";
  return status;
}


/* =========================================
   GLOBAL ALERT
========================================= */
function showGlobalAlert(message) {

  const alertBox = document.getElementById("globalAlert");
  const alertText = document.getElementById("alertText");

  alertText.innerText = message;
  alertBox.classList.remove("hidden");

  setTimeout(() => {
    alertBox.classList.add("hidden");
  }, 8000);
}

function closeAlert() {
  document.getElementById("globalAlert").classList.add("hidden");
}


/* =========================================
   SOUND ALERT
========================================= */
function playAlertSound() {
  const audio = new Audio("https://www.soundjay.com/buttons/sounds/beep-07.mp3");
  audio.play();
}


/* =========================================
   AUTO REFRESH
========================================= */
setInterval(loadCases, 10000);


/* =========================================
   LOGOUT
========================================= */
function logoutHospital() {

  localStorage.removeItem("hospitalToken");
  localStorage.removeItem("token");

  window.location.replace("login.html?role=hospital");
}


/* =========================================
   UTIL
========================================= */
function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

/* =========================================
   DOCTORS SECTION
========================================= */
/* =========================================
   DOCTORS SECTION (STEP 3 UPGRADED)
========================================= */
async function loadDoctorsSection() {

  const content = document.getElementById("dynamicContent");

  try {
    const res = await fetch(`${API_BASE}/api/hospital/doctors`, {
      headers: { Authorization: "Bearer " + getToken() }
    });

    const data = await res.json();

    const doctors = data.doctors || [];
    const stats = data.stats || {};
    const specializationStats = data.specializationStats || {};

    content.innerHTML = `

      <!-- TOP STATS -->
      <div class="overview-grid">
        <div class="overview-card">
          <h3>Total Doctors</h3>
          <p>${stats.totalDoctors || 0}</p>
        </div>

        <div class="overview-card">
          <h3>On Duty</h3>
          <p>${stats.onDutyDoctors || 0}</p>
        </div>

        <div class="overview-card">
          <h3>Off Duty</h3>
          <p>${stats.offDutyDoctors || 0}</p>
        </div>
      </div>

      <!-- ADD NEW DOCTOR -->
      <h2 style="margin-top:40px;">Add New Doctor</h2>

      <div class="doctor-form">
        <input id="docName" placeholder="Doctor Name" />
        <input id="docSpec" placeholder="Specialization" />
        <input id="docQual" placeholder="Qualification" />
        <input id="docExp" type="number" placeholder="Experience (Years)" />

        <label style="margin-top:10px;">
          <input type="checkbox" id="docPublic" />
          Show in Public Profile
        </label>

        <button onclick="addDoctor()" class="primary-btn">
          Add Doctor
        </button>
      </div>

      <!-- SPECIALIZATION STATS -->
      <h2 style="margin-top:40px;">Specializations</h2>
      <div id="specializationList" class="cases-grid"></div>

      <!-- DOCTOR LIST -->
      <h2 style="margin-top:40px;">Doctors</h2>
      <div id="doctorList" class="cases-grid"></div>
    `;

    /* ---- Specialization Breakdown ---- */
    const specContainer = document.getElementById("specializationList");

    Object.keys(specializationStats).forEach(spec => {

      const card = document.createElement("div");
      card.className = "case-card";

      card.innerHTML = `
        <h4>${spec}</h4>
        <p>Total: ${specializationStats[spec].total}</p>
        <p>On Duty: ${specializationStats[spec].onDuty}</p>
      `;

      specContainer.appendChild(card);
    });

    /* ---- Doctor List ---- */
    const doctorContainer = document.getElementById("doctorList");

    doctors.forEach(doc => {

      const card = document.createElement("div");
      card.className = "case-card";

      card.innerHTML = `
        <h4>${doc.name}</h4>
        <p><strong>Specialization:</strong> ${doc.specialization}</p>
        <p><strong>Experience:</strong> ${doc.experienceYears} yrs</p>
        <p><strong>Status:</strong> ${doc.isOnDuty ? "On Duty" : "Off Duty"}</p>

        <div style="margin-top:12px; display:flex; gap:8px;">

          <button onclick="toggleDoctorDuty('${doc._id}')"
            class="secondary-btn">
            ${doc.isOnDuty ? "Mark Off Duty" : "Mark On Duty"}
          </button>

          <button onclick="deleteDoctor('${doc._id}')"
            class="danger-btn">
            Fire Doctor
          </button>

        </div>
      `;

      doctorContainer.appendChild(card);
    });

  } catch (err) {
    console.error("Doctor section error:", err);
  }
}


/* =========================================
   ADD DOCTOR
========================================= */
async function addDoctor() {

  const name = document.getElementById("docName").value.trim();
  const specialization = document.getElementById("docSpec").value.trim();
  const qualification = document.getElementById("docQual").value.trim();
  const experienceYears = document.getElementById("docExp").value;
  const isPublicProfile = document.getElementById("docPublic").checked;

  if (!name || !specialization) {
    alert("Name and Specialization are required");
    return;
  }

  try {

    const res = await fetch(`${API_BASE}/api/hospital/doctors`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + getToken()
      },
      body: JSON.stringify({
        name,
        specialization,
        qualification,
        experienceYears,
        isPublicProfile
      })
    });

    const data = await res.json();

    if (!res.ok) {
      console.error("Add doctor failed:", data);
      alert(data.message || "Failed to add doctor");
      return;
    }

    console.log("Doctor added:", data);

    loadDoctorsSection();

  } catch (err) {
    console.error("Add doctor error:", err);
  }
}


/* =========================================
   TOGGLE DUTY
========================================= */
async function toggleDoctorDuty(id) {
  try {
    await fetch(`${API_BASE}/api/hospital/doctors/${id}/toggle-duty`, {
      method: "PATCH",
      headers: {
        Authorization: "Bearer " + getToken()
      }
    });

    loadDoctorsSection();

  } catch (err) {
    console.error("Toggle duty error:", err);
  }
}


/* =========================================
   DELETE DOCTOR
========================================= */
async function deleteDoctor(id) {

  if (!confirm("Are you sure you want to remove this doctor?")) return;

  try {
    await fetch(`${API_BASE}/api/hospital/doctors/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: "Bearer " + getToken()
      }
    });

    loadDoctorsSection();

  } catch (err) {
    console.error("Delete doctor error:", err);
  }
}
/* =========================================
   NURSES SECTION
========================================= */
async function loadNursesSection() {

  const content = document.getElementById("dynamicContent");

  try {
    const res = await fetch(`${API_BASE}/api/hospital/nurses`, {
      headers: { Authorization: "Bearer " + getToken() }
    });

    const data = await res.json();

    const nurses = data.nurses || [];
    const stats = data.stats || {};

    content.innerHTML = `

      <div class="overview-grid">
        <div class="overview-card">
          <h3>Total Nurses</h3>
          <p>${stats.totalNurses || 0}</p>
        </div>

        <div class="overview-card">
          <h3>On Duty</h3>
          <p>${stats.onDutyNurses || 0}</p>
        </div>

        <div class="overview-card">
          <h3>Off Duty</h3>
          <p>${stats.offDutyNurses || 0}</p>
        </div>
      </div>

      <h2 style="margin-top:40px;">Add New Nurse</h2>

      <div class="doctor-form">
        <input id="nurseName" placeholder="Nurse Name" />
        <input id="nurseDept" placeholder="Department" />
        <input id="nurseExp" type="number" placeholder="Experience (Years)" />

        <select id="nurseShift">
          <option value="morning">Morning Shift</option>
          <option value="evening">Evening Shift</option>
          <option value="night">Night Shift</option>
        </select>

        <button onclick="addNurse()" class="primary-btn">
          Add Nurse
        </button>
      </div>

      <h2 style="margin-top:40px;">Nurses</h2>
      <div id="nurseList" class="cases-grid"></div>
    `;

    const nurseContainer = document.getElementById("nurseList");

    nurses.forEach(nurse => {

      const card = document.createElement("div");
      card.className = "case-card";

      card.innerHTML = `
        <h4>${nurse.name}</h4>
        <p><strong>Department:</strong> ${nurse.department}</p>
        <p><strong>Shift:</strong> ${nurse.shift}</p>
        <p><strong>Status:</strong> ${nurse.isOnDuty ? "On Duty" : "Off Duty"}</p>

        <div style="margin-top:12px; display:flex; gap:8px;">

          <button onclick="toggleNurseDuty('${nurse._id}')"
            class="secondary-btn">
            ${nurse.isOnDuty ? "Mark Off Duty" : "Mark On Duty"}
          </button>

          <button onclick="deleteNurse('${nurse._id}')"
            class="danger-btn">
            Remove
          </button>

        </div>
      `;

      nurseContainer.appendChild(card);
    });

  } catch (err) {
    console.error("Nurse section error:", err);
  }
}


/* =========================================
   ADD NURSE
========================================= */
async function addNurse() {

  const name = document.getElementById("nurseName").value.trim();
  const department = document.getElementById("nurseDept").value.trim();
  const experienceYears = document.getElementById("nurseExp").value;
  const shift = document.getElementById("nurseShift").value;

  if (!name || !department) {
    alert("Name and Department required");
    return;
  }

  try {
    await fetch(`${API_BASE}/api/hospital/nurses`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + getToken()
      },
      body: JSON.stringify({
        name,
        department,
        experienceYears,
        shift
      })
    });

    loadNursesSection();

  } catch (err) {
    console.error("Add nurse error:", err);
  }
}


/* =========================================
   TOGGLE NURSE DUTY
========================================= */
async function toggleNurseDuty(id) {
  try {
    await fetch(`${API_BASE}/api/hospital/nurses/${id}/toggle-duty`, {
      method: "PATCH",
      headers: {
        Authorization: "Bearer " + getToken()
      }
    });

    loadNursesSection();

  } catch (err) {
    console.error("Toggle nurse error:", err);
  }
}


/* =========================================
   DELETE NURSE
========================================= */
async function deleteNurse(id) {

  if (!confirm("Remove this nurse?")) return;

  try {
    await fetch(`${API_BASE}/api/hospital/nurses/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: "Bearer " + getToken()
      }
    });

    loadNursesSection();

  } catch (err) {
    console.error("Delete nurse error:", err);
  }
}
/* =========================================
   EMERGENCY BOARD
========================================= */
async function loadEmergencyBoard() {

  const content = document.getElementById("dynamicContent");

  content.innerHTML = `
    <h2 style="margin-bottom:20px;">Active Emergency Cases</h2>
    <div id="emergencyList" class="cases-grid"></div>
  `;

  try {
    const res = await fetch(`${API_BASE}/api/hospital/emergencies`, {
      headers: { Authorization: "Bearer " + getToken() }
    });

    const data = await res.json();
    const cases = data.emergencies || [];

    const activeCases = cases.filter(c =>
      c.status === "active" || c.status === "in_progress"
    );

    const container = document.getElementById("emergencyList");

    if (activeCases.length === 0) {
      container.innerHTML =
        `<p style="opacity:0.6">No active emergency cases</p>`;
      return;
    }

    activeCases.forEach(c => {

      const card = document.createElement("div");
      card.className = "case-card";

      card.innerHTML = `
        <h4>${c.emergencyType}</h4>
        <p><strong>Patient:</strong> ${c.patient?.name || "Unknown"}</p>
        <p><strong>Phone:</strong> ${c.patientPhone || "N/A"}</p>
        <p><strong>Status:</strong> ${formatStatus(c.status)}</p>

        <div style="margin-top:15px;">
          ${
            c.status === "active"
              ? `<button onclick="updateEmergencyStatus('${c._id}','in_progress')">
                   Start Treatment
                 </button>`
              : ""
          }

          <button onclick="updateEmergencyStatus('${c._id}','completed')"
            style="margin-left:10px;">
            Mark Completed
          </button>
        </div>
      `;

      container.appendChild(card);
    });

  } catch (err) {
    console.error("Emergency board error:", err);
  }
}

async function updateEmergencyStatus(id, status) {

  try {
    await fetch(`${API_BASE}/api/hospital/emergencies/${id}/status`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + getToken()
      },
      body: JSON.stringify({ status })
    });

    loadEmergencyBoard();

  } catch (err) {
    console.error("Status update error:", err);
  }
}

/* =========================================
   BEDS & ICU SECTION
========================================= */
async function loadResourcesSection() {

  const content = document.getElementById("dynamicContent");

  try {

    const res = await fetch(`${API_BASE}/api/hospital/me`, {
      headers: { Authorization: "Bearer " + getToken() }
    });

    const hospital = await res.json();

    content.innerHTML = `
      <h2 class="section-title">Hospital Bed Management</h2>
      <p class="section-sub">Manage Normal Beds & ICU Capacity</p>

      <div class="form-grid">

        <div class="form-card">
          <h3>🛏 Normal Beds</h3>
          <label>Total Beds</label>
          <input id="totalBeds" type="number"
            value="${hospital.totalBeds || 0}" />

          <label>Available Beds</label>
          <input id="availableBeds" type="number"
            value="${hospital.availableBeds || 0}" />
        </div>

        <div class="form-card">
          <h3>🏥 ICU Beds</h3>
          <label>Total ICU Beds</label>
          <input id="totalICU" type="number"
            value="${hospital.totalICUBeds || 0}" />

          <label>Available ICU Beds</label>
          <input id="availableICU" type="number"
            value="${hospital.availableICUBeds || 0}" />
        </div>

      </div>

      <button onclick="updateBeds()" class="primary-btn">
        Save Changes
      </button>
    `;

  } catch (err) {
    console.error("Resources error:", err);
  }
}


async function updateBeds() {

  const totalBeds = Number(document.getElementById("totalBeds").value);
  const availableBeds = Number(document.getElementById("availableBeds").value);
  const totalICU = Number(document.getElementById("totalICU").value);
  const availableICU = Number(document.getElementById("availableICU").value);

  try {

    const res = await fetch(`${API_BASE}/api/hospital/operations`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + getToken()
      },
      body: JSON.stringify({
        totalBeds,
        availableBeds,
        totalICUBeds: totalICU,
        availableICUBeds: availableICU
      })
    });

    const data = await res.json();

    if (!res.ok) {
      alert(data.message || "Update failed");
      return;
    }

    console.log("Beds updated:", data);

    loadResourcesSection();

  } catch (err) {
    console.error("Update beds error:", err);
  }
}

 /* =========================================
   BLOOD BANK SECTION
========================================= */
async function loadBloodBankSection() {

  const content = document.getElementById("dynamicContent");

  const res = await fetch(`${API_BASE}/api/hospital/me`, {
    headers: { Authorization: "Bearer " + getToken() }
  });

  const hospital = await res.json();
  const blood = hospital.bloodInventory || {};

  content.innerHTML = `
    <h2 class="section-title">Blood Bank Inventory</h2>
    <p class="section-sub">Manage Available Blood Units</p>

    <div class="form-grid">

      ${renderBloodInput("O+ (O Positive)", "O+", blood["O+"])}
      ${renderBloodInput("O- (O Negative)", "O-", blood["O-"])}
      ${renderBloodInput("A+ (A Positive)", "A+", blood["A+"])}
      ${renderBloodInput("A- (A Negative)", "A-", blood["A-"])}
      ${renderBloodInput("B+ (B Positive)", "B+", blood["B+"])}
      ${renderBloodInput("B- (B Negative)", "B-", blood["B-"])}
      ${renderBloodInput("AB+ (AB Positive)", "AB+", blood["AB+"])}
      ${renderBloodInput("AB- (AB Negative)", "AB-", blood["AB-"])}

    </div>

    <button onclick="updateBlood()" class="primary-btn">
      Save Changes
    </button>
  `;
}

function renderBloodInput(label, type, value = 0) {
  const id = type.replace('+','pos').replace('-','neg');
  return `
    <div class="form-card">
      <label>${label}</label>
      <input id="blood_${id}"
        type="number"
        value="${value || 0}" />
    </div>
  `;
}



async function updateBlood() {

  const bloodInventory = {
    "O+": document.getElementById("blood_Opos").value,
    "O-": document.getElementById("blood_Oneg").value,
    "A+": document.getElementById("blood_Apos").value,
    "A-": document.getElementById("blood_Aneg").value,
    "B+": document.getElementById("blood_Bpos").value,
    "B-": document.getElementById("blood_Bneg").value,
    "AB+": document.getElementById("blood_ABpos").value,
    "AB-": document.getElementById("blood_ABneg").value
  };

  await fetch(`${API_BASE}/api/hospital/operations`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + getToken()
    },
    body: JSON.stringify({ bloodInventory })
  });

  loadBloodBankSection();
}


/* =========================================
   EQUIPMENT SECTION
========================================= */
async function loadEquipmentSection() {

  const content = document.getElementById("dynamicContent");

  const res = await fetch(`${API_BASE}/api/hospital/me`, {
    headers: { Authorization: "Bearer " + getToken() }
  });

  const hospital = await res.json();
  const equipment = hospital.equipment || {};

  content.innerHTML = `
    <h2 class="section-title">Medical Equipment Management</h2>
    <p class="section-sub">Update Available Equipment</p>

    <div class="form-grid">

      <div class="form-card">
        <label>🫁 Ventilators</label>
        <input id="ventilators" type="number"
          value="${equipment.ventilators || 0}" />
      </div>

      <div class="form-card">
        <label>🫁 Oxygen Cylinders</label>
        <input id="oxygenCylinders" type="number"
          value="${equipment.oxygenCylinders || 0}" />
      </div>

      <div class="form-card">
        <label>🚑 Ambulances</label>
        <input id="ambulances" type="number"
          value="${equipment.ambulances || 0}" />
      </div>

      <div class="form-card">
        <label>📟 Patient Monitors</label>
        <input id="monitors" type="number"
          value="${equipment.monitors || 0}" />
      </div>

    </div>

    <button onclick="updateEquipment()" class="primary-btn">
      Save Changes
    </button>
  `;
}


async function updateEquipment() {

  const equipment = {
    ventilators: document.getElementById("ventilators").value,
    oxygenCylinders: document.getElementById("oxygenCylinders").value,
    ambulances: document.getElementById("ambulances").value,
    monitors: document.getElementById("monitors").value
  };

  await fetch(`${API_BASE}/api/hospital/operations`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + getToken()
    },
    body: JSON.stringify({ equipment })
  });

  loadEquipmentSection();
}
/* =========================================
   LOAD HOSPITAL STATUS
========================================= */
async function loadHospitalStatus() {

  try {

    const res = await fetch(`${API_BASE}/api/hospital/me`, {
      headers: {
        Authorization: "Bearer " + getToken()
      }
    });

    const hospital = await res.json();

    const statusText = document.getElementById("hospitalStatus");

    if (hospital.isOpen) {
      statusText.innerHTML = "🟢 OPEN";
    } else {
      statusText.innerHTML = "🔴 CLOSED";
    }

  } catch (err) {
    console.error("Status load error:", err);
  }
}
async function toggleHospitalStatus() {

  const res = await fetch(`${API_BASE}/api/hospital/operations`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + getToken()
    },
    body: JSON.stringify({ toggleOpen: true })
  });

  const data = await res.json();

  if (!res.ok) {
    alert(data.message || "Update failed");
    return;
  }

  loadHospitalStatus();
}