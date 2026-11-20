const Nurse = require("../models/Nurse");

/* =========================
   ADD NURSE
========================= */
exports.addNurse = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { name, department, experienceYears, shift } = req.body;

    const nurse = await Nurse.create({
      name,
      department,
      experienceYears,
      shift,
      hospital: req.user._id
    });

    res.json({
      message: "Nurse added successfully",
      nurse
    });

  } catch (err) {
    console.error("Add Nurse Error:", err);
    res.status(500).json({ message: "Failed to add nurse" });
  }
};


/* =========================
   GET HOSPITAL NURSES
========================= */
exports.getHospitalNurses = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const nurses = await Nurse.find({
      hospital: req.user._id
    }).sort({ createdAt: -1 });

    const total = nurses.length;
    const onDuty = nurses.filter(n => n.isOnDuty).length;
    const offDuty = total - onDuty;

    res.json({
      nurses,
      stats: {
        totalNurses: total,
        onDutyNurses: onDuty,
        offDutyNurses: offDuty
      }
    });

  } catch (err) {
    console.error("Get Nurses Error:", err);
    res.status(500).json({ message: "Failed to fetch nurses" });
  }
};


/* =========================
   TOGGLE DUTY
========================= */
exports.toggleNurseDuty = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const nurse = await Nurse.findOne({
      _id: req.params.id,
      hospital: req.user._id
    });

    if (!nurse) {
      return res.status(404).json({ message: "Nurse not found" });
    }

    nurse.isOnDuty = !nurse.isOnDuty;
    await nurse.save();

    res.json({ message: "Nurse duty updated", nurse });

  } catch (err) {
    console.error("Toggle Nurse Error:", err);
    res.status(500).json({ message: "Failed to update nurse" });
  }
};


/* =========================
   DELETE NURSE
========================= */
exports.deleteNurse = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const nurse = await Nurse.findOneAndDelete({
      _id: req.params.id,
      hospital: req.user._id
    });

    if (!nurse) {
      return res.status(404).json({ message: "Nurse not found" });
    }

    res.json({ message: "Nurse removed successfully" });

  } catch (err) {
    console.error("Delete Nurse Error:", err);
    res.status(500).json({ message: "Failed to delete nurse" });
  }
};
