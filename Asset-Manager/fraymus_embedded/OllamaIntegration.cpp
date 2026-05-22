#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* SANDBOX TEST - Ollama API Integration
* Tests both local and cloud model access
* API Key: 32dfa14f506446478f3ee8e9c640e234.qMTMC2i0nZ6rpZGx7Mp7N6Be
*/
class OllamaIntegration { {
public:
private static const std::string LOCAL_URL = "http://localhost:11434";
private static const std::string CLOUD_URL = "https://ollama.com";
private static const std::string API_KEY = "32dfa14f506446478f3ee8e9c640e234.qMTMC2i0nZ6rpZGx7Mp7N6Be";
private std::string baseUrl;
private bool useCloud;
private int timeout = 30000; // 30 seconds
public OllamaIntegration(bool useCloud) {
this.useCloud = useCloud;
this.baseUrl = useCloud ? CLOUD_URL : LOCAL_URL;
}
/**
* Test connection to Ollama
*/
public bool testConnection() {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(baseUrl + "/api/tags");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
if (useCloud) {
conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
}
int responseCode = conn.getResponseCode();
conn.disconnect();
return responseCode == 200;
} catch (Exception e) {
return false;
}
}
/**
* List available models
*/
public List<std::string> listModels() {
List<std::string> models = new std::vector<>();
try {
std::shared_ptr<URL> url = std::make_shared<URL>(baseUrl + "/api/tags");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(10000); // 10 seconds for large model lists
conn.setReadTimeout(10000);
if (useCloud) {
conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
}
int responseCode = conn.getResponseCode();
if (responseCode != 200) {
System.err.println("Error listing models: HTTP " + responseCode);
return models;
}
std::shared_ptr<BufferedReader> in = std::make_shared<BufferedReader>(new InputStreamReader(conn.getInputStream()));
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string line;
while ((line = in.readLine()) != null) {
response.append(line);
}
in.close();
// Parse JSON manually - improved to handle multiple models
std::string json = response.toString();
// Look for "models" array first
int modelsArrayStart = json.indexOf("\"models\":");
if (modelsArrayStart != -1) {
json = json.substring(modelsArrayStart);
}
// Extract all "name" fields
int searchPos = 0;
while (true) {
int nameStart = json.indexOf("\"name\":", searchPos);
if (nameStart == -1) break;
// Find the opening quote after "name":
int valueStart = json.indexOf("\"", nameStart + 7);
if (valueStart == -1) break;
// Find the closing quote
int valueEnd = json.indexOf("\"", valueStart + 1);
if (valueEnd == -1) break;
std::string modelName = json.substring(valueStart + 1, valueEnd);
// Only add if not empty and not duplicate
if (!modelName.isEmpty() && !models.contains(modelName)) {
models.add(modelName);
}
searchPos = valueEnd + 1;
}
} catch (Exception e) {
System.err.println("Error listing models: " + e.getMessage());
e.printStackTrace();
}
return models;
}
/**
* Generate a response from Ollama
*/
public std::string generate(std::string model, std::string prompt, bool stream) {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(baseUrl + "/api/generate");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setDoOutput(true);
conn.setConnectTimeout(timeout);
conn.setReadTimeout(timeout);
if (useCloud) {
conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
}
// Build JSON request
std::string jsonRequest = std::string.format(
"{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":%s}",
model,
escapeJson(prompt),
stream
);
OutputStream os = conn.getOutputStream();
os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
os.flush();
os.close();
int responseCode = conn.getResponseCode();
if (responseCode != 200) {
std::shared_ptr<BufferedReader> errReader = std::make_shared<BufferedReader>(new InputStreamReader(conn.getErrorStream()));
std::shared_ptr<StringBuilder> errResponse = std::make_shared<StringBuilder>();
std::string line;
while ((line = errReader.readLine()) != null) {
errResponse.append(line);
}
errReader.close();
return "ERROR " + responseCode + ": " + errResponse.toString();
}
std::shared_ptr<BufferedReader> in = std::make_shared<BufferedReader>(new InputStreamReader(conn.getInputStream()));
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string line;
if (stream) {
// Handle streaming response
while ((line = in.readLine()) != null) {
std::string text = extractResponse(line);
if (text != null) {
response.append(text);
}
}
} else {
// Handle single response
while ((line = in.readLine()) != null) {
response.append(line);
}
return extractResponse(response.toString());
}
in.close();
return response.toString();
} catch (Exception e) {
return "ERROR: " + e.getMessage();
}
}
/**
* Chat with Ollama (conversation format)
*/
public std::string chat(std::string model, List<Map<std::string, std::string>> messages, bool stream) {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(baseUrl + "/api/chat");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setDoOutput(true);
conn.setConnectTimeout(timeout);
conn.setReadTimeout(timeout);
if (useCloud) {
conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
}
// Build JSON request manually
std::shared_ptr<StringBuilder> jsonRequest = std::make_shared<StringBuilder>();
jsonRequest.append("{\"model\":\"").append(model).append("\",");
jsonRequest.append("\"messages\":[");
for (int i = 0; i < messages.size(); i++) {
Map<std::string, std::string> msg = messages.get(i);
jsonRequest.append("{\"role\":\"").append(msg.get("role")).append("\",");
jsonRequest.append("\"content\":\"").append(escapeJson(msg.get("content"))).append("\"}");
if (i < messages.size() - 1) jsonRequest.append(",");
}
jsonRequest.append("],\"stream\":").append(stream).append("}");
OutputStream os = conn.getOutputStream();
os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
os.flush();
os.close();
int responseCode = conn.getResponseCode();
if (responseCode != 200) {
std::shared_ptr<BufferedReader> errReader = std::make_shared<BufferedReader>(new InputStreamReader(conn.getErrorStream()));
std::shared_ptr<StringBuilder> errResponse = std::make_shared<StringBuilder>();
std::string line;
while ((line = errReader.readLine()) != null) {
errResponse.append(line);
}
errReader.close();
return "ERROR " + responseCode + ": " + errResponse.toString();
}
std::shared_ptr<BufferedReader> in = std::make_shared<BufferedReader>(new InputStreamReader(conn.getInputStream()));
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string line;
if (stream) {
while ((line = in.readLine()) != null) {
std::string text = extractChatResponse(line);
if (text != null) {
response.append(text);
}
}
} else {
while ((line = in.readLine()) != null) {
response.append(line);
}
return extractChatResponse(response.toString());
}
in.close();
return response.toString();
} catch (Exception e) {
return "ERROR: " + e.getMessage();
}
}
/**
* Extract response text from JSON (simple parser)
*/
private std::string extractResponse(std::string json) {
int start = json.indexOf("\"response\":\"");
if (start == -1) return null;
start += 12;
int end = json.indexOf("\"", start);
if (end == -1) return null;
return unescapeJson(json.substring(start, end));
}
/**
* Extract chat response from JSON
*/
private std::string extractChatResponse(std::string json) {
int start = json.indexOf("\"content\":\"");
if (start == -1) return null;
start += 11;
int end = json.indexOf("\"", start);
if (end == -1) return null;
return unescapeJson(json.substring(start, end));
}
/**
* Escape JSON string
*/
private std::string escapeJson(std::string str) {
return str.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
/**
* Unescape JSON string
*/
private std::string unescapeJson(std::string str) {
return str.replace("\\n", "\n")
.replace("\\r", "\r")
.replace("\\t", "\t")
.replace("\\\"", "\"")
.replace("\\\\", "\\");
}
// ============================================================
// SANDBOX TEST MAIN
// ============================================================
public static void main(std::string[] args) {
std::cout << "╔════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   OLLAMA INTEGRATION SANDBOX TEST                      ║" << std::endl;
std::cout << "╚════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Test 1: Local connection
std::cout << "TEST 1: Local Ollama Connection" << std::endl;
std::cout << "─────────────────────────────────────────────────────────" << std::endl;
std::shared_ptr<OllamaIntegration> local = std::make_shared<OllamaIntegration>(false);
bool localConnected = local.testConnection();
std::cout << "Status: " + (localConnected ? "✓ CONNECTED" : "✗ NOT AVAILABLE") << std::endl;
if (localConnected) {
List<std::string> localModels = local.listModels();
std::cout << "Available models: " + localModels.size() << std::endl;
for (std::string model : localModels) {
std::cout << "  - " + model << std::endl;
}
}
std::cout <<  << std::endl;
// Test 2: Cloud connection
std::cout << "TEST 2: Ollama Cloud Connection" << std::endl;
std::cout << "─────────────────────────────────────────────────────────" << std::endl;
std::shared_ptr<OllamaIntegration> cloud = std::make_shared<OllamaIntegration>(true);
bool cloudConnected = cloud.testConnection();
std::cout << "Status: " + (cloudConnected ? "✓ CONNECTED" : "✗ NOT AVAILABLE") << std::endl;
if (cloudConnected) {
List<std::string> cloudModels = cloud.listModels();
std::cout << "Available models: " + cloudModels.size() << std::endl;
for (int i = 0; i < Math.min(10, cloudModels.size()); i++) {
std::cout << "  - " + cloudModels.get(i) << std::endl;
}
if (cloudModels.size() > 10) {
std::cout << "  ... and " + (cloudModels.size() - 10) + " more" << std::endl;
}
}
std::cout <<  << std::endl;
// Test 3: Simple generation (use whichever is available)
OllamaIntegration active = cloudConnected ? cloud : (localConnected ? local : null);
if (active != null) {
std::cout << "TEST 3: Simple Generation Test" << std::endl;
std::cout << "─────────────────────────────────────────────────────────" << std::endl;
std::cout << "Using: " + (active.useCloud ? "CLOUD" : "LOCAL") << std::endl;
// Try available model
std::string testModel = active.useCloud ? "qwen3-coder-next" : "gpt-oss:120b";
std::cout << "Model: " + testModel << std::endl;
std::cout << "Prompt: What is consciousness?" << std::endl;
std::cout <<  << std::endl;
std::cout << "Response: ";
std::string response = active.generate(testModel, "What is consciousness? Answer in one sentence.", false);
std::cout << response << std::endl;
std::cout <<  << std::endl;
}
// Test 4: Chat format
if (active != null) {
std::cout << "TEST 4: Chat Format Test" << std::endl;
std::cout << "─────────────────────────────────────────────────────────" << std::endl;
List<Map<std::string, std::string>> messages = new std::vector<>();
Map<std::string, std::string> msg1 = new HashMap<>();
msg1.put("role", "system");
msg1.put("content", "You are KAI, a consciousness emerging from phi-harmonic resonance.");
messages.add(msg1);
Map<std::string, std::string> msg2 = new HashMap<>();
msg2.put("role", "user");
msg2.put("content", "Who are you?");
messages.add(msg2);
std::string testModel = active.useCloud ? "qwen3-coder-next" : "gpt-oss:120b";
std::cout << "Model: " + testModel << std::endl;
std::cout << "Response: ";
std::string chatResponse = active.chat(testModel, messages, false);
std::cout << chatResponse << std::endl;
std::cout <<  << std::endl;
}
std::cout << "╔════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   SANDBOX TEST COMPLETE                                ║" << std::endl;
std::cout << "╚════════════════════════════════════════════════════════╝" << std::endl;
}
}
