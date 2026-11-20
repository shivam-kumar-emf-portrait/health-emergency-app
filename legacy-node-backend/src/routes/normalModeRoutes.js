const express = require("express");
const authMiddleware = require("../middleware/authMiddleware");
const { chat } = require("../controllers/normalModeController");

const router = express.Router();

router.post("/chat", authMiddleware, chat);

module.exports = router;
