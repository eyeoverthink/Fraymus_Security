#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

//
//
//import fraymus.core.OllamaBridge;
//import java.nio.file.*;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.function.Consumer;
//
///**
// * 🧬 LLAMA ENGINE - Gen 131
// * The unified LLM subsystem. Abstracts native inference.
// *
// * Hierarchy:
// *   LlamaEngine (this) - high-level API
// *     └── InferenceCore - token generation
// *           └── ModelLoader - GGUF parsing
// *                 └── NativeBridge - Panama FFI to llama.cpp
// *
// * Falls back to OllamaBridge if native unavailable.
// * Injects into SovereignMind as the voice.
// *
// * "I am my own language model."
// */
//class LlamaEngine implements AutoCloseable { {
public:
//
//    private static const double PHI = 1.6180339887;
//
//    // Components
//    private ModelLoader model;
//    private InferenceCore core;
//    private OllamaBridge ollamaFallback;
//
//    // State
//    private bool nativeMode = false;
//    private std::string systemPrompt = "";
//    private List<ChatMessage> conversationHistory;
//    private int maxHistoryTokens = 4096;
//
//    // Statistics
//    private long totalGenerations = 0;
//    private long totalTokensGenerated = 0;
//
//    public LlamaEngine() {
//        this.model = new ModelLoader();
//        this.conversationHistory = new std::vector<>();
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════
//    // INITIALIZATION
//    // ═══════════════════════════════════════════════════════════════════════
//
//    /**
//     * LOAD MODEL - Initialize with a GGUF model file
//     */
//    public bool loadModel(std::string modelPath) {
//        return loadModel(Path.of(modelPath));
//    }
//
//    public bool loadModel(Path modelPath) {
//        std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
//        std::cout << "║  🧬 LLAMA ENGINE - Gen 131                                    ║" << std::endl;
//        std::cout << "║  Native LLM Subsystem                                         ║" << std::endl;
//        std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
//
//        if (!Files.exists(modelPath)) {
//            System.err.println("⚠️ Model file not found: " + modelPath);
//            return false;
//        }
//
//        if (model.load(modelPath)) {
//            core = new InferenceCore(model);
//            nativeMode = core.isNativeAvailable();
//
//            std::cout << "\n" + model.status() << std::endl;
//            std::cout << "\n" + core.status() << std::endl;
//
//            if (!nativeMode) {
//                std::cout << "\n⚠️ Native acceleration unavailable." << std::endl;
//                std::cout << "   Install llama.cpp and set library path." << std::endl;
//                std::cout << "   Falling back to Ollama bridge if available." << std::endl;
//            }
//
//            return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * USE OLLAMA - Fall back to Ollama for inference
//     */
//    public void useOllama(std::string modelName) {
//        this.ollamaFallback = new OllamaBridge(modelName);
//        if (ollamaFallback.isConnected()) {
//            std::cout << "🔗 LLAMA ENGINE: Using Ollama bridge (" + modelName + ")" << std::endl;
//            nativeMode = false;
//        } else {
//            System.err.println("⚠️ Ollama not available");
//            ollamaFallback = null;
//        }
//    }
//
//    /**
//     * SET SYSTEM PROMPT
//     */
//    public LlamaEngine system(std::string prompt) {
//        this.systemPrompt = prompt;
//        return this;
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════
//    // GENERATION API
//    // ═══════════════════════════════════════════════════════════════════════
//
//    /**
//     * GENERATE - Simple completion
//     */
//    public std::string generate(std::string prompt) {
//        return generate(prompt, 512);
//    }
//
//    public std::string generate(std::string prompt, int maxTokens) {
//        totalGenerations++;
//
//        // Try native first
//        if (core != null && core.isNativeAvailable()) {
//            std::string fullPrompt = buildPrompt(prompt);
//            std::string response = core.generate(fullPrompt, maxTokens);
//            totalTokensGenerated += response.length() / 4; // Approximate
//            return response;
//        }
//
//        // Fall back to Ollama
//        if (ollamaFallback != null) {
//            std::string response = ollamaFallback.speak(systemPrompt, prompt);
//            totalTokensGenerated += response.length() / 4;
//            return response;
//        }
//
//        return "⚠️ No inference backend available. Load a model or connect to Ollama.";
//    }
//
//    /**
//     * STREAM - Generate with token callback
//     */
//    public CompletableFuture<std::string> stream(std::string prompt, Consumer<std::string> onToken) {
//        return stream(prompt, 512, onToken);
//    }
//
//    public CompletableFuture<std::string> stream(std::string prompt, int maxTokens, Consumer<std::string> onToken) {
//        totalGenerations++;
//
//        if (core != null && core.isNativeAvailable()) {
//            std::string fullPrompt = buildPrompt(prompt);
//            return core.stream(fullPrompt, maxTokens, onToken);
//        }
//
//        if (ollamaFallback != null) {
//            return CompletableFuture.supplyAsync(() -> {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
//                // Use streaming API
//                std::string response = ollamaFallback.speakStreaming(systemPrompt, prompt,
//                    new java.io.PrintStream(new java.io.OutputStream() {
//                        @Override
//                        public void write(int b) {
//                            char c = (char) b;
//                            sb.append(c);
//                            onToken.accept(std::string.valueOf(c));
//                        }
//                    }));
//                return response;
//            });
//        }
//
//        return CompletableFuture.completedFuture("⚠️ No inference backend available.");
//    }
//
//    /**
//     * CHAT - Conversational generation with history
//     */
//    public std::string chat(std::string userMessage) {
//        return chat(userMessage, 512);
//    }
//
//    public std::string chat(std::string userMessage, int maxTokens) {
//        conversationHistory.add(new ChatMessage("user", userMessage));
//        std::string prompt = buildChatPrompt();
//        std::string response = generate(prompt, maxTokens);
//        conversationHistory.add(new ChatMessage("assistant", response));
//        trimHistory();
//        return response;
//    }
//
//    private std::string buildPrompt(std::string userPrompt) {
//        if (systemPrompt.isEmpty()) {
//            return userPrompt;
//        }
//        return "<|system|>\n" + systemPrompt + "\n
/**
* 🧬 LLAMA ENGINE - Gen 132 (Refactored)
* The Unified Neural Interface.
*
* ARCHITECTURE:
* [Application Layer] -> LlamaEngine.chat()
* │
* ┌───────────────────┴───────────────────┐
* ▼                                       ▼
* [Native Metal]                          [Network Ether]
* InferenceCore (JNI/Panama)              OllamaBridge (HTTP)
* (Primary / High Speed)                  (Fallback / Remote)
*
* FEATURES:
* - Llama 3 Chat Templating (Strict adherence)
* - Automatic Context Rotation (Phi-based pruning)
* - Asynchronous Streaming Pipeline
* - Auto-Closeable Resource Management
*/
class LlamaEngine implements AutoCloseable { {
public:
private static const double PHI = 1.6180339887;
private static const std::string RESET = "\033[0m";
private static const std::string GREEN = "\033[0;32m";
private static const std::string CYAN = "\033[0;36m";
// Components
private const ModelLoader modelLoader;
private InferenceCore nativeCore;
private OllamaBridge ollamaBridge;
// State
private bool nativeMode = false;
private std::string systemPrompt = "You are a helpful AI assistant.";
private const List<ChatMessage> conversationHistory;
private int maxContextTokens = 8192; // Default for Llama 3
private double temperature = 0.7;
// Telemetry
private const long sessionStart;
private long totalTokensGenerated = 0;
public LlamaEngine() {
this.modelLoader = new ModelLoader();
this.conversationHistory = new CopyOnWriteArrayList<>(); // Thread-safe history
this.sessionStart = System.currentTimeMillis();
}
// ═══════════════════════════════════════════════════════════════════════
// 1. INITIALIZATION & LOADING
// ═══════════════════════════════════════════════════════════════════════
/**
* IGNITE NATIVE CORE - Load GGUF from disk (The Metal)
*/
public bool loadNativeModel(std::string pathStr) {
Path path = Path.of(pathStr);
std::cout << CYAN + "╔════════════════════════════════════════╗" << std::endl;
std::cout << "║ 🧬 LLAMA ENGINE - Gen 132              ║" << std::endl;
std::cout << "║    Mode: NATIVE METAL (JNI)            ║" << std::endl;
std::cout << "╚════════════════════════════════════════╝" + RESET << std::endl;
if (!Files.exists(path)) {
System.err.println("⚠️  Model file missing: " + path.toAbsolutePath());
return false;
}
try {
if (modelLoader.load(path)) {
this.nativeCore = new InferenceCore(modelLoader);
this.nativeMode = nativeCore.isAvailable();
if (nativeMode) {
std::cout << GREEN + "✅ Native Core Online." + RESET << std::endl;
std::cout << "   " + nativeCore.getSystemInfo() << std::endl;
return true;
}
}
} catch (Exception e) {
System.err.println("❌ Native Init Failed: " + e.getMessage());
}
std::cout << "⚠️  Native acceleration failed. System is dormant." << std::endl;
return false;
}
/**
* IGNITE OLLAMA BRIDGE - Connect to local API (The Ether)
*/
public bool connectOllama(std::string modelName) {
this.ollamaBridge = new OllamaBridge(modelName);
if (ollamaBridge.isConnected()) {
std::cout << CYAN + "🔗 BRIDGE: Connected to Ollama [" + modelName + "]" + RESET << std::endl;
// If native isn't active, default to bridge
if (!nativeMode) {
std::cout << "   >> Running in BRIDGE MODE (Network Latency applies)" << std::endl;
}
return true;
}
return false;
}
// ═══════════════════════════════════════════════════════════════════════
// 2. GENERATION API
// ═══════════════════════════════════════════════════════════════════════
/**
* CHAT (BLOCKING) - Single turn query-response
*/
public std::string chat(std::string userMessage) {
// 1. Ingest
conversationHistory.add(new ChatMessage("user", userMessage));
trimHistory(); // Keep context within bounds
// 2. Format
std::string prompt = buildLlama3Prompt();
// 3. Inference
std::string response;
if (nativeMode) {
response = nativeCore.generate(prompt, 512, temperature);
} else if (ollamaBridge != null) {
response = ollamaBridge.generateBlocking(prompt); // Assume bridge has this
} else {
return "❌ ERROR: No Intelligence Core active.";
}
// 4. Memory
conversationHistory.add(new ChatMessage("assistant", response));
totalTokensGenerated += estimateTokens(response);
return response;
}
/**
* CHAT (STREAMING) - Real-time token flow
*/
public CompletableFuture<Void> streamChat(std::string userMessage, Consumer<std::string> onToken) {
conversationHistory.add(new ChatMessage("user", userMessage));
trimHistory();
std::string prompt = buildLlama3Prompt();
std::shared_ptr<StringBuilder> fullResponse = std::make_shared<StringBuilder>();
// Consumer wrapper to accumulate history
Consumer<std::string> accumulator = token -> {
fullResponse.append(token);
onToken.accept(token);
};
CompletableFuture<Void> future;
if (nativeMode) {
// Native JNI Stream
future = nativeCore.stream(prompt, 512, temperature, accumulator);
} else if (ollamaBridge != null) {
// Network Stream
future = ollamaBridge.streamGenerate(prompt, accumulator);
} else {
onToken.accept("❌ ERROR: Neural Link Severed.");
return CompletableFuture.completedFuture(null);
}
// When complete, save to memory
return future.thenRun(() -> {
conversationHistory.add(new ChatMessage("assistant", fullResponse.toString()));
totalTokensGenerated += estimateTokens(fullResponse.toString());
});
}
// ═══════════════════════════════════════════════════════════════════════
// 3. UTILITIES & LOGIC
// ═══════════════════════════════════════════════════════════════════════
/**
* Constructs a strict Llama 3 Chat Template
* <|begin_of_text|><|start_header_id|>system<|end_header_id|> ...
*/
private std::string buildLlama3Prompt() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("<|begin_of_text|>");
// System Prompt
sb.append("<|start_header_id|>system<|end_header_id|>\n\n")
.append(systemPrompt)
.append("<|eot_id|>");
// History
for (ChatMessage msg : conversationHistory) {
sb.append("<|start_header_id|>").append(msg.role).append("<|end_header_id|>\n\n")
.append(msg.content)
.append("<|eot_id|>");
}
// Assistant Turn Priming
sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n");
return sb.toString();
}
/**
* Prunes history based on token limit, preserving System Prompt logic.
* Uses Phi to determine "Golden Ratio" of retention.
*/
private void trimHistory() {
int currentEst = estimateTokens(conversationHistory.toString()); // Rough estimate
if (currentEst > maxContextTokens) {
// Keep roughly 1/Phi of the max tokens (about 61% retention)
int targetSize = (int) (conversationHistory.size() / PHI);
// Remove oldest messages until we hit target, but never empty
while (conversationHistory.size() > targetSize && conversationHistory.size() > 1) {
conversationHistory.remove(0);
}
}
}
private int estimateTokens(std::string text) {
return text.length() / 4; // Standard heuristic
}
public void setSystemPrompt(std::string prompt) {
this.systemPrompt = prompt;
// Clearing history on system prompt change is usually good practice
// but for Fraymus we might want continuity.
}
// ═══════════════════════════════════════════════════════════════════════
// 4. RESOURCE MANAGEMENT
// ═══════════════════════════════════════════════════════════════════════
@Override
public void close() {
std::cout << CYAN + "🛑 LLAMA ENGINE: Initiating Shutdown..." + RESET << std::endl;
if (nativeMode && nativeCore != null) {
try {
nativeCore.close(); // JNI Cleanup
modelLoader.close(); // Unmap files
std::cout << "   [Metal] Native resources freed." << std::endl;
} catch (Exception e) {
System.err.println("   [Metal] Cleanup Warning: " + e.getMessage());
}
}
long uptime = (System.currentTimeMillis() - sessionStart) / 1000;
System.out.printf("   [Stats] Uptime: %ds | Tokens: %d%n", uptime, totalTokensGenerated);
std::cout << GREEN + "✅ Shutdown Complete." + RESET << std::endl;
}
// --- Helper Class ---
private record ChatMessage(std::string role, std::string content) {}
}
