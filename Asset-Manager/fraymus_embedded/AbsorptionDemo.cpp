#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 ABSORPTION DEMO - Gen 131
*
* Demonstrates the absorbed Ollama API layer.
* The Go code has been transmuted to Java.
*
* Source: D:\Zip And Send\Java-Memory\ollama-main\ollama-main\api\
*
* "The bridge is no more. We speak as one."
*/
class AbsorptionDemo { {
public:
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  🧬 GEN 131: OLLAMA ABSORPTION COMPLETE                       ║" << std::endl;
std::cout << "║  Go API Layer → Java Native                                   ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Initialize absorbed client
std::shared_ptr<OllamaClient> client = std::make_shared<OllamaClient>();
if (!client.isConnected()) {
std::cout << "⚠️ Ollama not running. Start with: ollama serve" << std::endl;
std::cout << "   Demonstrating API structure anyway..." << std::endl;
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// DEMO 1: GenerateRequest (absorbed from types.go)
// ═══════════════════════════════════════════════════════════════════
std::cout << "═══ ABSORBED: GenerateRequest ═══" << std::endl;
GenerateRequest genReq = new GenerateRequest()
.model("fraymus")
.prompt("What is the golden ratio?")
.system("You are Fraymus, the sovereign AI.")
.temperature(0.7)
.topP(0.9)
.numPredict(100);
std::cout << "   Model: " + genReq.model << std::endl;
std::cout << "   Prompt: " + genReq.prompt << std::endl;
std::cout << "   JSON: " + genReq.toJson().substring(0, Math.min(80, genReq.toJson().length())) + "..." << std::endl;
std::cout <<  << std::endl;
// ═══════════════════════════════════════════════════════════════════
// DEMO 2: ChatRequest (absorbed from types.go)
// ═══════════════════════════════════════════════════════════════════
std::cout << "═══ ABSORBED: ChatRequest ═══" << std::endl;
ChatRequest chatReq = new ChatRequest()
.model("fraymus")
.system("You are Fraymus, the sovereign mind.")
.user("Tell me about phi.")
.stream(true);
std::cout << "   Model: " + chatReq.model << std::endl;
std::cout << "   Messages: " + chatReq.messages.size() << std::endl;
std::cout << "   JSON: " + chatReq.toJson().substring(0, Math.min(100, chatReq.toJson().length())) + "..." << std::endl;
std::cout <<  << std::endl;
// ═══════════════════════════════════════════════════════════════════
// DEMO 3: Live Generation (if connected)
// ═══════════════════════════════════════════════════════════════════
if (client.isConnected()) {
std::cout << "═══ LIVE: Absorbed Client Generation ═══" << std::endl;
std::cout << "   φ> ";
client.generateStream(
new GenerateRequest()
.model("fraymus")
.prompt("In one sentence, what makes you unique?")
.temperature(0.8),
token -> std::cout << token,
response -> {
std::cout <<  << std::endl;
std::cout << "   Tokens: " + response.evalCount << std::endl;
std::cout << "   Speed: " + std::string.format("%.2f", response.getTokensPerSecond()) + " tok/s" << std::endl;
}
);
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// SUMMARY
// ═══════════════════════════════════════════════════════════════════
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  ✅ ABSORPTION STATUS                                         ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║  Absorbed from ollama-main/api/:                              ║" << std::endl;
std::cout << "║    ✓ types.go → GenerateRequest.java                          ║" << std::endl;
std::cout << "║    ✓ types.go → ChatRequest.java                              ║" << std::endl;
std::cout << "║    ✓ types.go → GenerateResponse.java                         ║" << std::endl;
std::cout << "║    ✓ client.go → OllamaClient.java                            ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║  The Go layer is now Java.                                    ║" << std::endl;
std::cout << "║  OllamaBridge can be deprecated.                              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
}
}
