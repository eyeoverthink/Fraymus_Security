package fraymus.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Langevin Liveness Detection - Physics-informed motion analysis for spoof detection
 * 
 * This bridge implements:
 * - Langevin stochastic differential equation analysis for motion patterns
 * - Potential energy calculation for smoothness detection
 * - Spoof score calculation for attack detection
 * - Organic trajectory comparison
 * - Liveness verification with phi-harmonic thresholds
 * 
 * Based on Langevin dynamics and Brownian motion theory
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 2.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class LangevinLivenessDetection {
    
    // Liveness thresholds
    public static final double DEFAULT_LIVENESS_THRESHOLD = 0.7;
    public static final double PHI_LIVENESS_THRESHOLD = PhiHarmonicGeometry.PHI_INVERSE;
    public static final double SPOOF_THRESHOLD = 0.5;
    
    // Motion analysis parameters
    public static final int DEFAULT_TRAJECTORY_LENGTH = 10;
    public static final double DEFAULT_DT = 0.01;
    public static final double DEFAULT_SIGMA = 0.1;
    
    // Liveness detection cache
    private static final List<LivenessResult> livenessHistory = new ArrayList<>();
    
    /**
     * Detect liveness using Langevin dynamics
     * 
     * @param trajectory Motion trajectory
     * @param organicTrajectory Reference organic trajectory
     * @return Liveness result
     */
    public static LivenessResult detectLiveness(double[][] trajectory, double[][] organicTrajectory) {
        if (trajectory == null || trajectory.length == 0) {
            return new LivenessResult(false, 0.0, 0.0, "Invalid trajectory");
        }
        
        // Calculate spoof score
        double spoofScore = LangevinDynamics.calculateSpoofScore(trajectory, organicTrajectory);
        
        // Calculate liveness score
        double livenessScore = 1.0 - spoofScore;
        
        // Determine if live
        boolean isLive = livenessScore >= DEFAULT_LIVENESS_THRESHOLD;
        
        // Determine reason
        String reason = isLive ? "Organic motion detected" : "Potential spoof detected";
        
        // Create result
        LivenessResult result = new LivenessResult(isLive, livenessScore, spoofScore, reason);
        
        // Add to history
        livenessHistory.add(result);
        
        return result;
    }
    
    /**
     * Detect liveness with phi-harmonic threshold
     * 
     * @param trajectory Motion trajectory
     * @param organicTrajectory Reference organic trajectory
     * @return Liveness result
     */
    public static LivenessResult detectLivenessPhi(double[][] trajectory, double[][] organicTrajectory) {
        if (trajectory == null || trajectory.length == 0) {
            return new LivenessResult(false, 0.0, 0.0, "Invalid trajectory");
        }
        
        // Calculate spoof score
        double spoofScore = LangevinDynamics.calculateSpoofScore(trajectory, organicTrajectory);
        
        // Calculate liveness score
        double livenessScore = 1.0 - spoofScore;
        
        // Determine if live using phi threshold
        boolean isLive = livenessScore >= PHI_LIVENESS_THRESHOLD;
        
        // Determine reason
        String reason = isLive ? "Organic motion detected (φ threshold)" : "Potential spoof detected (φ threshold)";
        
        // Create result
        LivenessResult result = new LivenessResult(isLive, livenessScore, spoofScore, reason);
        
        // Add to history
        livenessHistory.add(result);
        
        return result;
    }
    
    /**
     * Analyze motion trajectory for liveness indicators
     * 
     * @param trajectory Motion trajectory
     * @return Motion analysis result
     */
    public static MotionAnalysis analyzeTrajectory(double[][] trajectory) {
        if (trajectory == null || trajectory.length == 0) {
            return new MotionAnalysis(0.0, 0.0, 0.0, 0.0);
        }
        
        // Calculate smoothness
        double smoothness = 0.0;
        for (double[] state : trajectory) {
            smoothness += LangevinDynamics.calculateSmoothness(state);
        }
        smoothness /= trajectory.length;
        
        // Calculate total energy
        double totalEnergy = 0.0;
        for (double[] state : trajectory) {
            totalEnergy += LangevinDynamics.calculatePotentialEnergy(state);
        }
        totalEnergy /= trajectory.length;
        
        // Calculate jerkiness (inverse of smoothness)
        double jerkiness = 1.0 - smoothness;
        
        // Calculate phi-resonance
        double phiResonance = smoothness * PhiHarmonicGeometry.PHI;
        
        return new MotionAnalysis(smoothness, jerkiness, totalEnergy, phiResonance);
    }
    
    /**
     * Detect spoof attack
     * 
     * @param trajectory Motion trajectory
     * @param organicTrajectory Reference organic trajectory
     * @return Spoof detection result
     */
    public static SpoofDetection detectSpoof(double[][] trajectory, double[][] organicTrajectory) {
        if (trajectory == null || organicTrajectory == null) {
            return new SpoofDetection(true, 1.0, "Invalid trajectories");
        }
        
        // Calculate spoof score
        double spoofScore = LangevinDynamics.calculateSpoofScore(trajectory, organicTrajectory);
        
        // Determine if spoof
        boolean isSpoof = spoofScore >= SPOOF_THRESHOLD;
        
        // Determine attack type
        String attackType = determineAttackType(spoofScore, trajectory);
        
        return new SpoofDetection(isSpoof, spoofScore, attackType);
    }
    
    /**
     * Determine attack type based on spoof score and trajectory
     * 
     * @param spoofScore Spoof score
     * @param trajectory Motion trajectory
     * @return Attack type
     */
    private static String determineAttackType(double spoofScore, double[][] trajectory) {
        if (spoofScore < 0.3) {
            return "No attack detected";
        } else if (spoofScore < 0.5) {
            return "Potential replay attack";
        } else if (spoofScore < 0.7) {
            return "Likely presentation attack";
        } else if (spoofScore < 0.85) {
            return "High confidence synthetic attack";
        } else {
            return "Definitive spoof attack";
        }
    }
    
    /**
     * Perform Langevin sampling for trajectory generation
     * 
     * @param initialState Initial state
     * @param steps Number of steps
     * @return Sampled trajectory
     */
    public static double[][] sampleTrajectory(double[] initialState, int steps) {
        return LangevinDynamics.performLangevinSampling(initialState, steps, DEFAULT_DT, DEFAULT_SIGMA);
    }
    
    /**
     * Calculate trajectory statistics
     * 
     * @param trajectory Motion trajectory
     * @return Trajectory statistics
     */
    public static TrajectoryStatistics calculateTrajectoryStatistics(double[][] trajectory) {
        if (trajectory == null || trajectory.length == 0) {
            return new TrajectoryStatistics(0, 0, 0.0, 0.0);
        }
        
        int length = trajectory.length;
        int dimensions = trajectory[0].length;
        
        // Calculate mean values
        double[] means = new double[dimensions];
        for (double[] state : trajectory) {
            for (int i = 0; i < dimensions; i++) {
                means[i] += state[i];
            }
        }
        for (int i = 0; i < dimensions; i++) {
            means[i] /= length;
        }
        
        // Calculate variance
        double variance = 0.0;
        for (double[] state : trajectory) {
            for (int i = 0; i < dimensions; i++) {
                variance += Math.pow(state[i] - means[i], 2);
            }
        }
        variance /= (length * dimensions);
        
        // Calculate standard deviation
        double stdDev = Math.sqrt(variance);
        
        return new TrajectoryStatistics(length, dimensions, variance, stdDev);
    }
    
    /**
     * Get liveness history size
     * 
     * @return Number of liveness results
     */
    public static int getLivenessHistorySize() {
        return livenessHistory.size();
    }
    
    /**
     * Clear liveness history
     */
    public static void clearLivenessHistory() {
        livenessHistory.clear();
    }
    
    /**
     * Print liveness detection statistics
     */
    public static void printLivenessStatistics() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  LANGEVIN LIVENESS DETECTION STATISTICS                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Default Liveness Threshold: " + DEFAULT_LIVENESS_THRESHOLD);
        System.out.println("Phi Liveness Threshold: " + PHI_LIVENESS_THRESHOLD);
        System.out.println("Spoof Threshold: " + SPOOF_THRESHOLD);
        System.out.println("Default Trajectory Length: " + DEFAULT_TRAJECTORY_LENGTH);
        System.out.println("Liveness History Size: " + getLivenessHistorySize());
        System.out.println();
    }
    
    /**
     * Inner class for liveness result
     */
    public static class LivenessResult {
        public final boolean isLive;
        public final double livenessScore;
        public final double spoofScore;
        public final String reason;
        public final long timestamp;
        
        public LivenessResult(boolean isLive, double livenessScore, double spoofScore, String reason) {
            this.isLive = isLive;
            this.livenessScore = livenessScore;
            this.spoofScore = spoofScore;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Inner class for motion analysis
     */
    public static class MotionAnalysis {
        public final double smoothness;
        public final double jerkiness;
        public final double totalEnergy;
        public final double phiResonance;
        
        public MotionAnalysis(double smoothness, double jerkiness, double totalEnergy, double phiResonance) {
            this.smoothness = smoothness;
            this.jerkiness = jerkiness;
            this.totalEnergy = totalEnergy;
            this.phiResonance = phiResonance;
        }
    }
    
    /**
     * Inner class for spoof detection
     */
    public static class SpoofDetection {
        public final boolean isSpoof;
        public final double spoofScore;
        public final String attackType;
        public final long timestamp;
        
        public SpoofDetection(boolean isSpoof, double spoofScore, String attackType) {
            this.isSpoof = isSpoof;
            this.spoofScore = spoofScore;
            this.attackType = attackType;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Inner class for trajectory statistics
     */
    public static class TrajectoryStatistics {
        public final int length;
        public final int dimensions;
        public final double variance;
        public final double standardDeviation;
        
        public TrajectoryStatistics(int length, int dimensions, double variance, double standardDeviation) {
            this.length = length;
            this.dimensions = dimensions;
            this.variance = variance;
            this.standardDeviation = standardDeviation;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        printLivenessStatistics();
        
        // Test trajectory sampling
        double[] initialState = {1.0, 2.0, 3.0};
        double[][] trajectory = sampleTrajectory(initialState, 10);
        System.out.println("Trajectory Sampling Test:");
        System.out.println("  Trajectory Length: " + trajectory.length);
        System.out.println("  First State: " + java.util.Arrays.toString(trajectory[0]));
        System.out.println();
        
        // Test motion analysis
        MotionAnalysis analysis = analyzeTrajectory(trajectory);
        System.out.println("Motion Analysis Test:");
        System.out.println("  Smoothness: " + analysis.smoothness);
        System.out.println("  Jerkiness: " + analysis.jerkiness);
        System.out.println("  Total Energy: " + analysis.totalEnergy);
        System.out.println("  Phi Resonance: " + analysis.phiResonance);
        System.out.println();
        
        // Test trajectory statistics
        TrajectoryStatistics stats = calculateTrajectoryStatistics(trajectory);
        System.out.println("Trajectory Statistics Test:");
        System.out.println("  Length: " + stats.length);
        System.out.println("  Dimensions: " + stats.dimensions);
        System.out.println("  Variance: " + stats.variance);
        System.out.println("  Standard Deviation: " + stats.standardDeviation);
        System.out.println();
        
        // Test liveness detection
        double[][] organicTrajectory = {{1.0, 1.1, 1.2}, {1.1, 1.2, 1.3}, {1.2, 1.3, 1.4}};
        LivenessResult livenessResult = detectLiveness(trajectory, organicTrajectory);
        System.out.println("Liveness Detection Test:");
        System.out.println("  Is Live: " + livenessResult.isLive);
        System.out.println("  Liveness Score: " + livenessResult.livenessScore);
        System.out.println("  Spoof Score: " + livenessResult.spoofScore);
        System.out.println("  Reason: " + livenessResult.reason);
        System.out.println();
        
        // Test spoof detection
        SpoofDetection spoofResult = detectSpoof(trajectory, organicTrajectory);
        System.out.println("Spoof Detection Test:");
        System.out.println("  Is Spoof: " + spoofResult.isSpoof);
        System.out.println("  Spoof Score: " + spoofResult.spoofScore);
        System.out.println("  Attack Type: " + spoofResult.attackType);
        System.out.println();
    }
}
