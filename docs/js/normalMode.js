const chatBox = document.getElementById("chatBox");
const input = document.getElementById("symptoms");

/* Send on Enter (Shift+Enter for new line) */
input.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    sendSymptoms();
  }
});

function appendMessage(role, text) {
  const msg = document.createElement("div");
  msg.className = "chat-msg " + role;
  msg.innerHTML = `<strong>${role === "user" ? "You" : "AI"}:</strong> ${text}`;
  chatBox.appendChild(msg);
  chatBox.scrollTop = chatBox.scrollHeight;
}

function typeAIResponse(text) {
  const msg = document.createElement("div");
  msg.className = "chat-msg ai";
  msg.innerHTML = `<strong>AI:</strong> <span></span>`;
  chatBox.appendChild(msg);

  const span = msg.querySelector("span");
  let i = 0;

  const interval = setInterval(() => {
    span.innerHTML += text[i] || "";
    chatBox.scrollTop = chatBox.scrollHeight;
    i++;
    if (i >= text.length) clearInterval(interval);
  }, 18);
}

/* ---------------- MAIN SEND FUNCTION ---------------- */
async function sendSymptoms() {
  if (!input.value.trim()) return;

  const userText = input.value.trim();
  appendMessage("user", userText);
  input.value = "";

  const token = localStorage.getItem("token");
  if (!token) {
    typeAIResponse("Please login again to continue.");
    return;
  }

  try {
    const res = await fetch(
      "https://health-emergency-backend.onrender.com/api/normal/chat",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          symptoms: userText,
          chatSessionId: "default",
        }),
      }
    );

    if (!res.ok) {
      throw new Error("Server not responding");
    }

    const data = await res.json();

    if (data && data.aiResponse) {
      typeAIResponse(data.aiResponse.replace(/\n/g, " "));
    } else {
      typeAIResponse(
        "I couldn’t understand clearly. Could you describe your symptoms again?"
      );
    }

    /* Inline emergency hint */
    if (data?.severity === "EMERGENCY") {
      const warn = document.createElement("div");
      warn.className = "chat-warning";
      warn.innerText =
        "⚠️ This may be serious. Please switch to Emergency Mode.";
      chatBox.appendChild(warn);
      chatBox.scrollTop = chatBox.scrollHeight;
    }

  } catch (err) {
    console.error(err);
    typeAIResponse(
      "I’m having trouble connecting right now. Please try again in a moment."
    );
  }
}

/* New chat */
function newChat() {
  chatBox.innerHTML = "";
}
