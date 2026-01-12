let userLocation = null;
let selectedEmergency = null;

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

async function fetchHospitals() {
  const res = await fetch("http://localhost:5000/api/emergency/find-hospitals", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      lat: userLocation.lat,
      lng: userLocation.lng,
      emergencyType: selectedEmergency
    })
  });

  const data = await res.json();
  renderHospitals(data.hospitals);
}

function renderHospitals(hospitals) {
  const container = document.getElementById("hospitalResults");

  if (!hospitals || hospitals.length === 0) {
    container.innerHTML = "<p>No hospitals found.</p>";
    return;
  }

  container.innerHTML = "<h2>Best Matched Hospitals</h2>";

  hospitals.forEach(h => {
    container.innerHTML += `
      <div class="hospital-card">
        <h3>${h.name}</h3>
        <p>📍 Distance: ${h.distance} km</p>
        <p>🛏 ICU: ${h.hasICU ? "Available" : "Not Available"}</p>
        <p>🩸 Blood: ${h.bloodAvailable.join(", ") || "N/A"}</p>

        <div class="actions">
          <button onclick="notifyHospital('${h._id}')">🚨 Alert Hospital</button>
          <button onclick="navigateTo(${h.location.lat}, ${h.location.lng})">
            🧭 Navigate
          </button>
        </div>
      </div>
    `;
  });
}

async function notifyHospital(hospitalId) {
  await fetch("http://localhost:5000/api/emergency/notify", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      hospitalId,
      emergencyType: selectedEmergency,
      userLocation
    })
  });

  alert("🚨 Hospital alerted successfully");
}

function navigateTo(lat, lng) {
  window.open(
    `https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${userLocation.lat},${userLocation.lng};${lat},${lng}`,
    "_blank"
  );
}
