const EmergencyCase = require("../models/EmergencyCase");
const Doctor = require("../models/Doctor");
const Hospital = require("../models/Hospital");

/* =========================
   GET LOGGED IN HOSPITAL PROFILE
========================= */
exports.getHospitalProfile = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const hospital = await Hospital.findById(req.user._id);

    if (!hospital) {
      return res.status(404).json({ message: "Hospital not found" });
    }

    res.json(hospital);

  } catch (err) {
    console.error("Get Hospital Profile Error:", err);
    res.status(500).json({ message: "Failed to fetch hospital profile" });
  }
};
  

/* =========================
   GET ALL EMERGENCIES
========================= */
exports.getHospitalEmergencies = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const emergencies = await EmergencyCase.find({
      hospital: req.user._id
    })
      .populate("patient", "name phone")
      .populate("assignedDoctor", "name specialization")
      .sort({ createdAt: -1 });

    res.json({ emergencies });

  } catch (err) {
    console.error("Get Hospital Emergencies Error:", err);
    res.status(500).json({ message: "Failed to fetch emergencies" });
  }
};


/* =========================
   UPDATE EMERGENCY STATUS
========================= */
exports.updateEmergencyStatus = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { status } = req.body;
    const { id } = req.params;

    const allowedStatuses = ["in_progress", "completed"];

    if (!allowedStatuses.includes(status)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    const emergency = await EmergencyCase.findOne({
      _id: id,
      hospital: req.user._id
    });

    if (!emergency) {
      return res.status(404).json({ message: "Emergency not found" });
    }

    /* ======================
       ASSIGN DOCTOR WHEN STARTED
    ======================= */
    if (status === "in_progress") {

      if (!emergency.assignedDoctor) {

        const availableDoctor = await Doctor.findOne({
          hospital: req.user._id,
          isOnDuty: true
        });

        if (!availableDoctor) {
          return res.status(400).json({
            message: "No doctors available"
          });
        }

        emergency.assignedDoctor = availableDoctor._id;

        availableDoctor.isOnDuty = false;
        await availableDoctor.save();
      }
    }

    /* ======================
       RELEASE DOCTOR WHEN COMPLETED
    ======================= */
    if (status === "completed" && emergency.assignedDoctor) {

      const doctor = await Doctor.findById(emergency.assignedDoctor);

      if (doctor) {
        doctor.isOnDuty = true;
        await doctor.save();
      }
    }

    emergency.status = status;
    await emergency.save();

    res.json({
      message: "Status updated successfully",
      emergency
    });

  } catch (err) {
    console.error("Update Emergency Status Error:", err);
    res.status(500).json({ message: "Failed to update status" });
  }
};


/* =========================
   UPDATE HOSPITAL OPERATIONS
========================= */
exports.updateHospitalOperations = async (req, res) => {
  try {

    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const hospital = await Hospital.findById(req.user._id);

    if (!hospital) {
      return res.status(404).json({ message: "Hospital not found" });
    }

    /* ===== TOGGLE OPEN / CLOSE ===== */
    if (req.body.toggleOpen) {
      hospital.isOpen = !hospital.isOpen;
    }

    /* ===== BEDS ===== */
    if (req.body.totalBeds !== undefined) {
      hospital.totalBeds = req.body.totalBeds;
    }

    if (req.body.availableBeds !== undefined) {
      hospital.availableBeds = req.body.availableBeds;
    }

    if (req.body.totalICUBeds !== undefined) {
      hospital.totalICUBeds = req.body.totalICUBeds;
    }

    if (req.body.availableICUBeds !== undefined) {
      hospital.availableICUBeds = req.body.availableICUBeds;
    }

    /* ===== BLOOD INVENTORY ===== */
    if (req.body.bloodInventory) {
      hospital.bloodInventory = req.body.bloodInventory;
    }

    /* ===== EQUIPMENT ===== */
    if (req.body.equipment) {
      hospital.equipment = req.body.equipment;
    }

    await hospital.save();

    res.json({
      message: "Hospital operations updated successfully",
      hospital
    });

  } catch (err) {
    console.error("Update Hospital Operations Error:", err);
    res.status(500).json({ message: "Failed to update hospital operations" });
  }
};


/* =========================
   PUBLIC HOSPITAL DETAILS
========================= */
exports.getPublicHospitalDetails = async (req, res) => {
  try {
    const { id } = req.params;

    const hospital = await Hospital.findById(id);
    if (!hospital) {
      return res.status(404).json({ message: "Hospital not found" });
    }

    const publicDoctors = await Doctor.find({
      hospital: id,
      isPublicProfile: true
    });

    const specializationCounts = await Doctor.aggregate([
      { $match: { hospital: hospital._id } },
      {
        $group: {
          _id: "$specialization",
          count: { $sum: 1 }
        }
      }
    ]);

    res.json({
      hospital,
      publicDoctors,
      specializationCounts
    });

  } catch (err) {
    console.error("Public Hospital Details Error:", err);
    res.status(500).json({ message: "Failed to fetch hospital details" });
  }
};
exports.toggleHospitalOpenStatus = async (req, res) => {
  try {

    const hospital = await Hospital.findById(req.user.id);

    hospital.isOpen = !hospital.isOpen;

    await hospital.save();

    res.json({
      message: "Hospital status updated",
      isOpen: hospital.isOpen
    });

  } catch (err) {
    console.error("Toggle hospital status error:", err);
    res.status(500).json({ message: "Failed to update status" });
  }
};