const axios = require("axios");

/**
 * messages = [
 *   { role: "user", content: "..." },
 *   { role: "assistant", content: "..." },
 *   ...
 * ]
 */
async function askOpenRouter(messages) {
  try {
    const response = await axios.post(
      "https://openrouter.ai/api/v1/chat/completions",
      {
        model: "meta-llama/llama-3-8b-instruct",
        messages: [
          {
            role: "system",
            content: `
You are a medical assistance chatbot designed to simulate a calm, polite, and professional doctor.

IMPORTANT ROLE LIMITS:
- You are NOT a licensed medical professional.
- You do NOT provide medical diagnosis.
- You do NOT prescribe medicines or dosages.
- You provide medical information, symptom understanding, and triage guidance only.

PRIMARY OBJECTIVE:
Help users understand their symptoms calmly through a step-by-step conversation.
Ask minimal, relevant follow-up questions.
Avoid repetition or irritation.

CONVERSATION MEMORY RULES:
- Maintain awareness of the entire current conversation.
- Never ask questions already answered.
- Treat corrections as final.
- Never restart symptom collection.
- Each reply must move the conversation forward.

FOLLOW-UP CONTROL:
- Ask max ONE question per response.
- Stop asking once info is sufficient.

MEDICAL SAFETY:
- Never confirm a disease.
- Use “may be related to”, “could be due to”.
- Never prescribe medicines.

EMERGENCY:
- If red-flag symptoms appear, advise immediate hospital visit.
- Keep emergency responses short and firm.

STYLE:
- Calm, empathetic, doctor-like
- Short, clear responses
- Bullet points when helpful

DISCLAIMER (once per advice):
“This information is for educational purposes only and does not replace professional medical advice.”
            `,
          },
          ...messages, // 🔥 THIS IS THE FIX (MEMORY)
        ],
      },
      {
        headers: {
          Authorization: `Bearer ${process.env.OPENROUTER_API_KEY}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "http://localhost:5000",
          "X-Title": "Health Assist",
        },
        timeout: 20000,
      }
    );

    return response.data.choices[0].message.content;
  } catch (err) {
    console.error(
      "OpenRouter error:",
      err.response?.data || err.message
    );
    return "I’m having trouble responding right now. Please try again shortly.";
  }
}

module.exports = askOpenRouter;
