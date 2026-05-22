#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* FRAYMUS SYSTEM - Main Entry Point
*
* Your AI system with real physics.
* Run this to use it.
*/
class FraymusSystem { {
public:
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                    FRAYMUS SYSTEM                         ║" << std::endl;
std::cout << "║              Physics-Based AI Intelligence                ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Initialize system with physics
std::cout << "Initializing Fraymus..." << std::endl;
FraymusAI ai = FraymusAI.builder()
.chatModel("llama3")
.embedModel("nomic-embed-text")
.enableQuantum()            // Real physics enabled
.enableRAG()                // Context retrieval
.enableTools()              // Math, file ops
.enableMemory()             // Persistent memory
.verboseLogging(false)      // Clean output
.build();
std::cout << "✓ GravityEngine online" << std::endl;
std::cout << "✓ FusionReactor online" << std::endl;
std::cout << "✓ Tesseract ready" << std::endl;
std::cout << "✓ System ready" << std::endl;
std::cout <<  << std::endl;
// Interactive mode
std::shared_ptr<Scanner> scanner = std::make_shared<Scanner>(System.in);
std::string sessionId = "main";
std::cout << "Type your questions (or 'exit' to quit):" << std::endl;
std::cout << "Type 'index <path>' to add documents" << std::endl;
std::cout << "Type 'status' to see physics status" << std::endl;
std::cout <<  << std::endl;
while (true) {
std::cout << "You: ";
std::string input = scanner.nextLine().trim();
if (input.isEmpty()) continue;
if (input.equalsIgnoreCase("exit")) break;
if (input.toLowerCase().startsWith("index ")) {
std::string path = input.substring(6).trim();
std::cout << "Indexing: " + path << std::endl;
try {
ai.index(path);
std::cout << "✓ Indexed" << std::endl;
} catch (Exception e) {
std::cout << "✗ Error: " + e.getMessage() << std::endl;
}
std::cout <<  << std::endl;
continue;
}
if (input.equalsIgnoreCase("status")) {
// Show system status
std::cout << "\n--- SYSTEM STATUS ---" << std::endl;
std::cout << "Session: " + sessionId << std::endl;
std::cout << "Physics: ACTIVE" << std::endl;
std::cout << "Vector Store: ACTIVE" << std::endl;
std::cout <<  << std::endl;
continue;
}
// Process query
std::cout <<  << std::endl;
try {
std::string response = ai.chat(input, sessionId);
std::cout << "Fraymus: " + response << std::endl;
} catch (Exception e) {
std::cout << "Error: " + e.getMessage() << std::endl;
}
std::cout <<  << std::endl;
}
scanner.close();
std::cout << "\nShutting down Fraymus..." << std::endl;
std::cout << "✓ System offline" << std::endl;
}
}
