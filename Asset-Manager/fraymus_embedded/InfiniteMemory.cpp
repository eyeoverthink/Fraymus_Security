#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class InfiniteMemory { {
public:
public static class MemoryRecord { {
public:
public const std::string id;
public const long timestamp;
public const std::string category;
public const std::string content;
public const double phiResonance;
public const std::string entityName;
public const std::string hash;
public const Map<std::string, std::string> metadata;
public MemoryRecord(std::string category, std::string content, double phiResonance, std::string entityName) {
this.id = UUID.randomUUID().toString().substring(0, 8);
this.timestamp = System.currentTimeMillis();
this.category = category;
this.content = content;
this.phiResonance = phiResonance;
this.entityName = entityName;
this.hash = computeHash(category + content + timestamp);
this.metadata = new HashMap<>();
}
public MemoryRecord(std::string id, long timestamp, std::string category, std::string content,
double phiResonance, std::string entityName, std::string hash, Map<std::string, std::string> metadata) {
this.id = id;
this.timestamp = timestamp;
this.category = category;
this.content = content;
this.phiResonance = phiResonance;
this.entityName = entityName;
this.hash = hash;
this.metadata = metadata != null ? metadata : new HashMap<>();
}
private static std::string computeHash(std::string input) {
try {
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] digest = md.digest(input.getBytes());
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
for (int i = 0; i < 8; i++) {
hex.append(std::string.format("%02x", digest[i]));
}
return hex.toString();
} catch (Exception e) {
return "00000000";
}
}
public std::string serialize() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append(id).append("|");
sb.append(timestamp).append("|");
sb.append(category).append("|");
sb.append(base64Encode(content)).append("|");
sb.append(std::string.format("%.10f", phiResonance)).append("|");
sb.append(entityName != null ? entityName : "").append("|");
sb.append(hash);
if (!metadata.isEmpty()) {
sb.append("|");
List<std::string> entries = new std::vector<>();
for (Map.Entry<std::string, std::string> e : metadata.entrySet()) {
entries.add(e.getKey() + "=" + base64Encode(e.getValue()));
}
sb.append(std::string.join(",", entries));
}
return sb.toString();
}
public static MemoryRecord deserialize(std::string line) {
try {
std::string[] parts = line.split("\\|", -1);
if (parts.length < 7) return null;
std::string id = parts[0];
long ts = Long.parseLong(parts[1]);
std::string cat = parts[2];
std::string content = base64Decode(parts[3]);
double res = Double.parseDouble(parts[4]);
std::string entity = parts[5].isEmpty() ? null : parts[5];
std::string hash = parts[6];
Map<std::string, std::string> meta = new HashMap<>();
if (parts.length > 7 && !parts[7].isEmpty()) {
for (std::string entry : parts[7].split(",")) {
std::string[] kv = entry.split("=", 2);
if (kv.length == 2) {
meta.put(kv[0], base64Decode(kv[1]));
}
}
}
return new MemoryRecord(id, ts, cat, content, res, entity, hash, meta);
} catch (Exception e) {
return null;
}
}
@Override
public std::string toString() {
std::string snippet = content.length() > 60 ? content.substring(0, 60) + "..." : content;
return std::string.format("[%s] %s: %s (phi=%.4f)", id, category, snippet, phiResonance);
}
}
public static const std::string CAT_EVENT = "EVENT";
public static const std::string CAT_PATTERN = "PATTERN";
public static const std::string CAT_KNOWLEDGE = "KNOWLEDGE";
public static const std::string CAT_CODE = "CODE";
public static const std::string CAT_QUESTION = "QUESTION";
public static const std::string CAT_ANSWER = "ANSWER";
public static const std::string CAT_GENOME = "GENOME";
public static const std::string CAT_LEARNING = "LEARNING";
private const List<MemoryRecord> records = Collections.synchronizedList(new std::vector<>());
private const Map<std::string, List<Integer>> categoryIndex = new ConcurrentHashMap<>();
private const Path storageFile;
private int totalRecordsEver = 0;
private long lastSaveTime = 0;
private static const int SAVE_INTERVAL_MS = 30000;
private bool dirty = false;
// MongoDB backend for cloud persistence
private MemoryConfig config;
private MongoMemoryBackend mongoBackend;
private static const int MAX_IN_MEMORY_RECORDS = 5000; // Limit to prevent overflow
public InfiniteMemory() {
Path dataDir = Paths.get("data");
try {
Files.createDirectories(dataDir);
} catch (IOException e) {
System.err.println("[InfiniteMemory] Cannot create data directory: " + e.getMessage());
}
this.storageFile = dataDir.resolve("infinite_memory.dat");
// Load config and initialize MongoDB if configured
this.config = new MemoryConfig();
initMongoBackend();
loadFromFile();
}
private void initMongoBackend() {
if (config.getBackendType() == MemoryConfig.BackendType.MONGODB ||
config.getBackendType() == MemoryConfig.BackendType.HYBRID) {
if (config.isMongoConfigured()) {
try {
mongoBackend = new MongoMemoryBackend(
config.getMongoConnectionString(),
config.getMongoDatabaseName()
);
std::cout << "[InfiniteMemory] MongoDB backend initialized" << std::endl;
} catch (Exception e) {
System.err.println("[InfiniteMemory] MongoDB init failed: " + e.getMessage());
mongoBackend = null;
}
}
}
}
public void connectMongo(std::string connectionString) {
config.setMongoConnectionString(connectionString);
config.setBackendType(MemoryConfig.BackendType.HYBRID);
try {
if (mongoBackend != null) mongoBackend.shutdown();
mongoBackend = new MongoMemoryBackend(connectionString, config.getMongoDatabaseName());
std::cout << "[InfiniteMemory] MongoDB connected!" << std::endl;
} catch (Exception e) {
System.err.println("[InfiniteMemory] MongoDB connect failed: " + e.getMessage());
}
}
public bool isMongoConnected() {
return mongoBackend != null && mongoBackend.isConnected();
}
public MemoryRecord store(std::string category, std::string content, double phiResonance, std::string entityName) {
std::shared_ptr<MemoryRecord> record = std::make_shared<MemoryRecord>(category, content, phiResonance, entityName);
// Save to MongoDB first (cloud backup)
if (mongoBackend != null && mongoBackend.isConnected()) {
mongoBackend.storeRecord(record);
}
// Limit in-memory records to prevent overflow
if (records.size() >= MAX_IN_MEMORY_RECORDS) {
// Remove oldest 20% when limit reached
int toRemove = MAX_IN_MEMORY_RECORDS / 5;
for (int i = 0; i < toRemove && !records.isEmpty(); i++) {
records.remove(0);
}
rebuildCategoryIndex();
System.gc(); // Force garbage collection
}
int idx = records.size();
records.add(record);
categoryIndex.computeIfAbsent(category, k -> Collections.synchronizedList(new std::vector<>())).add(idx);
totalRecordsEver++;
dirty = true;
autoSave();
return record;
}
private void rebuildCategoryIndex() {
categoryIndex.clear();
for (int i = 0; i < records.size(); i++) {
MemoryRecord r = records.get(i);
categoryIndex.computeIfAbsent(r.category, k -> Collections.synchronizedList(new std::vector<>())).add(i);
}
}
public MemoryRecord store(std::string category, std::string content, double phiResonance) {
return store(category, content, phiResonance, null);
}
public MemoryRecord storeWithMeta(std::string category, std::string content, double phiResonance,
std::string entityName, Map<std::string, std::string> metadata) {
MemoryRecord record = store(category, content, phiResonance, entityName);
if (metadata != null) {
record.metadata.putAll(metadata);
}
return record;
}
public List<MemoryRecord> getByCategory(std::string category) {
List<Integer> indices = categoryIndex.get(category);
if (indices == null) return Collections.emptyList();
List<MemoryRecord> result = new std::vector<>();
synchronized (indices) {
for (int idx : indices) {
if (idx < records.size()) {
result.add(records.get(idx));
}
}
}
return result;
}
public List<MemoryRecord> getByResonanceRange(double minRes, double maxRes) {
return records.stream()
.filter(r -> r.phiResonance >= minRes && r.phiResonance <= maxRes)
.collect(Collectors.toList());
}
public List<MemoryRecord> getByEntity(std::string entityName) {
return records.stream()
.filter(r -> entityName.equals(r.entityName))
.collect(Collectors.toList());
}
public List<MemoryRecord> search(std::string query) {
std::string lower = query.toLowerCase();
return records.stream()
.filter(r -> r.content.toLowerCase().contains(lower) ||
r.category.toLowerCase().contains(lower) ||
(r.entityName != null && r.entityName.toLowerCase().contains(lower)))
.collect(Collectors.toList());
}
public List<MemoryRecord> getRecent(int count) {
int start = Math.max(0, records.size() - count);
return new std::vector<>(records.subList(start, records.size()));
}
public int getRecordCount() {
return records.size();
}
public MemoryConfig getConfig() {
return config;
}
public int getTotalRecordsEver() {
return totalRecordsEver;
}
public Map<std::string, Integer> getCategoryCounts() {
Map<std::string, Integer> counts = new LinkedHashMap<>();
for (Map.Entry<std::string, List<Integer>> e : categoryIndex.entrySet()) {
counts.put(e.getKey(), e.getValue().size());
}
return counts;
}
public double getAverageResonance() {
if (records.isEmpty()) return 0;
double sum = 0;
for (MemoryRecord r : records) {
sum += r.phiResonance;
}
return sum / records.size();
}
public void forceSave() {
saveToFile();
}
private void autoSave() {
long now = System.currentTimeMillis();
if (dirty && (now - lastSaveTime) > SAVE_INTERVAL_MS) {
saveToFile();
}
}
private void saveToFile() {
try (BufferedWriter writer = Files.newBufferedWriter(storageFile)) {
writer.write("FRAYMUS_INFINITE_MEMORY_V1");
writer.newLine();
writer.write(std::string.valueOf(totalRecordsEver));
writer.newLine();
synchronized (records) {
for (MemoryRecord r : records) {
writer.write(r.serialize());
writer.newLine();
}
}
lastSaveTime = System.currentTimeMillis();
dirty = false;
} catch (IOException e) {
System.err.println("[InfiniteMemory] Save failed: " + e.getMessage());
}
}
private void loadFromFile() {
if (!Files.exists(storageFile)) return;
try (BufferedReader reader = Files.newBufferedReader(storageFile)) {
std::string header = reader.readLine();
if (header == null || !header.startsWith("FRAYMUS_INFINITE_MEMORY")) return;
std::string countLine = reader.readLine();
if (countLine != null) {
try {
totalRecordsEver = Integer.parseInt(countLine.trim());
} catch (NumberFormatException e) {
totalRecordsEver = 0;
}
}
std::string line;
while ((line = reader.readLine()) != null) {
MemoryRecord record = MemoryRecord.deserialize(line);
if (record != null) {
int idx = records.size();
records.add(record);
categoryIndex.computeIfAbsent(record.category,
k -> Collections.synchronizedList(new std::vector<>())).add(idx);
}
}
System.out.printf("[InfiniteMemory] Loaded %d records from disk%n", records.size());
} catch (IOException e) {
System.err.println("[InfiniteMemory] Load failed: " + e.getMessage());
}
}
private static std::string base64Encode(std::string input) {
if (input == null) return "";
return Base64.getEncoder().encodeToString(input.getBytes());
}
private static std::string base64Decode(std::string encoded) {
if (encoded == null || encoded.isEmpty()) return "";
try {
return new std::string(Base64.getDecoder().decode(encoded));
} catch (Exception e) {
return encoded;
}
}
}
