const Hospital = require("../models/Hospital");
const EmergencyCase = require("../models/EmergencyCase");
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
    console.error("Find Hospitals Error:", err);
    res.status(500).json({ message: "Failed to find hospitals" });
  }
};

exports.notifyHospital = async (req, res) => {
  try {
    const { hospitalId, emergencyType, userLocation, severity } = req.body;

    if (!hospitalId || !emergencyType || !userLocation || !severity) {
      return res.status(400).json({ message: "Missing required data" });
    }

    const newCase = await EmergencyCase.create({
      patient: req.user.id,
      hospital: hospitalId,
      emergencyType,
      severity,
      patientPhone: req.user.phone || "Not Provided",
      location: {
        lat: userLocation.lat,
        lng: userLocation.lng
      },
      status: "active"
    });

    console.log("🚨 EMERGENCY ALERT STORED 🚨");
    console.log("Case ID:", newCase._id);

    res.json({
      success: true,
      message: "Hospital notified successfully",
      caseId: newCase._id
    });

  } catch (err) {
    console.error("Notify Hospital Error:", err);
    res.status(500).json({ message: "Failed to notify hospital" });
  }
};
