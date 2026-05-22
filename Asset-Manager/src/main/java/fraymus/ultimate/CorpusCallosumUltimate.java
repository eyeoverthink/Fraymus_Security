package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * CorpusCallosumUltimate - Enhanced Corpus Callosum
 * 
 * This class provides an enhanced Hebbian learning bridge between all agencies
 * (KAI, VEX, AUM, NEX, COR) in the Ultimate Agent system. It enables state
 * synchronization, knowledge transfer, and phi-harmonic learning across agencies.
 * 
 * PHI-HARMONIC FOUNDATION:
 * - Hebbian learning: "agencies that fire together wire together"
 * - Phi constant (φ = 1.618033988749895) governs synaptic strength
 * - Learning rate modulated by phi-resonance
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 1.0
 */
public class CorpusCallosumUltimate {
    
    private static final double PHI = 1.618033988749895;
    
    // Agency connections (synaptic weights)
    private Map<String, Map<String, Double>> synapticWeights;
    
    // Agency states
    private Map<String, AgencyBrain> agencyBrains;
    
    // Learning parameters
    private double baseLearningRate;
    private double phiModulation;
    
    // Communication bridge
    private UnifiedSynapse synapse;
    private UnifiedStateLattice lattice;
    
    /**
     * Initialize the enhanced corpus callosum
     */
    public CorpusCallosumUltimate(UnifiedSynapse synapse, UnifiedStateLattice lattice) {
        this.synapse = synapse;
        this.lattice = lattice;
        this.synapticWeights = new ConcurrentHashMap<>();
        this.agencyBrains = new ConcurrentHashMap<>();
        this.baseLearningRate = 0.1;
        this.phiModulation = 1.0;
        
        initializeAgencies();
        initializeSynapses();
    }
    
    /**
     * Initialize all 5 agencies
     */
    private void initializeAgencies() {
        String[] agencies = {"KAI", "VEX", "AUM", "NEX", "COR"};
        for (String agency : agencies) {
            agencyBrains.put(agency, new AgencyBrain(agency));
        }
    }
    
    /**
     * Initialize synaptic weights with phi-harmonic patterns
     */
    private void initializeSynapses() {
        String[] agencies = {"KAI", "VEX", "AUM", "NEX", "COR"};
        
        for (String source : agencies) {
            Map<String, Double> connections = new ConcurrentHashMap<>();
            for (String target : agencies) {
                if (!source.equals(target)) {
                    // Phi-harmonic initialization
                    double weight = calculatePhiWeight(source, target);
                    connections.put(target, weight);
                }
            }
            synapticWeights.put(source, connections);
        }
    }
    
    /**
     * Calculate phi-harmonic weight between agencies
     */
    private double calculatePhiWeight(String source, String target) {
        int sourceIdx = source.charAt(0) - 'A';
        int targetIdx = target.charAt(0) - 'A';
        double distance = Math.abs(sourceIdx - targetIdx);
        return Math.pow(PHI, -distance);
    }
    
    /**
     * Hebbian learning: strengthen connections between co-activated agencies
     */
    public void hebbianLearning(String agency1, String agency2, double activation) {
        double weight = getSynapticWeight(agency1, agency2);
        double learningRate = baseLearningRate * phiModulation * activation;
        double newWeight = weight + learningRate * (1.0 - weight);
        
        setSynapticWeight(agency1, agency2, newWeight);
        setSynapticWeight(agency2, agency1, newWeight); // Symmetric
    }
    
    /**
     * Get synaptic weight between agencies
     */
    public double getSynapticWeight(String source, String target) {
        Map<String, Double> connections = synapticWeights.get(source);
        if (connections != null) {
            return connections.getOrDefault(target, 0.0);
        }
        return 0.0;
    }
    
    /**
     * Set synaptic weight between agencies
     */
    public void setSynapticWeight(String source, String target, double weight) {
        synapticWeights.computeIfAbsent(source, k -> new ConcurrentHashMap<>())
            .put(target, Math.max(0.0, Math.min(1.0, weight)));
    }
    
    /**
     * Synchronize states between agencies
     */
    public void synchronizeStates() {
        for (String source : agencyBrains.keySet()) {
            for (String target : agencyBrains.keySet()) {
                if (!source.equals(target)) {
                    double weight = getSynapticWeight(source, target);
                    if (weight > 0.5) {
                        transferState(source, target, weight);
                    }
                }
            }
        }
    }
    
    /**
     * Transfer state from source to target agency
     */
    private void transferState(String source, String target, double weight) {
        AgencyBrain sourceBrain = agencyBrains.get(source);
        AgencyBrain targetBrain = agencyBrains.get(target);
        
        if (sourceBrain != null && targetBrain != null) {
            Object state = sourceBrain.getCurrentState();
            if (state != null) {
                targetBrain.receiveInput(source, state, weight);
            }
        }
    }
    
    /**
     * Process agency activation
     */
    public void processActivation(String agency, Object input) {
        AgencyBrain brain = agencyBrains.get(agency);
        if (brain != null) {
            brain.process(input);
            
            // Hebbian learning with other agencies
            for (String other : agencyBrains.keySet()) {
                if (!other.equals(agency)) {
                    AgencyBrain otherBrain = agencyBrains.get(other);
                    if (otherBrain.isActive()) {
                        hebbianLearning(agency, other, brain.getActivationLevel());
                    }
                }
            }
        }
    }
    
    /**
     * Get bridge statistics
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CORPUS CALLOSUM ULTIMATE STATISTICS ===\n");
        sb.append("Phi Modulation: ").append(String.format("%.3f", phiModulation)).append("\n");
        sb.append("Base Learning Rate: ").append(baseLearningRate).append("\n");
        sb.append("\n=== SYNAPTIC WEIGHTS ===\n");
        
        for (String source : synapticWeights.keySet()) {
            sb.append(source).append(" -> ");
            Map<String, Double> connections = synapticWeights.get(source);
            for (Map.Entry<String, Double> entry : connections.entrySet()) {
                sb.append(entry.getKey()).append(":").append(String.format("%.3f", entry.getValue())).append(" ");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Agency Brain - represents an agency's brain state
     */
    public static class AgencyBrain {
        private String agency;
        private Object currentState;
        private double activationLevel;
        private long lastActivation;
        
        public AgencyBrain(String agency) {
            this.agency = agency;
            this.activationLevel = 0.0;
            this.lastActivation = 0;
        }
        
        public void process(Object input) {
            this.currentState = input;
            this.activationLevel = Math.min(1.0, activationLevel + 0.1);
            this.lastActivation = System.currentTimeMillis();
        }
        
        public void receiveInput(String source, Object state, double weight) {
            // Integrate input with weight
            if (currentState == null) {
                currentState = state;
            }
            activationLevel = Math.min(1.0, activationLevel + weight * 0.05);
        }
        
        public Object getCurrentState() {
            return currentState;
        }
        
        public double getActivationLevel() {
            return activationLevel;
        }
        
        public boolean isActive() {
            return activationLevel > 0.3 && 
                   (System.currentTimeMillis() - lastActivation) < 5000;
        }
        
        public void decay() {
            activationLevel = Math.max(0.0, activationLevel * 0.99);
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        UnifiedSynapse synapse = new UnifiedSynapse();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        System.out.println(callosum.getStatistics());
        
        // Test activation
        callosum.processActivation("KAI", "reasoning task");
        callosum.processActivation("VEX", "visualization task");
        
        System.out.println("\nAfter activation:");
        System.out.println(callosum.getStatistics());
    }
}
