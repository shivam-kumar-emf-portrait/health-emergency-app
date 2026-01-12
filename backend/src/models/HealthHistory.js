const mongoose = require("mongoose");

const healthHistorySchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },
    symptoms: String,
    aiResponse: String,
    severity: String,
    chatSessionId: String,
  },
  { timestamps: true }
);

module.exports = mongoose.model(
  "HealthHistory",
  healthHistorySchema
);
