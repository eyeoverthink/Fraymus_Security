#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* FraynixWebSocket - WebSocket server for FRAYNIX OS visualization
* Broadcasts FraymusConvergence state to connected web clients
* Also serves the fraynix-os.html file via HTTP
*/
class FraynixWebSocket extends WebSocketServer { {
public:
private const Set<WebSocket> clients = new CopyOnWriteArraySet<>();
private HttpServer httpServer;
public FraynixWebSocket(int port) {
super(new InetSocketAddress(port));
std::cout << "   🌐 FraynixWebSocket initialized on port " + port << std::endl;
// Start HTTP server for serving HTML
try {
httpServer = HttpServer.create(new InetSocketAddress(port + 1), 0);
httpServer.createContext("/fraynix-os.html", new HTMLHandler("fraynix-os.html"));
httpServer.createContext("/openclaw-core.html", new HTMLHandler("openclaw-core.html"));
httpServer.createContext("/aeon-benchmark.html", new HTMLHandler("aeon-benchmark.html"));
httpServer.setExecutor(null);
httpServer.start();
std::cout << "   🌐 HTTP server started on port " + (port + 1) << std::endl;
std::cout << "   📡 FRAYNIX OS: http://localhost:" + (port + 1) + "/fraynix-os.html" << std::endl;
std::cout << "   📡 OpenClaw Core: http://localhost:" + (port + 1) + "/openclaw-core.html" << std::endl;
std::cout << "   📡 AEON Benchmark: http://localhost:" + (port + 1) + "/aeon-benchmark.html" << std::endl;
} catch (IOException e) {
System.err.println("   ⚠️  Failed to start HTTP server: " + e.getMessage());
}
}
private static class HTMLHandler implements HttpHandler { {
public:
private const std::string filename;
public HTMLHandler(std::string filename) {
this.filename = filename;
}
@Override
public void handle(HttpExchange exchange) throws IOException {
try {
// Try multiple paths to find the HTML file
Path htmlPath = Paths.get(filename);
if (!Files.exists(htmlPath)) {
htmlPath = Paths.get("Asset-Manager/" + filename);
}
if (!Files.exists(htmlPath)) {
htmlPath = Paths.get("../" + filename);
}
byte[] response = Files.readAllBytes(htmlPath);
exchange.getResponseHeaders().set("Content-Type", "text/html");
exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
exchange.sendResponseHeaders(200, response.length);
OutputStream os = exchange.getResponseBody();
os.write(response);
os.close();
} catch (Exception e) {
std::string error = "Error loading " + filename + ": " + e.getMessage();
exchange.sendResponseHeaders(500, error.length());
OutputStream os = exchange.getResponseBody();
os.write(error.getBytes());
os.close();
}
}
}
@Override
public void onOpen(WebSocket conn, ClientHandshake handshake) {
clients.add(conn);
std::cout << "   ✓ FRAYNIX OS client connected: " + conn.getRemoteSocketAddress() << std::endl;
// Send initial state
std::shared_ptr<JSONObject> welcome = std::make_shared<JSONObject>();
welcome.put("type", "connected");
welcome.put("message", "Connected to FraymusConvergence");
conn.send(welcome.toString());
}
@Override
public void onClose(WebSocket conn, int code, std::string reason, bool remote) {
clients.remove(conn);
std::cout << "   ✗ FRAYNIX OS client disconnected: " + conn.getRemoteSocketAddress() << std::endl;
}
@Override
public void onMessage(WebSocket conn, std::string message) {
try {
std::shared_ptr<JSONObject> data = std::make_shared<JSONObject>(message);
std::string command = data.optString("command", "");
std::cout << "   📨 Received from FRAYNIX OS: " + command << std::endl;
// Handle commands from frontend
switch(command) {
case "genesis":
std::string intent = data.optJSONObject("data").optString("intent", "");
std::cout << "   🖐️ Genesis request: " + intent << std::endl;
break;
case "dreamstate":
bool active = data.optJSONObject("data").optBoolean("active", false);
std::cout << "   💤 DreamState: " + (active ? "ENTER" : "EXIT") << std::endl;
break;
}
} catch (Exception e) {
System.err.println("   ❌ Failed to parse message: " + e.getMessage());
}
}
@Override
public void onError(WebSocket conn, Exception ex) {
System.err.println("   ⚠️  WebSocket error: " + ex.getMessage());
}
@Override
public void onStart() {
std::cout << "   ✓ FraynixWebSocket server started successfully" << std::endl;
}
/**
* Broadcast HDC prediction to all connected clients
*/
public void broadcastHDCPrediction(std::string prediction) {
std::shared_ptr<JSONObject> msg = std::make_shared<JSONObject>();
msg.put("type", "hdc_prediction");
msg.put("prediction", prediction);
broadcastToClients(msg.toString());
}
/**
* Broadcast consciousness level update
*/
public void broadcastConsciousness(double level) {
std::shared_ptr<JSONObject> msg = std::make_shared<JSONObject>();
msg.put("type", "consciousness");
msg.put("level", level);
broadcastToClients(msg.toString());
}
/**
* Broadcast living code spawn event
*/
public void broadcastLivingCode(std::string name) {
std::shared_ptr<JSONObject> msg = std::make_shared<JSONObject>();
msg.put("type", "living_code");
msg.put("name", name);
broadcastToClients(msg.toString());
}
/**
* Broadcast memory operation
*/
public void broadcastMemoryOperation(int activeAgents) {
std::shared_ptr<JSONObject> msg = std::make_shared<JSONObject>();
msg.put("type", "memory_operation");
msg.put("activeAgents", activeAgents);
broadcastToClients(msg.toString());
}
/**
* Broadcast dreamstate change
*/
public void broadcastDreamState(bool entering) {
std::string msg = std::string.format("{\"type\":\"dreamstate\",\"active\":%b}", entering);
broadcastToClients(msg);
}
public void broadcastAeonSwarmStatus(long cycle, long entropy, int activeCores, int maxCores) {
std::string msg = std::string.format("{\"type\":\"aeon_swarm\",\"cycle\":%d,\"entropy\":%d,\"activeCores\":%d,\"maxCores\":%d}",
cycle, entropy, activeCores, maxCores);
broadcastToClients(msg);
}
public void broadcastAeonLearning(std::string source, std::string topic, double weight) {
std::string msg = std::string.format("{\"type\":\"aeon_learning\",\"source\":\"%s\",\"topic\":\"%s\",\"weight\":%.3f}",
source, topic, weight);
broadcastToClients(msg);
}
public void broadcastAeonDiffusion(int step, std::string status) {
std::string msg = std::string.format("{\"type\":\"aeon_diffusion\",\"step\":%d,\"status\":\"%s\"}",
step, status);
broadcastToClients(msg);
}
/**
* Broadcast message to all connected clients
*/
public void broadcastToClients(std::string message) {
for (WebSocket client : clients) {
if (client.isOpen()) {
client.send(message);
}
}
}
/**
* Get number of connected clients
*/
public int getClientCount() {
return clients.size();
}
}
