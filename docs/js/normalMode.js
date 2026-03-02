const BASE_URL = "http://localhost:5000";

/* ================= USER-SPECIFIC STORAGE ================= */

function getUserId() {
  const token = localStorage.getItem("token");
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.id;
  } catch {
    return null;
  }
}

function getStorageKey() {
  const userId = getUserId();
  return userId ? `ha_chats_${userId}` : "ha_chats_guest";
}

let chats = JSON.parse(localStorage.getItem(getStorageKey())) || [];
let activeChatId = null;

/* ================= INIT ================= */
window.onload = function () {

  if (chats.length === 0) {
    createNewChat();
  } else {
    activeChatId = chats[0].id;
  }

  renderSidebar();
  renderActiveChat();

  const textarea = document.getElementById("symptoms");
  textarea.addEventListener("keydown", function (e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendSymptoms();
    }
  });
};

/* ================= CREATE NEW CHAT ================= */
function createNewChat() {
  const newChat = {
    id: Date.now(),
    title: "New Chat",
    messages: []
  };

  chats.unshift(newChat);
  activeChatId = newChat.id;

  saveChats();
  renderSidebar();
  renderActiveChat();
}

/* ================= SAVE ================= */
function saveChats() {
  localStorage.setItem(getStorageKey(), JSON.stringify(chats));
}

/* ================= SIDEBAR ================= */
function renderSidebar() {

  const sidebar = document.querySelector(".sidebar");

  const oldList = document.getElementById("chatList");
  if (oldList) oldList.remove();

  const chatList = document.createElement("div");
  chatList.id = "chatList";
  chatList.className = "chat-list";

  chats.forEach(chat => {

    const item = document.createElement("div");
    item.className = "chat-item";
    if (chat.id === activeChatId) item.classList.add("active");

    const title = document.createElement("span");
    title.innerText = chat.title;

    const deleteBtn = document.createElement("span");
    deleteBtn.innerText = "×";
    deleteBtn.className = "delete-chat";

    deleteBtn.onclick = (e) => {
      e.stopPropagation();
      deleteChat(chat.id);
    };

    item.onclick = () => {
      activeChatId = chat.id;
      renderSidebar();
      renderActiveChat();
    };

    item.appendChild(title);
    item.appendChild(deleteBtn);
    chatList.appendChild(item);
  });

  sidebar.appendChild(chatList);
}

function deleteChat(id) {
  chats = chats.filter(chat => chat.id !== id);

  if (chats.length === 0) {
    createNewChat();
  } else {
    activeChatId = chats[0].id;
  }

  saveChats();
  renderSidebar();
  renderActiveChat();
}

/* ================= RENDER CHAT ================= */
function renderActiveChat() {

  const chatBox = document.getElementById("chatBox");
  chatBox.innerHTML = "";

  const activeChat = chats.find(c => c.id === activeChatId);
  if (!activeChat) return;

  activeChat.messages.forEach(msg => {
    const div = document.createElement("div");
    div.className = msg.role === "user" ? "msg user" : "msg ai";
    div.innerText = msg.content;
    chatBox.appendChild(div);
  });

  chatBox.scrollTop = chatBox.scrollHeight;
}

/* ================= SEND ================= */
async function sendSymptoms() {

  const input = document.getElementById("symptoms");
  const text = input.value.trim();
  if (!text) return;

  const activeChat = chats.find(c => c.id === activeChatId);

  if (activeChat.messages.length === 0) {
    activeChat.title = text.slice(0, 25);
  }

  activeChat.messages.push({ role: "user", content: text });
  input.value = "";

  renderActiveChat();
  saveChats();
  renderSidebar();

  try {

    const res = await fetch(`${BASE_URL}/api/normal/chat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + localStorage.getItem("token")
      },
      body: JSON.stringify({ symptoms: text })
    });

    const data = await res.json();

    await typeWriterEffect(data.aiResponse, activeChat);

  } catch (err) {
    console.error("Chat error:", err);
  }
}

/* ================= TYPEWRITER ================= */
async function typeWriterEffect(text, activeChat) {

  const chatBox = document.getElementById("chatBox");

  const aiMessage = { role: "assistant", content: "" };
  activeChat.messages.push(aiMessage);

  const div = document.createElement("div");
  div.className = "msg ai";
  chatBox.appendChild(div);

  for (let i = 0; i < text.length; i++) {
    aiMessage.content += text[i];
    div.innerText = aiMessage.content;
    await new Promise(resolve => setTimeout(resolve, 8));
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  saveChats();
}

function newChat() {
  createNewChat();
}