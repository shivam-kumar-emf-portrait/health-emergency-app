const express = require("express");
const router = express.Router();

const {
  findHospitals,
  notifyHospital
} = require("../controllers/emergencyController");

router.post("/find-hospitals", findHospitals);
router.post("/notify", notifyHospital);

module.exports = router;
