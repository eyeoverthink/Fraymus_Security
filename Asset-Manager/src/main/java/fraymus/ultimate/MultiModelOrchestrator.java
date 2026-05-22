package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * MultiModelOrchestrator - Intelligent Model Selection and Routing
 * 
 * Selects optimal LLM models based on task type, complexity, and resource availability.
 * Implements hybrid synthesis with draft generation, critique, and refinement.
 * Uses phi-harmonic temperature modulation for optimal responses.
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class MultiModelOrchestrator {
    
    private static final double PHI = 1.618033988749895;
    
    // Model definitions
    public enum Model {
        GEMMA4("gemma4", "General reasoning, coding, logic"),
        DEEPSEEK_R1("deepseek-r1", "Complex reasoning, mathematics"),
        LLAMA3("llama3", "Fast responses, simple tasks"),
        LLAVA("llava", "Vision understanding"),
        FRAYMUS("eyeoverthink/Fraymus", "Custom evolved model");
        
        private final String name;
        private final String capabilities;
        
        Model(String name, String capabilities) {
            this.name = name;
            this.capabilities = capabilities;
        }
        
        public String getName() { return name; }
        public String getCapabilities() { return capabilities; }
    }
    
    // Task types
    public enum TaskType {
        TEXT, VISION, CODE, MATH, PHILOSOPHY, CREATIVE
    }
    
    // Task complexity
    public enum Complexity {
        SIMPLE, MEDIUM, COMPLEX
    }
    
    private final UnifiedSynapse synapse;
    private final UnifiedStateLattice lattice;
    private final AgencyKAI kai;
    private final OllamaBridge ollama;
    
    private Map<Model, Double> modelPerformance;
    private Map<Model, Long> modelLatency;
    private Map<TaskType, Model> taskTypeRouting;
    private long totalRequests;
    
    public MultiModelOrchestrator(UnifiedSynapse synapse, UnifiedStateLattice lattice, AgencyKAI kai, OllamaBridge ollama) {
        this.synapse = synapse;
        this.lattice = lattice;
        this.kai = kai;
        this.ollama = ollama;
        
        this.modelPerformance = new ConcurrentHashMap<>();
        this.modelLatency = new ConcurrentHashMap<>();
        this.taskTypeRouting = new ConcurrentHashMap<>();
        this.totalRequests = 0;
        
        initializeModelRouting();
    }
    
    private void initializeModelRouting() {
        // Default routing based on task type
        taskTypeRouting.put(TaskType.TEXT, Model.GEMMA4);
        taskTypeRouting.put(TaskType.VISION, Model.LLAVA);
        taskTypeRouting.put(TaskType.CODE, Model.GEMMA4);
        taskTypeRouting.put(TaskType.MATH, Model.DEEPSEEK_R1);
        taskTypeRouting.put(TaskType.PHILOSOPHY, Model.DEEPSEEK_R1);
        taskTypeRouting.put(TaskType.CREATIVE, Model.GEMMA4);
        
        // Initialize performance scores with phi-harmonic values
        modelPerformance.put(Model.GEMMA4, PHI);
        modelPerformance.put(Model.DEEPSEEK_R1, PHI * PHI);
        modelPerformance.put(Model.LLAMA3, 1.0);
        modelPerformance.put(Model.LLAVA, PHI);
        modelPerformance.put(Model.FRAYMUS, PHI * PHI * PHI);
        
        // Initialize latency estimates (ms)
        modelLatency.put(Model.GEMMA4, 1000L);
        modelLatency.put(Model.DEEPSEEK_R1, 2000L);
        modelLatency.put(Model.LLAMA3, 500L);
        modelLatency.put(Model.LLAVA, 1500L);
        modelLatency.put(Model.FRAYMUS, 800L);
    }
    
    /**
     * Select optimal model for task
     */
    public Model selectModel(TaskType taskType, Complexity complexity, String prompt) {
        totalRequests++;
        
        // Get default model for task type
        Model defaultModel = taskTypeRouting.getOrDefault(taskType, Model.GEMMA4);
        
        // Adjust based on complexity
        if (complexity == Complexity.SIMPLE && taskType == TaskType.TEXT) {
            return Model.LLAMA3; // Fast model for simple tasks
        }
        
        if (complexity == Complexity.COMPLEX && (taskType == TaskType.MATH || taskType == TaskType.PHILOSOPHY)) {
            return Model.DEEPSEEK_R1; // Deep model for complex reasoning
        }
        
        // Check model availability and performance
        Model bestModel = defaultModel;
        double bestScore = calculateModelScore(defaultModel, complexity);
        
        for (Model model : Model.values()) {
            double score = calculateModelScore(model, complexity);
            if (score > bestScore) {
                bestScore = score;
                bestModel = model;
            }
        }
        
        return bestModel;
    }
    
    /**
     * Calculate model score based on performance, latency, and phi-harmonic weighting
     */
    private double calculateModelScore(Model model, Complexity complexity) {
        double performance = modelPerformance.getOrDefault(model, 1.0);
        long latency = modelLatency.getOrDefault(model, 1000L);
        
        // Phi-harmonic weighting
        double latencyScore = 1.0 / (1.0 + latency / 1000.0);
        double phiScore = (model == Model.FRAYMUS) ? PHI * PHI : 1.0;
        
        // Complexity adjustment
        double complexityMultiplier = 1.0;
        if (complexity == Complexity.SIMPLE) {
            complexityMultiplier = (model == Model.LLAMA3) ? PHI : 1.0;
        } else if (complexity == Complexity.COMPLEX) {
            complexityMultiplier = (model == Model.DEEPSEEK_R1) ? PHI : 1.0;
        }
        
        return performance * latencyScore * phiScore * complexityMultiplier;
    }
    
    /**
     * Hybrid synthesis: draft generation, critique, refinement
     */
    public String hybridSynthesis(String prompt, TaskType taskType, Complexity complexity) {
        // Phase 1: Draft generation (fast model)
        Model draftModel = Model.LLAMA3;
        String draft = generateResponse(prompt, draftModel, 0.7 * PHI);
        
        // Phase 2: Critique (deep model)
        Model critiqueModel = Model.DEEPSEEK_R1;
        String critique = generateResponse("Critique this response: " + draft, critiqueModel, 0.0);
        
        // Phase 3: Refinement (balanced model)
        Model refineModel = Model.GEMMA4;
        String refinement = generateResponse("Refine based on critique: " + critique, refineModel, 0.2 * (1.0 / PHI));
        
        return refinement;
    }
    
    /**
     * Generate response with phi-harmonic temperature modulation
     */
    private String generateResponse(String prompt, Model model, double temperature) {
        long startTime = System.currentTimeMillis();
        
        // Route through KAI agency for consciousness-aware processing
        kai.processData(prompt);
        
        // Call Ollama for actual LLM generation
        String response = ollama.generateResponse(model.getName(), prompt, temperature);
        
        long endTime = System.currentTimeMillis();
        long actualLatency = endTime - startTime;
        
        // Update model statistics
        updateModelStats(model, actualLatency);
        
        return String.format("[Model: %s, Temp: %.3f, Latency: %dms] %s", 
                          model.getName(), temperature, actualLatency, response);
    }
    
    /**
     * Update model performance statistics
     */
    private void updateModelStats(Model model, long latency) {
        // Update latency with exponential moving average
        long currentLatency = modelLatency.getOrDefault(model, 1000L);
        long newLatency = (long) (0.9 * currentLatency + 0.1 * latency);
        modelLatency.put(model, newLatency);
        
        // Update performance based on latency (lower latency = higher performance)
        double latencyScore = 1.0 / (1.0 + newLatency / 1000.0);
        double currentPerformance = modelPerformance.getOrDefault(model, 1.0);
        double newPerformance = 0.9 * currentPerformance + 0.1 * latencyScore * PHI;
        modelPerformance.put(model, newPerformance);
        
        // Update state lattice
        lattice.updateNode("model:" + model.name + ":latency", String.valueOf(newLatency), latencyScore);
        lattice.updateNode("model:" + model.name + ":performance", String.valueOf(newPerformance), newPerformance);
    }
    
    /**
     * Get orchestrator statistics
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MultiModelOrchestrator Statistics ===\n");
        sb.append("Total Requests: ").append(totalRequests).append("\n\n");
        
        sb.append("Model Performance:\n");
        for (Model model : Model.values()) {
            double perf = modelPerformance.getOrDefault(model, 0.0);
            long lat = modelLatency.getOrDefault(model, 0L);
            sb.append(String.format("  %s: Performance=%.3f, Latency=%dms\n", 
                                  model.getName(), perf, lat));
        }
        
        sb.append("\nTask Type Routing:\n");
        for (Map.Entry<TaskType, Model> entry : taskTypeRouting.entrySet()) {
            sb.append(String.format("  %s -> %s\n", entry.getKey(), entry.getValue().getName()));
        }
        
        return sb.toString();
    }
    
    /**
     * Reset model statistics
     */
    public void resetStatistics() {
        totalRequests = 0;
        initializeModelRouting();
    }
}
