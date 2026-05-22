package fraymus.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * LangevinDynamics - Stochastic differential equation operations for physics-informed liveness detection
 * 
 * This utility class provides Langevin dynamics operations including:
 * - Langevin stochastic differential equation (SDE) simulation
 * - Underdamped Langevin dynamics with momentum
 * - Potential energy calculation
 * - Gradient computation
 * - Score-based generative modeling
 * - Physics-informed motion analysis
 * 
 * Based on Langevin's theory of Brownian motion and modern score-based generative models
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 2.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class LangevinDynamics {
    
    // Langevin parameters
    public static final int DEFAULT_STEPS = 1000;
    public static final double DEFAULT_DT = 0.01;
    public static final double DEFAULT_SIGMA = 0.1;
    public static final double DEFAULT_GAMMA = 1.0;
    
    // Cache for potential energy calculations
    private static final Map<String, Double> potentialEnergyCache = new HashMap<>();
    
    // Random number generator for stochastic processes
    private static final Random random = new Random();
    
    /**
     * Langevin Stochastic Differential Equation (SDE)
     * 
     * dX_t = -∇U(X_t)dt + σdW_t
     * 
     * Discretization (Euler-Maruyama):
     * X_{t+1} = X_t - ∇U(X_t)Δt + σ√Δt × N(0,1)
     * 
     * @param state Current state
     * @param gradient Gradient of potential energy
     * @param dt Time step
     * @param sigma Noise strength
     * @return Next state
     */
    public static double[] langevinStep(double[] state, double[] gradient, double dt, double sigma) {
        if (state == null || gradient == null || state.length != gradient.length) {
            return new double[0];
        }
        
        double[] nextState = new double[state.length];
        double sqrtDt = Math.sqrt(dt);
        
        for (int i = 0; i < state.length; i++) {
            // Deterministic drift: -∇U(X_t)Δt
            double drift = -gradient[i] * dt;
            
            // Stochastic diffusion: σ√Δt × N(0,1)
            double diffusion = sigma * sqrtDt * random.nextGaussian();
            
            nextState[i] = state[i] + drift + diffusion;
        }
        
        return nextState;
    }
    
    /**
     * Underdamped Langevin Dynamics with momentum
     * 
     * dX_t = V_t dt
     * dV_t = -∇U(X_t) dt - γV_t dt + σdW_t
     * 
     * @param position Current position
     * @param velocity Current momentum/velocity
     * @param gradient Gradient of potential energy
     * @param dt Time step
     * @param gamma Friction coefficient
     * @param sigma Noise strength
     * @return Next position and velocity
     */
    public static LangevinState underdampedLangevinStep(double[] position, double[] velocity, 
                                                          double[] gradient, double dt, double gamma, double sigma) {
        if (position == null || velocity == null || gradient == null) {
            return new LangevinState(new double[0], new double[0]);
        }
        
        int dimension = Math.min(position.length, Math.min(velocity.length, gradient.length));
        double[] nextPosition = new double[dimension];
        double[] nextVelocity = new double[dimension];
        double sqrtDt = Math.sqrt(dt);
        
        for (int i = 0; i < dimension; i++) {
            // Update position: dX_t = V_t dt
            nextPosition[i] = position[i] + velocity[i] * dt;
            
            // Update velocity: dV_t = -∇U(X_t) dt - γV_t dt + σdW_t
            double force = -gradient[i] * dt;
            double friction = -gamma * velocity[i] * dt;
            double noise = sigma * sqrtDt * random.nextGaussian();
            
            nextVelocity[i] = velocity[i] + force + friction + noise;
        }
        
        return new LangevinState(nextPosition, nextVelocity);
    }
    
    /**
     * Calculate potential energy for motion state
     * 
     * U(X) = Σᵢ (smoothness_reward - jerky_penalty) + quantum_potential
     * 
     * @param motionState Motion state (position, velocity, acceleration)
     * @return Potential energy
     */
    public static double calculatePotentialEnergy(double[] motionState) {
        if (motionState == null || motionState.length == 0) {
            return 0.0;
        }
        
        // Check cache
        String cacheKey = arrayToKey(motionState);
        if (potentialEnergyCache.containsKey(cacheKey)) {
            return potentialEnergyCache.get(cacheKey);
        }
        
        double energy = 0.0;
        
        // Penalize jerky motion (high acceleration changes)
        for (int i = 1; i < motionState.length; i++) {
            double jerk = Math.abs(motionState[i] - motionState[i-1]);
            energy += jerk * jerk; // Quadratic penalty
        }
        
        // Reward smoothness
        double smoothness = calculateSmoothness(motionState);
        energy -= smoothness;
        
        // Add quantum-inspired potential
        double quantumPotential = calculateQuantumPotential(motionState);
        energy += quantumPotential;
        
        // Cache result
        potentialEnergyCache.put(cacheKey, energy);
        
        return energy;
    }
    
    /**
     * Calculate gradient of potential energy
     * 
     * ∇U(X) = [∂U/∂x₁, ∂U/∂x₂, ..., ∂U/∂x_n]
     * 
     * @param motionState Motion state
     * @param epsilon Perturbation size for numerical gradient
     * @return Gradient vector
     */
    public static double[] calculateGradient(double[] motionState, double epsilon) {
        if (motionState == null || motionState.length == 0) {
            return new double[0];
        }
        
        double[] gradient = new double[motionState.length];
        double baseEnergy = calculatePotentialEnergy(motionState);
        
        for (int i = 0; i < motionState.length; i++) {
            // Perturb state
            double[] perturbed = motionState.clone();
            perturbed[i] += epsilon;
            
            // Calculate energy difference
            double perturbedEnergy = calculatePotentialEnergy(perturbed);
            
            // Numerical gradient: (U(x+ε) - U(x)) / ε
            gradient[i] = (perturbedEnergy - baseEnergy) / epsilon;
        }
        
        return gradient;
    }
    
    /**
     * Perform Langevin sampling for distribution sampling
     * 
     * @param initialState Initial state
     * @param steps Number of Langevin steps
     * @param dt Time step
     * @param sigma Noise strength
     * @return Sampled state trajectory
     */
    public static double[][] performLangevinSampling(double[] initialState, int steps, double dt, double sigma) {
        if (initialState == null) {
            return new double[0][];
        }
        
        double[][] trajectory = new double[steps + 1][initialState.length];
        double[] currentState = initialState.clone();
        
        trajectory[0] = currentState.clone();
        
        for (int step = 1; step <= steps; step++) {
            double[] gradient = calculateGradient(currentState, 1e-5);
            currentState = langevinStep(currentState, gradient, dt, sigma);
            trajectory[step] = currentState.clone();
        }
        
        return trajectory;
    }
    
    /**
     * Score-based generative modeling step
     * 
     * x_{t+1} = x_t + ε² s_θ(x_t) + ε × N(0,1)
     * 
     * where s_θ(x) = ∇_x log p_θ(x) is the score function
     * 
     * @param currentSample Current sample
     * @param score Score function estimate
     * @param epsilon Noise level
     * @return Next sample
     */
    public static double[] scoreBasedStep(double[] currentSample, double[] score, double epsilon) {
        if (currentSample == null || score == null || currentSample.length != score.length) {
            return new double[0];
        }
        
        double[] nextSample = new double[currentSample.length];
        
        for (int i = 0; i < currentSample.length; i++) {
            // Score-based drift: ε² s_θ(x_t)
            double drift = epsilon * epsilon * score[i];
            
            // Stochastic noise: ε × N(0,1)
            double noise = epsilon * random.nextGaussian();
            
            nextSample[i] = currentSample[i] + drift + noise;
        }
        
        return nextSample;
    }
    
    /**
     * Calculate motion smoothness
     * 
     * @param motionState Motion state
     * @return Smoothness score (higher is smoother)
     */
    public static double calculateSmoothness(double[] motionState) {
        if (motionState == null || motionState.length < 2) {
            return 0.0;
        }
        
        double totalVariation = 0.0;
        for (int i = 1; i < motionState.length; i++) {
            totalVariation += Math.abs(motionState[i] - motionState[i-1]);
        }
        
        // Smoothness = 1 / (1 + total variation)
        return 1.0 / (1.0 + totalVariation);
    }
    
    /**
     * Calculate quantum-inspired potential
     * 
     * U_quantum = φ × sin(2π × x / λ) where λ is characteristic wavelength
     * 
     * @param motionState Motion state
     * @return Quantum potential
     */
    public static double calculateQuantumPotential(double[] motionState) {
        if (motionState == null || motionState.length == 0) {
            return 0.0;
        }
        
        double quantumPotential = 0.0;
        double wavelength = PhiHarmonicGeometry.PHI; // Use phi as characteristic wavelength
        
        for (double value : motionState) {
            quantumPotential += PhiHarmonicGeometry.PHI * Math.sin(2 * Math.PI * value / wavelength);
        }
        
        return quantumPotential / motionState.length;
    }
    
    /**
     * Calculate spoof score based on Langevin analysis
     * 
     * @param trajectory Motion trajectory
     * @param organicTrajectory Reference organic trajectory
     * @return Spoof score (0.0 = organic, 1.0 = spoof)
     */
    public static double calculateSpoofScore(double[][] trajectory, double[][] organicTrajectory) {
        if (trajectory == null || organicTrajectory == null) {
            return 1.0; // Assume spoof if no data
        }
        
        // Calculate energy for both trajectories
        double trajectoryEnergy = 0.0;
        double organicEnergy = 0.0;
        
        for (double[] state : trajectory) {
            trajectoryEnergy += calculatePotentialEnergy(state);
        }
        
        for (double[] state : organicTrajectory) {
            organicEnergy += calculatePotentialEnergy(state);
        }
        
        // Normalize
        trajectoryEnergy /= trajectory.length;
        organicEnergy /= organicTrajectory.length;
        
        // Spoof score based on energy difference
        double energyDifference = Math.abs(trajectoryEnergy - organicEnergy);
        return Math.min(1.0, energyDifference / (organicEnergy + 1e-10));
    }
    
    /**
     * Clear potential energy cache
     */
    public static void clearCache() {
        potentialEnergyCache.clear();
    }
    
    /**
     * Get cache size
     * 
     * @return Number of cached entries
     */
    public static int getCacheSize() {
        return potentialEnergyCache.size();
    }
    
    /**
     * Convert array to cache key
     * 
     * @param array Input array
     * @return Cache key string
     */
    private static String arrayToKey(double[] array) {
        StringBuilder sb = new StringBuilder();
        for (double value : array) {
            sb.append(value).append(",");
        }
        return sb.toString();
    }
    
    /**
     * Print Langevin statistics
     */
    public static void printLangevinStatistics() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  LANGEVIN DYNAMICS STATISTICS                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Default Steps: " + DEFAULT_STEPS);
        System.out.println("Default dt: " + DEFAULT_DT);
        System.out.println("Default Sigma: " + DEFAULT_SIGMA);
        System.out.println("Default Gamma: " + DEFAULT_GAMMA);
        System.out.println("Cache Size: " + getCacheSize());
        System.out.println();
    }
    
    /**
     * Inner class for Langevin state (position and velocity)
     */
    public static class LangevinState {
        public final double[] position;
        public final double[] velocity;
        
        public LangevinState(double[] position, double[] velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        printLangevinStatistics();
        
        // Test Langevin step
        double[] state = {1.0, 2.0, 3.0};
        double[] gradient = {0.5, -0.3, 0.2};
        
        double[] nextState = langevinStep(state, gradient, DEFAULT_DT, DEFAULT_SIGMA);
        System.out.println("Langevin Step Test:");
        System.out.println("  Original: " + java.util.Arrays.toString(state));
        System.out.println("  Next: " + java.util.Arrays.toString(nextState));
        System.out.println();
        
        // Test potential energy
        double[] motionState = {1.0, 1.1, 1.2, 1.3, 1.4};
        double energy = calculatePotentialEnergy(motionState);
        System.out.println("Potential Energy Test:");
        System.out.println("  Motion State: " + java.util.Arrays.toString(motionState));
        System.out.println("  Energy: " + energy);
        System.out.println();
        
        // Test gradient calculation
        double[] calcGradient = calculateGradient(motionState, 1e-5);
        System.out.println("Gradient Test:");
        System.out.println("  Gradient: " + java.util.Arrays.toString(calcGradient));
        System.out.println();
        
        // Test Langevin sampling
        double[][] trajectory = performLangevinSampling(state, 10, DEFAULT_DT, DEFAULT_SIGMA);
        System.out.println("Langevin Sampling Test:");
        System.out.println("  Trajectory length: " + trajectory.length);
        System.out.println("  Final state: " + java.util.Arrays.toString(trajectory[trajectory.length - 1]));
        System.out.println();
        
        // Test smoothness
        double smoothness = calculateSmoothness(motionState);
        System.out.println("Smoothness Test:");
        System.out.println("  Smoothness: " + smoothness);
        System.out.println();
        
        // Test spoof score
        double[][] organicTrajectory = {{1.0, 1.1, 1.2}, {1.1, 1.2, 1.3}, {1.2, 1.3, 1.4}};
        double[][] spoofTrajectory = {{1.0, 1.0, 1.0}, {1.0, 1.0, 1.0}, {1.0, 1.0, 1.0}};
        double spoofScore = calculateSpoofScore(spoofTrajectory, organicTrajectory);
        System.out.println("Spoof Score Test:");
        System.out.println("  Spoof Score: " + spoofScore);
        System.out.println();
    }
}
