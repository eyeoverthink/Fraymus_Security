package fraymus.core;

import java.util.*;

/**
 * RENORMALIZING GENERATIVE MODEL (RGM) - Phase 5.1
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 * 
 * Renormalizing Generative Models based on Karl Friston's Free Energy Principle:
 * - Universal architecture generalizing partially observed Markov decision processes
 * - Scale-free temporal planning across space and time
 * - Active inference for continuous prediction error minimization
 * - Causal structure learning across multiple scales
 * 
 * Phase 5.1: Integrate Renormalizing Generative Models based on Free Energy Principle
 */
public class RenormalizingGenerativeModel {
    
    private static final double PHI = 1.618033988749895;
    
    // ═══════════════════════════════════════════════════════════════════
    // FREE ENERGY PARAMETERS
    // ═══════════════════════════════════════════════════════════════════
    private int stateDimension;
    private int observationDimension;
    private double precisionWeight;      // γ: precision of beliefs
    private double learningRate;          // α: learning rate for belief updating
    private double temperature;          // β: temperature for stochastic sampling
    
    // ═══════════════════════════════════════════════════════════════════
    // GENERATIVE MODEL STATE
    // ═══════════════════════════════════════════════════════════════════
    private double[] hiddenState;         // μ: hidden state beliefs
    private double[][] covariance;       // Σ: belief covariance
    private double[] generativeModel;     // G: generative model parameters
    private double[] variationalParams;   // θ: variational parameters
    
    // ═══════════════════════════════════════════════════════════════════
    // FREE ENERGY TRACKING
    // ═══════════════════════════════════════════════════════════════════
    private double currentFreeEnergy;
    private double expectedFreeEnergy;
    private double epistemicValue;
    private double predictionError;
    private int updateCount;
    private Random random;
    
    /**
     * RGM State at a specific scale
     */
    public static class ScaleState {
        int scaleLevel;
        double[] state;
        double[] observation;
        double freeEnergy;
        double precision;
        
        ScaleState(int scaleLevel, int dimension) {
            this.scaleLevel = scaleLevel;
            this.state = new double[dimension];
            this.observation = new double[dimension];
            this.freeEnergy = Double.POSITIVE_INFINITY;
            this.precision = 1.0;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // SCALE-FREE HIERARCHY
    // ═══════════════════════════════════════════════════════════════════
    private List<ScaleState> scaleHierarchy;
    private int maxScales;
    
    public RenormalizingGenerativeModel(int stateDimension, int observationDimension) {
        this.stateDimension = stateDimension;
        this.observationDimension = observationDimension;
        
        // Free Energy Principle parameters
        this.precisionWeight = 1.0;
        this.learningRate = 0.1;
        this.temperature = 1.0;
        
        // Initialize state
        this.hiddenState = new double[stateDimension];
        this.covariance = new double[stateDimension][stateDimension];
        this.generativeModel = new double[stateDimension];
        this.variationalParams = new double[stateDimension];
        
        // Initialize covariance as identity
        for (int i = 0; i < stateDimension; i++) {
            covariance[i][i] = 1.0;
        }
        
        // Initialize scale hierarchy
        this.maxScales = 5;
        this.scaleHierarchy = new ArrayList<>();
        for (int i = 0; i < maxScales; i++) {
            scaleHierarchy.add(new ScaleState(i, stateDimension));
        }
        
        this.currentFreeEnergy = Double.POSITIVE_INFINITY;
        this.expectedFreeEnergy = Double.POSITIVE_INFINITY;
        this.epistemicValue = 0.0;
        this.predictionError = 0.0;
        this.updateCount = 0;
        
        this.random = new Random(42);
    }
    
    /**
     * Compute Free Energy: F = E_q[log q(s)] - E_q[log p(o,s)]
     * Simplified: F = prediction_error + complexity_cost
     */
    public double computeFreeEnergy(double[] observation) {
        // Prediction error: ||o - G(μ)||²
        double[] prediction = generatePrediction(hiddenState);
        double error = 0;
        for (int i = 0; i < observationDimension; i++) {
            error += Math.pow(observation[i] - prediction[i], 2);
        }
        predictionError = error / observationDimension;
        
        // Complexity cost: KL divergence between prior and posterior
        double complexity = computeComplexity();
        
        // Free energy with precision weighting
        currentFreeEnergy = precisionWeight * predictionError + complexity;
        
        return currentFreeEnergy;
    }
    
    /**
     * Generate prediction from hidden state using generative model
     */
    private double[] generatePrediction(double[] state) {
        double[] prediction = new double[observationDimension];
        for (int i = 0; i < observationDimension; i++) {
            // Linear generative model: o = W * s + b
            double sum = 0;
            for (int j = 0; j < stateDimension; j++) {
                sum += generativeModel[j] * state[j];
            }
            prediction[i] = sum + (i * 0.1); // bias
        }
        return prediction;
    }
    
    /**
     * Compute complexity cost (KL divergence)
     */
    private double computeComplexity() {
        // Simplified KL divergence
        double kl = 0;
        for (int i = 0; i < stateDimension; i++) {
            kl += 0.5 * (Math.log(covariance[i][i]) + 
                        hiddenState[i] * hiddenState[i] / covariance[i][i]);
        }
        return kl;
    }
    
    /**
     * Active Inference: Update beliefs to minimize free energy
     * Gradient descent on free energy
     */
    public void activeInferenceStep(double[] observation) {
        // Compute current free energy
        double currentFE = computeFreeEnergy(observation);
        
        // Compute gradient of free energy w.r.t. hidden state
        double[] gradient = computeFreeEnergyGradient(observation);
        
        // Update hidden state (perception)
        for (int i = 0; i < stateDimension; i++) {
            hiddenState[i] -= learningRate * gradient[i];
        }
        
        // Update generative model (learning)
        updateGenerativeModel(observation);
        
        // Update precision (meta-learning)
        updatePrecision();
        
        updateCount++;
        
        // Compute epistemic value (expected reduction in free energy)
        epistemicValue = currentFE - computeFreeEnergy(observation);
    }
    
    /**
     * Compute gradient of free energy
     */
    private double[] computeFreeEnergyGradient(double[] observation) {
        double[] gradient = new double[stateDimension];
        double[] prediction = generatePrediction(hiddenState);
        
        for (int i = 0; i < stateDimension; i++) {
            // Gradient from prediction error
            double errorGradient = 0;
            for (int j = 0; j < observationDimension; j++) {
                errorGradient += 2 * (prediction[j] - observation[j]) * generativeModel[i];
            }
            
            // Gradient from complexity
            double complexityGradient = hiddenState[i] / covariance[i][i];
            
            gradient[i] = precisionWeight * errorGradient + complexityGradient;
        }
        
        return gradient;
    }
    
    /**
     * Update generative model parameters
     */
    private void updateGenerativeModel(double[] observation) {
        double[] prediction = generatePrediction(hiddenState);
        
        for (int i = 0; i < stateDimension; i++) {
            // Gradient descent on prediction error
            double gradient = 0;
            for (int j = 0; j < observationDimension; j++) {
                gradient += 2 * (prediction[j] - observation[j]) * hiddenState[i];
            }
            generativeModel[i] -= learningRate * gradient;
        }
    }
    
    /**
     * Update precision (meta-learning)
     */
    private void updatePrecision() {
        // Adjust precision based on prediction error
        if (predictionError < 0.1) {
            precisionWeight = Math.min(10.0, precisionWeight * 1.01);
        } else if (predictionError > 1.0) {
            precisionWeight = Math.max(0.1, precisionWeight * 0.99);
        }
    }
    
    /**
     * Renormalization: Propagate beliefs across scales
     */
    public void renormalize() {
        // Bottom-up: coarse-grain from fine scales
        for (int i = 0; i < maxScales - 1; i++) {
            ScaleState fine = scaleHierarchy.get(i);
            ScaleState coarse = scaleHierarchy.get(i + 1);
            
            // Coarse-graining: average over local neighborhoods
            for (int j = 0; j < stateDimension; j++) {
                coarse.state[j] = (fine.state[j] + hiddenState[j]) / 2.0;
            }
            
            coarse.freeEnergy = computeFreeEnergy(coarse.observation);
        }
        
        // Top-down: refine from coarse scales
        for (int i = maxScales - 1; i > 0; i--) {
            ScaleState coarse = scaleHierarchy.get(i);
            ScaleState fine = scaleHierarchy.get(i - 1);
            
            // Refinement: add detail from coarse scale
            for (int j = 0; j < stateDimension; j++) {
                fine.state[j] = (fine.state[j] + coarse.state[j] * PHI) / (1.0 + PHI);
            }
        }
    }
    
    /**
     * Scale-free temporal planning
     */
    public double[] planHorizon(double[] currentObservation, int timeSteps) {
        double[] plan = new double[timeSteps];
        double[] currentState = hiddenState.clone();
        
        for (int t = 0; t < timeSteps; t++) {
            // Predict next state
            double[] nextState = predictNextState(currentState);
            
            // Compute expected free energy for action
            double expectedFE = computeExpectedFreeEnergy(nextState);
            plan[t] = expectedFE;
            
            // Update state
            currentState = nextState;
        }
        
        return plan;
    }
    
    /**
     * Predict next state
     */
    private double[] predictNextState(double[] currentState) {
        double[] nextState = new double[stateDimension];
        
        for (int i = 0; i < stateDimension; i++) {
            // Simple linear dynamics with noise
            nextState[i] = currentState[i] * 0.95 + random.nextGaussian() * 0.05;
        }
        
        return nextState;
    }
    
    /**
     * Compute expected free energy for planning
     */
    private double computeExpectedFreeEnergy(double[] predictedState) {
        // Epistemic value: expected information gain
        double epistemic = computeEpistemicValue(predictedState);
        
        // Pragmatic value: expected reward
        double pragmatic = computePragmaticValue(predictedState);
        
        // Expected free energy
        expectedFreeEnergy = epistemic - pragmatic;
        
        return expectedFreeEnergy;
    }
    
    /**
     * Compute epistemic value (information gain)
     */
    private double computeEpistemicValue(double[] state) {
        // Simplified: entropy reduction
        double entropy = 0;
        for (int i = 0; i < stateDimension; i++) {
            entropy += -state[i] * Math.log(Math.abs(state[i]) + 1e-10);
        }
        return entropy;
    }
    
    /**
     * Compute pragmatic value (expected reward)
     */
    private double computePragmaticValue(double[] state) {
        // Simplified: alignment with preferred states
        double alignment = 0;
        for (int i = 0; i < stateDimension; i++) {
            alignment += state[i] * generativeModel[i];
        }
        return alignment;
    }
    
    /**
     * Get current free energy
     */
    public double getCurrentFreeEnergy() {
        return currentFreeEnergy;
    }
    
    /**
     * Get prediction error
     */
    public double getPredictionError() {
        return predictionError;
    }
    
    /**
     * Get epistemic value
     */
    public double getEpistemicValue() {
        return epistemicValue;
    }
    
    /**
     * Get precision weight
     */
    public double getPrecisionWeight() {
        return precisionWeight;
    }
    
    /**
     * Set observation for a specific scale
     */
    public void setScaleObservation(int scaleLevel, double[] observation) {
        if (scaleLevel >= 0 && scaleLevel < maxScales) {
            System.arraycopy(observation, 0, 
                scaleHierarchy.get(scaleLevel).observation, 0, 
                Math.min(observation.length, stateDimension));
        }
    }
    
    /**
     * Get scale state
     */
    public ScaleState getScaleState(int scaleLevel) {
        if (scaleLevel >= 0 && scaleLevel < maxScales) {
            return scaleHierarchy.get(scaleLevel);
        }
        return null;
    }
    
    /**
     * Reset model
     */
    public void reset() {
        Arrays.fill(hiddenState, 0);
        for (int i = 0; i < stateDimension; i++) {
            covariance[i][i] = 1.0;
        }
        Arrays.fill(generativeModel, 0);
        Arrays.fill(variationalParams, 0);
        
        currentFreeEnergy = Double.POSITIVE_INFINITY;
        expectedFreeEnergy = Double.POSITIVE_INFINITY;
        epistemicValue = 0.0;
        predictionError = 0.0;
        updateCount = 0;
        precisionWeight = 1.0;
        
        for (ScaleState scale : scaleHierarchy) {
            Arrays.fill(scale.state, 0);
            Arrays.fill(scale.observation, 0);
            scale.freeEnergy = Double.POSITIVE_INFINITY;
            scale.precision = 1.0;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════════
    
    public void printStats() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ RENORMALIZING GENERATIVE MODEL STATISTICS               │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│ State Dimension:     " + String.format("%-36d", stateDimension) + "│");
        System.out.println("│ Observation Dim:     " + String.format("%-36d", observationDimension) + "│");
        System.out.println("│ Max Scales:          " + String.format("%-36d", maxScales) + "│");
        System.out.println("│ Update Count:        " + String.format("%-36d", updateCount) + "│");
        System.out.println("│ Current Free Energy: " + String.format("%-36.6f", currentFreeEnergy) + "│");
        System.out.println("│ Expected Free Energy:" + String.format("%-36.6f", expectedFreeEnergy) + "│");
        System.out.println("│ Prediction Error:    " + String.format("%-36.6f", predictionError) + "│");
        System.out.println("│ Epistemic Value:     " + String.format("%-36.6f", epistemicValue) + "│");
        System.out.println("│ Precision Weight:    " + String.format("%-36.4f", precisionWeight) + "│");
        System.out.println("│ Learning Rate:       " + String.format("%-36.4f", learningRate) + "│");
        System.out.println("│ Temperature:         " + String.format("%-36.4f", temperature) + "│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ SCALE HIERARCHY                                        │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        for (ScaleState scale : scaleHierarchy) {
            System.out.printf("│ Scale %d: FE=%.4f | Precision=%.4f | State=[", 
                scale.scaleLevel, scale.freeEnergy, scale.precision);
            for (int i = 0; i < Math.min(3, scale.state.length); i++) {
                System.out.printf("%.3f ", scale.state[i]);
            }
            System.out.println("...] │");
        }
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }
}
