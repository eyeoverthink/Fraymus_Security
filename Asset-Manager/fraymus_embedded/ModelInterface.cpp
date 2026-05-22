#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* MODEL INTERFACE
*
* Abstraction layer for different LLM backends
* Enables seamless switching between models (Ollama, Gemma 4, etc.)
*
* This is the foundation for Fraynix's model transcendence
*/
public interface ModelInterface {
/**
* Generate response from prompt
*/
std::string generateResponse(std::string prompt);
/**
* Generate response with context
*/
std::string generateResponse(std::string prompt, std::string context);
/**
* Set active model
*/
void setModel(std::string modelName);
/**
* Get current model name
*/
std::string getModel();
/**
* Get available models
*/
std::string[] getAvailableModels();
/**
* Test connection to model
*/
bool testConnection();
/**
* Get model statistics
*/
ModelStats getStats();
/**
* Model statistics container
*/
class ModelStats { {
public:
public const std::string modelName;
public const int requestCount;
public const long averageResponseTime;
public const bool isConnected;
public ModelStats(std::string modelName, int requestCount, long averageResponseTime, bool isConnected) {
this.modelName = modelName;
this.requestCount = requestCount;
this.averageResponseTime = averageResponseTime;
this.isConnected = isConnected;
}
@Override
public std::string toString() {
return std::string.format("Model: %s | Requests: %d | Avg Time: %dms | Connected: %s",
modelName, requestCount, averageResponseTime, isConnected ? "Yes" : "No");
}
}
}
