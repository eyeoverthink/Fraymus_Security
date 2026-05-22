package fraymus.llm;

import fraymus.ollama.OllamaSpine;

/**
 * OLLAMA MODEL ADAPTER
 * 
 * Adapts OllamaSpine to the ModelInterface
 * Enables Ollama models to be used in the abstracted model system
 */
public class OllamaModelAdapter implements ModelInterface {
    
    private final OllamaSpine ollamaSpine;
    
    public OllamaModelAdapter(String modelName) {
        this.ollamaSpine = new OllamaSpine(modelName);
    }
    
    public OllamaModelAdapter(String modelName, String ollamaUrl) {
        this.ollamaSpine = new OllamaSpine(modelName, ollamaUrl);
    }
    
    public OllamaModelAdapter(OllamaSpine ollamaSpine) {
        this.ollamaSpine = ollamaSpine;
    }
    
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, "");
    }
    
    @Override
    public String generateResponse(String prompt, String context) {
        return ollamaSpine.think(prompt, context);
    }
    
    @Override
    public void setModel(String modelName) {
        ollamaSpine.setModel(modelName);
    }
    
    @Override
    public String getModel() {
        return ollamaSpine.getModel();
    }
    
    @Override
    public String[] getAvailableModels() {
        // Parse Ollama's model list
        String modelsJson = ollamaSpine.listModels();
        // Simple parsing - in production, use proper JSON parser
        if (modelsJson.contains("\"name\"")) {
            String[] parts = modelsJson.split("\"name\":");
            String[] modelNames = new String[Math.max(0, parts.length - 1)];
            for (int i = 1; i < parts.length; i++) {
                int start = parts[i].indexOf("\"") + 1;
                int end = parts[i].indexOf("\"", start);
                if (end > start) {
                    modelNames[i - 1] = parts[i].substring(start, end);
                }
            }
            return modelNames;
        }
        return new String[0];
    }
    
    @Override
    public boolean testConnection() {
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
