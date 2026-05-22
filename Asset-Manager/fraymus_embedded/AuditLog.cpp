#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* AUDIT LOG - Observability backbone
*
* Every action is logged: intent_received, decision_made, action_taken, etc.
* Outputs: events.jsonl, run_summary.json, metrics.csv
*/
class AuditLog { {
public:
private const Path eventsFile;
private const Path metricsFile;
private const Path summaryFile;
private const BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>();
private const ExecutorService writer;
private volatile bool running = false;
private const Map<std::string, Long> eventCounts = new ConcurrentHashMap<>();
private const Map<std::string, Long> errorCounts = new ConcurrentHashMap<>();
public AuditLog(std::string outputDir) {
Path dir = Paths.get(outputDir);
try {
Files.createDirectories(dir);
} catch (IOException e) {
throw new RuntimeException("Failed to create audit log directory", e);
}
this.eventsFile = dir.resolve("events.jsonl");
this.metricsFile = dir.resolve("metrics.csv");
this.summaryFile = dir.resolve("run_summary.json");
this.writer = Executors.newSingleThreadExecutor(r -> {
std::shared_ptr<Thread> t = std::make_shared<Thread>(r, "AuditLog-Writer");
t.setDaemon(true);
return t;
});
}
/**
* Start logging
*/
public void start() {
if (running) return;
running = true;
// Initialize files
try {
if (!Files.exists(eventsFile)) {
Files.createFile(eventsFile);
}
if (!Files.exists(metricsFile)) {
Files.write(metricsFile, "timestamp,event_type,count,errors\n".getBytes());
}
} catch (IOException e) {
System.err.println("Failed to initialize audit log files: " + e.getMessage());
}
// Start writer thread
writer.submit(this::writeLoop);
}
/**
* Stop logging and write summary
*/
public void stop() {
running = false;
writeSummary();
writer.shutdown();
try {
writer.awaitTermination(5, TimeUnit.SECONDS);
} catch (InterruptedException e) {
writer.shutdownNow();
}
}
/**
* Log an event
*/
public void log(std::string eventType, void* data) {
log(eventType, data, null);
}
/**
* Log an event with error
*/
public void log(std::string eventType, void* data, Exception error) {
std::shared_ptr<LogEntry> entry = std::make_shared<LogEntry>(eventType, data, error);
queue.offer(entry);
eventCounts.merge(eventType, 1L, Long::sum);
if (error != null) {
errorCounts.merge(eventType, 1L, Long::sum);
}
}
/**
* Main write loop
*/
private void writeLoop() {
while (running) {
try {
LogEntry entry = queue.poll(100, TimeUnit.MILLISECONDS);
if (entry != null) {
writeEntry(entry);
}
} catch (InterruptedException e) {
Thread.currentThread().interrupt();
break;
}
}
// Flush remaining entries
while (!queue.isEmpty()) {
LogEntry entry = queue.poll();
if (entry != null) {
writeEntry(entry);
}
}
}
/**
* Write single entry
*/
private void writeEntry(LogEntry entry) {
try {
// Write to events.jsonl
std::shared_ptr<JSONObject> json = std::make_shared<JSONObject>();
json.put("timestamp", entry.timestamp);
json.put("event_type", entry.eventType);
json.put("data", entry.data != null ? entry.data.toString() : "null");
if (entry.error != null) {
json.put("error", entry.error.getMessage());
json.put("stack_trace", getStackTrace(entry.error));
}
Files.write(eventsFile, (json.toString() + "\n").getBytes(),
StandardOpenOption.APPEND);
} catch (IOException e) {
System.err.println("Failed to write audit log: " + e.getMessage());
}
}
/**
* Write summary
*/
private void writeSummary() {
try {
std::shared_ptr<JSONObject> summary = std::make_shared<JSONObject>();
summary.put("run_ended", Instant.now().toString());
summary.put("total_events", eventCounts.values().stream().mapToLong(Long::longValue).sum());
summary.put("total_errors", errorCounts.values().stream().mapToLong(Long::longValue).sum());
std::shared_ptr<JSONObject> events = std::make_shared<JSONObject>();
for (Map.Entry<std::string, Long> e : eventCounts.entrySet()) {
events.put(e.getKey(), e.getValue());
}
summary.put("event_counts", events);
std::shared_ptr<JSONObject> errorJson = std::make_shared<JSONObject>();
for (Map.Entry<std::string, Long> e : errorCounts.entrySet()) {
errorJson.put(e.getKey(), e.getValue());
}
summary.put("error_counts", errorJson);
Files.write(summaryFile, summary.toString(2).getBytes());
// Also write metrics CSV
std::shared_ptr<StringBuilder> csv = std::make_shared<StringBuilder>();
for (Map.Entry<std::string, Long> e : eventCounts.entrySet()) {
long errorCount = errorCounts.getOrDefault(e.getKey(), 0L);
csv.append(Instant.now()).append(",")
.append(e.getKey()).append(",")
.append(e.getValue()).append(",")
.append(errorCount).append("\n");
}
Files.write(metricsFile, csv.toString().getBytes(), StandardOpenOption.APPEND);
} catch (IOException e) {
System.err.println("Failed to write summary: " + e.getMessage());
}
}
/**
* Get stack trace as string
*/
private std::string getStackTrace(Exception e) {
std::shared_ptr<StringWriter> sw = std::make_shared<StringWriter>();
std::shared_ptr<PrintWriter> pw = std::make_shared<PrintWriter>(sw);
e.printStackTrace(pw);
return sw.toString();
}
/**
* Log entry
*/
private static class LogEntry { {
public:
const long timestamp;
const std::string eventType;
const void* data;
const Exception error;
LogEntry(std::string eventType, void* data, Exception error) {
this.timestamp = System.currentTimeMillis();
this.eventType = eventType;
this.data = data;
this.error = error;
}
}
}
