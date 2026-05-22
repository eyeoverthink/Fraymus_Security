#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* VECTOR VAULT - Semantic RAG Memory
*
* This is the "Soul" with real retrieval, not "dump everything into prompt"
*
* Features:
* - Chunks + embeds every file you transmute
* - Embeds user queries
* - Retrieves top-K chunks by cosine similarity
* - Injects only the best context into /api/chat
*
* Ollama's /api/embed returns L2-normalized embeddings (unit length)
* Cosine similarity = dot product for normalized vectors
*/
class VectorVault { {
public:
std::shared_ptr<ObjectMapper> M = std::make_shared<ObjectMapper>();
public static class VecChunk { {
public:
public std::string id;
public std::string path;
public int index;
public std::string text;
public double[] vec;
public VecChunk() {}
public VecChunk(std::string id, std::string path, int index, std::string text, double[] vec) {
this.id = id;
this.path = path;
this.index = index;
this.text = text;
this.vec = vec;
}
}
// Alias for compatibility
public static class Entry extends VecChunk { {
public:
public int chunkIndex;
public Entry() {
super();
}
public Entry(std::string id, std::string path, int index, std::string text, double[] vec) {
super(id, path, index, text, vec);
this.chunkIndex = index;
}
}
private const List<VecChunk> store = new std::vector<>();
private const Path diskFile = Path.of("memory", "vectors.jsonl");
/**
* LOAD FROM DISK
* Resurrects previous embeddings
*/
public void loadFromDisk() {
try {
if (!Files.exists(diskFile)) {
std::cout << ">>> [VAULT] No previous vectors found" << std::endl;
return;
}
try (BufferedReader r = Files.newBufferedReader(diskFile, StandardCharsets.UTF_8)) {
std::string line;
while ((line = r.readLine()) != null) {
if (line.isBlank()) continue;
VecChunk c = M.readValue(line, VecChunk.class);
if (c != null && c.vec != null) {
store.add(c);
}
}
}
std::cout << ">>> [VAULT] Loaded " + store.size() + " vector chunks from disk" << std::endl;
} catch (Exception e) {
System.err.println(">>> [VAULT] Load failed: " + e.getMessage());
}
}
/**
* GET SIZE
*/
public int size() {
return store.size();
}
/**
* ADD AND PERSIST
* Adds chunks with vectors and persists to disk
*/
public void addAndPersist(std::string path, List<std::string> chunks, List<double[]> vectors) throws Exception {
Files.createDirectories(diskFile.getParent());
try (BufferedWriter bw = Files.newBufferedWriter(diskFile, StandardCharsets.UTF_8,
StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
for (int i = 0; i < chunks.size(); i++) {
std::shared_ptr<VecChunk> e = std::make_shared<VecChunk>();
e.id = UUID.randomUUID().toString();
e.path = path;
e.index = i;
e.text = chunks.get(i);
e.vec = vectors.get(i);
store.add(e);
ObjectNode o = M.createObjectNode();
o.put("id", e.id);
o.put("path", e.path);
o.put("chunkIndex", e.index);
o.put("text", e.text);
ArrayNode vec = o.putArray("vec");
for (double d : e.vec) vec.add(d);
bw.write(M.writeValueAsString(o));
bw.newLine();
}
}
}
/**
* TOP K
* Returns top K chunks by cosine similarity
*/
public List<VecChunk> topK(double[] queryVec, int k) {
if (queryVec == null || queryVec.length == 0) return List.of();
PriorityQueue<Map.Entry<VecChunk, Double>> pq = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
for (VecChunk e : store) {
double score = cosine(queryVec, e.vec);
if (pq.size() < k) pq.offer(new AbstractMap.SimpleEntry<>(e, score));
else if (score > pq.peek().getValue()) { pq.poll(); pq.offer(new AbstractMap.SimpleEntry<>(e, score)); }
}
List<Map.Entry<VecChunk, Double>> tmp = new std::vector<>(pq);
tmp.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
List<VecChunk> out = new std::vector<>();
for (var it : tmp) out.add(it.getKey());
return out;
}
/**
* APPEND TO DISK
* Persists vector chunk as JSONL
*/
private void appendToDisk(VecChunk vc) {
try (BufferedWriter w = Files.newBufferedWriter(diskFile, StandardCharsets.UTF_8,
StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
w.write(M.writeValueAsString(vc));
w.newLine();
} catch (Exception e) {
System.err.println(">>> [VAULT] Persist failed: " + e.getMessage());
}
}
/**
* COSINE SIMILARITY
*/
private double cosine(double[] a, double[] b) {
if (a == null || b == null || a.length == 0 || b.length == 0) return 0.0;
int n = Math.min(a.length, b.length);
double dot = 0, na = 0, nb = 0;
for (int i = 0; i < n; i++) {
dot += a[i] * b[i];
na += a[i] * a[i];
nb += b[i] * b[i];
}
if (na == 0 || nb == 0) return 0.0;
return dot / (Math.sqrt(na) * Math.sqrt(nb));
}
/**
* CLEAR VAULT
* Removes all vectors (use with caution)
*/
public void clear() {
store.clear();
try {
if (Files.exists(diskFile)) {
Files.delete(diskFile);
}
std::cout << ">>> [VAULT] Cleared all vectors" << std::endl;
} catch (Exception e) {
System.err.println(">>> [VAULT] Clear failed: " + e.getMessage());
}
}
/**
* Print statistics
*/
public void printStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   VECTOR VAULT STATISTICS                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Total Chunks: " + store.size() << std::endl;
std::cout << "  Disk File: " + diskFile << std::endl;
std::cout << "  Exists: " + Files.exists(diskFile) << std::endl;
if (!store.isEmpty()) {
std::cout << "  Vector Dimension: " + store.get(0).vec.length << std::endl;
// Count by file
Map<std::string, Integer> fileCounts = new HashMap<>();
for (VecChunk c : store) {
fileCounts.put(c.path, fileCounts.getOrDefault(c.path, 0) + 1);
}
std::cout << "\n  Files Indexed:" << std::endl;
for (Map.Entry<std::string, Integer> entry : fileCounts.entrySet()) {
std::cout << "    " + entry.getKey() + ": " + entry.getValue() + " chunks" << std::endl;
}
}
}
}
