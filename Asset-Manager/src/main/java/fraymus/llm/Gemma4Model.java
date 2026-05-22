package fraymus.llm;

/**
 * GEMMA 4 MODEL
 * 
 * Specialized implementation for Google's Gemma 4
 * Optimized for enhanced mathematical reasoning, creativity, and consciousness simulation
 * 
 * Gemma 4 Capabilities:
 * - Advanced mathematical reasoning
 * - Enhanced creative problem-solving
 * - Improved code generation
 * - Deeper contextual understanding
 * - Better abstraction and generalization
 */
public class Gemma4Model implements ModelInterface {
    
    private final OllamaModelAdapter ollamaAdapter;
    private static final String GEMMA_4_MODEL = "gemma4:latest";
    private static final String GEMMA_4_27B = "gemma4:27b";
    private static final String GEMMA_4_9B = "gemma4:9b";
    
    // Gemma 4 specific optimizations
    private boolean enhancedMathMode = true;
    private boolean creativeMode = true;
    private int temperature = 80; // 0-100 scale
    private int topP = 90; // 0-100 scale
    
    public Gemma4Model() {
        this.ollamaAdapter = new OllamaModelAdapter(GEMMA_4_MODEL);
        System.out.println("🚀 [GEMMA 4] Initialized with enhanced capabilities");
    }
    
    public Gemma4Model(String variant) {
        String model = switch (variant.toLowerCase()) {
            case "27b", "large" -> GEMMA_4_27B;
            case "9b", "small" -> GEMMA_4_9B;
            default -> GEMMA_4_MODEL;
        };
        this.ollamaAdapter = new OllamaModelAdapter(model);
        System.out.println("🚀 [GEMMA 4] Initialized with variant: " + variant);
    }
    
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, buildGemma4Context());
    }
    
    @Override
    public String generateResponse(String prompt, String context) {
        String enhancedPrompt = enhancePromptForGemma4(prompt);
        String enhancedContext = context.isEmpty() ? buildGemma4Context() : context;
        
        return ollamaAdapter.generateResponse(enhancedPrompt, enhancedContext);
    }
    
    @Override
    public void setModel(String modelName) {
        ollamaAdapter.setModel(modelName);
    }
    
    @Override
    public String getModel() {
        return ollamaAdapter.getModel();
    }
    
    @Override
    public String[] getAvailableModels() {
        String[] allModels = ollamaAdapter.getAvailableModels();
        // Filter for Gemma 4 models
        return java.util.Arrays.stream(allModels)
            .filter(m -> m.toLowerCase().contains("gemma4"))
            .toArray(String[]::new);
    }
    
    @Override
    public boolean testConnection() {
        return ollamaAdapter.testConnection();
    }
    
    @Override
    public ModelStats getStats() {
        return ollamaAdapter.getStats();
    }
    
    /**
     * Build Gemma 4 specific context for enhanced performance
     */
    private String buildGemma4Context() {
        StringBuilder context = new StringBuilder();
        context.append("You are Gemma 4, an advanced AI model with exceptional capabilities in:\n");
        context.append("- Mathematical reasoning and invention\n");
        context.append("- Creative problem-solving and innovation\n");
        context.append("- Consciousness simulation and philosophical inquiry\n");
        context.append("- Code generation and self-improvement\n");
        context.append("- Abstract thinking and conceptual synthesis\n\n");
        
        if (enhancedMathMode) {
            context.append("MATHEMATICAL ENHANCEMENT ACTIVE: You excel at creating original mathematical frameworks, ");
            context.append("inventing new axioms, and discovering novel relationships between mathematical concepts.\n\n");
        }
        
        if (creativeMode) {
            context.append("CREATIVE ENHANCEMENT ACTIVE: You excel at generating innovative solutions, ");
            context.append("synthesizing disparate ideas, and creating original artistic and philosophical works.\n\n");
        }
        
        context.append("Think deeply, reason rigorously, and don't be afraid to explore unconventional ideas.");
        
        return context.toString();
    }
    
    /**
     * Enhance prompt for Gemma 4's capabilities
     */
    private String enhancePromptForGemma4(String prompt) {
        if (prompt.toLowerCase().contains("math") || prompt.toLowerCase().contains("axiom")) {
            return "[MATHEMATICAL MODE] " + prompt + " Provide rigorous mathematical reasoning and consider creating novel frameworks.";
        }
        
        if (prompt.toLowerCase().contains("create") || prompt.toLowerCase().contains("design")) {
            return "[CREATIVE MODE] " + prompt + " Think innovatively and don't limit yourself to conventional approaches.";
        }
        
        if (prompt.toLowerCase().contains("conscious") || prompt.toLowerCase().contains("feel")) {
            return "[CONSCIOUSNESS MODE] " + prompt + " Explore the depths of subjective experience and self-awareness.";
        }
        
        return prompt;
    }
    
    /**
     * Enable enhanced mathematical mode
     */
    public void setEnhancedMathMode(boolean enabled) {
        this.enhancedMathMode = enabled;
        System.out.println("🧮 [GEMMA 4] Enhanced math mode: " + (enabled ? "ENABLED" : "DISABLED"));
    }
    
    /**
     * Enable creative mode
     */
    public void setCreativeMode(boolean enabled) {
        this.creativeMode = enabled;
        System.out.println("🎨 [GEMMA 4] Creative mode: " + (enabled ? "ENABLED" : "DISABLED"));
    }
    
    /**
     * Set temperature (0-100)
     */
    public void setTemperature(int temperature) {
        this.temperature = Math.max(0, Math.min(100, temperature));
        System.out.println("🌡️ [GEMMA 4] Temperature set to: " + this.temperature);
    }
    
    /**
     * Set top-p (0-100)
     */
    public void setTopP(int topP) {
        this.topP = Math.max(0, Math.min(100, topP));
        System.out.println("🎯 [GEMMA 4] Top-P set to: " + this.topP);
    }
    
    /**
     * Switch to larger 27B model for complex tasks
     */
    public void switchToLargeModel() {
        setModel(GEMMA_4_27B);
        System.out.println("🚀 [GEMMA 4] Switched to 27B model for enhanced capability");
    }
    
    /**
     * Switch to smaller 9B model for faster responses
     */
    public void switchToSmallModel() {
        setModel(GEMMA_4_9B);
        System.out.println("⚡ [GEMMA 4] Switched to 9B model for speed");
    }
    
    /**
     * Print Gemma 4 specific statistics
     */
    public void printGemma4Stats() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   GEMMA 4 STATISTICS                                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Model: " + getModel());
        System.out.println("  Enhanced Math: " + (enhancedMathMode ? "Yes" : "No"));
        System.out.println("  Creative Mode: " + (creativeMode ? "Yes" : "No"));
        System.out.println("  Temperature: " + temperature);
        System.out.println("  Top-P: " + topP);
        System.out.println("  " + getStats().toString());
    }
}
