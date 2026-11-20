const mongoose = require("mongoose");

const DoctorSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true
    },

    specialization: {
      type: String,
      required: true
    },

    qualification: {
      type: String
    },

    experienceYears: {
      type: Number,
      default: 0
    },

    isOnDuty: {
      type: Boolean,
      default: true
    },

    isPublicProfile: {
      type: Boolean,
      default: false
    },

    hospital: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Hospital",
      required: true
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Doctor", DoctorSchema);
