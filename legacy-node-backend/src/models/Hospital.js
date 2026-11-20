const mongoose = require("mongoose");

const HospitalSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true
    },

    email: {
      type: String,
      unique: true,
      sparse: true
    },

    password: {
      type: String
    },

    role: {
      type: String,
      enum: ["hospital_admin"],
      default: "hospital_admin"
    },

    isVerified: {
      type: Boolean,
      default: false
    },

    isActive: {
      type: Boolean,
      default: false
    },

    /* =========================
       📍 LOCATION
    ========================== */
    location: {
      lat: Number,
      lng: Number
    },

    hasEmergency: {
      type: Boolean,
      default: true
    },

    /* =========================
       🛏 BED MANAGEMENT
    ========================== */

    totalBeds: {
      type: Number,
      default: 0
    },

    availableBeds: {
      type: Number,
      default: 0
    },

    totalICUBeds: {
      type: Number,
      default: 0
    },

    availableICUBeds: {
      type: Number,
      default: 0
    },

    /* =========================
       🫁 EQUIPMENT
    ========================== */

    equipment: {
      ventilators: { type: Number, default: 0 },
      oxygenCylinders: { type: Number, default: 0 },
      ambulances: { type: Number, default: 0 },
      monitors: { type: Number, default: 0 }
    },

    /* =========================
       🩸 BLOOD BANK
    ========================== */

    bloodInventory: {
      "O+": { type: Number, default: 0 },
      "O-": { type: Number, default: 0 },
      "A+": { type: Number, default: 0 },
      "A-": { type: Number, default: 0 },
      "B+": { type: Number, default: 0 },
      "B-": { type: Number, default: 0 },
      "AB+": { type: Number, default: 0 },
      "AB-": { type: Number, default: 0 }
    },

    /* =========================
       🕒 OPERATIONAL STATUS
    ========================== */

    isOpen: {
      type: Boolean,
      default: true
    },

    openingTime: {
      type: String
    },

    closingTime: {
      type: String
    },

    technologies: [String],
    contactNumber: String
  },
  { timestamps: true }
);

module.exports = mongoose.model("Hospital", HospitalSchema);
