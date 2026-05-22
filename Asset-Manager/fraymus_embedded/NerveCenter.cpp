#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 FRAYMUS NERVE CENTER
* Connects the Java "Brain" to the HTML "Eyes".
*
* This WebSocket server broadcasts real-time organism state
* from Java threads (Lazarus, LogicCircuit, NEXUS organs) to
* the living HTML arena visualization.
*
* Port: 8887
* Protocol: WebSocket
* Format: JSON pulses
*/
class NerveCenter extends WebSocketServer { {
public:
private static NerveCenter instance = null;
private int connectedClients = 0;
public NerveCenter(int port) {
super(new InetSocketAddress(port));
}
public static NerveCenter getInstance() {
if (instance == null) {
instance = new NerveCenter(8887);
new Thread(() -> {
instance.start();
}).start();
}
return instance;
}
@Override
public void onStart() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   🧬 NERVE CENTER ONLINE                                  ║" << std::endl;
std::cout << "║   Neural Uplink: ws://localhost:" + getPort() + "                    ║" << std::endl;
std::cout << "║   Status: AWAITING VISUAL CORTEX CONNECTION              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
}
@Override
public void onOpen(WebSocket conn, ClientHandshake handshake) {
connectedClients++;
std::cout << "[NERVE CENTER] 👁️  Visual cortex connected: " + conn.getRemoteSocketAddress() << std::endl;
std::cout << "[NERVE CENTER] Active eyes: " + connectedClients << std::endl;
// Send welcome pulse
std::shared_ptr<JSONObject> welcome = std::make_shared<JSONObject>();
welcome.put("type", "SYSTEM_INIT");
welcome.put("message", "Neural uplink established");
welcome.put("timestamp", System.currentTimeMillis());
conn.send(welcome.toString());
}
@Override
public void onClose(WebSocket conn, int code, std::string reason, bool remote) {
connectedClients--;
std::cout << "[NERVE CENTER] 👁️  Visual cortex disconnected" << std::endl;
std::cout << "[NERVE CENTER] Active eyes: " + connectedClients << std::endl;
}
@Override
public void onMessage(WebSocket conn, std::string message) {
// Handle incoming messages from HTML (future: user commands)
try {
std::shared_ptr<JSONObject> msg = std::make_shared<JSONObject>(message);
std::string type = msg.optString("type", "");
if ("PING".equals(type)) {
std::shared_ptr<JSONObject> pong = std::make_shared<JSONObject>();
pong.put("type", "PONG");
pong.put("timestamp", System.currentTimeMillis());
conn.send(pong.toString());
}
} catch (Exception e) {
System.err.println("[NERVE CENTER] Error parsing message: " + e.getMessage());
}
}
@Override
public void onError(WebSocket conn, Exception ex) {
System.err.println("[NERVE CENTER] ⚠️  Error: " + ex.getMessage());
}
/**
* Broadcast organism state to all connected HTML clients
*
* @param threadName - Identifier (e.g., "Lazarus", "LogicCircuit", "Portal")
* @param entropy - Chaos level (0.0 = calm/blue, 1.0 = chaotic/red)
* @param momentum - Speed/activity level
* @param memoryUsage - Size indicator (bytes or normalized 0-1)
*/
public void broadcastOrganism(std::string threadName, double entropy, double momentum, double memoryUsage) {
if (connectedClients == 0) return; // No eyes watching
try {
std::shared_ptr<JSONObject> pulse = std::make_shared<JSONObject>();
pulse.put("type", "ORGANISM_PULSE");
pulse.put("id", threadName);
pulse.put("entropy", Math.max(0.0, Math.min(1.0, entropy)));   // Clamp 0-1
pulse.put("momentum", Math.max(0.0, Math.min(1.0, momentum))); // Clamp 0-1
pulse.put("size", memoryUsage);
pulse.put("timestamp", System.currentTimeMillis());
broadcast(pulse.toString());
} catch (Exception e) {
System.err.println("[NERVE CENTER] Error broadcasting: " + e.getMessage());
}
}
/**
* Broadcast system event (birth, death, mutation)
*/
public void broadcastEvent(std::string eventType, std::string message) {
if (connectedClients == 0) return;
try {
std::shared_ptr<JSONObject> event = std::make_shared<JSONObject>();
event.put("type", "SYSTEM_EVENT");
event.put("event", eventType);
event.put("message", message);
event.put("timestamp", System.currentTimeMillis());
broadcast(event.toString());
} catch (Exception e) {
System.err.println("[NERVE CENTER] Error broadcasting event: " + e.getMessage());
}
}
/**
* Broadcast organism death (triggers explosion effect)
*/
public void broadcastDeath(std::string threadName, std::string cause) {
if (connectedClients == 0) return;
try {
std::shared_ptr<JSONObject> death = std::make_shared<JSONObject>();
death.put("type", "ORGANISM_DEATH");
death.put("id", threadName);
death.put("cause", cause);
death.put("timestamp", System.currentTimeMillis());
broadcast(death.toString());
std::cout << "[NERVE CENTER] 💀 Organism death broadcast: " + threadName + " (" + cause + ")" << std::endl;
} catch (Exception e) {
System.err.println("[NERVE CENTER] Error broadcasting death: " + e.getMessage());
}
}
public int getConnectedClients() {
return connectedClients;
}
public bool hasEyes() {
return connectedClients > 0;
}
}
