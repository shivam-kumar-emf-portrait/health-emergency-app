const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");

const {
  getHospitalEmergencies,
  toggleHospitalOpenStatus,
  updateEmergencyStatus,
  getPublicHospitalDetails,
  updateHospitalOperations,
  getHospitalProfile   // ✅ NEW
} = require("../controllers/hospitalController");

/* =========================
   PRIVATE ROUTES (ADMIN)
========================= */

// Get logged-in hospital profile
router.get("/me", authMiddleware, getHospitalProfile); // ✅ NEW

// Emergencies
router.get("/emergencies", authMiddleware, getHospitalEmergencies);

router.patch(
  "/emergencies/:id/status",
  authMiddleware,
  updateEmergencyStatus
);

// Update hospital operations
router.patch(
  "/operations",
  authMiddleware,
  updateHospitalOperations
);

router.patch("/toggle-open", authMiddleware, toggleHospitalOpenStatus);


/* =========================
   PUBLIC ROUTES
========================= */

router.get("/public/:id", getPublicHospitalDetails);

module.exports = router;
