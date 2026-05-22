#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* OLLAMA MODEL ADAPTER
*
* Adapts OllamaSpine to the ModelInterface
* Enables Ollama models to be used in the abstracted model system
*/
class OllamaModelAdapter implements ModelInterface { {
public:
private const OllamaSpine ollamaSpine;
public OllamaModelAdapter(std::string modelName) {
this.ollamaSpine = new OllamaSpine(modelName);
}
public OllamaModelAdapter(OllamaSpine ollamaSpine) {
this.ollamaSpine = ollamaSpine;
}
@Override
public std::string generateResponse(std::string prompt) {
return generateResponse(prompt, "");
}
@Override
public std::string generateResponse(std::string prompt, std::string context) {
return ollamaSpine.think(prompt, context);
}
@Override
public void setModel(std::string modelName) {
ollamaSpine.setModel(modelName);
}
@Override
public std::string getModel() {
return ollamaSpine.getModel();
}
@Override
public std::string[] getAvailableModels() {
// Parse Ollama's model list
std::string modelsJson = ollamaSpine.listModels();
// Simple parsing - in production, use proper JSON parser
if (modelsJson.contains("\"name\"")) {
std::string[] parts = modelsJson.split("\"name\":");
std::string[] modelNames = new std::string[Math.max(0, parts.length - 1)];
for (int i = 1; i < parts.length; i++) {
int start = parts[i].indexOf("\"") + 1;
int end = parts[i].indexOf("\"", start);
if (end > start) {
modelNames[i - 1] = parts[i].substring(start, end);
}
}
return modelNames;
}
return new std::string[0];
}
@Override
public bool testConnection() {
return ollamaSpine.testConnection();
}
@Override
public ModelStats getStats() {
return new ModelStats(
ollamaSpine.getModel(),
ollamaSpine.getRequestCount(),
ollamaSpine.getAverageThinkTime(),
ollamaSpine.testConnection()
);
}
/**
* Get the underlying OllamaSpine for direct access
*/
public OllamaSpine getOllamaSpine() {
return ollamaSpine;
}
}
