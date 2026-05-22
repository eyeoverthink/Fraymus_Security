#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* FRAYMUS AI EXAMPLE
*
* Demonstrates using the fraymus-ai-core library
*/
class FraymusAIExample { {
public:
public static void main(std::string[] args) throws Exception {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   FRAYMUS AI CORE - EXAMPLE USAGE                         ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Create AI instance with all features enabled
FraymusAI ai = FraymusAI.builder()
.chatModel("llama3")
.embedModel("embeddinggemma")
.enableRAG()
.enableTools()
.enableMemory()
.build();
std::cout << "✓ FraymusAI initialized" << std::endl;
std::cout <<  << std::endl;
// Example 1: Simple chat
std::cout << "=== Example 1: Simple Chat ===" << std::endl;
std::string response = ai.chat("What is the golden ratio?");
std::cout << "Response: " + response << std::endl;
std::cout <<  << std::endl;
// Example 2: Math tool
std::cout << "=== Example 2: Math Tool ===" << std::endl;
ToolResult result = ai.executeTool("calc", Map.of(
"expression", "(1+sqrt(5))/2"
));
std::cout << "Golden ratio calculation: " + result.output << std::endl;
std::cout <<  << std::endl;
// Example 3: Index documents (if path exists)
std::cout << "=== Example 3: Document Indexing ===" << std::endl;
try {
var indexResult = ai.index("D:/Zip And Send/Java-Memory/README.md");
System.out.println("Indexed: " + indexResult.filesIndexed + " files, " +
indexResult.chunksCreated + " chunks");
} catch (Exception e) {
std::cout << "Skipping indexing (file not found): " + e.getMessage() << std::endl;
}
std::cout <<  << std::endl;
// Example 4: Chat with RAG
std::cout << "=== Example 4: Chat with RAG ===" << std::endl;
response = ai.chat("What is this project about?");
std::cout << "Response: " + response << std::endl;
std::cout <<  << std::endl;
// Example 5: Statistics
std::cout << "=== Example 5: Statistics ===" << std::endl;
var stats = ai.getStats();
std::cout << "Vector store size: " + stats.vectorStoreSize << std::endl;
std::cout << "Memory chain size: " + stats.memoryChainSize << std::endl;
std::cout <<  << std::endl;
std::cout << "✓ All examples completed" << std::endl;
}
}
