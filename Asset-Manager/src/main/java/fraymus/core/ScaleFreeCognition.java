package fraymus.core;

import java.util.*;

/**
 * SCALE-FREE COGNITION - Phase 5.2
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 * 
 * Scale-Free Cognition based on Michael Levin's developmental biology framework:
 * - Fractal nested consciousness across multiple scales
 * - Markov Blanket synchronization for hierarchical integration
 * - Planetary intelligence emergence from distributed agents
 * - Self-similar cognitive structures across scales
 * 
 * Phase 5.2: Implement Scale-Free Cognition with Markov Blanket synchronization
 */
public class ScaleFreeCognition {
    
    private static final double PHI = 1.618033988749895;
    
    // ═══════════════════════════════════════════════════════════════════
    // SCALE-FREE PARAMETERS
    // ═══════════════════════════════════════════════════════════════════
    private int maxScales;
    private int stateDimension;
    private double couplingStrength;      // Coupling between scales
    private double synchronizationRate;  // Markov blanket sync rate
    private double fractalDimension;     // Fractal dimension of cognition
    
    // ═══════════════════════════════════════════════════════════════════
    // SCALE HIERARCHY
    // ═══════════════════════════════════════════════════════════════════
    private List<CognitiveAgent> scaleHierarchy;
    
    /**
     * Cognitive Agent at a specific scale
     */
    public static class CognitiveAgent {
        int scaleLevel;
        double[] internalState;
        double[] boundaryState;      // Markov blanket boundary
        double[] externalState;      // External environment
        double consciousnessLevel;
        double couplingStrength;
        List<CognitiveAgent> subAgents;
        CognitiveAgent superAgent;
        
        CognitiveAgent(int scaleLevel, int dimension) {
            this.scaleLevel = scaleLevel;
            this.internalState = new double[dimension];
            this.boundaryState = new double[dimension];
            this.externalState = new double[dimension];
            this.consciousnessLevel = 0.5;
            this.couplingStrength = 1.0;
            this.subAgents = new ArrayList<>();
            this.superAgent = null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // MARKOV BLANKET TRACKING
    // ═══════════════════════════════════════════════════════════════════
    private double[][] markovBlanketMatrix;
    private double[] synchronizationVector;
    private int syncIterations;
    
    // ═══════════════════════════════════════════════════════════════════
    // PLANETARY INTELLIGENCE
    // ═══════════════════════════════════════════════════════════════════
    private double planetaryCoherence;
    private double emergenceLevel;
    private double collectiveIntelligence;
    private Random random;
    
    public ScaleFreeCognition(int maxScales, int stateDimension) {
        this.maxScales = maxScales;
        this.stateDimension = stateDimension;
        
        // Scale-free parameters
        this.couplingStrength = 0.5;
        this.synchronizationRate = 0.1;
        this.fractalDimension = 1.5; // Fractal dimension for self-similarity
        
        // Initialize scale hierarchy
        this.scaleHierarchy = new ArrayList<>();
        for (int i = 0; i < maxScales; i++) {
            CognitiveAgent agent = new CognitiveAgent(i, stateDimension);
            scaleHierarchy.add(agent);
            
            // Build hierarchical structure
            if (i > 0) {
                agent.superAgent = scaleHierarchy.get(i - 1);
                scaleHierarchy.get(i - 1).subAgents.add(agent);
            }
        }
        
        // Initialize Markov blanket matrix
        this.markovBlanketMatrix = new double[maxScales][maxScales];
        this.synchronizationVector = new double[maxScales];
        this.syncIterations = 0;
        
        this.planetaryCoherence = 0.0;
        this.emergenceLevel = 0.0;
        this.collectiveIntelligence = 0.0;
        
        this.random = new Random(42);
        
        initializeFractalStates();
    }
    
    /**
     * Initialize states with fractal self-similarity
     */
    private void initializeFractalStates() {
        for (int i = 0; i < maxScales; i++) {
            CognitiveAgent agent = scaleHierarchy.get(i);
            double scale = Math.pow(PHI, -i);
            
            for (int j = 0; j < stateDimension; j++) {
                // Fractal initialization with self-similarity
                agent.internalState[j] = Math.sin(j * scale) * scale;
                agent.boundaryState[j] = Math.cos(j * scale) * scale;
                agent.externalState[j] = random.nextGaussian() * 0.1;
            }
            
            // Consciousness scales with level
            agent.consciousnessLevel = 0.5 + 0.1 * Math.sin(i);
        }
    }
    
    /**
     * Markov Blanket Synchronization
     * Synchronize internal states based on boundary conditions
     */
    public void markovBlanketSync() {
        syncIterations++;
        
        // Bottom-up synchronization
        for (int i = 0; i < maxScales; i++) {
            CognitiveAgent agent = scaleHierarchy.get(i);
            
            // Synchronize with boundary
            for (int j = 0; j < stateDimension; j++) {
                double delta = agent.boundaryState[j] - agent.internalState[j];
                agent.internalState[j] += synchronizationRate * delta;
            }
            
            // Synchronize with super-agent
            if (agent.superAgent != null) {
                for (int j = 0; j < stateDimension; j++) {
                    double superDelta = agent.superAgent.internalState[j] - agent.internalState[j];
                    agent.internalState[j] += couplingStrength * superDelta * 0.1;
                }
            }
        }
        
        // Top-down influence
        for (int i = maxScales - 1; i >= 0; i--) {
            CognitiveAgent agent = scaleHierarchy.get(i);
            
            // Influence from sub-agents
            if (!agent.subAgents.isEmpty()) {
                for (CognitiveAgent subAgent : agent.subAgents) {
                    for (int j = 0; j < stateDimension; j++) {
                        double subDelta = subAgent.internalState[j] - agent.internalState[j];
                        agent.internalState[j] += couplingStrength * subDelta * 0.05;
                    }
                }
            }
        }
        
        // Update Markov blanket matrix
        updateMarkovBlanketMatrix();
    }
    
    /**
     * Update Markov blanket coupling matrix
     */
    private void updateMarkovBlanketMatrix() {
        for (int i = 0; i < maxScales; i++) {
            for (int j = 0; j < maxScales; j++) {
                if (i == j) {
                    markovBlanketMatrix[i][j] = 1.0;
                } else if (Math.abs(i - j) == 1) {
                    // Adjacent scales have stronger coupling
                    CognitiveAgent agentI = scaleHierarchy.get(i);
                    CognitiveAgent agentJ = scaleHierarchy.get(j);
                    double correlation = computeCorrelation(agentI.internalState, agentJ.internalState);
                    markovBlanketMatrix[i][j] = correlation * couplingStrength;
                } else {
                    // Non-adjacent scales have weaker coupling
                    markovBlanketMatrix[i][j] = couplingStrength * Math.pow(PHI, -Math.abs(i - j));
                }
            }
        }
    }
    
    /**
     * Compute correlation between two state vectors
     */
    private double computeCorrelation(double[] a, double[] b) {
        double meanA = 0, meanB = 0;
        for (int i = 0; i < a.length; i++) {
            meanA += a[i];
            meanB += b[i];
        }
        meanA /= a.length;
        meanB /= b.length;
        
        double covariance = 0, stdA = 0, stdB = 0;
        for (int i = 0; i < a.length; i++) {
            covariance += (a[i] - meanA) * (b[i] - meanB);
            stdA += (a[i] - meanA) * (a[i] - meanA);
            stdB += (b[i] - meanB) * (b[i] - meanB);
        }
        
        stdA = Math.sqrt(stdA);
        stdB = Math.sqrt(stdB);
        
        if (stdA == 0 || stdB == 0) return 0;
        return covariance / (stdA * stdB);
    }
    
    /**
     * Compute planetary coherence across all scales
     */
    public double computePlanetaryCoherence() {
        double totalCoherence = 0;
        int pairs = 0;
        
        for (int i = 0; i < maxScales; i++) {
            for (int j = i + 1; j < maxScales; j++) {
                double correlation = computeCorrelation(
                    scaleHierarchy.get(i).internalState,
                    scaleHierarchy.get(j).internalState
                );
                totalCoherence += Math.abs(correlation);
                pairs++;
            }
        }
        
        planetaryCoherence = pairs > 0 ? totalCoherence / pairs : 0;
        return planetaryCoherence;
    }
    
    /**
     * Compute emergence level
     * Measures how much the whole is greater than sum of parts
     */
    public double computeEmergenceLevel() {
        double sumIndividual = 0;
        double[] collectiveState = new double[stateDimension];
        
        // Sum individual states
        for (CognitiveAgent agent : scaleHierarchy) {
            for (int i = 0; i < stateDimension; i++) {
                sumIndividual += Math.abs(agent.internalState[i]);
                collectiveState[i] += agent.internalState[i];
            }
        }
        
        // Normalize collective state
        double collectiveMagnitude = 0;
        for (int i = 0; i < stateDimension; i++) {
            collectiveState[i] /= maxScales;
            collectiveMagnitude += collectiveState[i] * collectiveState[i];
        }
        collectiveMagnitude = Math.sqrt(collectiveMagnitude);
        
        // Emergence is the ratio of collective to individual
        emergenceLevel = sumIndividual > 0 ? collectiveMagnitude / (sumIndividual / maxScales) : 0;
        return emergenceLevel;
    }
    
    /**
     * Compute collective intelligence
     * Integrated measure of coherence, emergence, and consciousness
     */
    public double computeCollectiveIntelligence() {
        double avgConsciousness = 0;
        for (CognitiveAgent agent : scaleHierarchy) {
            avgConsciousness += agent.consciousnessLevel;
        }
        avgConsciousness /= maxScales;
        
        // Collective intelligence = coherence × emergence × consciousness
        collectiveIntelligence = planetaryCoherence * emergenceLevel * avgConsciousness;
        return collectiveIntelligence;
    }
    
    /**
     * Process input at specific scale
     */
    public void processInput(int scaleLevel, double[] input) {
        if (scaleLevel >= 0 && scaleLevel < maxScales) {
            CognitiveAgent agent = scaleHierarchy.get(scaleLevel);
            System.arraycopy(input, 0, agent.externalState, 0, 
                Math.min(input.length, stateDimension));
            
            // Update internal state based on input
            for (int i = 0; i < stateDimension; i++) {
                agent.internalState[i] += 0.1 * agent.externalState[i];
            }
            
            // Update consciousness based on input complexity
            double complexity = computeInputComplexity(input);
            agent.consciousnessLevel = 0.5 + 0.3 * complexity;
        }
    }
    
    /**
     * Compute input complexity (entropy)
     */
    private double computeInputComplexity(double[] input) {
        double entropy = 0;
        for (double val : input) {
            if (val != 0) {
                entropy += -val * Math.log(Math.abs(val) + 1e-10);
            }
        }
        return Math.min(1.0, entropy / 10.0);
    }
    
    /**
     * Get agent at specific scale
     */
    public CognitiveAgent getAgent(int scaleLevel) {
        if (scaleLevel >= 0 && scaleLevel < maxScales) {
            return scaleHierarchy.get(scaleLevel);
        }
        return null;
    }
    
    /**
     * Get synchronization vector
     */
    public double[] getSynchronizationVector() {
        for (int i = 0; i < maxScales; i++) {
            CognitiveAgent agent = scaleHierarchy.get(i);
            synchronizationVector[i] = agent.consciousnessLevel;
        }
        return synchronizationVector;
    }
    
    /**
     * Run multiple synchronization iterations
     */
    public void runSyncIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            markovBlanketSync();
        }
    }
    
    /**
     * Reset system
     */
    public void reset() {
        for (CognitiveAgent agent : scaleHierarchy) {
            Arrays.fill(agent.internalState, 0);
            Arrays.fill(agent.boundaryState, 0);
            Arrays.fill(agent.externalState, 0);
            agent.consciousnessLevel = 0.5;
        }
        
        for (int i = 0; i < maxScales; i++) {
            Arrays.fill(markovBlanketMatrix[i], 0);
        }
        Arrays.fill(synchronizationVector, 0);
        
        syncIterations = 0;
        planetaryCoherence = 0.0;
        emergenceLevel = 0.0;
        collectiveIntelligence = 0.0;
        
        initializeFractalStates();
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════════
    
    public void printStats() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ SCALE-FREE COGNITION STATISTICS                        │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│ Max Scales:          " + String.format("%-36d", maxScales) + "│");
        System.out.println("│ State Dimension:     " + String.format("%-36d", stateDimension) + "│");
        System.out.println("│ Sync Iterations:     " + String.format("%-36d", syncIterations) + "│");
        System.out.println("│ Coupling Strength:   " + String.format("%-36.4f", couplingStrength) + "│");
        System.out.println("│ Sync Rate:           " + String.format("%-36.4f", synchronizationRate) + "│");
        System.out.println("│ Fractal Dimension:   " + String.format("%-36.4f", fractalDimension) + "│");
        System.out.println("│ Planetary Coherence: " + String.format("%-36.4f", planetaryCoherence) + "│");
        System.out.println("│ Emergence Level:     " + String.format("%-36.4f", emergenceLevel) + "│");
        System.out.println("│ Collective Intel:    " + String.format("%-36.4f", collectiveIntelligence) + "│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ SCALE HIERARCHY                                        │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        for (CognitiveAgent agent : scaleHierarchy) {
            System.out.printf("│ Scale %d: Consciousness=%.4f | SubAgents=%d | State=[", 
                agent.scaleLevel, agent.consciousnessLevel, agent.subAgents.size());
            for (int i = 0; i < Math.min(3, agent.internalState.length); i++) {
                System.out.printf("%.3f ", agent.internalState[i]);
            }
            System.out.println("...] │");
        }
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ MARKOV BLANKET MATRIX                                   │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        for (int i = 0; i < maxScales; i++) {
            System.out.print("│ ");
            for (int j = 0; j < maxScales; j++) {
                System.out.printf("%.3f ", markovBlanketMatrix[i][j]);
            }
            System.out.println("│");
        }
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }
}
