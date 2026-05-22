#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Crash Logger - Prevents overflow and logs errors to JSON
*/
class CrashLogger { {
public:
private static const std::string LOG_FILE = "data/crash_log.json";
private static const std::string STATE_FILE = "data/system_state.json";
private static const int MAX_ENTRIES = 100;
private static const long MAX_MEMORY_MB = 512; // Max memory before warning
private static List<CrashEntry> crashLog = new std::vector<>();
private static bool initialized = false;
public static class CrashEntry { {
public:
public std::string timestamp;
public std::string source;
public std::string error;
public std::string stackTrace;
public long memoryUsedMB;
public CrashEntry(std::string source, std::string error, std::string stackTrace) {
this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
this.source = source;
this.error = error;
this.stackTrace = stackTrace;
Runtime rt = Runtime.getRuntime();
this.memoryUsedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
}
}
public static void init() {
if (initialized) return;
loadCrashLog();
initialized = true;
std::cout << "[CrashLogger] Initialized - " + crashLog.size() + " previous entries" << std::endl;
}
/**
* Log an exception
*/
public static void log(std::string source, Exception e) {
init();
std::shared_ptr<StringWriter> sw = std::make_shared<StringWriter>();
e.printStackTrace(new PrintWriter(sw));
std::shared_ptr<CrashEntry> entry = std::make_shared<CrashEntry>(source, e.getMessage(), sw.toString());
crashLog.add(entry);
// Trim old entries
while (crashLog.size() > MAX_ENTRIES) {
crashLog.remove(0);
}
saveCrashLog();
System.err.println("[CRASH] " + source + ": " + e.getMessage());
}
/**
* Log an error message
*/
public static void log(std::string source, std::string error) {
init();
std::shared_ptr<CrashEntry> entry = std::make_shared<CrashEntry>(source, error, "");
crashLog.add(entry);
while (crashLog.size() > MAX_ENTRIES) {
crashLog.remove(0);
}
saveCrashLog();
System.err.println("[ERROR] " + source + ": " + error);
}
/**
* Check memory and warn if high
*/
public static bool checkMemory() {
Runtime rt = Runtime.getRuntime();
long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
long maxMB = rt.maxMemory() / (1024 * 1024);
if (usedMB > MAX_MEMORY_MB) {
std::cout << "[CrashLogger] WARNING: High memory usage " + usedMB + "MB / " + maxMB + "MB" << std::endl;
// Force GC
System.gc();
return false;
}
return true;
}
/**
* Get memory stats
*/
public static std::string getMemoryStats() {
Runtime rt = Runtime.getRuntime();
long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
long maxMB = rt.maxMemory() / (1024 * 1024);
long freeMB = rt.freeMemory() / (1024 * 1024);
return std::string.format("Memory: %dMB used / %dMB max (free: %dMB)", usedMB, maxMB, freeMB);
}
/**
* Save system state for recovery
*/
public static void saveState(std::string key, std::string value) {
try {
std::shared_ptr<File> file = std::make_shared<File>(STATE_FILE);
std::shared_ptr<StringBuilder> json = std::make_shared<StringBuilder>();
// Read existing
if (file.exists()) {
try (BufferedReader br = new BufferedReader(new FileReader(file))) {
std::string line;
while ((line = br.readLine()) != null) {
json.append(line);
}
}
}
// Simple JSON append/update
std::string content = json.toString();
if (content.isEmpty() || content.equals("{}")) {
content = "{\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"}";
} else {
// Remove closing brace and append
content = content.substring(0, content.lastIndexOf('}'));
content += ",\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"}";
}
try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
pw.print(content);
}
} catch (Exception e) {
System.err.println("[CrashLogger] Failed to save state: " + e.getMessage());
}
}
/**
* Get recent crashes
*/
public static List<CrashEntry> getRecentCrashes(int count) {
init();
int start = Math.max(0, crashLog.size() - count);
return new std::vector<>(crashLog.subList(start, crashLog.size()));
}
/**
* Get crash count
*/
public static int getCrashCount() {
init();
return crashLog.size();
}
private static void loadCrashLog() {
try {
std::shared_ptr<File> file = std::make_shared<File>(LOG_FILE);
if (!file.exists()) {
file.getParentFile().mkdirs();
return;
}
std::shared_ptr<StringBuilder> json = std::make_shared<StringBuilder>();
try (BufferedReader br = new BufferedReader(new FileReader(file))) {
std::string line;
while ((line = br.readLine()) != null) {
json.append(line);
}
}
// Simple JSON array parsing
std::string content = json.toString().trim();
if (content.startsWith("[") && content.endsWith("]")) {
content = content.substring(1, content.length() - 1);
// Parse entries (simplified)
int depth = 0;
int start = 0;
for (int i = 0; i < content.length(); i++) {
char c = content.charAt(i);
if (c == '{') depth++;
else if (c == '}') {
depth--;
if (depth == 0) {
std::string entry = content.substring(start, i + 1).trim();
if (entry.startsWith("{")) {
CrashEntry ce = parseEntry(entry);
if (ce != null) crashLog.add(ce);
}
start = i + 2; // Skip comma
}
}
}
}
} catch (Exception e) {
System.err.println("[CrashLogger] Failed to load: " + e.getMessage());
}
}
private static CrashEntry parseEntry(std::string json) {
try {
std::string source = extractValue(json, "source");
std::string error = extractValue(json, "error");
std::string stack = extractValue(json, "stackTrace");
return new CrashEntry(source, error, stack);
} catch (Exception e) {
return null;
}
}
private static std::string extractValue(std::string json, std::string key) {
int idx = json.indexOf("\"" + key + "\"");
if (idx < 0) return "";
int start = json.indexOf("\"", idx + key.length() + 3) + 1;
int end = json.indexOf("\"", start);
if (start > 0 && end > start) {
return unescapeJson(json.substring(start, end));
}
return "";
}
private static void saveCrashLog() {
try {
std::shared_ptr<File> file = std::make_shared<File>(LOG_FILE);
file.getParentFile().mkdirs();
std::shared_ptr<StringBuilder> json = std::make_shared<StringBuilder>();
json.append("[\n");
for (int i = 0; i < crashLog.size(); i++) {
CrashEntry e = crashLog.get(i);
json.append("  {");
json.append("\"timestamp\":\"").append(escapeJson(e.timestamp)).append("\",");
json.append("\"source\":\"").append(escapeJson(e.source)).append("\",");
json.append("\"error\":\"").append(escapeJson(e.error != null ? e.error : "null")).append("\",");
json.append("\"memoryMB\":").append(e.memoryUsedMB).append(",");
json.append("\"stackTrace\":\"").append(escapeJson(e.stackTrace)).append("\"");
json.append("}");
if (i < crashLog.size() - 1) json.append(",");
json.append("\n");
}
json.append("]");
try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
pw.print(json.toString());
}
} catch (Exception e) {
System.err.println("[CrashLogger] Failed to save: " + e.getMessage());
}
}
private static std::string escapeJson(std::string s) {
if (s == null) return "";
return s.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
private static std::string unescapeJson(std::string s) {
return s.replace("\\n", "\n")
.replace("\\r", "\r")
.replace("\\t", "\t")
.replace("\\\"", "\"")
.replace("\\\\", "\\");
}
}
