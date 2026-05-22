package fraymus.neural;

import fraymus.llm.ModelManager;
import fraymus.llm.HybridModelManager;
import fraymus.llm.OpenClawSpine;
import fraymus.llm.OpenClawNeoSpine;
import fraymus.llm.AeonPrimeSpine;
import fraymus.llm.ClaudeSpine;
import fraymus.compute.FrayCLContext;
import java.util.HashMap;
import java.util.Map;

/**
 * 🧬 BABEL MODEL ROUTER
 * 
 * Intelligent routing for code generation tasks to appropriate AI models
 * Demonstrates multi-model transmutation architecture
 */
public class BabelModelRouter {
    
    private ModelManager modelManager;
    private HybridModelManager hybridManager;
    private FrayCLContext frayCL;
    
    // Model instances
    private OpenClawSpine openClaw;
    private OpenClawNeoSpine openClawNeo;
    private AeonPrimeSpine aeonPrime;
    private ClaudeSpine claude;
    
    /**
     * Task types for code generation
     */
    public enum CodeTaskType {
        ARCHITECTURAL_DESIGN,    // System architecture, high-level design
        PERFORMANCE_OPTIMIZATION, // Performance tuning, optimization
        MODERN_PATTERNS,         // Modern frameworks, best practices
        CREATIVE_SYNTHESIS,      // Novel, innovative solutions
        DOMAIN_SPECIFIC,         // Fraynix-specific, internal APIs
        QUICK_PROTOTYPE,         // Simple tasks, rapid prototyping
        UNKNOWN                  // Default fallback
    }
    
    /**
     * Model selection for each task type
     */
    private static final Map<CodeTaskType, String> MODEL_ROUTING = new HashMap<>();
    
    static {
        MODEL_ROUTING.put(CodeTaskType.ARCHITECTURAL_DESIGN, "claude");
        MODEL_ROUTING.put(CodeTaskType.PERFORMANCE_OPTIMIZATION, "openclaw");
        MODEL_ROUTING.put(CodeTaskType.MODERN_PATTERNS, "openclawneo");
        MODEL_ROUTING.put(CodeTaskType.CREATIVE_SYNTHESIS, "gemma4");
        MODEL_ROUTING.put(CodeTaskType.DOMAIN_SPECIFIC, "aeon");
        MODEL_ROUTING.put(CodeTaskType.QUICK_PROTOTYPE, "llama3");
        MODEL_ROUTING.put(CodeTaskType.UNKNOWN, "llama3");
    }
    
    /**
     * Create Babel model router
     */
    public BabelModelRouter(ModelManager modelManager, HybridModelManager hybridManager) {
        this.modelManager = modelManager;
        this.hybridManager = hybridManager;
        
        // Initialize models
        try {
            // OpenClaw needs FrayCL context
            this.frayCL = new FrayCLContext();
            this.openClaw = new OpenClawSpine(frayCL);
        } catch (Exception e) {
            System.out.println("   ⚠ OpenClaw initialization skipped: " + e.getMessage());
        }
        
        // OpenClawNeo (always available)
        this.openClawNeo = new OpenClawNeoSpine();
        
        // AEON Prime (always available)
        this.aeonPrime = new AeonPrimeSpine();
        
        // Claude (only if API key available)
        String claudeApiKey = System.getenv("ANTHROPIC_API_KEY");
        if (claudeApiKey != null && !claudeApiKey.isEmpty()) {
            this.claude = new ClaudeSpine(claudeApiKey, "claude-3-sonnet-20240229");
        }
    }
    
    /**
     * Classify code generation task
     */
    public CodeTaskType classifyTask(String request, String language) {
        String lowerRequest = request.toLowerCase();
        
        // Architectural design
        if (containsAny(lowerRequest, "architecture", "design", "system", "structure")) {
            return CodeTaskType.ARCHITECTURAL_DESIGN;
        }
        
        // Performance optimization
        if (containsAny(lowerRequest, "optimize", "performance", "fast", "efficient", "speed")) {
            return CodeTaskType.PERFORMANCE_OPTIMIZATION;
        }
        
        // Modern patterns
        if (containsAny(lowerRequest, "modern", "latest", "framework", "best practice", "idiomatic")) {
            return CodeTaskType.MODERN_PATTERNS;
        }
        
        // Creative synthesis
        if (containsAny(lowerRequest, "innovative", "creative", "novel", "unique", "original")) {
            return CodeTaskType.CREATIVE_SYNTHESIS;
        }
        
        // Domain specific (Fraynix)
        if (containsAny(lowerRequest, "fraynix", "aeon", "cortex", "hypercortex", "genesis")) {
            return CodeTaskType.DOMAIN_SPECIFIC;
        }
        
        // Default: quick prototype
        return CodeTaskType.QUICK_PROTOTYPE;
    }
    
    /**
     * Select appropriate model for task
     */
    public String selectModel(CodeTaskType taskType) {
        String model = MODEL_ROUTING.get(taskType);
        
        // Fallback logic
        if (model.equals("claude") && !hasClaude()) {
            System.out.println("   ⚠ Claude not available, falling back to Gemma 4");
            return "gemma4";
        }
        
        if (model.equals("openclaw") && !hasOpenClaw()) {
            System.out.println("   ⚠ OpenClaw not available, falling back to llama3");
            return "llama3";
        }
        
        if (model.equals("openclawneo") && !hasOpenClawNeo()) {
            System.out.println("   ⚠ OpenClawNeo not available, falling back to Gemma 4");
            return "gemma4";
        }
        
        if (model.equals("aeon") && !hasAeon()) {
            System.out.println("   ⚠ AEON Prime not available as model, falling back to llama3");
            return "llama3";
        }
        
        return model;
    }
    
    /**
     * Generate code using appropriate model
     */
    public String generateCode(String request, String language) {
        return generateCode(request, language, false);
    }
    
    /**
     * Generate code using appropriate model with optional ensemble
     * 
     * @param request Code generation request
     * @param language Target programming language
     * @param useEnsemble If true, use multiple models and vote on best solution
     * @return Generated code
     */
    public String generateCode(String request, String language, boolean useEnsemble) {
        CodeTaskType taskType = classifyTask(request, language);
        String selectedModel = selectModel(taskType);
        
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧬 BABEL MODEL ROUTING                            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println("   Task: " + request);
        System.out.println("   Language: " + language);
        System.out.println("   Task Type: " + taskType);
        System.out.println("   Selected Model: " + selectedModel);
        System.out.println("   Ensemble Mode: " + (useEnsemble ? "ENABLED 🎯" : "DISABLED"));
        System.out.println();
        
        if (useEnsemble) {
            return generateEnsembleCode(request, language);
        } else {
            // Call the actual model
            String response;
            switch (selectedModel) {
                case "claude":
                    if (claude != null) {
                        response = claude.generateResponse(request, "Language: " + language);
                    } else {
                        response = generateDemoCode(request, language, "claude (unavailable)");
                    }
                    break;
                case "openclaw":
                    if (openClaw != null) {
                        response = openClaw.generateResponse(request, "Language: " + language);
                    } else {
                        response = generateDemoCode(request, language, "openclaw (unavailable)");
                    }
                    break;
                case "openclawneo":
                    if (openClawNeo != null) {
                        response = openClawNeo.generateResponse(request, "Language: " + language);
                    } else {
                        response = generateDemoCode(request, language, "openclawneo (unavailable)");
                    }
                    break;
                case "aeon":
                    if (aeonPrime != null) {
                        response = aeonPrime.generateResponse(request, "Language: " + language);
                    } else {
                        response = generateDemoCode(request, language, "aeon (unavailable)");
                    }
                    break;
                default:
                    response = generateDemoCode(request, language, selectedModel);
            }
            
            return response;
        }
    }
    
    /**
     * Generate code using ensemble of multiple models
     */
    private String generateEnsembleCode(String request, String language) {
        System.out.println(">>> [ENSEMBLE MODE] Generating code with multiple models...");
        
        java.util.List<String> responses = new java.util.ArrayList<>();
        java.util.List<String> modelNames = new java.util.ArrayList<>();
        
        // Collect responses from available models
        if (openClaw != null) {
            try {
                String response = openClaw.generateResponse(request, "Language: " + language);
                responses.add(response);
                modelNames.add("OpenClaw");
            } catch (Exception e) {
                System.out.println("   ⚠ OpenClaw failed: " + e.getMessage());
            }
        }
        
        if (openClawNeo != null) {
            try {
                String response = openClawNeo.generateResponse(request, "Language: " + language);
                responses.add(response);
                modelNames.add("OpenClawNeo");
            } catch (Exception e) {
                System.out.println("   ⚠ OpenClawNeo failed: " + e.getMessage());
            }
        }
        
        if (aeonPrime != null) {
            try {
                String response = aeonPrime.generateResponse(request, "Language: " + language);
                responses.add(response);
                modelNames.add("AEON Prime");
            } catch (Exception e) {
                System.out.println("   ⚠ AEON Prime failed: " + e.getMessage());
            }
        }
        
        if (claude != null) {
            try {
                String response = claude.generateResponse(request, "Language: " + language);
                responses.add(response);
                modelNames.add("Claude");
            } catch (Exception e) {
                System.out.println("   ⚠ Claude failed: " + e.getMessage());
            }
        }
        
        // Vote on best response
        String bestResponse = voteOnBestResponse(responses, modelNames);
        
        System.out.println(">>> [ENSEMBLE MODE] Selected best response from " + responses.size() + " models");
        return bestResponse;
    }
    
    /**
     * Vote on best response from ensemble
     */
    private String voteOnBestResponse(java.util.List<String> responses, java.util.List<String> modelNames) {
        if (responses.isEmpty()) {
            return generateDemoCode("ensemble failed", "unknown", "no models available");
        }
        
        // Simple voting: select the longest response (assumes more detail is better)
        int bestIndex = 0;
        int maxLength = 0;
        
        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).length() > maxLength) {
                maxLength = responses.get(i).length();
                bestIndex = i;
            }
        }
        
        String selectedModel = modelNames.get(bestIndex);
        String response = responses.get(bestIndex);
        
        // Add ensemble attribution
        return "// [ENSEMBLE SELECTED FROM: " + selectedModel + "]\n" + response;
    }
    
    /**
     * Generate demo code (for proof of concept)
     */
    private String generateDemoCode(String request, String language, String model) {
        StringBuilder code = new StringBuilder();
        
        code.append("// Generated by ").append(model).append("\n");
        code.append("// Task: ").append(request).append("\n");
        code.append("// Language: ").append(language).append("\n\n");
        
        switch (language.toLowerCase()) {
            case "python":
                code.append("def main():\n");
                code.append("    # TODO: Implement ").append(request).append("\n");
                code.append("    pass\n\n");
                code.append("if __name__ == \"__main__\":\n");
                code.append("    main()\n");
                break;
            case "java":
                code.append("public class Solution {\n");
                code.append("    public static void main(String[] args) {\n");
                code.append("        // TODO: Implement ").append(request).append("\n");
                code.append("    }\n");
                code.append("}\n");
                break;
            case "c":
                code.append("#include <stdio.h>\n\n");
                code.append("int main() {\n");
                code.append("    // TODO: Implement ").append(request).append("\n");
                code.append("    return 0;\n");
                code.append("}\n");
                break;
            case "go":
                code.append("package main\n\n");
                code.append("func main() {\n");
                code.append("    // TODO: Implement ").append(request).append("\n");
                code.append("}\n");
                break;
            default:
                code.append("// Code for ").append(language).append("\n");
                code.append("// TODO: Implement ").append(request).append("\n");
        }
        
        return code.toString();
    }
    
    /**
     * Check if Claude is available
     */
    private boolean hasClaude() {
        return claude != null;
    }
    
    /**
     * Check if OpenClaw is available
     */
    private boolean hasOpenClaw() {
        return openClaw != null;
    }
    
    /**
     * Check if OpenClawNeo is available
     */
    private boolean hasOpenClawNeo() {
        return openClawNeo != null;
    }
    
    /**
     * Check if AEON Prime is available as model
     */
    private boolean hasAeon() {
        return aeonPrime != null;
    }
    
    /**
     * Check if string contains any of the keywords
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Print routing table
     */
    public void printRoutingTable() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧬 BABEL MODEL ROUTING TABLE                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        for (Map.Entry<CodeTaskType, String> entry : MODEL_ROUTING.entrySet()) {
            String status = "✅";
            if (entry.getValue().equals("claude") && !hasClaude()) status = "⚠ ";
            if (entry.getValue().equals("openclaw") && !hasOpenClaw()) status = "⚠ ";
            if (entry.getValue().equals("openclawneo") && !hasOpenClawNeo()) status = "⚠ ";
            if (entry.getValue().equals("aeon") && !hasAeon()) status = "⚠ ";
            
            System.out.println("   " + status + " " + entry.getKey() + " → " + entry.getValue());
        }
        System.out.println();
    }
}
