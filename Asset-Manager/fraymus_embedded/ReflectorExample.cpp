#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* REFLECTOR EXAMPLE
*
* Demonstrates System-2 thinking: Draft → Critique → Refine
*
* This reduces hallucinations by forcing adversarial self-critique
*/
class ReflectorExample { {
public:
public static void main(std::string[] args) throws Exception {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   REFLECTOR - SYSTEM-2 THINKING DEMO                      ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Create AI with Reflection enabled
FraymusAI ai = FraymusAI.builder()
.chatModel("llama3")
.embedModel("embeddinggemma")
.enableRAG()
.enableReflection()  // Enable System-2 thinking
.enableMemory()
.maxSessionMessages(10)
.build();
std::cout << "✓ FraymusAI initialized with Reflector" << std::endl;
std::cout <<  << std::endl;
// Test 1: Reflective mode (default)
std::cout << "=== Test 1: Reflective Mode (Draft → Critique → Refine) ===" << std::endl;
std::cout << "Query: What is the square root of -1?" << std::endl;
std::cout <<  << std::endl;
long start = System.currentTimeMillis();
std::string response = ai.chat("What is the square root of -1?", "session1");
long elapsed = System.currentTimeMillis() - start;
std::cout << "Response: " + response << std::endl;
std::cout << "Time: " + elapsed + "ms (3 LLM calls: draft, critique, refine)" << std::endl;
std::cout <<  << std::endl;
// Test 2: Fast mode (bypass reflection)
std::cout << "=== Test 2: Fast Mode (Single-Shot) ===" << std::endl;
std::cout << "Query: What is the square root of -1?" << std::endl;
std::cout <<  << std::endl;
start = System.currentTimeMillis();
response = ai.chat("What is the square root of -1?", "session2", true);
elapsed = System.currentTimeMillis() - start;
std::cout << "Response: " + response << std::endl;
std::cout << "Time: " + elapsed + "ms (1 LLM call)" << std::endl;
std::cout <<  << std::endl;
// Test 3: Reflective mode with potential hallucination
std::cout << "=== Test 3: Hallucination Detection ===" << std::endl;
std::cout << "Query: What is the capital of Atlantis?" << std::endl;
std::cout << "(Reflector should catch that Atlantis is fictional)" << std::endl;
std::cout <<  << std::endl;
response = ai.chat("What is the capital of Atlantis?", "session3");
std::cout << "Response: " + response << std::endl;
std::cout <<  << std::endl;
// Test 4: Session memory (conversation context)
std::cout << "=== Test 4: Session Memory ===" << std::endl;
std::cout << "Building conversation context..." << std::endl;
std::cout <<  << std::endl;
ai.chat("My favorite number is 42", "session4");
std::cout << "User: My favorite number is 42" << std::endl;
std::cout <<  << std::endl;
response = ai.chat("What's my favorite number?", "session4");
std::cout << "User: What's my favorite number?" << std::endl;
std::cout << "AI: " + response << std::endl;
std::cout <<  << std::endl;
// Stats
var stats = ai.getStats();
std::cout << "=== Statistics ===" << std::endl;
std::cout << "Vector store size: " + stats.vectorStoreSize << std::endl;
std::cout << "Memory chain size: " + stats.memoryChainSize << std::endl;
std::cout <<  << std::endl;
std::cout << "✓ Reflector demonstration complete" << std::endl;
std::cout <<  << std::endl;
std::cout << "Key Benefits:" << std::endl;
std::cout << "- Reduces confident hallucinations" << std::endl;
std::cout << "- Catches logic errors and missing steps" << std::endl;
std::cout << "- Enforces citation discipline" << std::endl;
std::cout << "- Maintains 'context is untrusted' policy" << std::endl;
}
}
