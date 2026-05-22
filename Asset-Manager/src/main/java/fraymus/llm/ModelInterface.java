package fraymus.llm;

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
    String generateResponse(String prompt);
    
    /**
     * Generate response with context
     */
    String generateResponse(String prompt, String context);
    
    /**
     * Set active model
     */
    void setModel(String modelName);
    
    /**
     * Get current model name
     */
    String getModel();
    
    /**
     * Get available models
     */
    String[] getAvailableModels();
    
    /**
     * Test connection to model
     */
    boolean testConnection();
    
    /**
     * Get model statistics
     */
    ModelStats getStats();
    
    /**
     * Model statistics container
     */
    class ModelStats {
        public final String modelName;
        public final int requestCount;
        public final long averageResponseTime;
        public final boolean isConnected;
        
        public ModelStats(String modelName, int requestCount, long averageResponseTime, boolean isConnected) {
            this.modelName = modelName;
            this.requestCount = requestCount;
            this.averageResponseTime = averageResponseTime;
            this.isConnected = isConnected;
        }
        
        @Override
        public String toString() {
            return String.format("Model: %s | Requests: %d | Avg Time: %dms | Connected: %s", 
                modelName, requestCount, averageResponseTime, isConnected ? "Yes" : "No");
        }
    }
}
