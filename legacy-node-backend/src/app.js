require("dotenv").config();
const express = require("express");
const cors = require("cors");

const connectDB = require("./config/db");

// Routes
const authRoutes = require("./routes/authRoutes");
const normalModeRoutes = require("./routes/normalModeRoutes");
const emergencyRoutes = require("./routes/emergencyRoutes");
const userRoutes = require("./routes/userRoutes");
const hospitalRoutes = require("./routes/hospitalRoutes");
const doctorRoutes = require("./routes/doctorRoutes");
const staffRoutes = require("./routes/staffRoutes"); // ✅ NEW
const nurseRoutes = require("./routes/nurseRoutes");

const app = express();


app.use((req, res, next) => {
  res.set(
    "Cache-Control",
    "no-store, no-cache, must-revalidate, private"
  );
  next();
});

// Middleware
app.use(cors());
app.use(express.json());

// Connect DB
connectDB();

// ===============================
// ROUTES
// ===============================

app.use("/api/auth", authRoutes);
app.use("/api/normal", normalModeRoutes);
app.use("/api/emergency", emergencyRoutes);
app.use("/api/user", userRoutes);

app.use("/api/hospital", hospitalRoutes);
app.use("/api/hospital/doctors", doctorRoutes);
app.use("/api/hospital/staff", staffRoutes); // ✅ NEW
app.use("/api/hospital/nurses", nurseRoutes);

// Test route
app.get("/", (req, res) => {
  res.send("Health Emergency Backend Running");
});

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
