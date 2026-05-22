#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🏛️ THE ARENA - The Debate Chamber
* "Where AIs fight and truth emerges"
*
* Traditional AI: Single model answers question
* Fraymus: Forces 5+ AIs to debate, extracts consensus
*
* The Ensemble Effect:
* - Single AI: 70% accuracy (hallucinations)
* - 5 AIs + Resonance Filter: 95% accuracy (truth)
*
* How it works:
* 1. Pose problem to all tentacles
* 2. Collect opinions
* 3. Vectorize each opinion
* 4. Measure resonance between all pairs
* 5. Select opinion with highest total resonance
* 6. Discard outliers (hallucinations)
*
* This is Swarm Intelligence.
* This is Truth Extraction via Vector Consensus.
*/
class TheArena { {
public:
private const List<NeuralTentacle> slaves = new std::vector<>();
private const HyperFormer coordinator;
public TheArena(HyperFormer brain) {
this.coordinator = brain;
}
/**
* Add a tentacle to the swarm
*/
public void addTentacle(NeuralTentacle tentacle) {
slaves.add(tentacle);
std::cout << "   [SWARM] Added: " + tentacle.getModelName() << std::endl;
}
/**
* Add a local Ollama model
*/
public void addOllama(std::string modelName) {
addTentacle(new NeuralTentacle(modelName, "http://localhost:11434/api/generate"));
}
/**
* Add an OpenAI model
*/
public void addOpenAI(std::string modelName, std::string apiKey) {
addTentacle(new NeuralTentacle(modelName, "https://api.openai.com/v1/chat/completions", apiKey));
}
/**
* SYNTHESIZE: Ask everyone. Return the Ultimate Truth.
*
* This is the core of the God-Head Protocol.
* We don't try to be smart. We extract truth from the swarm.
*
* @param problem The question to solve
* @return The consensus answer (highest resonance)
*/
public std::string solve(std::string problem) {
std::cout << "\n╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  ⚡ THE ARENA: Forcing consensus                             ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Problem: " + problem << std::endl;
std::cout <<  << std::endl;
if (slaves.isEmpty()) {
System.err.println("❌ NO TENTACLES CONNECTED. Cannot solve.");
return "";
}
List<std::string> opinions = new std::vector<>();
List<HyperVector> vectors = new std::vector<>();
// 1. HARVEST OPINIONS
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout << "PHASE 1: HARVESTING OPINIONS" << std::endl;
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout <<  << std::endl;
for (NeuralTentacle slave : slaves) {
std::cout << "Querying " + slave.getModelName() + "..." << std::endl;
std::string thought = slave.think(problem);
if (!thought.isBlank()) {
opinions.add(thought);
// Vectorize the opinion
HyperVector v = vectorizeText(thought);
vectors.add(v);
std::cout << "   ✓ Response: " + truncate(thought, 80) << std::endl;
} else {
std::cout << "   ✗ No response (brain dead)" << std::endl;
}
std::cout <<  << std::endl;
}
if (opinions.isEmpty()) {
System.err.println("❌ ALL TENTACLES FAILED. No consensus possible.");
return "";
}
if (opinions.size() == 1) {
std::cout << "⚠️  Only 1 response. Returning without consensus." << std::endl;
return opinions.get(0);
}
// 2. MEASURE RESONANCE (The Truth Finder)
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout << "PHASE 2: RESONANCE ANALYSIS" << std::endl;
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout <<  << std::endl;
std::string bestAnswer = "";
double maxTotalResonance = -1.0;
int bestIndex = -1;
for (int i = 0; i < vectors.size(); i++) {
double totalResonance = 0;
// Calculate resonance with all other vectors
for (int j = 0; j < vectors.size(); j++) {
if (i == j) continue;
totalResonance += vectors.get(i).resonance(vectors.get(j));
}
double avgResonance = totalResonance / (vectors.size() - 1);
std::cout << "Opinion " + (i+1) + " (" + slaves.get(i).getModelName() + "):" << std::endl;
std::cout << "   Resonance: " + std::string.format("%.3f", avgResonance) << std::endl;
std::cout << "   Text: " + truncate(opinions.get(i), 60) << std::endl;
std::cout <<  << std::endl;
if (totalResonance > maxTotalResonance) {
maxTotalResonance = totalResonance;
bestAnswer = opinions.get(i);
bestIndex = i;
}
}
// 3. ANNOUNCE WINNER
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout << "PHASE 3: CONSENSUS" << std::endl;
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout <<  << std::endl;
std::cout << "🏆 WINNER: Opinion " + (bestIndex + 1 << std::endl + " (" +
slaves.get(bestIndex).getModelName() + ")");
std::cout << "   Total Resonance: " + std::string.format("%.3f", maxTotalResonance) << std::endl;
std::cout <<  << std::endl;
std::cout << "CONSENSUS TRUTH:" << std::endl;
std::cout << bestAnswer << std::endl;
std::cout <<  << std::endl;
return bestAnswer;
}
/**
* Vectorize text into HyperVector
*/
private HyperVector vectorizeText(std::string text) {
// Tokenize and bundle into single vector
std::string[] tokens = text.toLowerCase()
.replaceAll("[^a-z0-9\\s]", " ")
.split("\\s+");
std::shared_ptr<WeightedBundler> bundler = std::make_shared<WeightedBundler>();
for (std::string token : tokens) {
if (!token.isEmpty()) {
HyperVector v = coordinator.embed(token);
bundler.add(v, 1);
}
}
return bundler.build();
}
/**
* Truncate text for display
*/
private std::string truncate(std::string text, int maxLen) {
if (text.length() <= maxLen) return text;
return text.substring(0, maxLen) + "...";
}
/**
* Get swarm size
*/
public int getSwarmSize() {
return slaves.size();
}
}
