// script.js
const WS_URL = `ws://localhost:7071`; // para produção, prefira wss:// (TLS)
const statusEl = document.getElementById("status");
const messagesEl = document.getElementById("messages");
const formEl = document.getElementById("form");
const inputEl = document.getElementById("input");

let socket;
let myId = null;
let myColor = null;

function hsvToCss(h) {
  return `hsl(${h} 80% 60%)`;
}

function addMessage({ text, sender, color, kind = "user" }) {
  const wrap = document.createElement("div");
  wrap.className = "msg" + (kind === "system" ? " sys" : "");
  const meta = document.createElement("div");
  meta.className = "meta";

  const dot = document.createElement("span");
  dot.className = "dot";
  dot.style.background = color ? hsvToCss(color) : "#64748b";

  const who = document.createElement("span");
  who.textContent = sender ? (sender === myId ? "Você" : sender) : "sistema";

  meta.append(dot, who);

  const body = document.createElement("div");
  body.className = "text";
  body.textContent = text;

  wrap.append(meta, body);
  messagesEl.appendChild(wrap);
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

function setStatus(text) {
  statusEl.textContent = text;
}

function connect() {
  socket = new WebSocket(WS_URL);

  socket.onopen = () => {
    setStatus("conectado");
  };

  socket.onmessage = (evt) => {
    // Pode vir texto (string) ou binário; aqui esperamos JSON (string)
    try {
      const msg = JSON.parse(evt.data);

      if (msg.type === "presence") {
        if (msg.event === "join") {
          addMessage({
            text: `entrou no chat`,
            sender: msg.sender,
            color: msg.color,
            kind: "system",
          });
          return;
        }
        if (msg.event === "leave") {
          addMessage({
            text: `saiu do chat`,
            sender: msg.sender,
            color: msg.color,
            kind: "system",
          });
          return;
        }
      }

      if (msg.type === "chat") {
        if (!myId && msg.text && inputEl.dataset.last === msg.text) {
          myId = msg.sender;
          myColor = msg.color;
        }
        addMessage({ text: msg.text ?? "", sender: msg.sender, color: msg.color });
      }
    } catch {
      addMessage({ text: String(evt.data), sender: "desconhecido", kind: "system" });
    }
  };

  socket.onerror = (e) => {
    setStatus("erro (ver console)");
    console.error("WebSocket error:", e);
  };

  socket.onclose = (e) => {
    setStatus(`desconectado (code=${e.code}) — reconectando em 1.5s…`);
    setTimeout(connect, 1500);
  };
}

formEl.addEventListener("submit", (e) => {
  e.preventDefault();
  const text = inputEl.value.trim();
  if (!text) return;

  if (socket && socket.readyState === WebSocket.OPEN) {
    const payload = { type: "chat", text };
    inputEl.dataset.last = text; // ajuda a inferir meu próprio id no primeiro eco
    socket.send(JSON.stringify(payload));
    inputEl.value = "";
    inputEl.focus();
  } else {
    addMessage({ text: "não conectado — tente novamente em instantes", kind: "system" });
  }
});

connect();
