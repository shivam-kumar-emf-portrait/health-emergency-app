const mongoose = require("mongoose");

const HospitalSchema = new mongoose.Schema({
  name: String,

  location: {
    lat: Number,
    lng: Number,
  },

  hasEmergency: { type: Boolean, default: true },
  hasICU: Boolean,
  icuBedsAvailable: Number,

  technologies: [String],
  bloodAvailable: [String],

  contactNumber: String
}, { timestamps: true });

module.exports = mongoose.model("Hospital", HospitalSchema);
