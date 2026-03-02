const BASE_URL = "http://localhost:5000";

let userLocation = null;
let selectedEmergency = null;

/* =========================================
   SELECT EMERGENCY
========================================= */
function selectEmergency(type) {

  selectedEmergency = type;

  if (!navigator.geolocation) {
    alert("Geolocation not supported");
    return;
  }

  navigator.geolocation.getCurrentPosition(
    pos => {
      userLocation = {
        lat: pos.coords.latitude,
        lng: pos.coords.longitude
      };
      fetchHospitals();
    },
    () => alert("Location permission is required")
  );
}

/* =========================================
   FETCH HOSPITALS
========================================= */
async function fetchHospitals() {

  const container = document.getElementById("hospitalResults");

  /* Loading UI */
  container.innerHTML = `
    <div class="loading-box">
      <div class="loader"></div>
      <p>Finding nearby hospitals...</p>
      <span>Analyzing availability and distance</span>
    </div>
  `;

  const res = await fetch(
    `${BASE_URL}/api/emergency/find-hospitals`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        lat: userLocation.lat,
        lng: userLocation.lng,
        emergencyType: selectedEmergency
      })
    }
  );

  const data = await res.json();

  renderHospitals(data.hospitals);
}

/* =========================================
   RENDER HOSPITALS (BEST + NEAREST + MORE)
========================================= */
function renderHospitals(hospitals) {

  const container = document.getElementById("hospitalResults");

  if (!hospitals || hospitals.length === 0) {
    container.innerHTML = "<p>No hospitals found.</p>";
    return;
  }

  hospitals.sort((a, b) => a.distance - b.distance);

  const bestMatch = hospitals[0];
  const nearest = hospitals.slice(1, 4);
  const remaining = hospitals.slice(4);

  container.innerHTML = `
    <h2 class="section-title">Best Match</h2>
    <div class="best-wrapper">
      ${renderHospitalCard(bestMatch, true)}
    </div>

    <h2 class="section-title">Nearest Hospitals</h2>
    <div class="nearest-wrapper">
      ${nearest.map(h => renderHospitalCard(h)).join("")}
    </div>
  `;

  if (remaining.length > 0) {
    container.innerHTML += `
      <div id="moreHospitals" style="display:none;">
        ${remaining.map(h => renderHospitalCard(h)).join("")}
      </div>

      <button class="show-more-btn" onclick="toggleMore()">
        Show More Hospitals
      </button>
    `;
  }
}

/* =========================================
   HOSPITAL CARD
========================================= */
function renderHospitalCard(h, highlight = false) {

  return `
    <div class="hospital-card ${highlight ? "best-card" : ""}"
         onclick="openHospitalModal('${h._id}')">

      <h3>${h.name}</h3>

      <div class="hospital-meta">
        <span>📍 ${h.distance} km</span>
        <span>${h.isOpen ? "Open Now" : "Closed"}</span>
      </div>

      <div class="hospital-badge">
        ${highlight ? "Recommended" : "Nearby"}
      </div>

    </div>
  `;
}

/* =========================================
   SHOW MORE TOGGLE
========================================= */
function toggleMore() {

  const more = document.getElementById("moreHospitals");
  const btn = document.querySelector(".show-more-btn");

  if (more.style.display === "none") {
    more.style.display = "block";
    btn.innerText = "Show Less";
  } else {
    more.style.display = "none";
    btn.innerText = "Show More Hospitals";
  }
}

/* =========================================
   OPEN MODAL WITH DETAILS
========================================= */
async function openHospitalModal(id) {

  const res = await fetch(`${BASE_URL}/api/hospital/public/${id}`);
  const data = await res.json();

  const hospital = data.hospital;
  const doctors = data.publicDoctors || [];

  const modal = document.getElementById("hospitalModal");
  const modalBody = document.getElementById("modalBody");

  modalBody.innerHTML = `
    <h2>${hospital.name}</h2>

    <div class="modal-grid">

      <div>
        <h4>🩺 Available Doctors</h4>
        ${
          doctors.length
            ? doctors.map(d => `<p>${d.name} (${d.specialization})</p>`).join("")
            : "<p>No public doctors listed</p>"
        }
      </div>

      <div>
        <h4>🛏 Beds & ICU</h4>
        <p>Total Beds: ${hospital.totalBeds || 0}</p>
        <p>Available Beds: ${hospital.availableBeds || 0}</p>
        <p>ICU Available: ${hospital.availableICUBeds || 0}</p>
      </div>

     <div>
  <h4>🩸 Blood Inventory</h4>
  ${
    hospital.bloodInventory
      ? Object.entries(hospital.bloodInventory)
          .map(([type, qty]) => `<p>${type}: ${qty}</p>`)
          .join("")
      : "<p>Not Updated</p>"
  }
</div>

<div>
  <h4>🫁 Equipment</h4>
  <p>Ventilators: ${hospital.equipment?.ventilators || 0}</p>
  <p>Oxygen Cylinders: ${hospital.equipment?.oxygenCylinders || 0}</p>
  <p>Ambulances: ${hospital.equipment?.ambulances || 0}</p>
  <p>Monitors: ${hospital.equipment?.monitors || 0}</p>
</div>

      <div>
        <h4>📞 Contact</h4>
        <p>${hospital.contactNumber || "Not Provided"}</p>
      </div>

    </div>

    <div class="modal-actions">
      <button onclick="notifyHospital('${hospital._id}')">
        🚨 Alert Hospital
      </button>
      <button onclick="navigateTo(${hospital.location.lat}, ${hospital.location.lng})">
        🧭 Navigate
      </button>
    </div>
  `;

  modal.style.display = "flex";
}

/* =========================================
   CLOSE MODAL
========================================= */
function closeModal() {
  document.getElementById("hospitalModal").style.display = "none";
}

/* =========================================
   NOTIFY HOSPITAL
========================================= */
async function notifyHospital(hospitalId) {

  try {

    const token = localStorage.getItem("token");

    if (!token) {
      alert("Please login first.");
      return;
    }

    const res = await fetch(
      `${BASE_URL}/api/emergency/notify`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
          hospitalId,
          emergencyType: selectedEmergency,
          severity: "emergency",
          userLocation
        })
      }
    );

    const data = await res.json();

    if (!res.ok) {
      alert(data.message || "Failed to alert hospital");
      return;
    }

    alert("🚨 Hospital alerted successfully");

  } catch (err) {
    console.error("Notify error:", err);
    alert("Something went wrong");
  }
}

/* =========================================
   NAVIGATE
========================================= */
function navigateTo(lat, lng) {

  window.open(
    `https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${userLocation.lat},${userLocation.lng};${lat},${lng}`,
    "_blank"
  );
}

/* =========================================
   GO TO NORMAL MODE
========================================= */
function goToNormalMode() {
  window.location.replace("dashboard.html");
}

