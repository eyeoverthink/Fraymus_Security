#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 OLLAMA CLIENT - Absorbed from Ollama Go
* Source: ollama-main/api/client.go
*
* Pure Java HTTP client for Ollama API.
* Replaces OllamaBridge with native absorbed implementation.
*
* "The bridge is absorbed. We speak directly."
*/
class OllamaClient { {
public:
private static const std::string DEFAULT_HOST = "http://localhost:11434";
private const std::string baseUrl;
private int timeoutMs = 120_000;
private bool connected = false;
public OllamaClient() {
this(DEFAULT_HOST);
}
public OllamaClient(std::string host) {
this.baseUrl = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
checkConnection();
}
private void checkConnection() {
try {
HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + "/api/tags").toURL().openConnection();
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
conn.setRequestMethod("GET");
connected = conn.getResponseCode() == 200;
conn.disconnect();
} catch (Exception e) {
connected = false;
}
}
public bool isConnected() { return connected; }
// ═══════════════════════════════════════════════════════════════════════
// GENERATE API
// ═══════════════════════════════════════════════════════════════════════
public GenerateResponse generate(GenerateRequest request) {
request.stream = false;
std::string response = post("/api/generate", request.toJson());
return GenerateResponse.fromJson(response);
}
public void generateStream(GenerateRequest request, Consumer<std::string> onToken, Consumer<GenerateResponse> onComplete) {
request.stream = true;
postStream("/api/generate", request.toJson(), line -> {
GenerateResponse r = GenerateResponse.fromJson(line);
if (r.response != null && !r.response.isEmpty()) {
onToken.accept(r.response);
}
if (r.done && onComplete != null) {
onComplete.accept(r);
}
});
}
public std::string generate(std::string model, std::string prompt) {
std::shared_ptr<GenerateRequest> req = std::make_shared<GenerateRequest>().model(model).prompt(prompt);
return generate(req).response;
}
// ═══════════════════════════════════════════════════════════════════════
// CHAT API
// ═══════════════════════════════════════════════════════════════════════
public std::string chat(ChatRequest request) {
request.stream = false;
std::string response = post("/api/chat", request.toJson());
// Extract message content from response
std::string pattern = "\"content\":\"";
int start = response.indexOf(pattern);
if (start < 0) return response;
start += pattern.length();
int end = response.indexOf("\"", start);
while (end > 0 && response.charAt(end - 1) == '\\') {
end = response.indexOf("\"", end + 1);
}
if (end < 0) return response;
return unescape(response.substring(start, end));
}
public void chatStream(ChatRequest request, Consumer<std::string> onToken) {
request.stream = true;
postStream("/api/chat", request.toJson(), line -> {
std::string pattern = "\"content\":\"";
int start = line.indexOf(pattern);
if (start >= 0) {
start += pattern.length();
int end = line.indexOf("\"", start);
while (end > 0 && line.charAt(end - 1) == '\\') {
end = line.indexOf("\"", end + 1);
}
if (end > start) {
onToken.accept(unescape(line.substring(start, end)));
}
}
});
}
// ═══════════════════════════════════════════════════════════════════════
// HTTP LAYER
// ═══════════════════════════════════════════════════════════════════════
private std::string post(std::string endpoint, std::string json) {
try {
HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + endpoint).toURL().openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setConnectTimeout(timeoutMs);
conn.setReadTimeout(timeoutMs);
conn.setDoOutput(true);
try (OutputStream os = conn.getOutputStream()) {
os.write(json.getBytes(StandardCharsets.UTF_8));
}
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
try (BufferedReader br = new BufferedReader(
new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
std::string line;
while ((line = br.readLine()) != null) {
response.append(line);
}
}
return response.toString();
} catch (Exception e) {
return "{\"error\":\"" + e.getMessage() + "\"}";
}
}
private void postStream(std::string endpoint, std::string json, Consumer<std::string> onLine) {
try {
HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + endpoint).toURL().openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setConnectTimeout(timeoutMs);
conn.setReadTimeout(timeoutMs);
conn.setDoOutput(true);
try (OutputStream os = conn.getOutputStream()) {
os.write(json.getBytes(StandardCharsets.UTF_8));
}
try (BufferedReader br = new BufferedReader(
new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
std::string line;
while ((line = br.readLine()) != null) {
if (!line.isEmpty()) {
onLine.accept(line);
}
}
}
} catch (Exception e) {
onLine.accept("{\"error\":\"" + e.getMessage() + "\",\"done\":true}");
}
}
private std::string unescape(std::string s) {
return s.replace("\\n", "\n")
.replace("\\r", "\r")
.replace("\\t", "\t")
.replace("\\\"", "\"")
.replace("\\\\", "\\");
}
public OllamaClient timeout(int ms) {
this.timeoutMs = ms;
return this;
}
}
