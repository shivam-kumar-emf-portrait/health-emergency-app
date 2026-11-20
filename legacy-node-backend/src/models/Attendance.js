const mongoose = require("mongoose");

const AttendanceSchema = new mongoose.Schema(
  {
    staffType: {
      type: String,
      enum: ["Doctor", "Nurse"],
      required: true
    },

    staffId: {
      type: mongoose.Schema.Types.ObjectId,
      required: true
    },

    hospital: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Hospital",
      required: true
    },

    date: {
      type: String, // YYYY-MM-DD format
      required: true
    },

    checkIn: {
      type: Date
    },

    checkOut: {
      type: Date
    },

    status: {
      type: String,
      enum: ["present", "absent"],
      default: "absent"
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Attendance", AttendanceSchema);
