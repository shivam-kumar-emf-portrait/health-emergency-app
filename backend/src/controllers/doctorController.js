const Doctor = require("../models/Doctor");

/* =========================
   ADD DOCTOR
========================= */
exports.addDoctor = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { name, specialization, qualification, experienceYears, isPublicProfile } = req.body;

    const doctor = await Doctor.create({
      name,
      specialization,
      qualification,
      experienceYears,
      isPublicProfile,
      hospital: req.user._id
    });

    res.json({
      message: "Doctor added successfully",
      doctor
    });

  } catch (err) {
    console.error("Add Doctor Error:", err);
    res.status(500).json({ message: "Failed to add doctor" });
  }
};

/* =========================
   GET HOSPITAL DOCTORS
========================= */
/* =========================
   GET HOSPITAL DOCTORS (UPGRADED)
========================= */
exports.getHospitalDoctors = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const doctors = await Doctor.find({
      hospital: req.user._id
    }).sort({ createdAt: -1 });

    // ===== STATS =====
    const totalDoctors = doctors.length;
    const onDutyDoctors = doctors.filter(d => d.isOnDuty).length;
    const offDutyDoctors = totalDoctors - onDutyDoctors;

    // ===== SPECIALIZATION BREAKDOWN =====
    const specializationStats = {};

    doctors.forEach(doc => {
      const spec = doc.specialization || "General";

      if (!specializationStats[spec]) {
        specializationStats[spec] = {
          total: 0,
          onDuty: 0
        };
      }

      specializationStats[spec].total++;

      if (doc.isOnDuty) {
        specializationStats[spec].onDuty++;
      }
    });

    res.json({
      doctors,
      stats: {
        totalDoctors,
        onDutyDoctors,
        offDutyDoctors
      },
      specializationStats
    });

  } catch (err) {
    console.error("Get Doctors Error:", err);
    res.status(500).json({ message: "Failed to fetch doctors" });
  }
};


/* =========================
   TOGGLE DUTY STATUS
========================= */
exports.toggleDoctorDuty = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { id } = req.params;

    const doctor = await Doctor.findOne({
      _id: id,
      hospital: req.user._id
    });

    if (!doctor) {
      return res.status(404).json({ message: "Doctor not found" });
    }

    doctor.isOnDuty = !doctor.isOnDuty;
    await doctor.save();

    res.json({
      message: "Doctor duty status updated",
      doctor
    });

  } catch (err) {
    console.error("Toggle Duty Error:", err);
    res.status(500).json({ message: "Failed to update duty" });
  }
};

/* =========================
   DELETE DOCTOR
========================= */
exports.deleteDoctor = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { id } = req.params;

    const doctor = await Doctor.findOneAndDelete({
      _id: id,
      hospital: req.user._id
    });

    if (!doctor) {
      return res.status(404).json({ message: "Doctor not found" });
    }

    res.json({ message: "Doctor deleted successfully" });

  } catch (err) {
    console.error("Delete Doctor Error:", err);
    res.status(500).json({ message: "Failed to delete doctor" });
  }
};
