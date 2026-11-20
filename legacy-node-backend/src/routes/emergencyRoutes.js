const express = require("express");
const router = express.Router();

const {
  findHospitals,
  notifyHospital
} = require("../controllers/emergencyController");

const authMiddleware = require("../middleware/authMiddleware");

// Find hospitals (can stay public if needed)
router.post("/find-hospitals", findHospitals);

// Protect notify route
router.post("/notify", authMiddleware, notifyHospital);

module.exports = router;
