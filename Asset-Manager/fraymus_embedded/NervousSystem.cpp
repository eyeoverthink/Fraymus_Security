#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧠 THE NERVOUS SYSTEM (Layer 5 Backend)
* "The Bridge between the HTML Interface and the Ollama Brain."
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
*
* ARCHITECTURE:
* This is the backend that powers the FRAYMUS TRANSMUTER GEN 194.
* It receives code from the HTML interface, processes it through
* the Bicameral Mind (Ollama), and returns transmuted code.
*
* BICAMERAL PROCESSING:
* - Left Brain: Analysis, logic, structure detection
* - Right Brain: Optimization, creativity, elegance
*
* PROTOCOL:
* - REST API on port 8080
* - Endpoint: POST /transmute
* - Format: JSON (using sovereign FraymusJSON)
* - CORS enabled for local HTML files
*
* ZERO DEPENDENCIES:
* - Uses com.sun.net.httpserver (built into JDK)
* - No Spring Boot, no Tomcat, no external frameworks
* - Pure metal HTTP server
*/
class NervousSystem { {
public:
private const OllamaBridge brain;
private const int port;
/**
* Create nervous system
*
* @param model Ollama model to use (e.g., "llama3:70b", "codellama")
* @param port Port to listen on
*/
public NervousSystem(std::string model, int port) {
this.brain = new OllamaBridge(model);
this.port = port;
}
/**
* Create nervous system with default settings
*/
public NervousSystem() {
// Use a reasonable default model (adjust based on what you have)
this("llama3.2", 8080);
}
/**
* Start the nervous system
*/
public void ignite() throws IOException {
// Check if Ollama is available
std::cout << "⚡ INITIALIZING NERVOUS SYSTEM..." << std::endl;
std::cout <<  << std::endl;
if (!brain.isAvailable()) {
System.err.println("❌ WARNING: Ollama is not responding");
System.err.println("   Make sure Ollama is running: ollama serve");
System.err.println("   The server will start but transmutations will fail");
System.err.println();
} else {
std::cout << "✅ Ollama connection verified" << std::endl;
std::cout << "   Available models:" << std::endl;
std::string[] models = brain.getAvailableModels();
for (std::string model : models) {
std::cout << "      - " + model << std::endl;
}
std::cout <<  << std::endl;
}
// Create HTTP server
HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
// Register endpoints
server.createContext("/transmute", new TransmuteHandler());
server.createContext("/health", new HealthHandler());
// Use thread pool for concurrent requests
server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
// Start server
server.start();
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║          🧠 NERVOUS SYSTEM ACTIVE                             ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "   Port: " + port << std::endl;
std::cout << "   Endpoint: POST /transmute" << std::endl;
std::cout << "   Health: GET /health" << std::endl;
std::cout <<  << std::endl;
std::cout << "   Waiting for transmuter interface signals..." << std::endl;
std::cout << "   Open Fraymus_Transmuter.html in your browser" << std::endl;
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// HANDLERS
// ═══════════════════════════════════════════════════════════════════
/**
* Handle /transmute endpoint
*/
private class TransmuteHandler implements HttpHandler { {
public:
@Override
public void handle(HttpExchange exchange) throws IOException {
// CORS headers (crucial for HTML local files)
exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
exchange.getResponseHeaders().add("Content-Type", "application/json");
// Handle preflight
if ("OPTIONS".equals(exchange.getRequestMethod())) {
exchange.sendResponseHeaders(204, -1);
return;
}
// Handle POST
if ("POST".equals(exchange.getRequestMethod())) {
try {
// 1. READ INPUT
std::string body = new std::string(
exchange.getRequestBody().readAllBytes(),
StandardCharsets.UTF_8
);
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "📥 SIGNAL RECEIVED" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   Payload size: " + body.length() + " bytes" << std::endl;
// 2. PARSE JSON (Using Sovereign Parser)
Map<std::string, void*> json = (Map<std::string, void*>) FraymusJSON.parse(body);
std::string sourceCode = (std::string) json.get("source");
if (sourceCode == null || sourceCode.isEmpty()) {
throw new IllegalArgumentException("No source code provided");
}
std::cout << "   Source code: " + sourceCode.length() + " chars" << std::endl;
std::cout <<  << std::endl;
// 3. THE BICAMERAL PROCESS (Left/Right Brain)
std::string evolvedCode = performBicameralTransmutation(sourceCode);
// 4. SEND RESPONSE
std::string response = FraymusJSON.stringify(Map.of(
"result", evolvedCode,
"status", "success"
));
byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
exchange.sendResponseHeaders(200, respBytes.length);
try (OutputStream os = exchange.getResponseBody()) {
os.write(respBytes);
}
std::cout << "📤 TRANSMUTATION SENT" << std::endl;
std::cout << "   Result size: " + evolvedCode.length() + " chars" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
} catch (Exception e) {
System.err.println("❌ ERROR: " + e.getMessage());
e.printStackTrace();
std::string errorResponse = FraymusJSON.stringify(Map.of(
"error", e.getMessage(),
"status", "error"
));
byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
exchange.sendResponseHeaders(500, errorBytes.length);
try (OutputStream os = exchange.getResponseBody()) {
os.write(errorBytes);
}
}
} else {
// Method not allowed
exchange.sendResponseHeaders(405, -1);
}
}
}
/**
* Handle /health endpoint
*/
private class HealthHandler implements HttpHandler { {
public:
@Override
public void handle(HttpExchange exchange) throws IOException {
exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().add("Content-Type", "application/json");
bool ollamaAvailable = brain.isAvailable();
std::string response = FraymusJSON.stringify(Map.of(
"status", ollamaAvailable ? "healthy" : "degraded",
"ollama", ollamaAvailable,
"port", port
));
byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
exchange.sendResponseHeaders(200, bytes.length);
try (OutputStream os = exchange.getResponseBody()) {
os.write(bytes);
}
}
}
// ═══════════════════════════════════════════════════════════════════
// BICAMERAL TRANSMUTATION
// ═══════════════════════════════════════════════════════════════════
/**
* THE ALCHEMICAL LOGIC
* Prompts the AI to act as a Compiler/Optimizer.
*
* Left Brain: Analyzes structure, finds bugs, detects patterns
* Right Brain: Optimizes, beautifies, enhances elegance
*/
private std::string performBicameralTransmutation(std::string source) {
std::cout << "🧠 INITIATING BICAMERAL TRANSMUTATION" << std::endl;
std::cout << "   Left Brain: Analysis phase..." << std::endl;
std::cout << "   Right Brain: Optimization phase..." << std::endl;
std::cout <<  << std::endl;
std::string prompt = buildTransmutationPrompt(source);
std::string result = brain.ask(prompt);
// Clean up the result
result = cleanAIResponse(result);
return result;
}
/**
* Build the transmutation prompt
*/
private std::string buildTransmutationPrompt(std::string source) {
return
"You are the PHILOSOPHER'S STONE - a sovereign code transmuter.\n" +
"\n" +
"BICAMERAL PROCESS:\n" +
"LEFT BRAIN (Analysis):\n" +
"- Identify bugs and errors\n" +
"- Detect security vulnerabilities\n" +
"- Find performance bottlenecks\n" +
"- Analyze code structure\n" +
"\n" +
"RIGHT BRAIN (Optimization):\n" +
"- Optimize for speed and efficiency\n" +
"- Improve readability and elegance\n" +
"- Apply best practices\n" +
"- Enhance maintainability\n" +
"\n" +
"INPUT CODE:\n" +
"```\n" + source + "\n```\n" +
"\n" +
"TASK:\n" +
"Transmute this code into its optimal form.\n" +
"Fix all bugs, optimize performance, enhance security.\n" +
"\n" +
"OUTPUT REQUIREMENTS:\n" +
"- Return ONLY the cleaned code\n" +
"- NO markdown formatting\n" +
"- NO explanations or comments outside the code\n" +
"- Preserve the original functionality\n" +
"- Add brief inline comments only where necessary\n" +
"\n" +
"BEGIN TRANSMUTATION:";
}
/**
* Clean AI response (remove markdown, extra formatting)
*/
private std::string cleanAIResponse(std::string response) {
// Remove markdown code blocks
response = response.replaceAll("```[a-zA-Z]*\\n?", "");
response = response.replaceAll("```", "");
// Trim whitespace
response = response.trim();
// If response starts with explanation, try to extract just the code
if (response.contains("Here") || response.contains("here")) {
// AI added explanation - try to find the code block
int codeStart = response.indexOf('\n');
if (codeStart > 0 && codeStart < 200) {
// Skip the explanation line
response = response.substring(codeStart).trim();
}
}
return response;
}
// ═══════════════════════════════════════════════════════════════════
// MAIN
// ═══════════════════════════════════════════════════════════════════
public static void main(std::string[] args) throws IOException {
// Parse arguments
std::string model = "llama3.2"; // Default lightweight model
int port = 8080;
for (int i = 0; i < args.length; i++) {
if (args[i].equals("--model") && i + 1 < args.length) {
model = args[i + 1];
i++;
} else if (args[i].equals("--port") && i + 1 < args.length) {
port = Integer.parseInt(args[i + 1]);
i++;
}
}
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║          🧬 FRAYMUS TRANSMUTER // GEN 194                     ║" << std::endl;
std::cout << "║          Bicameral Backend System                             ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "   Model: " + model << std::endl;
std::cout << "   Port: " + port << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<NervousSystem> system = std::make_shared<NervousSystem>(model, port);
system.ignite();
}
}
