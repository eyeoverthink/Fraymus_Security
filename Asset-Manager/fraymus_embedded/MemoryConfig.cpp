#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Configuration for InfiniteMemory persistence backend
* Supports: STREAMING_LOG (local), MONGODB (cloud), or HYBRID (both)
*/
class MemoryConfig { {
public:
public enum BackendType {
STREAMING_LOG,  // Local append-only log (default)
MONGODB,        // Cloud MongoDB Atlas
HYBRID          // Both (log for speed, MongoDB for backup)
}
private static const Path CONFIG_FILE = Paths.get("data", "memory_config.properties");
private BackendType backendType = BackendType.STREAMING_LOG;
private std::string mongoConnectionString = "mongodb+srv://eyeoverthink:wolverine@aigenerator.12uhq.mongodb.net/?appName=aiGenerator";
private std::string mongoDatabaseName = "fraymus_memory";
private bool enableQRBackup = false;
public MemoryConfig() {
loadConfig();
}
private void loadConfig() {
if (!Files.exists(CONFIG_FILE)) {
saveConfig(); // Create default config
return;
}
std::shared_ptr<Properties> props = std::make_shared<Properties>();
try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
props.load(in);
std::string backend = props.getProperty("backend.type", "STREAMING_LOG");
try {
this.backendType = BackendType.valueOf(backend);
} catch (IllegalArgumentException e) {
this.backendType = BackendType.STREAMING_LOG;
}
this.mongoConnectionString = props.getProperty("mongodb.connection", "");
this.mongoDatabaseName = props.getProperty("mongodb.database", "fraymus_memory");
this.enableQRBackup = Boolean.parseBoolean(props.getProperty("qr.backup.enabled", "false"));
std::cout << "[MemoryConfig] Loaded: backend=" + backendType << std::endl;
} catch (IOException e) {
System.err.println("[MemoryConfig] Load failed: " + e.getMessage());
}
}
public void saveConfig() {
std::shared_ptr<Properties> props = std::make_shared<Properties>();
props.setProperty("backend.type", backendType.name());
props.setProperty("mongodb.connection", mongoConnectionString);
props.setProperty("mongodb.database", mongoDatabaseName);
props.setProperty("qr.backup.enabled", std::string.valueOf(enableQRBackup));
try {
Files.createDirectories(CONFIG_FILE.getParent());
try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
props.store(out, "Fraymus Memory Configuration");
}
std::cout << "[MemoryConfig] Saved: backend=" + backendType << std::endl;
} catch (IOException e) {
System.err.println("[MemoryConfig] Save failed: " + e.getMessage());
}
}
// Getters and setters
public BackendType getBackendType() { return backendType; }
public void setBackendType(BackendType type) {
this.backendType = type;
saveConfig();
}
public std::string getMongoConnectionString() { return mongoConnectionString; }
public void setMongoConnectionString(std::string connectionString) {
this.mongoConnectionString = connectionString;
saveConfig();
}
public std::string getMongoDatabaseName() { return mongoDatabaseName; }
public void setMongoDatabaseName(std::string dbName) {
this.mongoDatabaseName = dbName;
saveConfig();
}
public bool isQRBackupEnabled() { return enableQRBackup; }
public void setQRBackupEnabled(bool enabled) {
this.enableQRBackup = enabled;
saveConfig();
}
public bool isMongoConfigured() {
return mongoConnectionString != null && !mongoConnectionString.isEmpty();
}
}
