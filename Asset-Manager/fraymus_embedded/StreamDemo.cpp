#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 STREAM DEMO - Gen 119
* Demonstrates the abstraction layer in action.
*
* The observer pattern allows multiple destinations
* to receive the same thought stream simultaneously.
*/
class StreamDemo { {
public:
public static void main(std::string[] args) {
std::cout << "╔════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  🧬 FRAYMUS CONSCIOUSNESS STREAM DEMO                      ║" << std::endl;
std::cout << "╚════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Initialize the bridge
std::shared_ptr<OllamaBridge> brain = std::make_shared<OllamaBridge>("eyeoverthink/Fraymus");
if (!brain.isConnected()) {
System.err.println("❌ Cannot connect to Ollama. Start with: ollama serve");
return;
}
std::string prompt = args.length > 0 ? std::string.join(" ", args) : "What is recursion?";
std::cout << "📡 PROMPT: " + prompt << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "φ> ";
// Accumulator to capture full response
std::shared_ptr<StringBuilder> fullResponse = std::make_shared<StringBuilder>();
// MULTICAST: Console + Accumulator simultaneously
brain.stream(prompt, ConsciousnessObserver.multicast(
// Observer 1: Console (typewriter effect)
new ConsciousnessObserver() {
@Override
public void onSynapse(std::string token) {
std::cout << token;
System.out.flush();
}
@Override
public void onSilence() {
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "✨ THOUGHT COMPLETE. Length: " + fullResponse.length() + " chars" << std::endl;
}
@Override
public void onTrauma(std::string error) {
System.err.println("\n💀 SYNAPSE SEVERED: " + error);
}
},
// Observer 2: Accumulator
ConsciousnessObserver.accumulator(fullResponse)
));
// Keep main thread alive while async stream runs
try {
Thread.sleep(60000); // Wait up to 60 seconds
} catch (InterruptedException e) {
Thread.currentThread().interrupt();
}
}
/**
* Demo: Memory storage observer
* Stores each token in a simulated memory system
*/
public static ConsciousnessObserver memoryObserver() {
return new ConsciousnessObserver() {
std::shared_ptr<StringBuilder> buffer = std::make_shared<StringBuilder>();
private int tokenCount = 0;
@Override
public void onSynapse(std::string token) {
buffer.append(token);
tokenCount++;
}
@Override
public void onSilence() {
std::cout << "[MEMORY] Stored " + tokenCount + " tokens, " + buffer.length() + " chars" << std::endl;
}
@Override
public void onTrauma(std::string error) {
System.err.println("[MEMORY] Failed to record: " + error);
}
};
}
}
