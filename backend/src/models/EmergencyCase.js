const mongoose = require("mongoose");

const EmergencyCaseSchema = new mongoose.Schema({

  patient: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true,
    index: true
  },

  hospital: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Hospital",
    required: true,
    index: true
  },

  emergencyType: {
    type: String,
    required: true
  },

  severity: {
    type: String,
    enum: ["mild", "moderate", "emergency"],
    required: true
  },

  patientPhone: {
    type: String,
    required: true
  },

  location: {
    lat: Number,
    lng: Number
  },

  assignedDoctor: {
  type: mongoose.Schema.Types.ObjectId,
  ref: "Doctor",
  default: null
},


  status: {
    type: String,
    enum: ["active", "in_progress", "completed"],
    default: "active",
    index: true
  }

}, { timestamps: true });

module.exports = mongoose.model("EmergencyCase", EmergencyCaseSchema);
