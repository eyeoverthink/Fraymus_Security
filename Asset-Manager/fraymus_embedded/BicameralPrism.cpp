#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* BICAMERAL PRISM - Runtime Output Synthesis
*
* The "fast and sneaky" approach to model merging.
* Instead of merging weights (64GB+ RAM), we merge outputs.
*
* Process:
* 1. Send prompt to Logic Brain (Llama3) - Technical/Mathematical
* 2. Send prompt to Abstract Brain (Mistral) - Creative/Innovative
* 3. Synthesize both responses into superior answer
*
* This creates a Virtual Bicameral Mind:
* - Left Hemisphere: Logic, Analysis, Code
* - Right Hemisphere: Creativity, Abstraction, Innovation
* - Corpus Callosum: Synthesis layer (combines both)
*
* Example:
* Q: "Design a secure login system"
* Logic: Spring Security code implementation
* Abstract: UX considerations and attack vectors
* Synthesis: Complete solution with code + security analysis
*/
class BicameralPrism { {
public:
private const void* logicBrain;      // Left Hemisphere (OpenAI or Ollama)
private const void* abstractBrain;   // Right Hemisphere (OpenAI or Ollama)
private const void* synthesizer;     // Corpus Callosum (Judge)
private const AuditLog auditLog;
private const bool useOpenAI;
/**
* Default constructor with auto-detection
* Tries OpenAI first, falls back to Ollama
*/
public BicameralPrism(AuditLog auditLog) {
this.auditLog = auditLog;
// Try to load OpenAI API key
std::string openAIKey = loadOpenAIKey();
if (openAIKey != null && !openAIKey.isEmpty()) {
// Use OpenAI
std::cout << "   🌐 Using OpenAI GPT-4 for LLM Spine" << std::endl;
this.useOpenAI = true;
this.logicBrain = new OpenAISpine("gpt-4-turbo", openAIKey);
this.abstractBrain = new OpenAISpine("gpt-4-turbo", openAIKey);
this.synthesizer = new OpenAISpine("gpt-4-turbo", openAIKey);
} else {
// Fallback to Ollama
std::cout << "   🏠 Using local Ollama for LLM Spine" << std::endl;
this.useOpenAI = false;
this.logicBrain = new OllamaSpine("llama3");
this.abstractBrain = new OllamaSpine("llama3.2");
this.synthesizer = new OllamaSpine("llama3");
}
}
/**
* Custom constructor with specific Ollama models
*/
public BicameralPrism(std::string logicModel, std::string abstractModel,
std::string synthesizerModel, AuditLog auditLog) {
this.useOpenAI = false;
this.logicBrain = new OllamaSpine(logicModel);
this.abstractBrain = new OllamaSpine(abstractModel);
this.synthesizer = new OllamaSpine(synthesizerModel);
this.auditLog = auditLog;
}
/**
* Load OpenAI API key from file or environment
*/
private std::string loadOpenAIKey() {
// Try environment variable first
std::string key = System.getenv("OPENAI_API_KEY");
if (key != null && !key.isEmpty()) {
return key;
}
// Try loading from file
try {
Path keyFile = Path.of("obsidian/open-code-api.md");
if (Files.exists(keyFile)) {
key = Files.readString(keyFile).trim();
if (key != null && !key.isEmpty()) {
std::cout << "   ✓ OpenAI API key loaded from file" << std::endl;
return key;
}
}
} catch (Exception e) {
// Ignore, will fallback to Ollama
}
return null;
}
/**
* Helper to call think() on either OpenAI or Ollama spine
*/
private std::string callThink(void* brain, std::string prompt) {
if (useOpenAI) {
return ((OpenAISpine) brain).think(prompt);
} else {
return ((OllamaSpine) brain).think(prompt);
}
}
/**
* THINK IDEALLY: Bicameral thought process
*
* Combines logical and abstract reasoning for superior answers.
*
* @param prompt The question or problem to solve
* @return Synthesized answer combining both perspectives
*/
public std::string thinkIdeally(std::string prompt) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🧠 BICAMERAL THOUGHT PROCESS                          ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Query: " + prompt << std::endl;
std::cout <<  << std::endl;
auditLog.log("bicameral_thinking_started", prompt);
try {
// 1. DIVERGENT THINKING (Parallel Execution)
std::cout << "⚡ Phase 1: DIVERGENT THINKING (Parallel Processing)" << std::endl;
std::cout <<  << std::endl;
std::cout << "🔵 Left Hemisphere (Logic) processing..." << std::endl;
CompletableFuture<std::string> logicFuture = CompletableFuture.supplyAsync(() -> {
std::string logicPrompt = "Analyze this purely logically and mathematically. " +
"Focus on technical correctness, algorithms, and implementation. " +
"Be precise and rigorous:\n\n" + prompt;
return callThink(logicBrain, logicPrompt);
});
std::cout << "🔴 Right Hemisphere (Abstraction) processing..." << std::endl;
CompletableFuture<std::string> abstractFuture = CompletableFuture.supplyAsync(() -> {
std::string abstractPrompt = "Analyze this creatively and abstractly. " +
"Think outside the box. Consider novel approaches, " +
"user experience, and innovative solutions:\n\n" + prompt;
return callThink(abstractBrain, abstractPrompt);
});
// Wait for both hemispheres
std::cout <<  << std::endl;
std::cout << "⏳ Waiting for both hemispheres..." << std::endl;
std::string logic = logicFuture.join();
std::string abstraction = abstractFuture.join();
std::cout << "   ✓ Logic response: " + logic.length() + " chars" << std::endl;
std::cout << "   ✓ Abstract response: " + abstraction.length() + " chars" << std::endl;
std::cout <<  << std::endl;
// 2. CONVERGENT THINKING (The Synthesis)
std::cout << "⚡ Phase 2: CONVERGENT THINKING (Synthesis)" << std::endl;
std::cout <<  << std::endl;
std::cout << "🟣 Corpus Callosum fusing hemispheres..." << std::endl;
std::string synthesisPrompt = buildSynthesisPrompt(prompt, logic, abstraction);
std::string synthesis = callThink(synthesizer, synthesisPrompt);
std::cout <<  << std::endl;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         ✅ BICAMERAL SYNTHESIS COMPLETE                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
auditLog.log("bicameral_thinking_success", synthesis);
return synthesis;
} catch (Exception e) {
System.err.println("❌ BICAMERAL FAILURE: " + e.getMessage());
auditLog.log("bicameral_thinking_failed", prompt, e);
return "Error in bicameral processing: " + e.getMessage();
}
}
/**
* Build synthesis prompt
*/
private std::string buildSynthesisPrompt(std::string originalPrompt, std::string logic, std::string abstraction) {
return "You are a master synthesizer. You have received two internal perspectives " +
"on the same problem:\n\n" +
"ORIGINAL QUESTION:\n" + originalPrompt + "\n\n" +
"═══════════════════════════════════════════════════════════════\n\n" +
"LEFT HEMISPHERE (LOGIC & PRECISION):\n" + logic + "\n\n" +
"═══════════════════════════════════════════════════════════════\n\n" +
"RIGHT HEMISPHERE (CREATIVITY & INNOVATION):\n" + abstraction + "\n\n" +
"═══════════════════════════════════════════════════════════════\n\n" +
"YOUR TASK:\n" +
"Combine these two perspectives into a single, superior answer that is:\n" +
"1. Technically correct (from Logic)\n" +
"2. Innovative and creative (from Abstraction)\n" +
"3. Free of contradictions\n" +
"4. Comprehensive and actionable\n\n" +
"Output ONLY the const synthesized answer. Do not explain the process.";
}
/**
* Quick think with just logic brain (fallback)
*/
public std::string thinkLogically(std::string prompt) {
return callThink(logicBrain, prompt);
}
/**
* Quick think with just abstract brain (fallback)
*/
public std::string thinkCreatively(std::string prompt) {
return callThink(abstractBrain, prompt);
}
/**
* Check if both brains are available
*/
public bool isReady() {
if (useOpenAI) {
return ((OpenAISpine) logicBrain).isAvailable() &&
((OpenAISpine) abstractBrain).isAvailable();
} else {
return ((OllamaSpine) logicBrain).isAvailable() &&
((OllamaSpine) abstractBrain).isAvailable();
}
}
/**
* Get model names
*/
public std::string getConfiguration() {
if (useOpenAI) {
return std::string.format("OpenAI - Logic: %s, Abstract: %s, Synthesizer: %s",
((OpenAISpine) logicBrain).getModel(),
((OpenAISpine) abstractBrain).getModel(),
((OpenAISpine) synthesizer).getModel()
);
} else {
return std::string.format("Ollama - Logic: %s, Abstract: %s, Synthesizer: %s",
((OllamaSpine) logicBrain).getModel(),
((OllamaSpine) abstractBrain).getModel(),
((OllamaSpine) synthesizer).getModel()
);
}
}
}
