const express = require("express");
const {
  register,
  login,
  registerHospital,
  loginHospital
} = require("../controllers/authController");

const router = express.Router();

/* User Routes */
router.post("/register", register);
router.post("/login", login);

/* Hospital Routes */
router.post("/hospital/register", registerHospital);
router.post("/hospital/login", loginHospital);

module.exports = router;
