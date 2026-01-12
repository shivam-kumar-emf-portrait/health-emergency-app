const Hospital = require("../models/Hospital");
const matchHospitals = require("../services/emergencyMatcher");

exports.findHospitals = async (req, res) => {
  try {
    const { lat, lng, emergencyType } = req.body;

    if (!lat || !lng || !emergencyType) {
      return res.status(400).json({ message: "Missing data" });
    }

    const hospitals = await Hospital.find({ hasEmergency: true });

    const matched = matchHospitals(
      hospitals,
      { lat, lng },
      emergencyType
    );

    res.json({ hospitals: matched });
  } catch (err) {
    res.status(500).json({ message: "Failed to find hospitals" });
  }
};

exports.notifyHospital = async (req, res) => {
  const { hospitalId, emergencyType, userLocation } = req.body;

  console.log("🚨 EMERGENCY ALERT 🚨");
  console.log("Hospital:", hospitalId);
  console.log("Type:", emergencyType);
  console.log("User Location:", userLocation);

  res.json({
    success: true,
    message: "Hospital notified successfully"
  });
};
