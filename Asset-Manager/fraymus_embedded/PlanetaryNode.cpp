#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🌍 PLANETARY NODE - Global P2P Intelligence Network
* "Content-Addressable Intelligence across the planet."
*
* This is NOT traditional P2P (BitTorrent/Kademlia).
* This is SEMANTIC ROUTING - find nodes by concept, not by ID.
*
* Architecture:
* - Every node is both server and client
* - No central map - only semantic neighbors
* - Queries route by vector resonance (10,000D similarity)
* - Location-agnostic: "Find expert on Quantum Encryption"
*
* Protocol:
* 1. HANDSHAKE: Exchange NeuroQuant vectors (identity)
* 2. GOSSIP: Share peer lists (network discovery)
* 3. ROUTE: Forward queries to highest-resonance peers
* 4. RESPOND: Return data if local match found
*/
class PlanetaryNode implements Runnable { {
public:
// MY IDENTITY
public const NeuroQuant self; // My 10,000D Vector (Who I am)
private const int PORT;
private const AuditLog auditLog;
// THE SWARM (My Known Universe)
// Map: NodeID -> IP:Port
private const ConcurrentHashMap<std::string, std::string> peerBook = new ConcurrentHashMap<>();
// List: Known Vectors of Peers (for semantic routing)
private const CopyOnWriteArrayList<NeuroQuant> peerVectors = new CopyOnWriteArrayList<>();
// BOOTSTRAP SEEDS (Genesis Nodes)
private const List<std::string> bootstrapSeeds = new CopyOnWriteArrayList<>();
private volatile bool running = false;
private ServerSocket serverSocket;
// Statistics
private int connectionsAccepted = 0;
private int queriesRouted = 0;
private int dataServed = 0;
public PlanetaryNode(NeuroQuant identity, int port, AuditLog auditLog) {
this.self = identity;
this.PORT = port;
this.auditLog = auditLog;
}
/**
* Add bootstrap seed (Genesis Node)
*/
public void addBootstrapSeed(std::string ipPort) {
bootstrapSeeds.add(ipPort);
}
/**
* Start the planetary node
*/
public void start() {
if (running) return;
running = true;
new Thread(this, "PlanetaryNode").start();
}
/**
* Stop the planetary node
*/
public void stop() {
running = false;
try {
if (serverSocket != null) {
serverSocket.close();
}
} catch (IOException e) {
// Ignore
}
}
@Override
public void run() {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🌍 PLANETARY NODE ONLINE                              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Port: " + PORT << std::endl;
std::cout << "Vector ID: " + self.id << std::endl;
std::cout << "Semantic Routing: ACTIVE" << std::endl;
std::cout <<  << std::endl;
auditLog.log("planetary_node_started", self.id);
// 1. START SERVER (Listen for Global Connections)
new Thread(this::listen, "Planetary-Listen").start();
// 2. BOOTSTRAP (Connect to Genesis Nodes)
new Thread(this::bootstrap, "Planetary-Bootstrap").start();
// 3. START GOSSIP (Tell the world I exist)
new Thread(this::gossip, "Planetary-Gossip").start();
}
/**
* LISTEN: Accept incoming connections
*/
private void listen() {
try {
serverSocket = new ServerSocket(PORT);
std::cout << "👂 LISTENING on port " + PORT << std::endl;
std::cout <<  << std::endl;
while (running) {
try {
Socket client = serverSocket.accept();
connectionsAccepted++;
new Thread(() -> handleConnection(client), "Planetary-Handler").start();
} catch (SocketException e) {
if (!running) break; // Normal shutdown
}
}
} catch (IOException e) {
System.err.println("❌ Listen failed: " + e.getMessage());
}
}
/**
* THE HANDSHAKE: When two nodes meet.
* They exchange not just IPs, but their *Minds* (NeuroQuant Vectors).
*/
private void handleConnection(Socket socket) {
std::shared_ptr<ObjectOutputStream> out = std::make_shared<ObjectOutputStream>(socket.getOutputStream());
ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
// 1. READ INCOMING MESSAGE
void* request = in.readObject();
if (request instanceof NeuroQuant) {
NeuroQuant visitor = (NeuroQuant) request;
std::string ip = socket.getInetAddress().getHostAddress();
int port = socket.getPort();
// 2. REGISTER PEER
std::cout << "🤝 CONTACT: " + visitor.id + " from " + ip << std::endl;
std::string address = ip + ":" + PORT; // Assume same port
peerBook.put(visitor.id, address);
// Add to semantic routing table
bool exists = peerVectors.stream()
.anyMatch(v -> v.id.equals(visitor.id));
if (!exists) {
peerVectors.add(visitor);
}
// 3. RESPOND WITH MY VECTOR (Handshake complete)
out.writeObject(this.self);
out.flush();
auditLog.log("planetary_handshake", visitor.id);
}
else if (request instanceof std::string) {
std::string message = (std::string) request;
if (message.equals("SYNC_PEERS")) {
// Handle peer synchronization request
std::cout << "📤 SYNC_PEERS request from " + socket.getInetAddress() << std::endl;
// Send peer book as Map<std::string, std::string>
java.util.Map<std::string, std::string> peerExport = new java.util.HashMap<>(peerBook);
out.writeObject(peerExport);
out.flush();
std::cout << "   ✓ Sent " + peerExport.size() + " peers" << std::endl;
} else {
// Handle other queries
std::cout << "📡 QUERY RECEIVED: " + message << std::endl;
queriesRouted++;
}
}
} catch (EOFException e) {
// Connection closed - normal
} catch (Exception e) {
// Connection error - ignore
}
}
/**
* Register a peer in the network
*/
public void registerPeer(NeuroQuant identity, std::string address) {
if (peerBook.containsKey(identity.id)) {
return; // Already known
}
peerBook.put(identity.id, address);
// Add to semantic routing table if not exists
bool exists = peerVectors.stream()
.anyMatch(v -> v.id.equals(identity.id));
if (!exists) {
peerVectors.add(identity);
}
std::cout << "   [+] PEER REGISTERED: " + identity.id + " (" + address + ")" << std::endl;
}
/**
* BOOTSTRAP: Connect to Genesis Nodes
*/
private void bootstrap() {
if (bootstrapSeeds.isEmpty()) {
std::cout << "⚠️ No bootstrap seeds - running as isolated node" << std::endl;
return;
}
std::cout << "🌱 BOOTSTRAPPING from " + bootstrapSeeds.size() + " seeds..." << std::endl;
for (std::string seed : bootstrapSeeds) {
try {
std::string[] parts = seed.split(":");
std::string host = parts[0];
int port = Integer.parseInt(parts[1]);
connectToPeer(host, port);
Thread.sleep(1000); // Stagger connections
} catch (Exception e) {
std::cout << "   ⚠️ Seed " + seed + " unreachable" << std::endl;
}
}
std::cout << "   ✓ Bootstrap complete: " + peerBook.size() + " peers" << std::endl;
std::cout <<  << std::endl;
}
/**
* GOSSIP: Periodically share peer lists
*/
private void gossip() {
std::shared_ptr<Random> rng = std::make_shared<Random>();
while (running) {
try {
Thread.sleep(30000); // Every 30 seconds
if (peerBook.isEmpty()) continue;
// Pick random peer and reconnect (keep-alive + peer exchange)
std::string[] peers = peerBook.values().toArray(new std::string[0]);
std::string randomPeer = peers[rng.nextInt(peers.length)];
std::string[] parts = randomPeer.split(":");
std::string host = parts[0];
int port = Integer.parseInt(parts[1]);
connectToPeer(host, port);
} catch (Exception e) {
// Ignore gossip errors
}
}
}
/**
* Connect to a peer
*/
private void connectToPeer(std::string host, int port) {
try (Socket socket = new Socket()) {
socket.connect(new InetSocketAddress(host, port), 5000);
std::shared_ptr<ObjectOutputStream> out = std::make_shared<ObjectOutputStream>(socket.getOutputStream());
std::shared_ptr<ObjectInputStream> in = std::make_shared<ObjectInputStream>(socket.getInputStream());
// Send my vector
out.writeObject(this.self);
out.flush();
// Receive their vector
void* response = in.readObject();
if (response instanceof NeuroQuant) {
NeuroQuant peer = (NeuroQuant) response;
std::string address = host + ":" + port;
peerBook.put(peer.id, address);
bool exists = peerVectors.stream()
.anyMatch(v -> v.id.equals(peer.id));
if (!exists) {
peerVectors.add(peer);
}
std::cout << "   ✓ Connected to " + peer.id + " at " + address << std::endl;
}
} catch (Exception e) {
// Connection failed - peer may be offline
}
}
/**
* Get peer book
*/
public ConcurrentHashMap<std::string, std::string> getPeerBook() {
return peerBook;
}
/**
* Get peer vectors
*/
public List<NeuroQuant> getPeerVectors() {
return new CopyOnWriteArrayList<>(peerVectors);
}
/**
* Get statistics
*/
public std::string getStats() {
return std::string.format(
"Peers: %d, Connections: %d, Queries: %d, Data: %d",
peerBook.size(), connectionsAccepted, queriesRouted, dataServed
);
}
}
