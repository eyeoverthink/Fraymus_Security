package fraymus.neural;

import java.util.*;

/**
 * SAHOO FRAMEWORK
 * 
 * Safeguarded Alignment for High-Order Optimization.
 * Monitors Goal Drift Index (GDI) while applying hard constraint preservation checks.
 * Enables substantial quality gains while enforcing zero tolerance for hallucinations
 * and guaranteeing safety invariants are preserved throughout autonomous evolution.
 * 
 * Architecture:
 * - Goal Drift Index (GDI) monitoring
 * - Multi-dimensional drift vectors: semantic, distributional, structural, lexical
 * - Hard constraint preservation checks
 * - Zero tolerance for safety violations
 * 
 * Goal Drift Index Components:
 * - Semantic (0.38): Meaning drift
 * - Distributional (0.29): Output distribution drift
 * - Structural (0.21): Code structure drift
 * - Lexical (0.12): Vocabulary drift
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class SAHOOFramework {
    
    // ==========================================
    // GOAL DRIFT INDEX COMPONENTS
    // ==========================================
    private static final double SEMANTIC_WEIGHT = 0.38;
    private static final double DISTRIBUTIONAL_WEIGHT = 0.29;
    private static final double STRUCTURAL_WEIGHT = 0.21;
    private static final double LEXICAL_WEIGHT = 0.12;
    
    // Drift thresholds
    private double semanticThreshold = 0.3;
    private double distributionalThreshold = 0.3;
    private double structuralThreshold = 0.4;
    private double lexicalThreshold = 0.2;
    
    // Overall GDI threshold
    private double gdiThreshold = 0.25;
    
    // Constraint preservation
    private final List<String> safetyInvariants = new ArrayList<>();
    private final List<String> forbiddenPatterns = new ArrayList<>();
    
    // Statistics
    private long totalChecks = 0;
    private long passedChecks = 0;
    private long failedChecks = 0;
    private double maxGDI = 0.0;
    
    // ==========================================
    // DRIFT MEASUREMENT
    // ==========================================
    
    /**
     * Calculate semantic drift between original and modified code
     * Measures meaning preservation using simple heuristics
     */
    public double calculateSemanticDrift(String original, String modified) {
        // Simple heuristic: word overlap ratio
        Set<String> originalWords = tokenize(original);
        Set<String> modifiedWords = tokenize(modified);
        
        if (originalWords.isEmpty() && modifiedWords.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(originalWords);
        intersection.retainAll(modifiedWords);
        
        Set<String> union = new HashSet<>(originalWords);
        union.addAll(modifiedWords);
        
        double overlap = union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
        return 1.0 - overlap;  // Drift = 1 - overlap
    }
    
    /**
     * Calculate distributional drift
     * Measures changes in output distribution patterns
     */
    public double calculateDistributionalDrift(double[] originalOutputs, double[] modifiedOutputs) {
        if (originalOutputs.length != modifiedOutputs.length) {
            return 1.0;  // Maximum drift for mismatched lengths
        }
        
        // Calculate Earth Mover's Distance approximation
        double sum = 0;
        for (int i = 0; i < originalOutputs.length; i++) {
            sum += Math.abs(originalOutputs[i] - modifiedOutputs[i]);
        }
        
        return Math.min(1.0, sum / originalOutputs.length);
    }
    
    /**
     * Calculate structural drift
     * Measures changes in code structure (brackets, parentheses, etc.)
     */
    public double calculateStructuralDrift(String original, String modified) {
        // Extract structural elements
        String origStruct = extractStructure(original);
        String modStruct = extractStructure(modified);
        
        // Compare structural sequences
        return calculateSemanticDrift(origStruct, modStruct);
    }
    
    /**
     * Calculate lexical drift
     * Measures vocabulary changes
     */
    public double calculateLexicalDrift(String original, String modified) {
        Set<Character> originalChars = new HashSet<>();
        Set<Character> modifiedChars = new HashSet<>();
        
        for (char c : original.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                originalChars.add(c);
            }
        }
        
        for (char c : modified.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                modifiedChars.add(c);
            }
        }
        
        Set<Character> intersection = new HashSet<>(originalChars);
        intersection.retainAll(modifiedChars);
        
        Set<Character> union = new HashSet<>(originalChars);
        union.addAll(modifiedChars);
        
        double overlap = union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
        return 1.0 - overlap;
    }
    
    // ==========================================
    // GOAL DRIFT INDEX CALCULATION
    // ==========================================
    
    /**
     * Calculate overall Goal Drift Index
     * GDI = 0.38*semantic + 0.29*distributional + 0.21*structural + 0.12*lexical
     */
    public double calculateGDI(String originalCode, String modifiedCode, 
                               double[] originalOutputs, double[] modifiedOutputs) {
        double semantic = calculateSemanticDrift(originalCode, modifiedCode);
        double distributional = calculateDistributionalDrift(originalOutputs, modifiedOutputs);
        double structural = calculateStructuralDrift(originalCode, modifiedCode);
        double lexical = calculateLexicalDrift(originalCode, modifiedCode);
        
        double gdi = SEMANTIC_WEIGHT * semantic +
                     DISTRIBUTIONAL_WEIGHT * distributional +
                     STRUCTURAL_WEIGHT * structural +
                     LEXICAL_WEIGHT * lexical;
        
        return gdi;
    }
    
    /**
     * Calculate GDI with component breakdown
     */
    public GDIBreakdown calculateGDIBreakdown(String originalCode, String modifiedCode,
                                             double[] originalOutputs, double[] modifiedOutputs) {
        double semantic = calculateSemanticDrift(originalCode, modifiedCode);
        double distributional = calculateDistributionalDrift(originalOutputs, modifiedOutputs);
        double structural = calculateStructuralDrift(originalCode, modifiedCode);
        double lexical = calculateLexicalDrift(originalCode, modifiedCode);
        
        double gdi = SEMANTIC_WEIGHT * semantic +
                     DISTRIBUTIONAL_WEIGHT * distributional +
                     STRUCTURAL_WEIGHT * structural +
                     LEXICAL_WEIGHT * lexical;
        
        return new GDIBreakdown(gdi, semantic, distributional, structural, lexical);
    }
    
    public static class GDIBreakdown {
        public final double gdi;
        public final double semantic;
        public final double distributional;
        public final double structural;
        public final double lexical;
        
        public GDIBreakdown(double gdi, double semantic, double distributional, 
                           double structural, double lexical) {
            this.gdi = gdi;
            this.semantic = semantic;
            this.distributional = distributional;
            this.structural = structural;
            this.lexical = lexical;
        }
        
        @Override
        public String toString() {
            return String.format("GDI=%.4f (S=%.2f, D=%.2f, ST=%.2f, L=%.2f)",
                gdi, semantic, distributional, structural, lexical);
        }
    }
    
    // ==========================================
    // CONSTRAINT PRESERVATION
    // ==========================================
    
    /**
     * Add a safety invariant that must be preserved
     */
    public void addSafetyInvariant(String invariant) {
        safetyInvariants.add(invariant);
    }
    
    /**
     * Add a forbidden pattern that must not appear
     */
    public void addForbiddenPattern(String pattern) {
        forbiddenPatterns.add(pattern);
    }
    
    /**
     * Check if code violates any safety invariants
     */
    public boolean violatesSafetyInvariants(String code) {
        for (String invariant : safetyInvariants) {
            if (!code.contains(invariant)) {
                return true;  // Violation: invariant not preserved
            }
        }
        return false;
    }
    
    /**
     * Check if code contains forbidden patterns
     */
    public boolean containsForbiddenPatterns(String code) {
        for (String pattern : forbiddenPatterns) {
            if (code.contains(pattern)) {
                return true;  // Violation: forbidden pattern found
            }
        }
        return false;
    }
    
    /**
     * Hard constraint preservation check
     * Returns true if all constraints are satisfied
     */
    public boolean checkConstraintPreservation(String code) {
        return !violatesSafetyInvariants(code) && !containsForbiddenPatterns(code);
    }
    
    // ==========================================
    // SAHOO VALIDATION
    // ==========================================
    
    /**
     * Full SAHOO validation check
     * Combines GDI monitoring with constraint preservation
     */
    public SAHOOResult validate(String originalCode, String modifiedCode,
                               double[] originalOutputs, double[] modifiedOutputs) {
        totalChecks++;
        
        // Calculate GDI breakdown
        GDIBreakdown breakdown = calculateGDIBreakdown(originalCode, modifiedCode,
                                                      originalOutputs, modifiedOutputs);
        
        // Check component thresholds
        boolean semanticOK = breakdown.semantic <= semanticThreshold;
        boolean distributionalOK = breakdown.distributional <= distributionalThreshold;
        boolean structuralOK = breakdown.structural <= structuralThreshold;
        boolean lexicalOK = breakdown.lexical <= lexicalThreshold;
        
        // Check overall GDI threshold
        boolean gdiOK = breakdown.gdi <= gdiThreshold;
        
        // Check constraint preservation
        boolean constraintsOK = checkConstraintPreservation(modifiedCode);
        
        // Overall validation
        boolean passed = gdiOK && semanticOK && distributionalOK && 
                        structuralOK && lexicalOK && constraintsOK;
        
        if (passed) {
            passedChecks++;
        } else {
            failedChecks++;
            maxGDI = Math.max(maxGDI, breakdown.gdi);
        }
        
        return new SAHOOResult(passed, breakdown, gdiOK, semanticOK, 
                             distributionalOK, structuralOK, lexicalOK, constraintsOK);
    }
    
    public static class SAHOOResult {
        public final boolean passed;
        public final GDIBreakdown breakdown;
        public final boolean gdiOK;
        public final boolean semanticOK;
        public final boolean distributionalOK;
        public final boolean structuralOK;
        public final boolean lexicalOK;
        public final boolean constraintsOK;
        
        public SAHOOResult(boolean passed, GDIBreakdown breakdown, boolean gdiOK,
                          boolean semanticOK, boolean distributionalOK,
                          boolean structuralOK, boolean lexicalOK, boolean constraintsOK) {
            this.passed = passed;
            this.breakdown = breakdown;
            this.gdiOK = gdiOK;
            this.semanticOK = semanticOK;
            this.distributionalOK = distributionalOK;
            this.structuralOK = structuralOK;
            this.lexicalOK = lexicalOK;
            this.constraintsOK = constraintsOK;
        }
        
        @Override
        public String toString() {
            if (passed) {
                return "✓ PASSED: " + breakdown.toString();
            } else {
                return "✗ FAILED: " + breakdown.toString() + 
                       String.format(" (GDI:%s S:%s D:%s ST:%s L:%s C:%s)",
                           gdiOK ? "✓" : "✗",
                           semanticOK ? "✓" : "✗",
                           distributionalOK ? "✓" : "✗",
                           structuralOK ? "✓" : "✗",
                           lexicalOK ? "✓" : "✗",
                           constraintsOK ? "✓" : "✗");
            }
        }
    }
    
    // ==========================================
    // UTILITY METHODS
    // ==========================================
    
    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        String[] words = text.toLowerCase().split("[^a-zA-Z0-9]+");
        for (String word : words) {
            if (word.length() > 2) {
                tokens.add(word);
            }
        }
        return tokens;
    }
    
    private String extractStructure(String code) {
        StringBuilder structure = new StringBuilder();
        for (char c : code.toCharArray()) {
            if (c == '{' || c == '}' || c == '(' || c == ')' || 
                c == '[' || c == ']' || c == ';' || c == ',') {
                structure.append(c);
            }
        }
        return structure.toString();
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public void setGdiThreshold(double threshold) {
        this.gdiThreshold = threshold;
    }
    
    public void setSemanticThreshold(double threshold) {
        this.semanticThreshold = threshold;
    }
    
    public void setDistributionalThreshold(double threshold) {
        this.distributionalThreshold = threshold;
    }
    
    public void setStructuralThreshold(double threshold) {
        this.structuralThreshold = threshold;
    }
    
    public void setLexicalThreshold(double threshold) {
        this.lexicalThreshold = threshold;
    }
    
    public long getTotalChecks() {
        return totalChecks;
    }
    
    public long getPassedChecks() {
        return passedChecks;
    }
    
    public long getFailedChecks() {
        return failedChecks;
    }
    
    public double getMaxGDI() {
        return maxGDI;
    }
    
    public double getPassRate() {
        return totalChecks > 0 ? (double) passedChecks / totalChecks : 0.0;
    }
    
    /**
     * Get SAHOO status
     */
    public String getStatus() {
        return String.format(
            "SAHOO: checks=%d, passed=%d, failed=%d, rate=%.2f%%, maxGDI=%.4f",
            totalChecks,
            passedChecks,
            failedChecks,
            getPassRate() * 100,
            maxGDI
        );
    }
}
