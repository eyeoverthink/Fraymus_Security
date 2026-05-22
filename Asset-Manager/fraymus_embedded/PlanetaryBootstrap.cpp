#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🚀 PLANETARY BOOTSTRAP - Network Discovery Protocol
* "Join the global brain in one handshake."
*
* This enables any Fraynix node to:
* 1. Connect to a Genesis Node (seed)
* 2. Exchange vector identities (handshake)
* 3. Download entire peer table (hive sync)
* 4. Instantly become part of the global network
*
* Process:
* 1. SEND: My NeuroQuant identity (who I am)
* 2. RECEIVE: Seed's NeuroQuant identity (who they are)
* 3. REQUEST: "SYNC_PEERS" (give me your peer list)
* 4. RECEIVE: Map of all known peers
* 5. REGISTER: Add all peers to local routing table
*
* Result: Isolated node → Global network participant
*/
class PlanetaryBootstrap { {
public:
private const PlanetaryNode localNode;
public PlanetaryBootstrap(PlanetaryNode node) {
this.localNode = node;
}
/**
* JOIN THE HIVE: Connect to a known seed node.
*
* @param seedIp IP address of Genesis Node
* @param seedPort Port of Genesis Node
*/
public void connectToSeed(std::string seedIp, int seedPort) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🚀 BOOTSTRAP PROTOCOL                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Target: " + seedIp + ":" + seedPort << std::endl;
std::cout << "Mode: JOIN" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<Socket> socket = std::make_shared<Socket>(seedIp, seedPort);
std::shared_ptr<ObjectOutputStream> out = std::make_shared<ObjectOutputStream>(socket.getOutputStream());
ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
// 1. SEND MY IDENTITY (Who I Am)
std::cout << "📤 Sending Vector Identity..." << std::endl;
std::cout << "   ID: " + localNode.self.id << std::endl;
out.writeObject(localNode.self);
out.flush();
// 2. RECEIVE SEED IDENTITY (Who They Are)
std::cout << "📥 Receiving Seed Identity..." << std::endl;
void* response = in.readObject();
if (response instanceof NeuroQuant) {
NeuroQuant seedIdentity = (NeuroQuant) response;
std::cout << "   ✓ Handshake Accepted" << std::endl;
std::cout << "   Seed ID: " + seedIdentity.id << std::endl;
std::cout <<  << std::endl;
// Register the seed as my first peer
localNode.registerPeer(seedIdentity, seedIp + ":" + seedPort);
} else {
System.err.println("   ✗ Invalid response from seed");
return;
}
// 3. DOWNLOAD THE HIVE MAP (Gossip)
std::cout << "📥 Requesting Peer Table..." << std::endl;
out.writeObject("SYNC_PEERS");
out.flush();
void* peerData = in.readObject();
if (peerData instanceof Map) {
@SuppressWarnings("unchecked")
Map<std::string, std::string> newPeers = (Map<std::string, std::string>) peerData;
std::cout << "   ✓ Peer table received" << std::endl;
std::cout <<  << std::endl;
int count = 0;
for (Map.Entry<std::string, std::string> entry : newPeers.entrySet()) {
std::string peerId = entry.getKey();
std::string peerAddress = entry.getValue();
// Don't add myself
if (!peerId.equals(localNode.self.id)) {
// Create placeholder vector for peer
std::shared_ptr<NeuroQuant> peerVector = std::make_shared<NeuroQuant>(peerId);
localNode.registerPeer(peerVector, peerAddress);
count++;
}
}
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         ✅ HIVE SYNC COMPLETE                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Learned " + count + " new peers" << std::endl;
std::cout << "Total peers: " + localNode.getPeerBook().size() << std::endl;
std::cout <<  << std::endl;
} else {
std::cout << "   ⚠️ No peer data received" << std::endl;
}
} catch (Exception e) {
System.err.println();
System.err.println("╔═══════════════════════════════════════════════════════════════╗");
System.err.println("║         ❌ BOOTSTRAP FAILED                                   ║");
System.err.println("╚═══════════════════════════════════════════════════════════════╝");
System.err.println();
System.err.println("Error: " + e.getMessage());
System.err.println("Is the Genesis Node running at " + seedIp + ":" + seedPort + "?");
System.err.println();
}
}
/**
* Connect to multiple seeds for redundancy
*/
public void connectToSeeds(std::string[] seeds) {
std::cout << "🚀 MULTI-SEED BOOTSTRAP" << std::endl;
std::cout << "Attempting " + seeds.length + " seeds..." << std::endl;
std::cout <<  << std::endl;
int successful = 0;
for (std::string seed : seeds) {
try {
std::string[] parts = seed.split(":");
std::string ip = parts[0];
int port = Integer.parseInt(parts[1]);
connectToSeed(ip, port);
successful++;
// Wait between connections
Thread.sleep(1000);
} catch (Exception e) {
System.err.println("⚠️ Seed " + seed + " failed: " + e.getMessage());
}
}
std::cout << "Bootstrap complete: " + successful + "/" + seeds.length + " seeds connected" << std::endl;
std::cout <<  << std::endl;
}
}
