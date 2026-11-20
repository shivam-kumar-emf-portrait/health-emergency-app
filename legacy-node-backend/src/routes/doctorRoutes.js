const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");

const {
  addDoctor,
  getHospitalDoctors,
  toggleDoctorDuty,
  deleteDoctor
} = require("../controllers/doctorController");

router.post("/", authMiddleware, addDoctor);
router.get("/", authMiddleware, getHospitalDoctors);
router.patch("/:id/toggle-duty", authMiddleware, toggleDoctorDuty);
router.delete("/:id", authMiddleware, deleteDoctor);

module.exports = router;
