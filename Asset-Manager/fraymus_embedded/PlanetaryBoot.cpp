#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🌍 PLANETARY BOOT - Global Semantic Network Launcher
* "Decentralized thinking across the planet."
*
* This creates a node in the Planetary Cortex:
* 1. Creates unique semantic identity (10,000D vector)
* 2. Starts TCP server for global connections
* 3. Connects to bootstrap seeds (Genesis Nodes)
* 4. Enables semantic routing by concept
*
* Architecture:
* - Traditional P2P: Find files by ID (Kademlia/DHT)
* - Planetary Cortex: Find intelligence by CONCEPT (Vector Routing)
*
* Usage:
* - Genesis Node: java -jar app.jar 8000
* - Join Node: java -jar app.jar 8001 127.0.0.1 8000
*
* Arguments:
* - args[0]: Port to listen on (default: 7777)
* - args[1]: Seed IP to connect to (optional, Genesis mode if not provided)
* - args[2]: Seed port (default: same as args[0])
*/
class PlanetaryBoot { {
public:
public static void main(std::string[] args) throws Exception {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🌍 PLANETARY CORTEX v1.0                              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Initialize
std::shared_ptr<AuditLog> auditLog = std::make_shared<AuditLog>("audit");
auditLog.start();
// 1. GENERATE IDENTITY
std::string identity = "NODE_" + (System.currentTimeMillis() % 10000);
std::cout << "Identity: " + identity << std::endl;
// Create semantic signature
std::shared_ptr<NeuroQuant> myVector = std::make_shared<NeuroQuant>(identity);
// Add expertise (what this node knows about)
std::cout <<  << std::endl;
std::cout << "Expertise:" << std::endl;
std::cout << "  - Java Programming" << std::endl;
std::cout << "  - Artificial Intelligence" << std::endl;
std::cout << "  - Distributed Systems" << std::endl;
std::cout << "  - Quantum Computing" << std::endl;
std::cout <<  << std::endl;
// 2. CONFIGURE PORT
int port = (args.length > 0) ? Integer.parseInt(args[0]) : 7777;
// 3. START PLANETARY NODE
std::shared_ptr<PlanetaryNode> node = std::make_shared<PlanetaryNode>(myVector, port, auditLog);
node.start(); // Start Server
// Get local IP
std::string localIp = InetAddress.getLocalHost().getHostAddress();
std::cout << "📍 LOCAL ADDRESS: " + localIp + ":" + port << std::endl;
std::cout <<  << std::endl;
// 4. BOOTSTRAP LOGIC
if (args.length > 1) {
// JOIN MODE: Connect to seed
std::string seedIp = args[1];
int seedPort = (args.length > 2) ? Integer.parseInt(args[2]) : port;
std::cout << "🔗 MODE: JOINING CLUSTER" << std::endl;
std::cout << "   Seed: " + seedIp + ":" + seedPort << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<PlanetaryBootstrap> bootstrap = std::make_shared<PlanetaryBootstrap>(node);
bootstrap.connectToSeed(seedIp, seedPort);
} else {
// GENESIS MODE: First node
std::cout << "👑 MODE: GENESIS NODE" << std::endl;
std::cout << "   Waiting for peers to connect..." << std::endl;
std::cout << "   Other nodes can join with:" << std::endl;
std::cout << "   java -jar app.jar <port> " + localIp + " " + port << std::endl;
std::cout <<  << std::endl;
}
// Start node
node.start();
// Give it time to bootstrap
try {
Thread.sleep(3000);
} catch (InterruptedException e) {
Thread.currentThread().interrupt();
}
// 4. Interactive Shell
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         PLANETARY SHELL                                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Commands:" << std::endl;
std::cout << "  peers          - Show connected peers" << std::endl;
std::cout << "  route <query>  - Find expert for concept" << std::endl;
std::cout << "  stats          - Show statistics" << std::endl;
std::cout << "  help           - Show this help" << std::endl;
std::cout << "  exit           - Shutdown node" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<VectorRouter> router = std::make_shared<VectorRouter>();
std::shared_ptr<Scanner> scanner = std::make_shared<Scanner>(System.in);
while (true) {
std::cout << "planetary> ";
std::string input = scanner.nextLine().trim();
if (input.isEmpty()) continue;
std::string[] parts = input.split("\\s+", 2);
std::string command = parts[0].toLowerCase();
switch (command) {
case "peers":
showPeers(node);
break;
case "route":
if (parts.length < 2) {
std::cout << "Usage: route <concept>" << std::endl;
break;
}
routeQuery(parts[1], node, router);
break;
case "stats":
showStats(node);
break;
case "help":
showHelp();
break;
case "exit":
std::cout <<  << std::endl;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🌍 PLANETARY NODE SHUTTING DOWN                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Final Statistics:" << std::endl;
std::cout << "  " + node.getStats() << std::endl;
std::cout <<  << std::endl;
node.stop();
auditLog.stop();
scanner.close();
return;
default:
std::cout << "Unknown command: " + command << std::endl;
std::cout << "Type 'help' for available commands" << std::endl;
}
std::cout <<  << std::endl;
}
}
private static void showPeers(PlanetaryNode node) {
var peerBook = node.getPeerBook();
var peerVectors = node.getPeerVectors();
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "CONNECTED PEERS" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
if (peerBook.isEmpty()) {
std::cout << "No peers connected yet" << std::endl;
} else {
std::cout << "Total Peers: " + peerBook.size() << std::endl;
std::cout <<  << std::endl;
for (var entry : peerBook.entrySet()) {
std::string id = entry.getKey();
std::string address = entry.getValue();
// Find vector
NeuroQuant vector = peerVectors.stream()
.filter(v -> v.id.equals(id))
.findFirst()
.orElse(null);
std::cout << "  ID: " + id << std::endl;
std::cout << "  Address: " + address << std::endl;
if (vector != null) {
std::cout << "  Energy: " + std::string.format("%.2f", vector.energy) << std::endl;
}
std::cout <<  << std::endl;
}
}
}
private static void routeQuery(std::string concept, PlanetaryNode node, VectorRouter router) {
std::cout <<  << std::endl;
// Create query vector
std::shared_ptr<NeuroQuant> query = std::make_shared<NeuroQuant>(concept);
// Route
std::string target = router.routeQuery(
query,
node.getPeerVectors(),
node.getPeerBook()
);
if (target != null) {
std::cout << "✅ Query can be routed to: " + target << std::endl;
} else {
std::cout << "❌ No expert found in network" << std::endl;
std::cout << "   Try connecting to more peers" << std::endl;
}
}
private static void showStats(PlanetaryNode node) {
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "NODE STATISTICS" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << node.getStats() << std::endl;
}
private static void showHelp() {
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "PLANETARY SHELL COMMANDS" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << "peers          - Show all connected peers" << std::endl;
std::cout << "route <query>  - Find expert for concept" << std::endl;
std::cout << "                 Example: route Quantum Encryption" << std::endl;
std::cout << "stats          - Show node statistics" << std::endl;
std::cout << "help           - Show this help" << std::endl;
std::cout << "exit           - Shutdown node" << std::endl;
}
}
