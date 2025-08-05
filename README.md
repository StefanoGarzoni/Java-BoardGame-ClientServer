# Galaxy Trucker — Java Client/Server Board Game

Replica of the famous **Galaxy Trucker** board game. **Client–server** architecture focused on optimization, modularity, and extensibility. It supports multiple communication logics (**Socket & RMI**), a clear separation of concerns (MVC + States), and includes a **TUI demo** for quick testing.

---

🔍 Introduction
This project provides:

Multiplayer client/server gameplay
Two transport layers: **Java Sockets** and **Java RMI**
State-driven clients (lobby, setup, playing, endgame…)
Controllers and proxies for message routing
Periodic backups and player liveness checks
A minimal **Text User Interface (TUI)** for fast demos
Everything runs locally with standard Java (no external services).

---

🛠️ Features

✔ **Dual Networking** — Pluggable **Socket** and **RMI** flows (client/server proxies & welcome proxies)

✔ **Clean Architecture** — **Model–View–Controller**, **Observer/Publisher**, **State** pattern for client and server controller logic

✔ **Game Engine** — Core entities (deck, tiles, ship board, flight board, dice/hourglass, player, rules validation)

✔ **Match Orchestration** — **AllGamesManagers**, lobby management, concurrent matches

✔ **Reliability** — **PlayersAlivenessChecker** and **PeriodicGamesBackupper** for resilience

✔ **TUI Demo** — Lightweight text client to play/test without a GUI

✔ **Extensible** — Clear module boundaries and adapters for commands/messages

---

🧰 Tech Stack

* **Language:** Java 8+
* **Networking:** Java **Sockets** and **RMI**
* **Patterns:** MVC, State, Observer/Publisher, Strategy/Factory (where applicable)
* **Build:** Maven (standard `src/main/java` layout)
* **UI:** TUI (with hooks for GUI handlers)

---

⚙️ How It Works?

1️⃣ **Startup & Transport Selection**
Run the server main and choose a transport (`socket` or `rmi`). Clients start with the same mode and connect to the **Welcome Server** (socket or RMI) to register nickname and join/create a match.

2️⃣ **Lobby & Match Creation**
The **AllGamesManagers** component groups players into lobbies and spins up game controllers. Health checks (**PlayersAlivenessChecker**) keep rooms clean from dropped clients.

3️⃣ **Gameplay Flow**
Clients run a **State Machine** (`ClientLobbyState`, `ClientSetupState`, `ClientPlayingState`, `ClientEndGameState`, …).
Commands are serialized by **adapters** and routed through client/server **proxies** to the **Controller**, which validates and updates the **Model**.

4️⃣ **Model & Rules**
Core classes handle decks, tiles and components, ship building/validation, flight phase, events, damage, scoring, and rankings. The **Publisher/Observer** chain notifies clients of authoritative state changes.

5️⃣ **Reliability & Backups**
A periodic backup persists running matches, while aliveness checks detect timeouts. On restart, the system can reconcile state from backups (policy configurable).


---

🔒 Benefits

✅ **Two transports, one codebase** — easy to benchmark/compare Socket vs RMI
✅ **Strong separation of concerns** — easier to maintain and extend
✅ **Production-like server features** — liveness checks, backups, multi-game orchestration
✅ **Course-ready** — compact TUI to demo gameplay without extra tooling

---

NOTE:

* This is a **didactic recreation** of *Galaxy Trucker*; assets and rules are implemented for educational use.
* Networking defaults (ports, registry) and backup policies are configurable; see the constants/config in code.
* GUI hooks are present; the shipped demo is **TUI**.
* Be mindful of thread-safety when extending proxies or controllers.

---

📧 Contact
Questions or contributions? Open an **Issue** or reach out on GitHub! 🚀

