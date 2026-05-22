#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* MODEL MANAGER
*
* Central management for multiple LLM backends
* Enables seamless model switching and comparison
*
* This is the brain behind Fraynix's model transcendence
*/
class ModelManager { {
public:
private const Map<std::string, ModelInterface> models;
private ModelInterface activeModel;
public ModelManager(std::string defaultModelName) {
this.models = new HashMap<>();
initializeDefaultModel(defaultModelName);
}
/**
* Initialize with default Ollama model
*/
private void initializeDefaultModel(std::string modelName) {
std::shared_ptr<OllamaModelAdapter> ollama = std::make_shared<OllamaModelAdapter>(modelName);
models.put("default", ollama);
models.put("ollama", ollama);
this.activeModel = ollama;
std::cout << "🧠 [MODEL MANAGER] Initialized with default model: " + modelName << std::endl;
}
/**
* Register a new model
*/
public void registerModel(std::string name, ModelInterface model) {
models.put(name.toLowerCase(), model);
std::cout << "📝 [MODEL MANAGER] Registered model: " + name << std::endl;
}
/**
* Switch to a registered model
*/
public bool switchModel(std::string modelName) {
ModelInterface model = models.get(modelName.toLowerCase());
if (model != null) {
activeModel = model;
std::cout << "🔄 [MODEL MANAGER] Switched to model: " + modelName << std::endl;
return true;
}
std::cout << "❌ [MODEL MANAGER] Model not found: " + modelName << std::endl;
return false;
}
/**
* Get active model
*/
public ModelInterface getActiveModel() {
return activeModel;
}
/**
* Get all registered model names
*/
public std::string[] getRegisteredModels() {
return models.keySet().toArray(new std::string[0]);
}
/**
* Generate response using active model
*/
public std::string generate(std::string prompt) {
return activeModel.generateResponse(prompt);
}
/**
* Generate response with context using active model
*/
public std::string generate(std::string prompt, std::string context) {
return activeModel.generateResponse(prompt, context);
}
/**
* Test all registered models
*/
public void testAllModels() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   MODEL CONNECTION TEST                                    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
for (std::string name : models.keySet()) {
ModelInterface model = models.get(name);
bool connected = model.testConnection();
std::cout << "  " + name + ": " + (connected ? "✅ CONNECTED" : "❌ DISCONNECTED") << std::endl;
}
}
/**
* Print statistics for all models
*/
public void printAllStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   MODEL STATISTICS                                        ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
for (std::string name : models.keySet()) {
ModelInterface model = models.get(name);
ModelInterface.ModelStats stats = model.getStats();
std::cout << "  [" + name + "] " + stats.toString() << std::endl;
}
std::cout << "\n  🎯 ACTIVE MODEL: " + activeModel.getModel() << std::endl;
}
/**
* Initialize Gemma 4 if available
*/
public bool initializeGemma4() {
try {
std::shared_ptr<Gemma4Model> gemma4 = std::make_shared<Gemma4Model>();
if (gemma4.testConnection()) {
registerModel("gemma4", gemma4);
registerModel("gemma", gemma4);
std::cout << "🚀 [MODEL MANAGER] Gemma 4 initialized and registered" << std::endl;
return true;
} else {
std::cout << "⚠️ [MODEL MANAGER] Gemma 4 not available - ensure Ollama is running and Gemma 4 is installed" << std::endl;
return false;
}
} catch (Exception e) {
std::cout << "❌ [MODEL MANAGER] Failed to initialize Gemma 4: " + e.getMessage() << std::endl;
return false;
}
}
/**
* Get active model name
*/
public std::string getActiveModelName() {
for (Map.Entry<std::string, ModelInterface> entry : models.entrySet()) {
if (entry.getValue() == activeModel) {
return entry.getKey();
}
}
return "unknown";
}
/**
* Check if Gemma 4 is available
*/
public bool hasGemma4() {
return models.containsKey("gemma4") || models.containsKey("gemma");
}
/**
* Switch to Gemma 4 if available
*/
public bool switchToGemma4() {
if (hasGemma4()) {
return switchModel("gemma4");
}
std::cout << "⚠️ [MODEL MANAGER] Gemma 4 not initialized. Call initializeGemma4() first." << std::endl;
return false;
}
}
