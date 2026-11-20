const User = require("../models/User");
const Hospital = require("../models/Hospital");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

/* =========================
   USER REGISTER
========================= */
exports.register = async (req, res) => {
  try {
    const { name, email, password, age, gender, knownConditions, phone } = req.body;

    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(400).json({ message: "User already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const user = await User.create({
      name,
      email,
      password: hashedPassword,
      age,
      gender,
      knownConditions,
      phone,
      role: "user"
    });

    res.json({ message: "User registered successfully" });

  } catch (err) {
    console.error("User Register Error:", err);
    res.status(500).json({ message: "Registration failed" });
  }
};

/* =========================
   USER LOGIN
========================= */
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;

    const user = await User.findOne({ email });
    if (!user) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    const token = jwt.sign(
      { id: user._id, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    res.json({ token, user });

  } catch (err) {
    console.error("User Login Error:", err);
    res.status(500).json({ message: "Login failed" });
  }
};

/* =========================
   HOSPITAL REGISTER
========================= */
exports.registerHospital = async (req, res) => {
  try {
    const { name, email, password, contactNumber, location } = req.body;

    const existingHospital = await Hospital.findOne({ email });
    if (existingHospital) {
      return res.status(400).json({ message: "Hospital already registered" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const hospital = await Hospital.create({
      name,
      email,
      password: hashedPassword,
      contactNumber,
      location,
      role: "hospital_admin",
      isVerified: false,
      isActive: false
    });

    res.json({
      message: "Hospital registered successfully. Awaiting verification."
    });

  } catch (err) {
    console.error("Hospital Register Error:", err);
    res.status(500).json({ message: "Hospital registration failed" });
  }
};

/* =========================
   HOSPITAL LOGIN
========================= */
exports.loginHospital = async (req, res) => {
  try {
    const { email, password } = req.body;

    const hospital = await Hospital.findOne({ email });

    if (!hospital) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    if (!hospital.isVerified || !hospital.isActive) {
      return res.status(403).json({
        message: "Hospital not verified yet"
      });
    }

    const isMatch = await bcrypt.compare(password, hospital.password);

    if (!isMatch) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    const token = jwt.sign(
      { id: hospital._id, role: hospital.role },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    // Remove password before sending to frontend
    hospital.password = undefined;

    res.json({ token, hospital });

  } catch (err) {
    console.error("Hospital Login Error:", err);
    res.status(500).json({ message: "Login failed" });
  }
};