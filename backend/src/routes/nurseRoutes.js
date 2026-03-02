const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");

const {
  addNurse,
  getHospitalNurses,
  toggleNurseDuty,
  deleteNurse
} = require("../controllers/nurseController");

router.post("/", authMiddleware, addNurse);
router.get("/", authMiddleware, getHospitalNurses);
router.patch("/:id/toggle-duty", authMiddleware, toggleNurseDuty);
router.delete("/:id", authMiddleware, deleteNurse);

module.exports = router;
