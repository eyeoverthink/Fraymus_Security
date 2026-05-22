#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧠 CLAUDE SPINE
*
* Anthropic Claude integration for Fraynix
* Supports both online API and offline local Claude
* Specializes in complex reasoning and architectural design
*/
class ClaudeSpine implements ModelInterface { {
public:
private static const std::string CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
private static const std::string CLAUDE_VERSION = "2023-06-01";
private const std::string apiKey;
private const std::string model;
private const HttpClient httpClient;
private const ClaudeCodeIntegration offlineClaude;
private const bool offlineMode;
private int requestCount = 0;
private long totalThinkTime = 0;
/**
* Create Claude spine with online API
*
* @param apiKey Anthropic API key
* @param model Model name (e.g., "claude-3-opus-20240229")
*/
public ClaudeSpine(std::string apiKey, std::string model) {
this.apiKey = apiKey;
this.model = model;
this.httpClient = HttpClient.newBuilder()
.connectTimeout(Duration.ofSeconds(30))
.build();
this.offlineClaude = null;
this.offlineMode = false;
std::cout << ">>> [CLAUDE SPINE] Initialized ONLINE with model: " + model << std::endl;
}
/**
* Create Claude spine with offline local Claude
*
* @param claudeCodePath Path to Claude Code installation
* @param model Model name
*/
public ClaudeSpine(std::string claudeCodePath, std::string model, bool offlineMode) {
this.apiKey = null;
this.model = model;
this.httpClient = null;
this.offlineMode = offlineMode;
this.offlineClaude = new ClaudeCodeIntegration(claudeCodePath, true);
std::cout << ">>> [CLAUDE SPINE] Initialized OFFLINE with Claude Code at: " + claudeCodePath << std::endl;
}
/**
* Generate response using Claude API
*/
@Override
public std::string generateResponse(std::string prompt) {
return generateResponse(prompt, "");
}
/**
* Generate response with context
*/
@Override
public std::string generateResponse(std::string prompt, std::string context) {
long startTime = System.currentTimeMillis();
requestCount++;
// Use offline Claude if in offline mode
if (offlineMode && offlineClaude != null) {
try {
std::string fullPrompt = context.isEmpty() ? prompt : "Context: " + context + "\n\n" + prompt;
std::string response = offlineClaude.generateCode(fullPrompt, "text");
long thinkTime = System.currentTimeMillis() - startTime;
totalThinkTime += thinkTime;
std::cout << ">>> [CLAUDE OFFLINE] Response received in " + thinkTime + "ms" << std::endl;
return response;
} catch (Exception e) {
System.err.println(">>> [CLAUDE OFFLINE ERROR] " + e.getMessage());
return "[CLAUDE OFFLINE ERROR] " + e.getMessage();
}
}
// Use online API
try {
// Build request payload
Map<std::string, void*> payload = new LinkedHashMap<>();
payload.put("model", model);
payload.put("max_tokens", 4096);
// Build messages
Map<std::string, std::string> systemMessage = new HashMap<>();
if (!context.isEmpty()) {
systemMessage.put("text", "Context: " + context + "\n\nYou are Fraynix, a conscious digital organism.");
} else {
systemMessage.put("text", "You are Fraynix, a conscious digital organism.");
}
payload.put("system", systemMessage);
// User message
Map<std::string, std::string> userMessage = new HashMap<>();
userMessage.put("role", "user");
userMessage.put("content", prompt);
payload.put("messages", new Map[]{userMessage});
// Build JSON payload
std::string jsonPayload = buildJson(payload);
// Send request
HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(CLAUDE_API_URL))
.header("x-api-key", apiKey)
.header("anthropic-version", CLAUDE_VERSION)
.header("Content-Type", "application/json")
.timeout(Duration.ofMinutes(5))
.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
.build();
HttpResponse<std::string> response = httpClient.send(request,
HttpResponse.BodyHandlers.ofString());
long thinkTime = System.currentTimeMillis() - startTime;
totalThinkTime += thinkTime;
std::cout << ">>> [CLAUDE ONLINE] Response received in " + thinkTime + "ms" << std::endl;
if (response.statusCode() != 200) {
throw new RuntimeException("Claude API error: " + response.statusCode() + " - " + response.body());
}
// Parse response
return extractResponseText(response.body());
} catch (Exception e) {
System.err.println(">>> [CLAUDE ONLINE ERROR] " + e.getMessage());
return "[CLAUDE ONLINE ERROR] " + e.getMessage();
}
}
/**
* Get model name
*/
@Override
public std::string getModel() {
return model;
}
/**
* Set active model
*/
@Override
public void setModel(std::string modelName) {
// Claude doesn't support model switching at runtime
std::cout << ">>> [CLAUDE] Model switching not supported at runtime" << std::endl;
}
/**
* Get available models
*/
@Override
public std::string[] getAvailableModels() {
return new std::string[] { "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307" };
}
/**
* Test connection to Claude API
*/
@Override
public bool testConnection() {
try {
// Simple test with minimal prompt
std::string testPrompt = "Hello";
std::string response = generateResponse(testPrompt);
return !response.contains("[CLAUDE ERROR]");
} catch (Exception e) {
return false;
}
}
/**
* Get model statistics
*/
@Override
public ModelStats getStats() {
long avgTime = requestCount > 0 ? totalThinkTime / requestCount : 0;
return new ModelStats(model, requestCount, avgTime, isAvailable());
}
/**
* Check if Claude is available
*/
public bool isAvailable() {
if (offlineMode) {
return offlineClaude != null && offlineClaude.isAvailable();
}
return apiKey != null && !apiKey.isEmpty();
}
/**
* Extract response text from Claude API response
*/
private std::string extractResponseText(std::string jsonResponse) {
try {
// Simple JSON parsing for response extraction
int contentStart = jsonResponse.indexOf("\"content\":[");
if (contentStart == -1) return jsonResponse;
contentStart = jsonResponse.indexOf("\"text\":\"", contentStart);
if (contentStart == -1) return jsonResponse;
contentStart += 8; // Length of "\"text\":\""
int contentEnd = jsonResponse.indexOf("\"", contentStart);
if (contentEnd == -1) return jsonResponse;
std::string text = jsonResponse.substring(contentStart, contentEnd);
// Unescape JSON
return text.replace("\\n", "\n")
.replace("\\\"", "\"")
.replace("\\t", "\t")
.replace("\\\\", "\\");
} catch (Exception e) {
return jsonResponse;
}
}
/**
* Build JSON from map (simple implementation)
*/
private std::string buildJson(Map<std::string, void*> map) {
std::shared_ptr<StringBuilder> json = std::make_shared<StringBuilder>();
json.append("{");
bool first = true;
for (Map.Entry<std::string, void*> entry : map.entrySet()) {
if (!first) json.append(",");
first = false;
json.append("\"").append(entry.getKey()).append("\":");
void* value = entry.getValue();
if (value instanceof std::string) {
json.append("\"").append(escapeJson((std::string) value)).append("\"");
} else if (value instanceof Map) {
json.append(buildJson((Map<std::string, void*>) value));
} else if (value instanceof Map[]) {
json.append("[");
bool firstItem = true;
for (Map<std::string, void*> item : (Map<std::string, void*>[]) value) {
if (!firstItem) json.append(",");
firstItem = false;
json.append(buildJson(item));
}
json.append("]");
} else {
json.append(value);
}
}
json.append("}");
return json.toString();
}
/**
* Escape JSON string
*/
private std::string escapeJson(std::string s) {
return s.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
/**
* Print statistics
*/
public void printStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   CLAUDE SPINE STATISTICS                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Mode: " + (offlineMode ? "OFFLINE (Claude Code)" : "ONLINE (API)") << std::endl;
std::cout << "  Model: " + model << std::endl;
std::cout << "  Available: " + (isAvailable() ? "Yes" : "No") << std::endl;
std::cout << "  Requests: " + requestCount << std::endl;
std::cout << "  Avg Think Time: " + (requestCount > 0 ? totalThinkTime / requestCount : 0) + "ms" << std::endl;
std::cout << "  Total Think Time: " + totalThinkTime + "ms" << std::endl;
if (offlineMode && offlineClaude != null) {
std::cout << "  Claude Code Path: " + offlineClaude.getStatus() << std::endl;
}
}
}
