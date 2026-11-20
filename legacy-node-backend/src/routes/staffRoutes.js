const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");

const {
  assignShift,
  checkIn,
  checkOut,
  getActiveStaff
} = require("../controllers/staffController");

/* ===============================
   SHIFT MANAGEMENT
=============================== */
router.post("/shift", authMiddleware, assignShift);

/* ===============================
   ATTENDANCE
=============================== */
router.post("/check-in", authMiddleware, checkIn);
router.post("/check-out", authMiddleware, checkOut);

/* ===============================
   ACTIVE STAFF (REAL-TIME)
=============================== */
router.get("/active", authMiddleware, getActiveStaff);

module.exports = router;
