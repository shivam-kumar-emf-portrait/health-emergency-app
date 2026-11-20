const mongoose = require("mongoose");

const userSchema = new mongoose.Schema(
  {
    name: String,

    email: { 
      type: String, 
      unique: true 
    },

    password: String,

    phone: {
      type: String
    },

    age: Number,

    gender: String,

    knownConditions: [String],

    role: {
      type: String,
      enum: ["user", "hospital_admin", "super_admin"],
      default: "user"
    }

  },
  { timestamps: true }
);

module.exports = mongoose.model("User", userSchema);
