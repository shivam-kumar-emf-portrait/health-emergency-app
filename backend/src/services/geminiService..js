const axios = require("axios");
const ChatSession = require("../models/ChatSession");

async function askGemini({ user, symptoms }) {
  if (!user || !user._id) {
    throw new Error("User not found in request");
  }

  let chat = await ChatSession.findOne({ user: user._id });

  if (!chat) {
    chat = await ChatSession.create({
      user: user._id,
      messages: []
    });
  }

  chat.messages.push({
    role: "user",
    content: symptoms
  });

  const conversation = chat.messages
    .map(m => `${m.role === "user" ? "User" : "AI"}: ${m.content}`)
    .join("\n");

  const prompt = `
You are a calm, empathetic medical assistant.

Rules:
- Do NOT diagnose
- Do NOT prescribe medicine
- Ask 3–4 natural follow-up questions
- Be human and reassuring

User profile:
Age: ${user.age}
Known conditions: ${user.knownConditions.join(", ") || "None"}

Conversation:
${conversation}
`;

  try {
    const response = await axios.post(
      "https://openrouter.ai/api/v1/chat/completions",
      {
        model: "meta-llama/llama-3-8b-instruct",
        messages: [{ role: "user", content: prompt }]
      },
      {
        headers: {
          Authorization: `Bearer ${process.env.OPENROUTER_API_KEY}`,
          "Content-Type": "application/json"
        }
      }
    );

    const aiReply =
      response.data?.choices?.[0]?.message?.content ||
      "Can you tell me more about what you're feeling?";

    chat.messages.push({
      role: "assistant",
      content: aiReply
    });

    await chat.save();

    return aiReply;
  } catch (err) {
    console.error("OpenRouter error:", err.message);
    return "I’m having trouble responding right now.";
  }
}

module.exports = askGemini;
