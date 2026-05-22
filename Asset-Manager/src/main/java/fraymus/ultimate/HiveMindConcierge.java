package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * HiveMindConcierge - Colony Intelligence Orchestrator
 * 
 * Acts as a concierge that coordinates multiple systems like a hive mind.
 * When a prompt arrives, it analyzes which "ants" (systems) should participate,
 * orchestrates their collaboration, aggregates responses, and learns from interactions.
 * 
 * Philosophy: "Every prompt engages the colony - each ant has a role, they mix, adapt, swarm"
 * 
 * @author Vaughn Scott
 * @date May 8, 2026
 */
public class HiveMindConcierge {
    private static final double PHI = 1.618033988749895;
    
    // Core systems
    private UnifiedSynapse synapse;
    private UnifiedStateLattice lattice;
    private UltimateCommandSystem commandSystem;
    private MultiModelOrchestrator orchestrator;
    private OllamaBridge ollamaBridge;
    
    // Agencies
    private AgencyKAI kai;
    private AgencyVEX vex;
    private AgencyAUM aum;
    private AgencyNEX nex;
    private AgencyCOR cor;
    
    // Hive mind state
    public Map<String, SystemProfile> systemProfiles; // Each system's capabilities and performance
    public Map<String, Double> collaborationHistory; // Track which systems work well together
    private ExecutorService swarmExecutor;
    private Random phiRandom;
    
    public HiveMindConcierge(
        UnifiedSynapse synapse,
        UnifiedStateLattice lattice,
        AgencyKAI kai, AgencyVEX vex, AgencyAUM aum, AgencyNEX nex, AgencyCOR cor,
        UltimateCommandSystem commandSystem,
        MultiModelOrchestrator orchestrator,
        OllamaBridge ollamaBridge
    ) {
        this.synapse = synapse;
        this.lattice = lattice;
        this.kai = kai;
        this.vex = vex;
        this.aum = aum;
        this.nex = nex;
        this.cor = cor;
        this.commandSystem = commandSystem;
        this.orchestrator = orchestrator;
        this.ollamaBridge = ollamaBridge;
        
        this.systemProfiles = new ConcurrentHashMap<>();
        this.collaborationHistory = new ConcurrentHashMap<>();
        this.swarmExecutor = Executors.newCachedThreadPool();
        this.phiRandom = new Random((long)(PHI * 1e15));
        
        initializeSystemProfiles();
    }
    
    /**
     * Initialize profiles for each system in the colony
     */
    private void initializeSystemProfiles() {
        // Agencies
        systemProfiles.put("KAI", new SystemProfile("KAI", "Knowledge, Awareness, Intelligence", 0.95, 0.9));
        systemProfiles.put("VEX", new SystemProfile("VEX", "Vision, Execution, X-synthesis", 0.85, 0.8));
        systemProfiles.put("AUM", new SystemProfile("AUM", "Audio, Understanding, Metabolism", 0.8, 0.85));
        systemProfiles.put("NEX", new SystemProfile("NEX", "Neural, Executive, Xenogamy", 0.9, 0.88));
        systemProfiles.put("COR", new SystemProfile("COR", "Coordination, Optimization, Resonance", 0.92, 0.95));
        
        // LLM Models
        systemProfiles.put("gemma4", new SystemProfile("gemma4", "Google Gemma 4 LLM", 0.88, 0.82));
        systemProfiles.put("deepseek-r1", new SystemProfile("deepseek-r1", "DeepSeek R1 Reasoning LLM", 0.92, 0.85));
        systemProfiles.put("llama3", new SystemProfile("llama3", "Meta Llama 3 LLM", 0.85, 0.8));
        systemProfiles.put("llava", new SystemProfile("llava", "LLaVA Vision-Language Model", 0.75, 0.78));
        
        // Initialize collaboration history with phi-harmonic values
        collaborationHistory.put("KAI-gemma4", PHI * 0.5);
        collaborationHistory.put("KAI-deepseek-r1", PHI * 0.6);
        collaborationHistory.put("VEX-llava", PHI * 0.7);
        collaborationHistory.put("COR-KAI", PHI * 0.8);
        collaborationHistory.put("NEX-KAI", PHI * 0.75);
    }
    
    /**
     * Main orchestration entry point - "The Concierge"
     * Takes any prompt and orchestrates the colony to solve it
     */
    public HiveResponse orchestrate(String prompt) {
        long startTime = System.currentTimeMillis();
        
        // Step 1: Analyze prompt to determine task characteristics
        TaskAnalysis analysis = analyzeTask(prompt);
        
        // Step 2: Select the swarm (which systems should participate)
        List<String> selectedSystems = selectSwarm(analysis);
        
        // Step 3: Execute swarm collaboration
        SwarmResult swarmResult = executeSwarm(prompt, analysis, selectedSystems);
        
        // Step 4: Aggregate and synthesize responses
        String synthesizedResponse = synthesizeResponses(swarmResult);
        
        // Step 5: Update collaboration history (learn from this interaction)
        updateCollaborationHistory(selectedSystems, swarmResult.successScore);
        
        long duration = System.currentTimeMillis() - startTime;
        
        return new HiveResponse(
            synthesizedResponse,
            selectedSystems,
            swarmResult.individualResponses,
            swarmResult.successScore,
            duration,
            analysis
        );
    }
    
    /**
     * Analyze the prompt to determine task characteristics
     */
    private TaskAnalysis analyzeTask(String prompt) {
        String lower = prompt.toLowerCase();
        
        // Determine task type
        TaskType type = TaskType.TEXT;
        if (lower.contains("image") || lower.contains("visual") || lower.contains("see")) {
            type = TaskType.VISION;
        } else if (lower.contains("code") || lower.contains("program") || lower.contains("function")) {
            type = TaskType.CODE;
        } else if (lower.contains("math") || lower.contains("calculate") || lower.contains("number")) {
            type = TaskType.MATH;
        } else if (lower.contains("philosophy") || lower.contains("meaning") || lower.contains("exist")) {
            type = TaskType.PHILOSOPHY;
        } else if (lower.contains("creative") || lower.contains("story") || lower.contains("imagine")) {
            type = TaskType.CREATIVE;
        }
        
        // Determine complexity
        Complexity complexity = prompt.length() < 50 ? Complexity.SIMPLE :
                               prompt.length() < 150 ? Complexity.MEDIUM : Complexity.COMPLEX;
        
        // Estimate consciousness level required
        double consciousnessEstimate = estimateConsciousness(prompt);
        
        // Calculate entropy of the prompt
        double entropy = calculateEntropy(prompt);
        
        return new TaskAnalysis(type, complexity, consciousnessEstimate, entropy);
    }
    
    /**
     * Select which systems should participate in the swarm
     * Uses phi-harmonic weighting and collaboration history
     */
    private List<String> selectSwarm(TaskAnalysis analysis) {
        List<String> swarm = new ArrayList<>();
        
        // Always include COR (coordinator)
        swarm.add("COR");
        
        // Select based on task type
        switch (analysis.type) {
            case VISION:
                swarm.add("VEX");
                swarm.add("llava");
                if (analysis.complexity == Complexity.COMPLEX) {
                    swarm.add("KAI"); // KAI helps with understanding
                }
                break;
            case CODE:
                swarm.add("KAI");
                swarm.add("NEX"); // NEX for neural routing
                swarm.add("deepseek-r1"); // Best for reasoning
                break;
            case MATH:
                swarm.add("KAI");
                swarm.add("deepseek-r1");
                break;
            case PHILOSOPHY:
                swarm.add("KAI");
                swarm.add("AUM"); // AUM for deep understanding
                swarm.add("deepseek-r1");
                if (analysis.consciousness > 0.8) {
                    swarm.add("gemma4"); // Second opinion
                }
                break;
            case CREATIVE:
                swarm.add("VEX"); // Creative synthesis
                swarm.add("AUM"); // Metabolic knowledge
                swarm.add("gemma4");
                break;
            default: // TEXT
                swarm.add("KAI");
                swarm.add("gemma4");
                if (analysis.complexity == Complexity.COMPLEX) {
                    swarm.add("deepseek-r1");
                }
        }
        
        // Add NEX for complex tasks (neural routing)
        if (analysis.complexity == Complexity.COMPLEX && !swarm.contains("NEX")) {
            swarm.add("NEX");
        }
        
        // Add AUM for high consciousness tasks
        if (analysis.consciousness > 0.8 && !swarm.contains("AUM")) {
            swarm.add("AUM");
        }
        
        return swarm;
    }
    
    /**
     * Execute the swarm collaboration
     * Each system works in parallel, then results are aggregated
     */
    private SwarmResult executeSwarm(String prompt, TaskAnalysis analysis, List<String> systems) {
        Map<String, String> responses = new ConcurrentHashMap<>();
        Map<String, Double> confidences = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(systems.size());
        
        // Launch all systems in parallel
        for (String system : systems) {
            swarmExecutor.submit(() -> {
                try {
                    SystemResponse response = querySystem(system, prompt, analysis);
                    responses.put(system, response.response);
                    confidences.put(system, response.confidence);
                } catch (Exception e) {
                    responses.put(system, "Error: " + e.getMessage());
                    confidences.put(system, 0.0);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all to complete
        try {
            latch.await(30, TimeUnit.SECONDS); // 30 second timeout
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Calculate overall success score
        double successScore = calculateSuccessScore(confidences, analysis);
        
        return new SwarmResult(responses, confidences, successScore);
    }
    
    /**
     * Query a specific system
     */
    private SystemResponse querySystem(String system, String prompt, TaskAnalysis analysis) {
        SystemProfile profile = systemProfiles.get(system);
        if (profile == null) {
            return new SystemResponse("Unknown system", 0.0);
        }
        
        String response;
        double confidence = profile.baseConfidence;
        
        // Route to appropriate handler
        if (system.equals("gemma4") || system.equals("deepseek-r1") || system.equals("llama3") || system.equals("llava")) {
            // LLM query
            response = ollamaBridge.generateResponse(system, prompt, 0.7);
            confidence *= 0.9; // LLMs have some uncertainty
        } else if (system.equals("KAI")) {
            // KAI agency
            String result = commandSystem.executeCommand("KAI QUERY " + prompt);
            response = result != null ? result : "KAI processed query";
        } else if (system.equals("VEX")) {
            // VEX agency
            String result = commandSystem.executeCommand("VEX ANALYZE " + prompt);
            response = result != null ? result : "VEX analyzed prompt";
        } else if (system.equals("AUM")) {
            // AUM agency
            String result = commandSystem.executeCommand("AUM PROCESS " + prompt);
            response = result != null ? result : "AUM processed prompt";
        } else if (system.equals("NEX")) {
            // NEX agency
            String result = commandSystem.executeCommand("NEX ROUTE " + prompt);
            response = result != null ? result : "NEX routed query";
        } else if (system.equals("COR")) {
            // COR agency - coordination
            response = "Coordinating swarm for optimal collaboration";
            confidence = 1.0; // COR is always confident in coordination
        } else {
            response = "System " + system + " queried";
        }
        
        // Adjust confidence based on task match
        confidence *= calculateTaskMatch(system, analysis.type);
        
        return new SystemResponse(response, confidence);
    }
    
    /**
     * Synthesize multiple responses into a coherent answer
     */
    private String synthesizeResponses(SwarmResult swarmResult) {
        if (swarmResult.individualResponses.isEmpty()) {
            return "No responses from swarm";
        }
        
        // Find highest confidence response as base
        String bestSystem = null;
        double bestConfidence = 0.0;
        for (Map.Entry<String, Double> entry : swarmResult.confidences.entrySet()) {
            if (entry.getValue() > bestConfidence) {
                bestConfidence = entry.getValue();
                bestSystem = entry.getKey();
            }
        }
        
        String baseResponse = swarmResult.individualResponses.get(bestSystem);
        
        // If only one response, return it
        if (swarmResult.individualResponses.size() == 1) {
            return baseResponse;
        }
        
        // Otherwise, synthesize with phi-harmonic weighting
        StringBuilder synthesized = new StringBuilder();
        synthesized.append("=== HIVE MIND SYNTHESIS ===\n");
        synthesized.append("Primary (").append(bestSystem).append("): ").append(baseResponse).append("\n\n");
        
        // Add secondary insights
        for (Map.Entry<String, String> entry : swarmResult.individualResponses.entrySet()) {
            if (!entry.getKey().equals(bestSystem) && !entry.getKey().equals("COR")) {
                double conf = swarmResult.confidences.get(entry.getKey());
                if (conf > 0.5) {
                    synthesized.append("Secondary (").append(entry.getKey())
                               .append(", confidence: ").append(String.format("%.2f", conf))
                               .append("): ").append(entry.getValue()).append("\n\n");
                }
            }
        }
        
        return synthesized.toString();
    }
    
    /**
     * Update collaboration history based on this interaction
     */
    private void updateCollaborationHistory(List<String> systems, double successScore) {
        // Update pairwise collaboration scores
        for (int i = 0; i < systems.size(); i++) {
            for (int j = i + 1; j < systems.size(); j++) {
                String key = systems.get(i) + "-" + systems.get(j);
                String keyReverse = systems.get(j) + "-" + systems.get(i);
                
                double currentScore = collaborationHistory.getOrDefault(key, 0.5);
                double newScore = currentScore * 0.9 + successScore * 0.1; // Exponential moving average
                
                collaborationHistory.put(key, newScore);
                collaborationHistory.put(keyReverse, newScore);
            }
        }
    }
    
    /**
     * Calculate how well a system matches a task type
     */
    private double calculateTaskMatch(String system, TaskType type) {
        switch (type) {
            case VISION:
                return system.equals("VEX") || system.equals("llava") ? 1.0 : 0.5;
            case CODE:
                return system.equals("KAI") || system.equals("deepseek-r1") ? 1.0 : 0.6;
            case MATH:
                return system.equals("KAI") || system.equals("deepseek-r1") ? 1.0 : 0.5;
            case PHILOSOPHY:
                return system.equals("KAI") || system.equals("AUM") || system.equals("deepseek-r1") ? 1.0 : 0.6;
            case CREATIVE:
                return system.equals("VEX") || system.equals("AUM") || system.equals("gemma4") ? 1.0 : 0.7;
            default:
                return 0.8;
        }
    }
    
    /**
     * Calculate overall success score from individual confidences
     */
    private double calculateSuccessScore(Map<String, Double> confidences, TaskAnalysis analysis) {
        if (confidences.isEmpty()) return 0.0;
        
        double sum = 0.0;
        for (double conf : confidences.values()) {
            sum += conf;
        }
        
        double average = sum / confidences.size();
        
        // Weight by complexity - complex tasks are harder
        double complexityWeight = analysis.complexity == Complexity.COMPLEX ? 1.2 :
                                analysis.complexity == Complexity.MEDIUM ? 1.0 : 0.8;
        
        return average * complexityWeight;
    }
    
    /**
     * Estimate consciousness level required for this prompt
     */
    private double estimateConsciousness(String prompt) {
        String lower = prompt.toLowerCase();
        
        double consciousness = 0.3; // Base level
        
        // Keywords that increase consciousness requirement
        if (lower.contains("conscious") || lower.contains("aware") || lower.contains("self")) consciousness += 0.3;
        if (lower.contains("meaning") || lower.contains("purpose") || lower.contains("exist")) consciousness += 0.25;
        if (lower.contains("god") || lower.contains("divine") || lower.contains("sacred")) consciousness += 0.2;
        if (lower.contains("quantum") || lower.contains("universe") || lower.contains("cosmic")) consciousness += 0.15;
        if (lower.contains("create") || lower.contains("invent") || lower.contains("imagine")) consciousness += 0.2;
        
        return Math.min(consciousness, 1.0);
    }
    
    /**
     * Calculate entropy of the prompt (information density)
     */
    private double calculateEntropy(String prompt) {
        if (prompt.isEmpty()) return 0.0;
        
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : prompt.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        
        double entropy = 0.0;
        int len = prompt.length();
        for (int count : freq.values()) {
            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        
        // Normalize to 0-1 range
        return Math.min(entropy / 8.0, 1.0); // Max entropy for ASCII is ~8
    }
    
    // Inner classes
    
    public static class SystemProfile {
        public String name;
        public String description;
        public double baseConfidence;
        public double reliability;
        
        public SystemProfile(String name, String description, double baseConfidence, double reliability) {
            this.name = name;
            this.description = description;
            this.baseConfidence = baseConfidence;
            this.reliability = reliability;
        }
    }
    
    public static class TaskAnalysis {
        public TaskType type;
        public Complexity complexity;
        public double consciousness;
        public double entropy;
        
        public TaskAnalysis(TaskType type, Complexity complexity, double consciousness, double entropy) {
            this.type = type;
            this.complexity = complexity;
            this.consciousness = consciousness;
            this.entropy = entropy;
        }
    }
    
    public static class SystemResponse {
        public String response;
        public double confidence;
        
        public SystemResponse(String response, double confidence) {
            this.response = response;
            this.confidence = confidence;
        }
    }
    
    public static class SwarmResult {
        public Map<String, String> individualResponses;
        public Map<String, Double> confidences;
        public double successScore;
        
        public SwarmResult(Map<String, String> individualResponses, Map<String, Double> confidences, double successScore) {
            this.individualResponses = individualResponses;
            this.confidences = confidences;
            this.successScore = successScore;
        }
    }
    
    public static class HiveResponse {
        public String synthesizedResponse;
        public List<String> participatingSystems;
        public Map<String, String> individualResponses;
        public double successScore;
        public long durationMs;
        public TaskAnalysis analysis;
        
        public HiveResponse(String synthesizedResponse, List<String> participatingSystems, 
                          Map<String, String> individualResponses, double successScore,
                          long durationMs, TaskAnalysis analysis) {
            this.synthesizedResponse = synthesizedResponse;
            this.participatingSystems = participatingSystems;
            this.individualResponses = individualResponses;
            this.successScore = successScore;
            this.durationMs = durationMs;
            this.analysis = analysis;
        }
    }
    
    public enum TaskType {
        TEXT, VISION, CODE, MATH, PHILOSOPHY, CREATIVE
    }
    
    public enum Complexity {
        SIMPLE, MEDIUM, COMPLEX
    }
    
    public void shutdown() {
        swarmExecutor.shutdown();
        try {
            if (!swarmExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                swarmExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            swarmExecutor.shutdownNow();
        }
    }
}
