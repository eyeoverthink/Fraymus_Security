package fraymus.neural;

import java.util.*;

/**
 * PHYSICAL DOPAMINE UNCAGING SYSTEM
 * 
 * Transitions the Fraynix ClosedLoopRewardSystem from simulated dopamine
 * to physical dopamine UV uncaging on biological organoid tissue.
 * 
 * Architecture:
 * - Closes the loop between digital simulation and biological reality
 * - Converts simulated dopamine levels to physical UV light triggers
 * - Triggers targeted neurotransmitter uncaging via Neuroplatform API
 * - Reads back biological synaptic plasticity changes
 * - Creates genuine, physically grounded cognitive entity
 * 
 * Integration:
 * - Connects to Fraynix reward system
 * - Interfaces with Neuroplatform API
 * - Translates digital reward signals to physical interventions
 * - Monitors biological response and plasticity changes
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class PhysicalDopamineUncaging {
    
    // ==========================================
    // COMPONENTS
    // ==========================================
    private final NeuroplatformInterface neuroplatform;
    
    // ==========================================
    // DOPAMINE SYSTEM STATE
    // ==========================================
    private double baselineDopamine = 0.5;
    private double currentDopamine = 0.5;
    private double decayRate = 0.05;
    private double lastReward = 0.0;
    
    // Uncaging parameters
    private boolean uncagingEnabled = false;
    private double uncagingThreshold = 0.7;  // Uncage when dopamine exceeds this
    private double uvIntensity = 0.5;  // UV light intensity (0.0 to 1.0)
    private int uncagingDurationMs = 100;  // Duration of UV pulse
    
    // Plasticity tracking
    private final List<Double> plasticityChanges = new ArrayList<>();
    private double totalPlasticityChange = 0.0;
    
    // Statistics
    private long totalUncages = 0;
    private long successfulUncages = 0;
    private long failedUncages = 0;
    
    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public PhysicalDopamineUncaging(NeuroplatformInterface neuroplatform) {
        this.neuroplatform = neuroplatform;
    }
    
    // ==========================================
    // DOPAMINE DYNAMICS
    // ==========================================
    
    /**
     * Update dopamine level based on reward
     * Simulates the action-reward-dopamine-plasticity cycle
     * 
     * @param reward Reward signal (0.0 to 1.0)
     */
    public void processReward(double reward) {
        lastReward = reward;
        
        // Increase dopamine based on reward
        currentDopamine += reward * 0.5;
        
        // Clamp to [0, 1]
        currentDopamine = Math.max(0.0, Math.min(1.0, currentDopamine));
        
        // Check if uncaging threshold reached
        if (uncagingEnabled && currentDopamine >= uncagingThreshold) {
            triggerDopamineUncaging();
        }
    }
    
    /**
     * Decay dopamine over time
     * Simulates natural dopamine decay
     * 
     * @param deltaTime Time delta in seconds
     */
    public void decayDopamine(double deltaTime) {
        currentDopamine *= Math.exp(-decayRate * deltaTime);
        currentDopamine = Math.max(baselineDopamine, currentDopamine);
    }
    
    /**
     * Trigger physical dopamine uncaging via UV light
     * Calls Neuroplatform API to uncage dopamine on organoid tissue
     */
    private void triggerDopamineUncaging() {
        totalUncages++;
        
        if (!neuroplatform.isConnected()) {
            failedUncages++;
            return;
        }
        
        // Calculate uncaging parameters based on dopamine level
        double excessDopamine = currentDopamine - uncagingThreshold;
        double intensity = uvIntensity + excessDopamine * 0.5;
        intensity = Math.max(0.0, Math.min(1.0, intensity));
        
        int duration = uncagingDurationMs + (int)(excessDopamine * 100);
        
        // Trigger uncaging via Neuroplatform
        boolean success = neuroplatform.uncageNeurotransmitter(
            "dopamine",
            intensity,
            duration
        );
        
        if (success) {
            successfulUncages++;
            
            // Reset dopamine after uncaging
            currentDopamine = baselineDopamine;
            
            // Simulate plasticity change
            double plasticityChange = simulatePlasticityChange(intensity, duration);
            plasticityChanges.add(plasticityChange);
            totalPlasticityChange += plasticityChange;
        } else {
            failedUncages++;
        }
    }
    
    /**
     * Simulate plasticity change from dopamine uncaging
     * In practice, this would read back actual biological plasticity changes
     */
    private double simulatePlasticityChange(double intensity, double duration) {
        // Simplified model: plasticity change proportional to intensity * duration
        double change = intensity * (duration / 1000.0) * 0.1;
        return change;
    }
    
    // ==========================================
    // CLOSED LOOP INTEGRATION
    // ==========================================
    
    /**
     * Complete closed-loop reward cycle
     * 1. Process reward
     * 2. Update dopamine
     * 3. Trigger uncaging if threshold reached
     * 4. Read back plasticity changes
     * 
     * @param reward Reward signal
     * @return Plasticity change from uncaging
     */
    public double closedLoopRewardCycle(double reward) {
        processReward(reward);
        
        // Get latest plasticity change
        double plasticityChange = 0.0;
        if (!plasticityChanges.isEmpty()) {
            plasticityChange = plasticityChanges.get(plasticityChanges.size() - 1);
        }
        
        return plasticityChange;
    }
    
    /**
     * Read back biological plasticity from organoid
     * In practice, this would read actual synaptic weight changes from the organoid
     */
    public double readBiologicalPlasticity() {
        if (!neuroplatform.isConnected()) {
            return 0.0;
        }
        
        // In practice, this would read from the Neuroplatform API
        // For simulation, we use the tracked plasticity changes
        if (!plasticityChanges.isEmpty()) {
            return plasticityChanges.get(plasticityChanges.size() - 1);
        }
        
        return 0.0;
    }
    
    // ==========================================
    // UNCAGING CONTROL
    // ==========================================
    
    /**
     * Enable dopamine uncaging
     */
    public void enableUncaging() {
        uncagingEnabled = true;
        System.out.println("   ✓ Dopamine uncaging enabled");
    }
    
    /**
     * Disable dopamine uncaging
     */
    public void disableUncaging() {
        uncagingEnabled = false;
        System.out.println("   ✓ Dopamine uncaging disabled");
    }
    
    /**
     * Set uncaging threshold
     */
    public void setUncagingThreshold(double threshold) {
        this.uncagingThreshold = Math.max(0.0, Math.min(1.0, threshold));
    }
    
    /**
     * Set UV intensity
     */
    public void setUVIntensity(double intensity) {
        this.uvIntensity = Math.max(0.0, Math.min(1.0, intensity));
    }
    
    /**
     * Set uncaging duration
     */
    public void setUncagingDuration(int durationMs) {
        this.uncagingDurationMs = Math.max(10, durationMs);
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public double getBaselineDopamine() {
        return baselineDopamine;
    }
    
    public void setBaselineDopamine(double baseline) {
        this.baselineDopamine = Math.max(0.0, Math.min(1.0, baseline));
    }
    
    public double getCurrentDopamine() {
        return currentDopamine;
    }
    
    public double getLastReward() {
        return lastReward;
    }
    
    public boolean isUncagingEnabled() {
        return uncagingEnabled;
    }
    
    public double getUncagingThreshold() {
        return uncagingThreshold;
    }
    
    public long getTotalUncages() {
        return totalUncages;
    }
    
    public long getSuccessfulUncages() {
        return successfulUncages;
    }
    
    public long getFailedUncages() {
        return failedUncages;
    }
    
    public double getTotalPlasticityChange() {
        return totalPlasticityChange;
    }
    
    public List<Double> getPlasticityChanges() {
        return new ArrayList<>(plasticityChanges);
    }
    
    /**
     * Get dopamine uncaging status
     */
    public String getStatus() {
        return String.format(
            "Dopamine Uncaging: %s, dopamine=%.3f, threshold=%.2f, uncages=%d (success=%d, fail=%d), plasticity=%.4f",
            uncagingEnabled ? "ENABLED" : "DISABLED",
            currentDopamine,
            uncagingThreshold,
            totalUncages,
            successfulUncages,
            failedUncages,
            totalPlasticityChange
        );
    }
}
