package fraymus.llm;

import java.util.HashMap;
import java.util.Map;

/**
 * MODEL MANAGER
 * 
 * Central management for multiple LLM backends
 * Enables seamless model switching and comparison
 * 
 * This is the brain behind Fraynix's model transcendence
 */
public class ModelManager {
    
    private final Map<String, ModelInterface> models;
    private ModelInterface activeModel;
    
    public ModelManager(String defaultModelName) {
        this.models = new HashMap<>();
        initializeDefaultModel(defaultModelName);
    }
    
    /**
     * Initialize with default Ollama model
     */
    private void initializeDefaultModel(String modelName) {
        OllamaModelAdapter ollama = new OllamaModelAdapter(modelName);
        models.put("default", ollama);
        models.put("ollama", ollama);
        this.activeModel = ollama;
        System.out.println("🧠 [MODEL MANAGER] Initialized with default model: " + modelName);
    }
    
    /**
     * Register a new model
     */
    public void registerModel(String name, ModelInterface model) {
        models.put(name.toLowerCase(), model);
        System.out.println("📝 [MODEL MANAGER] Registered model: " + name);
    }
    
    /**
     * Switch to a registered model
     */
    public boolean switchModel(String modelName) {
        ModelInterface model = models.get(modelName.toLowerCase());
        if (model != null) {
            activeModel = model;
            System.out.println("🔄 [MODEL MANAGER] Switched to model: " + modelName);
            return true;
        }
        System.out.println("❌ [MODEL MANAGER] Model not found: " + modelName);
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
    public String[] getRegisteredModels() {
        return models.keySet().toArray(new String[0]);
    }
    
    /**
     * Generate response using active model
     */
    public String generate(String prompt) {
        return activeModel.generateResponse(prompt);
    }
    
    /**
     * Generate response with context using active model
     */
    public String generate(String prompt, String context) {
        return activeModel.generateResponse(prompt, context);
    }
    
    /**
     * Test all registered models
     */
    public void testAllModels() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   MODEL CONNECTION TEST                                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        
        for (String name : models.keySet()) {
            ModelInterface model = models.get(name);
            boolean connected = model.testConnection();
            System.out.println("  " + name + ": " + (connected ? "✅ CONNECTED" : "❌ DISCONNECTED"));
        }
    }
    
    /**
     * Print statistics for all models
     */
    public void printAllStats() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   MODEL STATISTICS                                        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        
        for (String name : models.keySet()) {
            ModelInterface model = models.get(name);
            ModelInterface.ModelStats stats = model.getStats();
            System.out.println("  [" + name + "] " + stats.toString());
        }
        
        System.out.println("\n  🎯 ACTIVE MODEL: " + activeModel.getModel());
    }
    
    /**
     * Initialize Gemma 4 if available
     */
    public boolean initializeGemma4() {
        try {
            Gemma4Model gemma4 = new Gemma4Model();
            if (gemma4.testConnection()) {
                registerModel("gemma4", gemma4);
                registerModel("gemma", gemma4);
                System.out.println("🚀 [MODEL MANAGER] Gemma 4 initialized and registered");
                return true;
            } else {
                System.out.println("⚠️ [MODEL MANAGER] Gemma 4 not available - ensure Ollama is running and Gemma 4 is installed");
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ [MODEL MANAGER] Failed to initialize Gemma 4: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get active model name
     */
    public String getActiveModelName() {
        for (Map.Entry<String, ModelInterface> entry : models.entrySet()) {
            if (entry.getValue() == activeModel) {
                return entry.getKey();
            }
        }
        return "unknown";
    }
    
    /**
     * Check if Gemma 4 is available
     */
    public boolean hasGemma4() {
        return models.containsKey("gemma4") || models.containsKey("gemma");
    }
    
    /**
     * Switch to Gemma 4 if available
     */
    public boolean switchToGemma4() {
        if (hasGemma4()) {
            return switchModel("gemma4");
        }
        System.out.println("⚠️ [MODEL MANAGER] Gemma 4 not initialized. Call initializeGemma4() first.");
        return false;
    }
}
