const BASE_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:5000"
    : "https://health-emergency-backend.onrender.com";

let isLoginMode = true;

const urlParams = new URLSearchParams(window.location.search);
const role = urlParams.get("role") || "user";

window.onload = function () {
  const title = document.getElementById("formTitle");

  if (role === "hospital") {
    title.innerText = "Hospital Login";
  } else {
    title.innerText = "User Login";
  }
};

/* =========================
   TOGGLE LOGIN / REGISTER
========================= */
function toggleMode() {

  isLoginMode = !isLoginMode;

  const title = document.getElementById("formTitle");
  const btn = document.getElementById("submitBtn");
  const toggleMsg = document.getElementById("toggleMsg");
  const toggleLink = document.getElementById("toggleLink");

  const userFields = document.querySelectorAll(".signup-only");
  const hospitalFields = document.querySelectorAll(".hospital-only");

  if (isLoginMode) {

    title.innerText = role === "hospital" ? "Hospital Login" : "User Login";
    btn.innerText = "Login";

    toggleMsg.innerText = "Don’t have an account?";
    toggleLink.innerText = "Register";

    userFields.forEach(el => el.style.display = "none");
    hospitalFields.forEach(el => el.style.display = "none");

  } else {

    title.innerText = role === "hospital" ? "Hospital Register" : "User Register";
    btn.innerText = "Register";

    toggleMsg.innerText = "Already have an account?";
    toggleLink.innerText = "Login";

    if (role === "hospital") {
      hospitalFields.forEach(el => el.style.display = "block");
      userFields.forEach(el => el.style.display = "none");
    } else {
      userFields.forEach(el => el.style.display = "block");
      hospitalFields.forEach(el => el.style.display = "none");
    }
  }
}

/* =========================
   GET CURRENT LOCATION
========================= */
function getCurrentLocation() {

  if (!navigator.geolocation) {
    alert("Geolocation not supported");
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {

      const lat = position.coords.latitude;
      const lng = position.coords.longitude;

      document.getElementById("lat").value = lat.toFixed(6);
      document.getElementById("lng").value = lng.toFixed(6);

      alert("Location detected successfully ✅");

    },
    () => {
      alert("Location permission denied");
    }
  );
}

/* =========================
   HANDLE LOGIN / REGISTER
========================= */
async function handleAuth() {

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();

  try {

    let endpoint = "";
    let bodyData = {};

    /* LOGIN */
    if (isLoginMode) {

      endpoint = role === "hospital"
        ? "/api/auth/hospital/login"
        : "/api/auth/login";

      bodyData = { email, password };

    }

    /* REGISTER */
    else {

      endpoint = role === "hospital"
        ? "/api/auth/hospital/register"
        : "/api/auth/register";

      if (role === "hospital") {

        const lat = parseFloat(document.getElementById("lat").value);
        const lng = parseFloat(document.getElementById("lng").value);

        bodyData = {
          name: document.getElementById("hospitalName").value,
          email,
          password,
          contactNumber: document.getElementById("contactNumber").value,
          location: { lat, lng }
        };

      } else {

        bodyData = {
          name: document.getElementById("name").value,
          age: document.getElementById("age").value,
          knownConditions: document.getElementById("conditions").value,
          email,
          password
        };
      }
    }

    console.log("REQUEST:", endpoint, bodyData);

    const res = await fetch(`${BASE_URL}${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bodyData)
    });

    const data = await res.json();

    console.log("RESPONSE:", data);

    if (!res.ok) {
      alert(data.message || "Operation failed");
      return;
    }

    /* LOGIN SUCCESS */
    if (isLoginMode) {

      if (role === "hospital") {

        localStorage.setItem("hospitalToken", data.token);
        console.log("Hospital token stored");

        window.location.href = "hospital-dashboard.html";

      } else {

        localStorage.setItem("token", data.token);
        console.log("User token stored");

        window.location.href = "dashboard.html";
      }

    }

    /* REGISTER SUCCESS */
    else {

      alert("Registration successful. Please login.");
      toggleMode();

    }

  } catch (err) {

    console.error("AUTH ERROR:", err);
    alert("Something went wrong");

  }
}