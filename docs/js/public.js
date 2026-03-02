function goLogin(role) {
  window.location.href = `login.html?role=${role}`;
}

function scrollToFeatures() {
  document.getElementById("features").scrollIntoView({
    behavior: "smooth"
  });
}
/* ================= CURSOR GLOW MOVE ================= */

const glow = document.querySelector(".cursor-glow");

document.addEventListener("mousemove", (e) => {
  glow.style.left = e.clientX + "px";
  glow.style.top = e.clientY + "px";
});

/* ================= SCROLL REVEAL ================= */

const sections = document.querySelectorAll(".section, .final");

const observer = new IntersectionObserver(entries => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add("visible");
    }
  });
}, { threshold: 0.2 });

sections.forEach(section => observer.observe(section));