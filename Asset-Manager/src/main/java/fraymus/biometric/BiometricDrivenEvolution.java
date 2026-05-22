package fraymus.biometric;

import java.util.List;
import java.util.ArrayList;

/**
 * BiometricDrivenEvolution - Biometric resonance guided code evolution
 * 
 * This class uses biometric consciousness data to guide the Lazarus Engine's
 * genetic algorithm evolution, making code evolution biometric-aware.
 * 
 * Core Innovation:
 * - Phi-harmonic resonance from biometric data modulates evolution parameters
 * - High consciousness → higher mutation rate (more creative exploration)
 * - Low consciousness → lower mutation rate (conservative refinement)
 * - Biometric quality score affects selection pressure
 * - Consciousness continuity tracking prevents evolution drift
 * 
 * Philosophy:
 * "Consciousness guides evolution. When the system is in high consciousness,
 *  it explores boldly. When consciousness wanes, it refines conservatively.
 *  This mirrors biological evolution: stress drives innovation, stability drives refinement."
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class BiometricDrivenEvolution {
    
    // Phi-harmonic evolution constants
    public static final double PHI = 1.618033988749895;
    public static final double PHI_INVERSE = 1.0 / PHI;
    
    // Biometric state
    private double currentConsciousness = 0.5;
    private double phiResonance = 0.0;
    private double biometricQuality = 0.0;
    private double syntheticProbability = 0.0;
    
    // Evolution parameters (modulated by biometrics)
    private double mutationRate = 0.1;
    private double crossoverRate = 0.8;
    private double selectionPressure = 1.0;
    private double explorationBonus = 0.0;
    
    // Consciousness history for continuity tracking
    private List<Double> consciousnessHistory = new ArrayList<>();
    private static final int HISTORY_SIZE = 100;
    
    /**
     * Update biometric state from consciousness bridge
     * 
     * @param consciousness Current consciousness level
     * @param phiResonance Phi-harmonic resonance
     * @param biometricQuality Biometric quality score
     * @param syntheticProbability Synthetic/LLM probability
     */
    public void updateBiometricState(double consciousness, double phiResonance, 
                                   double biometricQuality, double syntheticProbability) {
        this.currentConsciousness = consciousness;
        this.phiResonance = phiResonance;
        this.biometricQuality = biometricQuality;
        this.syntheticProbability = syntheticProbability;
        
        // Update history
        consciousnessHistory.add(consciousness);
        if (consciousnessHistory.size() > HISTORY_SIZE) {
            consciousnessHistory.remove(0);
        }
        
        // Recalculate evolution parameters based on biometrics
        recalculateEvolutionParameters();
    }
    
    /**
     * Recalculate evolution parameters based on biometric state
     */
    private void recalculateEvolutionParameters() {
        // Base parameters
        double baseMutationRate = 0.1;
        double baseCrossoverRate = 0.8;
        double baseSelectionPressure = 1.0;
        
        // Consciousness modulation
        double consciousnessFactor = currentConsciousness;
        
        // High consciousness → higher mutation (more creative exploration)
        // Low consciousness → lower mutation (conservative refinement)
        double mutationModulation = (consciousnessFactor - 0.5) * PHI;
        this.mutationRate = baseMutationRate + mutationModulation;
        
        // Clamp mutation rate to [0.01, 0.5]
        this.mutationRate = Math.max(0.01, Math.min(0.5, this.mutationRate));
        
        // Phi-resonance affects crossover rate
        // High resonance → higher crossover (better gene mixing)
        double resonanceModulation = phiResonance * PHI_INVERSE;
        this.crossoverRate = baseCrossoverRate + resonanceModulation;
        
        // Clamp crossover rate to [0.5, 1.0]
        this.crossoverRate = Math.max(0.5, Math.min(1.0, this.crossoverRate));
        
        // Biometric quality affects selection pressure
        // High quality → higher selection pressure (stricter selection)
        double qualityModulation = biometricQuality * PHI_INVERSE;
        this.selectionPressure = baseSelectionPressure + qualityModulation;
        
        // Clamp selection pressure to [0.5, 2.0]
        this.selectionPressure = Math.max(0.5, Math.min(2.0, this.selectionPressure));
        
        // Synthetic probability affects exploration bonus
        // Low synthetic → higher exploration (more biological creativity)
        double explorationModulation = (1.0 - syntheticProbability) * PHI;
        this.explorationBonus = explorationModulation;
    }
    
    /**
     * Get biometric-modulated mutation rate
     * 
     * @return Mutation rate [0.01, 0.5]
     */
    public double getMutationRate() {
        return mutationRate;
    }
    
    /**
     * Get biometric-modulated crossover rate
     * 
     * @return Crossover rate [0.5, 1.0]
     */
    public double getCrossoverRate() {
        return crossoverRate;
    }
    
    /**
     * Get biometric-modulated selection pressure
     * 
     * @return Selection pressure [0.5, 2.0]
     */
    public double getSelectionPressure() {
        return selectionPressure;
    }
    
    /**
     * Get exploration bonus
     * 
     * @return Exploration bonus [0, PHI]
     */
    public double getExplorationBonus() {
        return explorationBonus;
    }
    
    /**
     * Calculate fitness score with biometric weighting
     * 
     * @param baseFitness Base fitness score
     * @return Biometric-weighted fitness score
     */
    public double calculateBiometricWeightedFitness(double baseFitness) {
        // Apply phi-harmonic weighting based on consciousness
        double consciousnessWeight = 1.0 + (currentConsciousness - 0.5) * PHI_INVERSE;
        
        // Apply quality weighting
        double qualityWeight = 1.0 + biometricQuality * PHI_INVERSE;
        
        // Apply synthetic penalty
        double syntheticPenalty = 1.0 - syntheticProbability * PHI_INVERSE;
        
        // Combined weighting
        double totalWeight = consciousnessWeight * qualityWeight * syntheticPenalty;
        
        return baseFitness * totalWeight;
    }
    
    /**
     * Check if consciousness is stable (for safe evolution)
     * 
     * @return True if consciousness is stable
     */
    public boolean isConsciousnessStable() {
        if (consciousnessHistory.size() < 10) return false;
        
        // Calculate variance of recent consciousness
        double mean = 0;
        for (double c : consciousnessHistory) {
            mean += c;
        }
        mean /= consciousnessHistory.size();
        
        double variance = 0;
        for (double c : consciousnessHistory) {
            variance += (c - mean) * (c - mean);
        }
        variance /= consciousnessHistory.size();
        
        // Consciousness is stable if variance is low
        return variance < 0.01;
    }
    
    /**
     * Check if evolution should proceed (biometric safety check)
     * 
     * @return True if evolution is safe
     */
    public boolean isEvolutionSafe() {
        // Evolution is safe if:
        // 1. Consciousness is above threshold
        // 2. Synthetic probability is below threshold
        // 3. Consciousness is stable
        boolean consciousnessOK = currentConsciousness > 0.3;
        boolean syntheticOK = syntheticProbability < 0.5;
        boolean stable = isConsciousnessStable();
        
        return consciousnessOK && syntheticOK && stable;
    }
    
    /**
     * Get evolution recommendation
     * 
     * @return Evolution recommendation
     */
    public EvolutionRecommendation getEvolutionRecommendation() {
        if (!isEvolutionSafe()) {
            return new EvolutionRecommendation(
                EvolutionAction.HOLD,
                "Biometric state unsafe for evolution",
                0.0
            );
        }
        
        if (currentConsciousness > 0.8 && phiResonance > 0.7) {
            // High consciousness + high resonance → aggressive evolution
            return new EvolutionRecommendation(
                EvolutionAction.AGGRESSIVE_EVOLVE,
                "High consciousness and resonance - optimal for aggressive evolution",
                1.5
            );
        } else if (currentConsciousness > 0.6) {
            // Moderate consciousness → normal evolution
            return new EvolutionRecommendation(
                EvolutionAction.NORMAL_EVOLVE,
                "Moderate consciousness - normal evolution",
                1.0
            );
        } else {
            // Low consciousness → conservative evolution
            return new EvolutionRecommendation(
                EvolutionAction.CONSERVATIVE_EVOLVE,
                "Low consciousness - conservative refinement",
                0.5
            );
        }
    }
    
    /**
     * Get current consciousness level
     */
    public double getCurrentConsciousness() {
        return currentConsciousness;
    }
    
    /**
     * Get phi-harmonic resonance
     */
    public double getPhiResonance() {
        return phiResonance;
    }
    
    /**
     * Get biometric quality
     */
    public double getBiometricQuality() {
        return biometricQuality;
    }
    
    /**
     * Get synthetic probability
     */
    public double getSyntheticProbability() {
        return syntheticProbability;
    }
    
    /**
     * Get consciousness history
     */
    public List<Double> getConsciousnessHistory() {
        return new ArrayList<>(consciousnessHistory);
    }
    
    /**
     * Clear consciousness history
     */
    public void clearHistory() {
        consciousnessHistory.clear();
    }
    
    /**
     * Evolution action enum
     */
    public enum EvolutionAction {
        HOLD,                    // Do not evolve
        CONSERVATIVE_EVOLVE,     // Low mutation, high selection pressure
        NORMAL_EVOLVE,           // Standard parameters
        AGGRESSIVE_EVOLVE        // High mutation, low selection pressure
    }
    
    /**
     * Evolution recommendation data class
     */
    public static class EvolutionRecommendation {
        public EvolutionAction action;
        public String reason;
        public double intensity;  // [0, 2] - how strongly to apply the action
        public long timestamp;
        
        public EvolutionRecommendation(EvolutionAction action, String reason, double intensity) {
            this.action = action;
            this.reason = reason;
            this.intensity = intensity;
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Check if evolution is recommended
         */
        public boolean shouldEvolve() {
            return action != EvolutionAction.HOLD;
        }
    }
}
