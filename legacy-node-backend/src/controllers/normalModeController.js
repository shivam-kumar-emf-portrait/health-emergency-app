const askOpenRouter = require("../services/openRouterService");
const ChatSession = require("../models/ChatSession");

exports.chat = async (req, res) => {
  try {
    const { symptoms } = req.body;

    if (!symptoms) {
      return res.status(400).json({
        aiResponse: "Please describe your symptoms.",
        severity: "NORMAL",
      });
    }

    // ✅ user from authMiddleware
    const user = req.user;

    // ✅ Load or create chat session
    let chat = await ChatSession.findOne({ user: user._id });

    if (!chat) {
      chat = await ChatSession.create({
        user: user._id,
        messages: [],
      });
    }

    // ✅ Save user message
    chat.messages.push({
      role: "user",
      content: symptoms,
    });

    // 🔥 IMPORTANT FIX:
    // Send FULL conversation history to OpenRouter
    const aiResponse = await askOpenRouter(chat.messages);

    // ✅ Save AI reply
    chat.messages.push({
      role: "assistant",
      content: aiResponse,
    });

    await chat.save();

    res.json({
      aiResponse,
      severity: "NORMAL",
    });

  } catch (err) {
    console.error("Normal chat error:", err);
    res.status(500).json({
      aiResponse:
        "I'm having trouble connecting right now. Please try again in a moment.",
      severity: "NORMAL",
    });
  }
};
