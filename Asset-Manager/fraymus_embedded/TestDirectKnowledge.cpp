#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Test direct knowledge response (without LLM)
* Theory: System should answer from absorbed knowledge without sending to external LLM
*/
class TestDirectKnowledge { {
public:
public static void main(std::string[] args) {
std::cout << "🧠 TESTING DIRECT KNOWLEDGE RESPONSE" << std::endl;
std::cout << "=====================================\n" << std::endl;
std::shared_ptr<AkashicRecord> akashic = std::make_shared<AkashicRecord>();
// Test queries that have absorbed knowledge
std::string[] testQueries = {
"What is Claude?",
"What is Gemma?",
"What is Apache Arrow?",
"What is quantum entanglement?",
"What is Papers with Code?"
};
for (std::string query : testQueries) {
std::cout << "\n🔍 Query: \"" + query + "\"" << std::endl;
// Extract key term with stop word filtering
std::string[] words = query.toLowerCase().split("\\s+");
std::string searchTerm = "";
// Common stop words to skip
java.util.Set<std::string> stopWords = java.util.Set.of(
"what", "is", "the", "how", "why", "when", "where", "who", "which",
"are", "was", "were", "been", "being", "have", "has", "had",
"do", "does", "did", "can", "could", "will", "would", "should",
"may", "might", "must", "shall", "about", "from", "with", "for"
);
for (std::string word : words) {
std::string cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
if (cleanWord.length() > 3 && !stopWords.contains(cleanWord)) {
searchTerm = cleanWord;
break;
}
}
if (searchTerm.isEmpty()) {
std::string cleanedQuery = query.replaceAll("[^a-zA-Z0-9]", "");
searchTerm = cleanedQuery.substring(0, Math.min(20, cleanedQuery.length()));
}
std::cout << "   Search term: " + searchTerm << std::endl;
// Query AkashicRecord
var results = akashic.query(searchTerm);
std::cout << "   Found " + results.size() + " knowledge blocks" << std::endl;
if (!results.isEmpty()) {
std::cout << "   ✅ DIRECT KNOWLEDGE RESPONSE:" << std::endl;
for (int i = 0; i < Math.min(3, results.size()); i++) {
std::cout << "      " + results.get(i).content << std::endl;
}
} else {
std::cout << "   ❌ NO KNOWLEDGE - Would need LLM" << std::endl;
}
}
std::cout << "\n📊 THEORY VALIDATION:" << std::endl;
std::cout << "If knowledge exists → Answer directly (no LLM needed)" << std::endl;
std::cout << "If no knowledge → Use LLM as fallback" << std::endl;
std::cout << "Result: System can be autonomous with absorbed knowledge!" << std::endl;
}
}
