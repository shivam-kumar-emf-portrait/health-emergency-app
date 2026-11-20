const mongoose = require("mongoose");

const NurseSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true
    },

    department: {
      type: String,
      required: true
    },

    experienceYears: {
      type: Number,
      default: 0
    },

    shift: {
      type: String,
      enum: ["morning", "evening", "night"],
      default: "morning"
    },

    isOnDuty: {
      type: Boolean,
      default: true
    },

    hospital: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Hospital",
      required: true
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Nurse", NurseSchema);
