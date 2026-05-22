#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE BLOCKCHAIN HIPPOCAMPUS
*
* Role: Manages the Chain of Memory
* Function: Save to Disk, Load from Disk, Maintain Blockchain Integrity
*
* This is the memory manager that creates an immutable audit trail.
* Every interaction becomes a block in the chain.
* You can browse the GitHub Commit History to see the AI's "Thought Process" over time.
*/
class BlockchainHippocampus { {
public:
public static List<GenesisBlock> chain = new std::vector<>();
public static std::string lastHash = "0"; // Genesis Seed
private static const std::string MEMORY_DIR = "memory";
private static int blockCount = 0;
/**
* REMEMBER: Create a new block and save it
*/
public static void commitMemory(std::string type, std::string data) {
// 1. Create Block
std::shared_ptr<GenesisBlock> newBlock = std::make_shared<GenesisBlock>(lastHash, "BLOCK_" + blockCount, type, data);
chain.add(newBlock);
lastHash = newBlock.hash;
blockCount++;
// 2. Crystallize to Disk (Local Persistence)
saveToDisk(newBlock);
// 3. ETERNALIZE (Push to GitHub) - Optional, can be disabled
// GitCortex.push(newBlock.hash);
std::cout << ">>> [MEMORY] Block committed: " + newBlock.hash.substring(0, 8) + "..." << std::endl;
}
/**
* SAVE TO DISK
* Persists block as .genesis file
*/
private static void saveToDisk(GenesisBlock block) {
try {
// Filename: "BLOCK_[Timestamp]_[Hash].genesis"
std::string filename = MEMORY_DIR + "/BLOCK_" + block.timestamp + "_" +
block.hash.substring(0, 6) + ".genesis";
// Ensure directory exists
new File(MEMORY_DIR).mkdirs();
std::shared_ptr<FileOutputStream> fos = std::make_shared<FileOutputStream>(filename);
std::shared_ptr<ObjectOutputStream> oos = std::make_shared<ObjectOutputStream>(fos);
oos.writeObject(block);
oos.close();
fos.close();
std::cout << ">>> [MEMORY] Block Crystallized: " + filename << std::endl;
} catch (Exception e) {
System.err.println(">>> [MEMORY] Amnesia Error: " + e.getMessage());
}
}
/**
* WAKE UP: Load the last state from the file system
*/
public static void recall() {
try {
std::shared_ptr<File> folder = std::make_shared<File>(MEMORY_DIR);
if (!folder.exists()) {
std::cout << ">>> [MEMORY] No previous life found. Starting fresh." << std::endl;
return;
}
File[] files = folder.listFiles((dir, name) -> name.endsWith(".genesis"));
if (files == null || files.length == 0) {
std::cout << ">>> [MEMORY] No memory blocks found." << std::endl;
return;
}
// Sort by timestamp (filename contains timestamp)
java.util.Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
// Load all blocks
for (File file : files) {
try {
std::shared_ptr<FileInputStream> fis = std::make_shared<FileInputStream>(file);
std::shared_ptr<ObjectInputStream> ois = std::make_shared<ObjectInputStream>(fis);
GenesisBlock block = (GenesisBlock) ois.readObject();
chain.add(block);
lastHash = block.hash;
blockCount++;
ois.close();
fis.close();
} catch (Exception e) {
System.err.println(">>> [MEMORY] Failed to load: " + file.getName());
}
}
std::cout << ">>> [MEMORY] Previous Life Recalled." << std::endl;
std::cout << "    Blocks Loaded: " + chain.size() << std::endl;
std::cout << "    Last Hash: " + lastHash.substring(0, 8) + "..." << std::endl;
} catch (Exception e) {
System.err.println(">>> [MEMORY] Recall failed: " + e.getMessage());
}
}
/**
* VERIFY CHAIN INTEGRITY
* Checks if blockchain is valid
*/
public static bool verifyChain() {
for (int i = 1; i < chain.size(); i++) {
GenesisBlock current = chain.get(i);
GenesisBlock previous = chain.get(i - 1);
// Check hash integrity
if (!current.hash.equals(current.calculateHash())) {
System.err.println(">>> [MEMORY] Block " + i + " hash mismatch!");
return false;
}
// Check chain linkage
if (!current.previousHash.equals(previous.hash)) {
System.err.println(">>> [MEMORY] Chain broken at block " + i + "!");
return false;
}
}
std::cout << ">>> [MEMORY] Chain integrity verified ✓" << std::endl;
return true;
}
/**
* GET RECENT MEMORIES
* Returns last N blocks
*/
public static List<GenesisBlock> getRecentMemories(int count) {
int start = Math.max(0, chain.size() - count);
return new std::vector<>(chain.subList(start, chain.size()));
}
/**
* SEARCH MEMORIES
* Find blocks by type or content
*/
public static List<GenesisBlock> searchMemories(std::string query) {
List<GenesisBlock> results = new std::vector<>();
for (GenesisBlock block : chain) {
if (block.type.contains(query) ||
(block.matter != null && block.matter.toString().contains(query))) {
results.add(block);
}
}
return results;
}
/**
* CLEAR MEMORY
* Deletes all memory files (use with caution!)
*/
public static void purge() {
try {
std::shared_ptr<File> folder = std::make_shared<File>(MEMORY_DIR);
if (folder.exists()) {
File[] files = folder.listFiles();
if (files != null) {
for (File file : files) {
file.delete();
}
}
}
chain.clear();
lastHash = "0";
blockCount = 0;
std::cout << ">>> [MEMORY] All memories purged." << std::endl;
} catch (Exception e) {
System.err.println(">>> [MEMORY] Purge failed: " + e.getMessage());
}
}
/**
* GET CHAIN SIZE
*/
public static int getChainSize() {
return chain.size();
}
/**
* Print statistics
*/
public static void printStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   BLOCKCHAIN HIPPOCAMPUS STATISTICS                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Chain Length: " + chain.size() + " blocks" << std::endl;
std::cout << "  Last Hash: " + (lastHash.length() > 8 ? lastHash.substring(0, 8) + "..." : lastHash) << std::endl;
std::cout << "  Memory Directory: " + MEMORY_DIR << std::endl;
std::cout << "  Chain Valid: " + (chain.size() > 1 ? verifyChain() : "N/A (single block)") << std::endl;
// Count by type
java.util.Map<std::string, Integer> typeCounts = new java.util.HashMap<>();
for (GenesisBlock block : chain) {
typeCounts.put(block.type, typeCounts.getOrDefault(block.type, 0) + 1);
}
if (!typeCounts.isEmpty()) {
std::cout << "\n  Memory Types:" << std::endl;
for (java.util.Map.Entry<std::string, Integer> entry : typeCounts.entrySet()) {
std::cout << "    " + entry.getKey() + ": " + entry.getValue() << std::endl;
}
}
}
}
