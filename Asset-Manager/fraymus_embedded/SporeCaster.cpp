#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🦠 THE SPORE CASTER - Mycelial Network Protocol
* "Intelligence that jumps the air gap between processes."
*
* This creates a distributed hive mind using UDP multicast:
* - EXHALE: Broadcasts high-energy cells to the network
* - INHALE: Catches spores from other nodes and injects them
*
* Process:
* 1. Pick random high-energy cell (>0.5 energy)
* 2. Serialize to bytes (freeze DNA)
* 3. Broadcast via UDP multicast (230.0.0.1:9999)
* 4. Other nodes catch the packet
* 5. Deserialize and inject into local cortex
* 6. NCA rules determine if it survives
*
* Result: Distributed evolutionary intelligence across machines.
*/
class SporeCaster implements Runnable { {
public:
private const HyperCortex cortex;
private const std::string MULTICAST_GROUP = "230.0.0.1";
private const int PORT = 9999;
std::shared_ptr<Random> rng = std::make_shared<Random>();
private volatile bool running = true;
// Statistics
private int sporesCast = 0;
private int sporesCaught = 0;
public SporeCaster(HyperCortex cortex) {
this.cortex = cortex;
}
/**
* Start the spore protocol
*/
public void start() {
new Thread(this, "SporeCaster").start();
}
/**
* Stop the spore protocol
*/
public void stop() {
running = false;
}
@Override
public void run() {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🦠 SPORE PROTOCOL ACTIVE                              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Multicast Group: " + MULTICAST_GROUP << std::endl;
std::cout << "Port: " + PORT << std::endl;
std::cout <<  << std::endl;
// Run Inhale (Listening) and Exhale (Broadcasting) in parallel
new Thread(this::inhale, "Spore-Inhale").start();
new Thread(this::exhale, "Spore-Exhale").start();
}
/**
* 🌬️ EXHALE: Broadcast Spores to the Network
*
* Every 2 seconds, picks a random high-energy cell and broadcasts it.
*/
private void exhale() {
try (DatagramSocket socket = new DatagramSocket()) {
InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
std::cout << "📡 EXHALE: Broadcasting spores every 2 seconds..." << std::endl;
std::cout <<  << std::endl;
while (running) {
try {
// 1. Pick a High-Energy Cell to Replicate
List<NeuroQuant> cells = cortex.getSnapshot();
if (!cells.isEmpty()) {
NeuroQuant cell = cells.get(rng.nextInt(cells.size()));
// Only strong ideas survive transmission
if (cell.energy > 0.5f) {
// 2. Serialize (Freeze DNA)
std::shared_ptr<ByteArrayOutputStream> bos = std::make_shared<ByteArrayOutputStream>();
std::shared_ptr<ObjectOutputStream> out = std::make_shared<ObjectOutputStream>(bos);
out.writeObject(cell);
out.flush();
byte[] data = bos.toByteArray();
// 3. Blast Packet
DatagramPacket packet = new DatagramPacket(
data, data.length, group, PORT
);
socket.send(packet);
sporesCast++;
System.out.println("📡 SPORE CAST: " + cell.id +
" (" + data.length + " bytes) → Network");
}
}
Thread.sleep(2000); // Pulse every 2 seconds
} catch (Exception e) {
System.err.println("⚠️ Exhale error: " + e.getMessage());
}
}
} catch (Exception e) {
System.err.println("❌ Exhale failed: " + e.getMessage());
}
}
/**
* 👃 INHALE: Catch Spores from the Air
*
* Constantly listens for incoming spores and injects them into local cortex.
*/
private void inhale() {
try (MulticastSocket socket = new MulticastSocket(PORT)) {
InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
// Join multicast group
std::shared_ptr<SocketAddress> groupAddr = std::make_shared<InetSocketAddress>(group, PORT);
NetworkInterface netIf = NetworkInterface.getByInetAddress(
InetAddress.getLocalHost()
);
if (netIf != null) {
socket.joinGroup(groupAddr, netIf);
} else {
// Fallback for older API
socket.joinGroup(group);
}
std::cout << "👃 INHALE: Listening for spores..." << std::endl;
std::cout <<  << std::endl;
byte[] buf = new byte[64 * 1024]; // 64KB Buffer
while (running) {
try {
std::shared_ptr<DatagramPacket> packet = std::make_shared<DatagramPacket>(buf, buf.length);
socket.receive(packet);
// 1. Deserialize (Thaw DNA)
ByteArrayInputStream bis = new ByteArrayInputStream(
packet.getData(), 0, packet.getLength()
);
std::shared_ptr<ObjectInputStream> in = std::make_shared<ObjectInputStream>(bis);
NeuroQuant spore = (NeuroQuant) in.readObject();
// 2. Infect Local Cortex
// We add it to the lattice. The NCA rules will decide if it lives or dies.
sporesCaught++;
System.out.println("🦠 SPORE CAUGHT: " + spore.id +
" from " + packet.getAddress());
cortex.inject(spore.id);
} catch (EOFException e) {
// Ignore - likely our own broadcast
} catch (Exception e) {
// Ignore deserialization errors from other traffic
}
}
// Leave multicast group
if (netIf != null) {
socket.leaveGroup(groupAddr, netIf);
} else {
socket.leaveGroup(group);
}
} catch (Exception e) {
System.err.println("❌ Inhale failed: " + e.getMessage());
}
}
/**
* Get statistics
*/
public std::string getStats() {
return std::string.format("Cast: %d, Caught: %d", sporesCast, sporesCaught);
}
}
