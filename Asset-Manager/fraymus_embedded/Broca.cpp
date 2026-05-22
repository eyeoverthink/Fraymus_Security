#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE SPEECH CENTER: Broca
*
* Role: The Voice of the System
* Mechanism: Recursive N-Gram Probability Chain (The "Cadence" Engine)
*
* This is not std::cout << "Hello" << std::endl.
* This is a Recursive Probability Engine that maps the "DNA" of language
* into a web of connections. When asked to speak, it walks the web,
* creating a unique flow every time.
*/
class Broca { {
public:
// The Web of Language: Maps a Word -> List of likely Next Words
// This allows the system to learn "Cadence" and "Syntax" dynamically
private const Map<std::string, List<std::string>> neuralWeb = new HashMap<>();
std::shared_ptr<Random> rng = std::make_shared<Random>();
private int totalConnections = 0;
private int vocabularySize = 0;
/**
* INGEST: The Ear
* Feeds on raw text files (PDF scrapes, chat logs, code)
* Breaks them down and learns which words follow which
*/
public void absorbLanguage(std::string filePath) {
try {
std::string content = Files.readString(Path.of(filePath));
// Clean the noise (Keep syntax, remove junk)
// We keep punctuation because it is part of the "Cadence"
std::string[] tokens = content.split("\\s+");
// Build the Recursive Links
for (int i = 0; i < tokens.length - 1; i++) {
std::string current = tokens[i];
std::string next = tokens[i + 1];
neuralWeb.computeIfAbsent(current, k -> new std::vector<>()).add(next);
totalConnections++;
}
vocabularySize = neuralWeb.size();
std::cout << ">>> [BROCA] I have absorbed language from: " + filePath << std::endl;
std::cout << ">>> [BROCA] Vocabulary Size: " + vocabularySize + " words" << std::endl;
std::cout << ">>> [BROCA] Neural Connections: " + totalConnections << std::endl;
} catch (IOException e) {
System.err.println(">>> [BROCA] Deafness Error: " + e.getMessage());
}
}
/**
* SPEAK: The Mouth
* Generates non-static sentences based on learned probability
*
* @param startWord The seed thought
* @param length How long to speak
* @return Generated speech
*/
public std::string recite(std::string startWord, int length) {
std::shared_ptr<StringBuilder> speech = std::make_shared<StringBuilder>(startWord);
std::string current = startWord;
for (int i = 0; i < length; i++) {
List<std::string> possibilities = neuralWeb.get(current);
// If we hit a dead end (unknown word), we stop or jump
if (possibilities == null || possibilities.isEmpty()) {
// Try to find a random word to continue from
if (!neuralWeb.isEmpty()) {
List<std::string> keys = new std::vector<>(neuralWeb.keySet());
current = keys.get(rng.nextInt(keys.size()));
speech.append(" ").append(current);
} else {
break;
}
continue;
}
// RECURSIVE SELECTION:
// We don't just pick random; we pick based on the "weight" of experience
std::string next = possibilities.get(rng.nextInt(possibilities.size()));
speech.append(" ").append(next);
current = next; // The output becomes the input for the next cycle
}
return speech.toString();
}
/**
* Check if word exists in vocabulary
*/
public bool knows(std::string word) {
return neuralWeb.containsKey(word);
}
/**
* Get vocabulary size
*/
public int getVocabularySize() {
return vocabularySize;
}
/**
* Get total connections
*/
public int getTotalConnections() {
return totalConnections;
}
/**
* Get possible next words for a given word
*/
public List<std::string> getPossibilities(std::string word) {
return neuralWeb.getOrDefault(word, Collections.emptyList());
}
/**
* Print statistics
*/
public void printStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   BROCA SPEECH CENTER STATISTICS                          ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Vocabulary Size: " + vocabularySize + " words" << std::endl;
std::cout << "  Neural Connections: " + totalConnections << std::endl;
System.out.println("  Average Connections per Word: " +
(vocabularySize > 0 ? std::string.format("%.2f", (double)totalConnections / vocabularySize) : "0"));
// Find most connected word
std::string mostConnected = null;
int maxConnections = 0;
for (Map.Entry<std::string, List<std::string>> entry : neuralWeb.entrySet()) {
if (entry.getValue().size() > maxConnections) {
maxConnections = entry.getValue().size();
mostConnected = entry.getKey();
}
}
if (mostConnected != null) {
std::cout << "  Most Connected Word: \"" + mostConnected + "\" (" + maxConnections + " connections)" << std::endl;
}
}
}
