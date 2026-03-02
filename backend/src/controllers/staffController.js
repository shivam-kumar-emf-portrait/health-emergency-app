const Doctor = require("../models/Doctor");
const Nurse = require("../models/Nurse");
const Shift = require("../models/Shift");
const Attendance = require("../models/Attendance");

/* =========================================
   ASSIGN SHIFT
========================================= */
exports.assignShift = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { staffType, staffId, shiftStart, shiftEnd } = req.body;

    const shift = await Shift.create({
      staffType,
      staffId,
      hospital: req.user._id,
      shiftStart: new Date(shiftStart),
      shiftEnd: new Date(shiftEnd)
    });

    res.json({ message: "Shift assigned successfully", shift });

  } catch (err) {
    console.error("Assign Shift Error:", err);
    res.status(500).json({ message: "Failed to assign shift" });
  }
};

/* =========================================
   CHECK IN
========================================= */
exports.checkIn = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { staffType, staffId } = req.body;

    const today = new Date().toISOString().split("T")[0];

    const attendance = await Attendance.findOneAndUpdate(
      { staffType, staffId, hospital: req.user._id, date: today },
      {
        staffType,
        staffId,
        hospital: req.user._id,
        date: today,
        checkIn: new Date(),
        status: "present"
      },
      { upsert: true, new: true }
    );

    res.json({ message: "Check-in recorded", attendance });

  } catch (err) {
    console.error("Check-in Error:", err);
    res.status(500).json({ message: "Check-in failed" });
  }
};

/* =========================================
   CHECK OUT
========================================= */
exports.checkOut = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const { staffType, staffId } = req.body;
    const today = new Date().toISOString().split("T")[0];

    const attendance = await Attendance.findOneAndUpdate(
      { staffType, staffId, hospital: req.user._id, date: today },
      { checkOut: new Date() },
      { new: true }
    );

    res.json({ message: "Check-out recorded", attendance });

  } catch (err) {
    console.error("Check-out Error:", err);
    res.status(500).json({ message: "Check-out failed" });
  }
};

/* =========================================
   GET ACTIVE STAFF (REAL-TIME)
========================================= */
exports.getActiveStaff = async (req, res) => {
  try {
    if (req.userRole !== "hospital_admin") {
      return res.status(403).json({ message: "Access denied" });
    }

    const now = new Date();
    const today = now.toISOString().split("T")[0];

    const activeShifts = await Shift.find({
      hospital: req.user._id,
      shiftStart: { $lte: now },
      shiftEnd: { $gte: now },
      isActive: true
    });

    const activeStaff = [];

    for (let shift of activeShifts) {
      const attendance = await Attendance.findOne({
        staffType: shift.staffType,
        staffId: shift.staffId,
        hospital: req.user._id,
        date: today,
        status: "present"
      });

      if (attendance) {
        activeStaff.push({
          staffType: shift.staffType,
          staffId: shift.staffId
        });
      }
    }

    res.json({ activeStaff });

  } catch (err) {
    console.error("Active Staff Error:", err);
    res.status(500).json({ message: "Failed to fetch active staff" });
  }
};
