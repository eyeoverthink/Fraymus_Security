package fraymus.neural;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MAP-ELITES MUTATOR
 * 
 * Multi-dimensional Archive of Phenotypic Elites for quality-diversity algorithms.
 * Maintains a persistent, multidimensional archive of high-performing, structurally diverse code variants.
 * Ensures the system explores a wide evolutionary landscape of potential algorithms.
 * 
 * Architecture:
 * - Multi-dimensional performance space (behavior descriptors)
 * - Archive of elite variants per cell
 * - Quality-Diversity optimization
 * - Prevents collapse into narrow, localized optima
 * 
 * Mathematical Model:
 * Archive[desc1][desc2]...[descN] = best variant in that behavioral cell
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class MAPElitesMutator {
    
    // ==========================================
    // ARCHIVE STRUCTURE
    // ==========================================
    private final Map<String, EliteVariant> archive = new ConcurrentHashMap<>();
    
    // Behavioral descriptor dimensions
    private final int numDescriptors;
    private final int binsPerDescriptor;
    private final double[] descriptorMin;
    private final double[] descriptorMax;
    
    // Mutation parameters
    private double mutationRate = 0.1;
    private double mutationStrength = 0.2;
    
    // Statistics
    private long totalMutations = 0;
    private long successfulMutations = 0;
    private long archiveSize = 0;
    
    // ==========================================
    // ELITE VARIANT CLASS
    // ==========================================
    public static class EliteVariant {
        public final String code;
        public final double fitness;
        public final double[] descriptors;
        public final String hash;
        public final long timestamp;
        
        public EliteVariant(String code, double fitness, double[] descriptors) {
            this.code = code;
            this.fitness = fitness;
            this.descriptors = descriptors;
            this.hash = Integer.toHexString(code.hashCode());
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Check if this variant dominates another
         * (higher fitness in same behavioral cell)
         */
        public boolean dominates(EliteVariant other) {
            return this.fitness > other.fitness;
        }
    }
    
    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public MAPElitesMutator(int numDescriptors, int binsPerDescriptor) {
        this.numDescriptors = numDescriptors;
        this.binsPerDescriptor = binsPerDescriptor;
        this.descriptorMin = new double[numDescriptors];
        this.descriptorMax = new double[numDescriptors];
        
        // Initialize descriptor ranges
        for (int i = 0; i < numDescriptors; i++) {
            descriptorMin[i] = 0.0;
            descriptorMax[i] = 1.0;
        }
    }
    
    /**
     * Constructor with custom descriptor ranges
     */
    public MAPElitesMutator(int numDescriptors, int binsPerDescriptor, double[] min, double[] max) {
        this.numDescriptors = numDescriptors;
        this.binsPerDescriptor = binsPerDescriptor;
        this.descriptorMin = Arrays.copyOf(min, min.length);
        this.descriptorMax = Arrays.copyOf(max, max.length);
    }
    
    // ==========================================
    // BEHAVIORAL DESCRIPTOR CALCULATION
    // ==========================================
    
    /**
     * Calculate behavioral descriptor vector for code variant
     * Descriptors should capture behavioral characteristics, not just fitness
     * 
     * Example descriptors:
     * - Execution speed
     * - Memory usage
     * - Code complexity
     * - Causal coherence
     */
    public double[] calculateDescriptors(String code, double fitness) {
        double[] descriptors = new double[numDescriptors];
        
        // Descriptor 0: Code length (normalized)
        descriptors[0] = Math.min(1.0, code.length() / 10000.0);
        
        // Descriptor 1: Fitness (normalized to [0,1])
        descriptors[1] = Math.max(0.0, Math.min(1.0, fitness));
        
        // Descriptor 2: Code complexity (estimated by unique characters)
        long uniqueChars = code.chars().distinct().count();
        descriptors[2] = Math.min(1.0, uniqueChars / 100.0);
        
        // Additional descriptors would be computed from actual code analysis
        for (int i = 3; i < numDescriptors; i++) {
            descriptors[i] = 0.5;  // Default neutral value
        }
        
        return descriptors;
    }
    
    // ==========================================
    // ARCHIVE MANAGEMENT
    // ==========================================
    
    /**
     * Generate cell key from descriptor vector
     */
    private String generateCellKey(double[] descriptors) {
        StringBuilder key = new StringBuilder();
        
        for (int i = 0; i < numDescriptors; i++) {
            // Normalize descriptor to [0, binsPerDescriptor-1]
            double normalized = (descriptors[i] - descriptorMin[i]) / (descriptorMax[i] - descriptorMin[i]);
            normalized = Math.max(0.0, Math.min(1.0, normalized));
            int bin = (int) (normalized * binsPerDescriptor);
            bin = Math.max(0, Math.min(binsPerDescriptor - 1, bin));
            
            key.append(bin);
            if (i < numDescriptors - 1) {
                key.append("_");
            }
        }
        
        return key.toString();
    }
    
    /**
     * Add variant to archive if it's elite in its cell
     */
    public boolean addToArchive(String code, double fitness) {
        double[] descriptors = calculateDescriptors(code, fitness);
        String cellKey = generateCellKey(descriptors);
        
        EliteVariant newVariant = new EliteVariant(code, fitness, descriptors);
        
        EliteVariant existing = archive.get(cellKey);
        
        if (existing == null || newVariant.dominates(existing)) {
            archive.put(cellKey, newVariant);
            archiveSize = archive.size();
            successfulMutations++;
            return true;
        }
        
        return false;
    }
    
    /**
     * Get elite variant from specific cell
     */
    public EliteVariant getElite(double[] descriptors) {
        String cellKey = generateCellKey(descriptors);
        return archive.get(cellKey);
    }
    
    /**
     * Get random elite from archive
     */
    public EliteVariant getRandomElite() {
        if (archive.isEmpty()) {
            return null;
        }
        
        List<String> keys = new ArrayList<>(archive.keySet());
        Random random = new Random();
        String randomKey = keys.get(random.nextInt(keys.size()));
        
        return archive.get(randomKey);
    }
    
    /**
     * Get all elites
     */
    public Collection<EliteVariant> getAllElites() {
        return archive.values();
    }
    
    // ==========================================
    // MUTATION OPERATIONS
    // ==========================================
    
    /**
     * Mutate code string
     * Applies various mutation operations to generate new variants
     */
    public String mutate(String code) {
        totalMutations++;
        
        StringBuilder mutated = new StringBuilder(code);
        Random random = new Random();
        
        // Apply mutations based on mutation rate
        for (int i = 0; i < mutated.length(); i++) {
            if (random.nextDouble() < mutationRate) {
                char c = mutated.charAt(i);
                
                // Mutation types
                int mutationType = random.nextInt(4);
                
                switch (mutationType) {
                    case 0:  // Character substitution
                        mutated.setCharAt(i, substituteChar(c));
                        break;
                    case 1:  // Character insertion
                        if (i < mutated.length() - 1) {
                            mutated.insert(i, randomChar());
                            i++;  // Skip inserted character
                        }
                        break;
                    case 2:  // Character deletion
                        mutated.deleteCharAt(i);
                        i--;  // Recheck this position
                        break;
                    case 3:  // Character swap with neighbor
                        if (i < mutated.length() - 1) {
                            char temp = mutated.charAt(i);
                            mutated.setCharAt(i, mutated.charAt(i + 1));
                            mutated.setCharAt(i + 1, temp);
                        }
                        break;
                }
            }
        }
        
        return mutated.toString();
    }
    
    /**
     * Substitute character with similar one
     */
    private char substituteChar(char c) {
        Random random = new Random();
        
        if (Character.isLetter(c)) {
            // Swap case
            return Character.isLowerCase(c) ? Character.toUpperCase(c) : Character.toLowerCase(c);
        } else if (Character.isDigit(c)) {
            // Change digit
            return (char) ('0' + random.nextInt(10));
        } else if (c == ' ') {
            return (random.nextDouble() < 0.5) ? '_' : '-';
        }
        
        return c;
    }
    
    /**
     * Generate random character
     */
    private char randomChar() {
        Random random = new Random();
        int type = random.nextInt(3);
        
        switch (type) {
            case 0:  // Letter
                return (char) ('a' + random.nextInt(26));
            case 1:  // Digit
                return (char) ('0' + random.nextInt(10));
            default:  // Symbol
                char[] symbols = {'_', '-', '+', '*', '/'};
                return symbols[random.nextInt(symbols.length)];
        }
    }
    
    /**
     * Evolve code through MAP-Elites
     * Generate mutations, evaluate, and add elite variants to archive
     */
    public EliteVariant evolve(String initialCode, double initialFitness, int generations) {
        // Add initial variant
        addToArchive(initialCode, initialFitness);
        
        String currentCode = initialCode;
        double currentFitness = initialFitness;
        EliteVariant bestVariant = new EliteVariant(initialCode, initialFitness, calculateDescriptors(initialCode, initialFitness));
        
        for (int gen = 0; gen < generations; gen++) {
            // Generate mutation
            String mutatedCode = mutate(currentCode);
            
            // In practice, fitness would be evaluated by running the code
            // For now, use simulated fitness
            double mutatedFitness = evaluateFitness(mutatedCode, currentFitness);
            
            // Add to archive if elite
            addToArchive(mutatedCode, mutatedFitness);
            
            // Update best variant
            if (mutatedFitness > bestVariant.fitness) {
                bestVariant = new EliteVariant(mutatedCode, mutatedFitness, calculateDescriptors(mutatedCode, mutatedFitness));
            }
            
            // Accept mutation if improved
            if (mutatedFitness > currentFitness) {
                currentCode = mutatedCode;
                currentFitness = mutatedFitness;
            }
        }
        
        return bestVariant;
    }
    
    /**
     * Simulated fitness evaluation
     * In practice, this would execute the code and measure performance
     */
    private double evaluateFitness(String code, double parentFitness) {
        Random random = new Random();
        
        // Fitness is parent fitness plus random improvement/degradation
        // with mutation strength bias
        double change = (random.nextDouble() - 0.5) * mutationStrength * 2;
        
        return Math.max(0.0, Math.min(1.0, parentFitness + change));
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public void setMutationRate(double rate) {
        this.mutationRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    public void setMutationStrength(double strength) {
        this.mutationStrength = Math.max(0.0, strength);
    }
    
    public long getTotalMutations() {
        return totalMutations;
    }
    
    public long getSuccessfulMutations() {
        return successfulMutations;
    }
    
    public long getArchiveSize() {
        return archiveSize;
    }
    
    public double getSuccessRate() {
        return totalMutations > 0 ? (double) successfulMutations / totalMutations : 0.0;
    }
    
    /**
     * Get MAP-Elites status
     */
    public String getStatus() {
        return String.format(
            "MAP-Elites: archive=%d, mutations=%d, success=%.2f%%, rate=%.2f, strength=%.2f",
            archiveSize,
            totalMutations,
            getSuccessRate() * 100,
            mutationRate,
            mutationStrength
        );
    }
}
