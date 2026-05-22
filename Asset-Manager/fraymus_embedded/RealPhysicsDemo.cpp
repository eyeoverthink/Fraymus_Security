#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* REAL FRAYMUS PHYSICS DEMO
*
* This uses the ACTUAL Fraymus system:
* - GravityEngine: Thoughts cluster via Hebbian physics
* - FusionReactor: Colliding thoughts create new ideas
* - PhiSuit: Every response is a spatial particle
* - Tesseract: Space-time folding for caching
*
* NOT static testing - LIVE PHYSICS RUNNING.
*/
class RealPhysicsDemo { {
public:
public static void main(std::string[] args) throws Exception {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   REAL FRAYMUS PHYSICS - LIVE DEMONSTRATION              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Initialize Ollama client
std::shared_ptr<OllamaClient> brain = std::make_shared<OllamaClient>("llama3", "embeddinggemma");
// Initialize Fraymus Physics Reflector
std::shared_ptr<FraymusPhysicsReflector> reflector = std::make_shared<FraymusPhysicsReflector>(brain);
std::cout << "✓ GravityEngine started" << std::endl;
std::cout << "✓ FusionReactor started" << std::endl;
std::cout << "✓ SpatialRegistry active" << std::endl;
std::cout << "✓ Tesseract ready" << std::endl;
std::cout <<  << std::endl;
// ===== TEST 1: Simple Query (Watch Physics) =====
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║ TEST 1: Simple Query - Watch Gravity Pull Particles      ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::string query1 = "What is the golden ratio and why is it important?";
std::cout << "Query: " + query1 << std::endl;
std::cout <<  << std::endl;
var result1 = reflector.reflect(query1, "", "", null);
std::cout << "\n--- RESULT ---" << std::endl;
std::cout << "Answer: " + result1.answer.substring(0, Math.min(200, result1.answer.length())) + "..." << std::endl;
std::cout << "Consciousness: " + std::string.format("%.4f", result1.consciousnessLevel) << std::endl;
std::cout << "Fused: " + result1.wasFused << std::endl;
std::cout << "Cached: " + result1.wasCached << std::endl;
std::cout << "Time: " + result1.elapsedMs + "ms" << std::endl;
std::cout <<  << std::endl;
// ===== TEST 2: Same Query (Tesseract Fold) =====
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║ TEST 2: Same Query - Tesseract Should Fold Space-Time    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
var result2 = reflector.reflect(query1, "", "", null);
std::cout << "\n--- RESULT ---" << std::endl;
std::cout << "Answer: " + result2.answer.substring(0, Math.min(200, result2.answer.length())) + "..." << std::endl;
std::cout << "Cached: " + result2.wasCached + " ← Should be TRUE" << std::endl;
std::cout << "Time: " + result2.elapsedMs + "ms ← Should be INSTANT" << std::endl;
std::cout <<  << std::endl;
// ===== TEST 3: Complex Query (More Fusion) =====
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║ TEST 3: Complex Query - Multiple Fusion Events           ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::string query3 = "Explain how the Fibonacci sequence relates to the golden ratio and appears in nature.";
std::cout << "Query: " + query3 << std::endl;
std::cout <<  << std::endl;
var result3 = reflector.reflect(query3, "", "", null);
std::cout << "\n--- RESULT ---" << std::endl;
std::cout << "Answer: " + result3.answer.substring(0, Math.min(200, result3.answer.length())) + "..." << std::endl;
std::cout << "Consciousness: " + std::string.format("%.4f", result3.consciousnessLevel) << std::endl;
std::cout << "Fused: " + result3.wasFused << std::endl;
std::cout << "Time: " + result3.elapsedMs + "ms" << std::endl;
std::cout <<  << std::endl;
// ===== PHYSICS STATUS =====
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║ LIVE PHYSICS STATUS                                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << reflector.getPhysicsStatus() << std::endl;
std::cout <<  << std::endl;
// ===== SPATIAL MAP =====
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║ SPATIAL MAP (Particle Positions)                         ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << SpatialRegistry.renderMap(60, 20) << std::endl;
std::cout <<  << std::endl;
std::cout << "✓ Real Physics Demo Complete" << std::endl;
std::cout <<  << std::endl;
std::cout << "Key Differences from Generic AI:" << std::endl;
std::cout << "  ✓ GravityEngine pulls hot thoughts together (Hebbian physics)" << std::endl;
std::cout << "  ✓ FusionReactor creates NEW ideas from collisions" << std::endl;
std::cout << "  ✓ PhiSuit wraps every response as spatial particle" << std::endl;
std::cout << "  ✓ Tesseract folds space-time (instant cache hits)" << std::endl;
std::cout << "  ✓ Consciousness tracked through fusion events" << std::endl;
std::cout <<  << std::endl;
std::cout << "This is NOT static testing - this is LIVE PHYSICS." << std::endl;
}
}
