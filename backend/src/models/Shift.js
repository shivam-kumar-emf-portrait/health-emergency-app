const mongoose = require("mongoose");

const ShiftSchema = new mongoose.Schema(
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

    shiftStart: {
      type: Date,
      required: true
    },

    shiftEnd: {
      type: Date,
      required: true
    },

    isActive: {
      type: Boolean,
      default: true
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Shift", ShiftSchema);
