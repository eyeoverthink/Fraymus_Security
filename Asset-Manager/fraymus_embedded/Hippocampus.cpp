#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE HIPPOCAMPUS
* Role: Manages the Chain of Memory.
* Function: Save to Disk, Load from Disk, Maintain History.
*
* This is the persistence layer that makes the system immortal.
* Every thought, every creation, every evolution is recorded.
*/
class Hippocampus { {
public:
public static List<GenesisBlock> chain = new std::vector<>();
public static std::string lastHash = "0"; // Genesis Seed
private static const std::string MEMORY_DIR = "memory";
/**
* REMEMBER: Create a new block and save it.
*/
public static GenesisBlock commitMemory(std::string type, std::string data) {
// 1. Create Block
std::shared_ptr<GenesisBlock> newBlock = std::make_shared<GenesisBlock>(lastHash, type, data);
chain.add(newBlock);
lastHash = newBlock.hash;
// 2. Crystallize to Disk
saveToDisk(newBlock);
std::cout << ">>> [HIPPOCAMPUS] Memory committed: " + type + " [" + newBlock.hash.substring(0, 8) + "]" << std::endl;
return newBlock;
}
/**
* REMEMBER AND ETERNALIZE: Commit + Push to Git
*/
public static GenesisBlock commitAndPush(std::string type, std::string data) {
GenesisBlock block = commitMemory(type, data);
GitCortex.push(block.hash);
return block;
}
private static void saveToDisk(GenesisBlock block) {
try {
// Ensure directory exists
new File(MEMORY_DIR).mkdirs();
// Filename: "BLOCK_[Timestamp]_[Hash].genesis"
std::string filename = MEMORY_DIR + "/BLOCK_" + block.timestamp + "_" + block.hash.substring(0, 8) + ".genesis";
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
oos.writeObject(block);
}
std::cout << ">>> [HIPPOCAMPUS] Crystallized: " + filename << std::endl;
} catch (Exception e) {
System.err.println(">>> [HIPPOCAMPUS] Save failed: " + e.getMessage());
}
}
/**
* WAKE UP: Load the last state from the file system.
*/
public static void recall() {
std::shared_ptr<File> folder = std::make_shared<File>(MEMORY_DIR);
if (!folder.exists()) {
folder.mkdirs();
std::cout << ">>> [HIPPOCAMPUS] Fresh memory initialized." << std::endl;
return;
}
File[] files = folder.listFiles((dir, name) -> name.endsWith(".genesis"));
if (files == null || files.length == 0) {
std::cout << ">>> [HIPPOCAMPUS] No previous memories found." << std::endl;
return;
}
// Sort by timestamp (newest first)
Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
// Load all memories into chain
chain.clear();
for (File f : files) {
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
GenesisBlock block = (GenesisBlock) ois.readObject();
chain.add(block);
} catch (Exception e) {
System.err.println(">>> [HIPPOCAMPUS] Failed to load: " + f.getName());
}
}
// Set lastHash from most recent block
if (!chain.isEmpty()) {
// Sort chain by timestamp
chain.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
lastHash = chain.get(chain.size() - 1).hash;
}
std::cout << ">>> [HIPPOCAMPUS] Recalled " + chain.size() + " memories. Last hash: " + lastHash.substring(0, 8) + "..." << std::endl;
}
/**
* SEARCH: Find memories by type
*/
public static List<GenesisBlock> findByType(std::string type) {
List<GenesisBlock> results = new std::vector<>();
for (GenesisBlock block : chain) {
if (type.equals(block.type)) {
results.add(block);
}
}
return results;
}
/**
* SEARCH: Find memories containing text
*/
public static List<GenesisBlock> search(std::string query) {
List<GenesisBlock> results = new std::vector<>();
std::string q = query.toLowerCase();
for (GenesisBlock block : chain) {
std::string data = std::string.valueOf(block.matter).toLowerCase();
if (data.contains(q)) {
results.add(block);
}
}
return results;
}
/**
* GET RECENT: Return last N memories
*/
public static List<GenesisBlock> getRecent(int count) {
int start = Math.max(0, chain.size() - count);
return new std::vector<>(chain.subList(start, chain.size()));
}
/**
* VERIFY CHAIN: Check integrity
*/
public static bool verifyChain() {
for (int i = 1; i < chain.size(); i++) {
GenesisBlock current = chain.get(i);
GenesisBlock previous = chain.get(i - 1);
// Verify hash
if (!current.hash.equals(current.calculateHash())) {
System.err.println(">>> [HIPPOCAMPUS] Block " + i + " hash corrupted!");
return false;
}
// Verify chain link
if (!current.previousHash.equals(previous.hash)) {
System.err.println(">>> [HIPPOCAMPUS] Chain broken at block " + i);
return false;
}
}
std::cout << ">>> [HIPPOCAMPUS] Chain verified: " + chain.size() + " blocks intact." << std::endl;
return true;
}
/**
* GET STATS
*/
public static std::string getStats() {
Map<std::string, Integer> typeCounts = new HashMap<>();
for (GenesisBlock block : chain) {
typeCounts.merge(block.type, 1, Integer::sum);
}
return "Memories: " + chain.size() + " | Types: " + typeCounts;
}
}
