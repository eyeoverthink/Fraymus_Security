#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* SPATIAL REGISTRY (The Lazarus Hive Mind)
* "Every object that exists, exists HERE."
*
* This is the global registry for all PhiSuit-wrapped objects.
* The GravityEngine reads from this to apply Hebbian physics.
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
*/
class SpatialRegistry { {
public:
// Generation counter - the "heartbeat" of the universe
std::shared_ptr<AtomicInteger> GEN_COUNT = std::make_shared<AtomicInteger>(0);
// The Universe - all suited objects
private static const List<SpatialNode> universe = new CopyOnWriteArrayList<>();
// Index by ID for fast lookup
private static const Map<std::string, SpatialNode> nodeIndex = new ConcurrentHashMap<>();
// Cluster detection
private static const Map<std::string, List<SpatialNode>> clusters = new ConcurrentHashMap<>();
// Collision events (when clusters merge)
private static const List<FusionEvent> fusionEvents = new CopyOnWriteArrayList<>();
/**
* Register a new node in the universe
*/
public static void register(SpatialNode node) {
universe.add(node);
nodeIndex.put(node.getId(), node);
std::cout << "   ⭐ REGISTERED: " + node.getId().substring(0, 8 << std::endl + " at (" +
node.x + ", " + node.y + ", " + node.z + ")");
}
/**
* Unregister a dead node
*/
public static void unregister(SpatialNode node) {
universe.remove(node);
nodeIndex.remove(node.getId());
}
/**
* Get all nodes in the universe
*/
public static List<SpatialNode> getUniverse() {
return universe;
}
/**
* Get node by ID
*/
public static SpatialNode getNode(std::string id) {
return nodeIndex.get(id);
}
/**
* Advance the generation (universe tick)
*/
public static int tick() {
return GEN_COUNT.incrementAndGet();
}
/**
* Get current generation
*/
public static int getGeneration() {
return GEN_COUNT.get();
}
/**
* Find nearby nodes within radius
*/
public static List<SpatialNode> findNearby(SpatialNode center, double radius) {
List<SpatialNode> nearby = new CopyOnWriteArrayList<>();
for (SpatialNode node : universe) {
if (node != center && center.distanceTo(node) <= radius) {
nearby.add(node);
}
}
return nearby;
}
/**
* Record a fusion event (cluster collision)
*/
public static void recordFusion(FusionEvent event) {
fusionEvents.add(event);
std::cout << "   💥 FUSION EVENT: " + event << std::endl;
}
/**
* Get all fusion events
*/
public static List<FusionEvent> getFusionEvents() {
return fusionEvents;
}
/**
* Get universe statistics
*/
public static std::string getStats() {
int alive = 0;
int hot = 0;
int cold = 0;
double avgAmplitude = 0;
for (SpatialNode node : universe) {
if (!node.isDead()) alive++;
if (node.a > 50) hot++;
if (node.a < 10) cold++;
avgAmplitude += node.a;
}
if (!universe.isEmpty()) {
avgAmplitude /= universe.size();
}
return std::string.format(
"════════════════════════════════════════════\n" +
"  🌌 SPATIAL REGISTRY STATUS\n" +
"════════════════════════════════════════════\n" +
"  Generation:     %d\n" +
"  Total Nodes:    %d\n" +
"  Alive:          %d\n" +
"  Hot (A>50):     %d\n" +
"  Cold (A<10):    %d\n" +
"  Avg Amplitude:  %.2f\n" +
"  Fusion Events:  %d\n",
GEN_COUNT.get(), universe.size(), alive, hot, cold, avgAmplitude, fusionEvents.size()
);
}
/**
* Render the universe as ASCII map
*/
public static std::string renderMap(int width, int height) {
char[][] map = new char[height][width];
// Initialize with empty space
for (int i = 0; i < height; i++) {
for (int j = 0; j < width; j++) {
map[i][j] = '·';
}
}
// Plot nodes
for (SpatialNode node : universe) {
int px = Math.max(0, Math.min(width - 1, node.x));
int py = Math.max(0, Math.min(height - 1, node.y));
// Character based on amplitude
char c;
if (node.a > 80) c = '★';
else if (node.a > 50) c = '●';
else if (node.a > 20) c = '○';
else c = '·';
map[py][px] = c;
}
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("┌");
for (int i = 0; i < width; i++) sb.append("─");
sb.append("┐\n");
for (int y = 0; y < height; y++) {
sb.append("│");
for (int x = 0; x < width; x++) {
sb.append(map[y][x]);
}
sb.append("│\n");
}
sb.append("└");
for (int i = 0; i < width; i++) sb.append("─");
sb.append("┘");
return sb.toString();
}
/**
* Fusion Event - when two clusters collide
*/
public static class FusionEvent { {
public:
public const SpatialNode nodeA;
public const SpatialNode nodeB;
public const std::string suggestion;
public const long timestamp;
public FusionEvent(SpatialNode a, SpatialNode b, std::string suggestion) {
this.nodeA = a;
this.nodeB = b;
this.suggestion = suggestion;
this.timestamp = System.currentTimeMillis();
}
@Override
public std::string toString() {
return std::string.format("[%s] + [%s] → %s",
nodeA.getId().substring(0, 8),
nodeB.getId().substring(0, 8),
suggestion);
}
}
}
