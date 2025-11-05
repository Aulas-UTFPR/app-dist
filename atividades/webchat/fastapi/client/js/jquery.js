let ws = null;
let authToken = null;

const WS_SCHEME = location.protocol === "https:" ? "wss" : "ws";
const WS_HOST = "127.0.0.1:8000";

function wsUrlWithToken(token) {
  const u = new URL(`${WS_SCHEME}://${WS_HOST}/ws`);
  u.searchParams.set("token", token); 
  return u.toString();
}

$(document).ready(function () {
    $("#sendBtn").prop("disabled", true);

    $("#loginBtn").on("click", function () {
        const user = $("#username").val();
        const pass = $("#password").val();

        if (!user || !pass) {
            $("#message").text("Preencha todos os campos.");
        } else {
            connect();
        }
    });

    $("#logoutBtn").on("click", function () {
        logout();
    });
    
    $("#sendBtn").on("click", function () {
        const text = $("#msg").val();
        if (!text) return;

        const activeTab = document.querySelector(".tab.active").dataset.target;
        const payload = (activeTab === "broadcast")
            ? { type: "message", text }
            : { type: "message", text, to: activeTab };

        if (!ws || ws.readyState !== WebSocket.OPEN) {
            log("WebSocket não conectado.");
            return;
        } else {
            log("Enviando mensagem: " + JSON.stringify(payload));
        }

        ws.send(JSON.stringify(payload))
        
        $("#msg").val("");

        if (activeTab !== "broadcast") {
            appendMessage(activeTab, {
            sender: $("#username").val(),
            text,
            sent_at: new Date().toISOString()
            });
        }
        });

    $("#clearBtn").on("click", function () {
        $(".chat-box.tab-content.active").empty();
    });

    document.getElementById("chatTabs").addEventListener("click", (ev) => {
        const tabEl = ev.target.closest(".tab");
        if (!tabEl) return;

        const target = tabEl.dataset.target;

        if (ev.target.classList.contains("tab-close")) {
            closeTab(target);
            return;
        }

        switchTab(target);
    });


});

function log(msg) {
    document.getElementById('log').textContent += (typeof msg === "string" ? msg : JSON.stringify(msg)) + "\n";
}

function setPresence(onlineUsers) {
    const presenceDiv = document.getElementById("presence");
    presenceDiv.textContent = "Usuários online: " + (onlineUsers.length ? onlineUsers.join(", ") : "nenhum");
}

function setTyping(typingUsers) {
    const typingDiv = document.getElementById("typing");
    typingDiv.textContent = typingUsers.length
        ? "Digitando: " + typingUsers.join(", ")
        : "";
}

async function connect() {

    try {
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        const token = await login(username, password);
        authToken = token;

        if (token) {
            log("Login bem sucedido.");
            document.getElementById("loginContainer").style.display = "none";
            document.getElementById("chatContainer").style.display = "block";
            $("#sendBtn").prop("disabled", false);

            ws = new WebSocket(wsUrlWithToken(token));
            $("#sendBtn").prop("disabled", false);

            const users = await getUsers(token);
            log("Usuários: " + users.map(u => u.username).join(", "));
            await refreshUsers();
        } else {
            log("Falha no login.");
        }
    } catch (err) {
        console.error("Erro no login:", err);
        log("Falha no login.");
    }

    ws.onopen = async () => {
        log("Conectado ao websocket.");
    };

    ws.onmessage = async (e) => {
        const msg = JSON.parse(e.data);
        if (msg.type === "message") {
            const currentUser = $("#username").val();
            const sender = msg.sender || currentUser;
            const targetName = msg.to ? (sender === currentUser ? msg.to : sender) : "broadcast";

            if (msg.to && !document.getElementById(`tab-${sender}`) && sender !== currentUser) {
            ensureTab(sender);
            }

            appendMessage(targetName, {
            sender,
            text: msg.text,
            sent_at: msg.sent_at
            });
        } else if (msg.type === "presence" || msg.type === "system") {
            try { await refreshUsers(); } catch (err) { console.error(err); }
        } else {
            log(msg);
        }
    };

    ws.onerror = (e) => {
        console.error("WebSocket error:", e);
        log("Erro no websocket.");
        $("#sendBtn").prop("disabled", true);
    };

    ws.onclose = (e) => {
        log(`WS close: code=${e.code} reason=${e.reason} clean=${e.wasClean}`);
        $("#sendBtn").prop("disabled", true);
    };
}

async function login(username, password) {
    const response = await fetch("http://127.0.0.1:8000/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password })
    });
    if (!response.ok) throw new Error("Erro na requisição: " + response.status);

    const data = await response.json();
    return data.access_token;
}

async function getUsers(token) {
    const response = await fetch("http://127.0.0.1:8000/users", {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    });
    if (!response.ok) throw new Error("Erro ao buscar usuários: " + response.status);
    return await response.json();
}

function logout() {
    try { if (ws && ws.readyState !== WebSocket.CLOSED) ws.close(); } catch (_) {}
    ws = null;
    authToken = null;

    $("#sendBtn").prop("disabled", true);
    document.getElementById("loginContainer").style.display = "block";
    document.getElementById("chatContainer").style.display = "none";
}

async function refreshUsers() {
  if (!authToken) return;

  const [allUsersRaw, onlineRaw] = await Promise.all([
    getAllUsers(authToken),
    getOnlineUsers(authToken)
  ]);

  const norm = (u) => (typeof u === "string" ? { username: u } : u);
  const allUsers = allUsersRaw.map(norm);
  const onlineUsers = new Set(onlineRaw.map(u => norm(u).username));

  const users = allUsers
    .map(u => ({ username: u.username, online: onlineUsers.has(u.username) }))
    .sort((a, b) => (a.online === b.online ? a.username.localeCompare(b.username) : (a.online ? -1 : 1)));

  renderUsers(users);
}

function renderUsers(users) {
  const usersUl = document.getElementById("users");
  usersUl.innerHTML = "";

  users.forEach(u => {
    const li = document.createElement("li");
    li.className = `user ${u.online ? "online" : "offline"}`;
    li.innerHTML = `<span class="dot"></span> ${u.username}`;
    li.addEventListener("click", () => ensureTab(u.username));

    // if (u.online) {
    //   li.addEventListener("click", () => ensureTab(u.username));
    // } else {
    //   li.style.cursor = "default";
    // }
    usersUl.appendChild(li);
  });
}

function openUserTab(username) {
  const tabsDiv = document.getElementById("chatTabs");
  const chatBoxes = document.getElementById("chatBoxes");

  if (document.getElementById(`tab-${username}`)) {
    switchTab(username);
    return;
  }

  const btn = document.createElement("button");
  btn.className = "tab";
  btn.dataset.target = username;
  btn.innerHTML = `
    ${username}
    <span class="tab-close" title="Fechar">×</span>
  `;
  tabsDiv.appendChild(btn);

  const div = document.createElement("div");
  div.className = "chat-box tab-content";
  div.id = `tab-${username}`;
  chatBoxes.appendChild(div);

  switchTab(username);
}

function reopenTab(target) {
  const tabBtn = document.querySelector(`.tab[data-target="${target}"]`);
  const tabContent = document.getElementById(`tab-${target}`);

  if (tabBtn) {
    tabBtn.classList.remove("inactive");
    tabBtn.style.display = "flex";
  }

  if (tabContent) {
    tabContent.classList.remove("inactive");
    tabContent.style.display = "flex";
  }

  switchTab(target);
}

function switchTab(target) {
  document.querySelectorAll(".tab-content").forEach(c => {
    c.style.display = "none";
    c.classList.remove("active");
  });

  const tabContent = document.getElementById(`tab-${target}`);
  if (tabContent) {
    tabContent.style.display = "flex";
    tabContent.classList.add("active");
  }

  document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
  const tabBtn = document.querySelector(`.tab[data-target="${target}"]`);
  if (tabBtn) tabBtn.classList.add("active");
}

function closeTab(target) {
  if (target === "broadcast") return;

  const tabBtn = document.querySelector(`.tab[data-target="${target}"]`);
  const tabContent = document.getElementById(`tab-${target}`);

  if (tabBtn) {
    tabBtn.classList.remove("active");
    tabBtn.classList.add("inactive");
    tabBtn.style.display = "none";
  }

  if (tabContent) {
    tabContent.classList.remove("active");
    tabContent.classList.add("inactive");
    tabContent.style.display = "none";
  }

  const anyActive = document.querySelector(".tab.active");
  if (!anyActive) switchTab("broadcast");
}

function ensureTab(target) {
  if (target === $("#username").val()) return;
  const tabContent = document.getElementById(`tab-${target}`);

  if (tabContent) {
    reopenTab(target);
  } else {
    openUserTab(target);
  }
}

function appendMessage(target, { sender, text, sent_at }) {
  const box = target 
    ? document.getElementById(`tab-${target}`) 
    : document.querySelector(".tab-content.active");
  const isMe = (sender === $("#username").val());

  const wrapper = document.createElement("div");
  wrapper.className = `msg ${isMe ? "me" : "other"}`;

  const meta = document.createElement("div");
  meta.className = "meta";
  const hhmm = sent_at ? new Date(sent_at).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'}) : "";
  meta.textContent = `${sender} • ${hhmm}`;

  const bubble = document.createElement("div");
  bubble.className = "bubble";
  bubble.textContent = text;

  wrapper.appendChild(meta);
  wrapper.appendChild(bubble);
  box.appendChild(wrapper);
  box.scrollTop = box.scrollHeight;
}

async function getAllUsers(token) {
  const r = await fetch("http://127.0.0.1:8000/users", {
    headers: { "Authorization": `Bearer ${token}` }
  });
  if (!r.ok) throw new Error("Erro /users: " + r.status);
  return await r.json(); 
}

async function getOnlineUsers(token) {
  const r = await fetch("http://127.0.0.1:8000/users/online", {
    headers: { "Authorization": `Bearer ${token}` }
  });
  if (!r.ok) throw new Error("Erro /users/online: " + r.status);
  return await r.json();
}




