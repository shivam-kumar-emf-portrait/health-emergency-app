function showEmergency() {
  document.getElementById("normalMode").style.display = "none";
  document.getElementById("emergencyMode").style.display = "block";
}

function selectEmergency(type) {
  alert("Emergency selected: " + type);
}
