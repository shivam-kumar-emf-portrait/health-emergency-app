const API_URL = "http://localhost:5000/api/auth";
let isSignup = false;

/* ---------- UI helpers ---------- */
function showToast(message) {
  const toast = document.getElementById("toast");
  toast.innerText = message;
  toast.style.display = "block";
  setTimeout(() => toast.style.display = "none", 2500);
}

function setLoading(state) {
  const btn = document.getElementById("submitBtn");
  state ? btn.classList.add("btn-loading") : btn.classList.remove("btn-loading");
}

/* ---------- Toggle ---------- */
function toggleMode() {
  isSignup = !isSignup;

  formTitle.innerText = isSignup ? "Register" : "Login";
  submitBtn.innerText = isSignup ? "Register" : "Login";
  submitBtn.onclick = isSignup ? registerUser : loginUser;

  toggleMsg.innerText = isSignup
    ? "Already have an account?"
    : "Don’t have an account?";

  toggleLink.innerText = isSignup ? "Login" : "Register";

  document.querySelectorAll(".signup-only").forEach(el => {
    el.style.display = isSignup ? "block" : "none";
  });
}

/* ---------- Register ---------- */
async function registerUser() {
  setLoading(true);

  try {
    const res = await fetch(`${API_URL}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: name.value,
        age: age.value,
        knownConditions: conditions.value.split(",").map(v => v.trim()),
        email: email.value,
        password: password.value
      }),
    });

    const result = await res.json();
    setLoading(false);

    if (res.ok) {
      showToast("Registered successfully");
      toggleMode();
    } else {
      showToast(result.message || "Registration failed");
    }
  } catch {
    setLoading(false);
    showToast("Registration failed");
  }
}

/* ---------- Login ---------- */
async function loginUser() {
  setLoading(true);
  const card = document.querySelector(".auth-card");

  try {
    const res = await fetch(`${API_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email.value,
        password: password.value
      }),
    });

    const result = await res.json();
    setLoading(false);

    if (result.token) {
      localStorage.setItem("token", result.token);
      showToast("Login successful");
      setTimeout(() => {
        window.location.href = "dashboard.html";
      }, 900);
    } else {
      card.classList.add("shake");
      showToast(result.message || "Invalid credentials");
      setTimeout(() => card.classList.remove("shake"), 400);
    }
  } catch {
    setLoading(false);
    showToast("Login failed");
  }
}
