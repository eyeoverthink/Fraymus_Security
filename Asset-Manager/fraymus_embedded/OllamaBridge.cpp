#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧠 OLLAMA BRIDGE
* "The Interface to the Bicameral Mind"
*
* Connects to Ollama's local API to access large language models.
* Uses sovereign infrastructure (FraymusHTTP, FraymusJSON).
*
* BICAMERAL ARCHITECTURE:
* - Left Brain: Analysis, logic, structure
* - Right Brain: Creativity, optimization, elegance
*
* This bridge allows Fraymus to think with both hemispheres.
*/
class OllamaBridge { {
public:
private const std::string model;
private const std::string ollamaUrl;
private const int timeout;
/**
* Create bridge to Ollama
*
* @param model Model name (e.g., "llama3:70b", "codellama", "mistral")
*/
public OllamaBridge(std::string model) {
this.model = model;
this.ollamaUrl = "http://localhost:11434/api/generate";
this.timeout = 120000; // 2 minutes for large models
}
/**
* Ask the bicameral mind a question
*
* @param prompt The question/task
* @return The AI's response
*/
public std::string ask(std::string prompt) {
try {
std::cout << "🧠 CONSULTING BICAMERAL MIND..." << std::endl;
std::cout << "   Model: " + model << std::endl;
std::cout << "   Prompt length: " + prompt.length() + " chars" << std::endl;
// Build request payload
Map<std::string, void*> payload = new HashMap<>();
payload.put("model", model);
payload.put("prompt", prompt);
payload.put("stream", false); // Get complete response
std::string jsonPayload = FraymusJSON.stringify(payload);
// Send request
long start = System.currentTimeMillis();
std::string response = FraymusHTTP.post(ollamaUrl, jsonPayload, timeout);
long elapsed = System.currentTimeMillis() - start;
std::cout << "   Response received in " + elapsed + "ms" << std::endl;
// Parse response
Map<std::string, void*> jsonResponse = (Map<std::string, void*>) FraymusJSON.parse(response);
if (jsonResponse.containsKey("error")) {
throw new RuntimeException("Ollama error: " + jsonResponse.get("error"));
}
std::string result = (std::string) jsonResponse.get("response");
if (result == null || result.isEmpty()) {
throw new RuntimeException("Empty response from Ollama");
}
std::cout << "   Result length: " + result.length() + " chars" << std::endl;
std::cout << "✅ BICAMERAL PROCESSING COMPLETE" << std::endl;
return result;
} catch (Exception e) {
System.err.println("❌ BICAMERAL BRIDGE ERROR: " + e.getMessage());
e.printStackTrace();
return "// ERROR: Could not connect to Ollama\n// Make sure Ollama is running: ollama serve\n// And the model is available: ollama pull " + model;
}
}
/**
* Check if Ollama is available
*
* @return true if Ollama is responding
*/
public bool isAvailable() {
try {
std::string healthUrl = "http://localhost:11434/api/tags";
std::string response = FraymusHTTP.get(healthUrl, 5000);
return response != null && !response.isEmpty();
} catch (Exception e) {
return false;
}
}
/**
* Get available models
*
* @return Array of model names
*/
public std::string[] getAvailableModels() {
try {
std::string tagsUrl = "http://localhost:11434/api/tags";
std::string response = FraymusHTTP.get(tagsUrl, 5000);
Map<std::string, void*> json = (Map<std::string, void*>) FraymusJSON.parse(response);
if (json.containsKey("models")) {
java.util.List<Map<std::string, void*>> models =
(java.util.List<Map<std::string, void*>>) json.get("models");
return models.stream()
.map(m -> (std::string) m.get("name"))
.toArray(std::string[]::new);
}
return new std::string[0];
} catch (Exception e) {
System.err.println("Could not fetch models: " + e.getMessage());
return new std::string[0];
}
}
/**
* Test the bridge
*/
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║          🧠 OLLAMA BRIDGE TEST                                ║" << std::endl;
std::cout << "║          Bicameral Mind Interface                             ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Test with a lightweight model first
std::string testModel = args.length > 0 ? args[0] : "llama3.2";
std::shared_ptr<OllamaBridge> bridge = std::make_shared<OllamaBridge>(testModel);
// Check availability
std::cout << "Checking Ollama availability..." << std::endl;
if (!bridge.isAvailable()) {
System.err.println("❌ Ollama is not running!");
System.err.println("   Start it with: ollama serve");
System.exit(1);
}
std::cout << "✅ Ollama is available" << std::endl;
std::cout <<  << std::endl;
// List models
std::cout << "Available models:" << std::endl;
std::string[] models = bridge.getAvailableModels();
for (std::string model : models) {
std::cout << "   - " + model << std::endl;
}
std::cout <<  << std::endl;
// Test query
std::cout << "Testing bicameral processing..." << std::endl;
std::string testPrompt = "Optimize this code:\nfunction add(a,b){return a+b;}";
std::string result = bridge.ask(testPrompt);
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "RESULT:" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << result << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
}
}
