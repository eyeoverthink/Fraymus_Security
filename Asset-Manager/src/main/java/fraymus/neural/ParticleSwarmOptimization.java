package fraymus.neural;

import java.util.*;

/**
 * PARTICLE SWARM OPTIMIZATION (PSO)
 * 
 * Applies decentralized, self-organizing rules observed in natural collectives
 * directly to distributed AI architectures. Unlike centralized orchestration models,
 * swarm systems lack a central commander; intelligence is strictly an emergent
 * property of localized agent interactions following simple governing rules.
 * 
 * Architecture:
 * - Multi-dimensional search space optimization
 * - Individual agents evaluate objective function
 * - Velocity and trajectory updates based on collective successes
 * - High-speed adaptive routing without central vulnerability
 * - Federated learning for global intelligence without centralization
 * 
 * Mathematical Model:
 * v[i] = w * v[i] + c1 * r1 * (pBest[i] - x[i]) + c2 * r2 * (gBest - x[i])
 * x[i] = x[i] + v[i]
 * 
 * where:
 * - v: velocity
 * - w: inertia weight
 * - c1, c2: acceleration coefficients
 * - r1, r2: random numbers [0,1]
 * - pBest: personal best
 * - gBest: global best
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class ParticleSwarmOptimization {
    
    // ==========================================
    // SWARM PARAMETERS
    // ==========================================
    private int swarmSize = 50;
    private int dimension = 32;
    private double inertiaWeight = 0.729;
    private double cognitiveCoefficient = 1.49445;  // c1
    private double socialCoefficient = 1.49445;    // c2
    
    // Search space bounds
    private double[] minBounds;
    private double[] maxBounds;
    
    // ==========================================
    // PARTICLE STATE
    // ==========================================
    private static class Particle {
        double[] position;
        double[] velocity;
        double[] personalBest;
        double personalBestFitness;
        
        Particle(int dimension) {
            this.position = new double[dimension];
            this.velocity = new double[dimension];
            this.personalBest = new double[dimension];
            this.personalBestFitness = Double.NEGATIVE_INFINITY;
        }
    }
    
    private final List<Particle> swarm = new ArrayList<>();
    private double[] globalBest;
    private double globalBestFitness = Double.NEGATIVE_INFINITY;
    
    // ==========================================
    // OBJECTIVE FUNCTION
    // ==========================================
    private ObjectiveFunction objectiveFunction;
    
    public interface ObjectiveFunction {
        double evaluate(double[] position);
    }
    
    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public ParticleSwarmOptimization(int dimension) {
        this.dimension = dimension;
        this.minBounds = new double[dimension];
        this.maxBounds = new double[dimension];
        
        // Initialize bounds to [-1, 1]
        Arrays.fill(minBounds, -1.0);
        Arrays.fill(maxBounds, 1.0);
    }
    
    /**
     * Constructor with custom bounds
     */
    public ParticleSwarmOptimization(int dimension, double[] minBounds, double[] maxBounds) {
        this.dimension = dimension;
        this.minBounds = Arrays.copyOf(minBounds, minBounds.length);
        this.maxBounds = Arrays.copyOf(maxBounds, maxBounds.length);
    }
    
    /**
     * Initialize swarm with random positions
     */
    public void initializeSwarm() {
        swarm.clear();
        Random random = new Random();
        
        for (int i = 0; i < swarmSize; i++) {
            Particle p = new Particle(dimension);
            
            // Random position within bounds
            for (int d = 0; d < dimension; d++) {
                p.position[d] = minBounds[d] + random.nextDouble() * (maxBounds[d] - minBounds[d]);
                p.velocity[d] = (random.nextDouble() - 0.5) * (maxBounds[d] - minBounds[d]) * 0.1;
            }
            
            swarm.add(p);
        }
        
        // Initialize global best
        globalBest = new double[dimension];
    }
    
    // ==========================================
    // OPTIMIZATION CYCLE
    // ==========================================
    
    /**
     * Execute one PSO iteration
     * 
     * Process:
     * 1. Evaluate fitness for each particle
     * 2. Update personal bests
     * 3. Update global best
     * 4. Update velocities
     * 5. Update positions
     */
    public void iterate() {
        if (objectiveFunction == null) {
            throw new IllegalStateException("Objective function not set");
        }
        
        Random random = new Random();
        
        // Evaluate fitness and update personal/global bests
        for (Particle p : swarm) {
            double fitness = objectiveFunction.evaluate(p.position);
            
            // Update personal best
            if (fitness > p.personalBestFitness) {
                System.arraycopy(p.position, 0, p.personalBest, 0, dimension);
                p.personalBestFitness = fitness;
            }
            
            // Update global best
            if (fitness > globalBestFitness) {
                System.arraycopy(p.position, 0, globalBest, 0, dimension);
                globalBestFitness = fitness;
            }
        }
        
        // Update velocities and positions
        for (Particle p : swarm) {
            for (int d = 0; d < dimension; d++) {
                // Velocity update: v = w*v + c1*r1*(pBest-x) + c2*r2*(gBest-x)
                double r1 = random.nextDouble();
                double r2 = random.nextDouble();
                
                p.velocity[d] = inertiaWeight * p.velocity[d] +
                               cognitiveCoefficient * r1 * (p.personalBest[d] - p.position[d]) +
                               socialCoefficient * r2 * (globalBest[d] - p.position[d]);
                
                // Clamp velocity
                double maxVelocity = (maxBounds[d] - minBounds[d]) * 0.2;
                p.velocity[d] = Math.max(-maxVelocity, Math.min(maxVelocity, p.velocity[d]));
                
                // Position update: x = x + v
                p.position[d] += p.velocity[d];
                
                // Clamp to bounds
                p.position[d] = Math.max(minBounds[d], Math.min(maxBounds[d], p.position[d]));
            }
        }
    }
    
    /**
     * Run optimization for specified iterations
     */
    public PSOResult optimize(int iterations) {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            iterate();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        return new PSOResult(
            iterations,
            globalBestFitness,
            Arrays.copyOf(globalBest, globalBest.length),
            duration,
            calculateSwarmDiversity()
        );
    }
    
    /**
     * Calculate swarm diversity (average distance from global best)
     */
    private double calculateSwarmDiversity() {
        double totalDistance = 0;
        
        for (Particle p : swarm) {
            double distance = 0;
            for (int d = 0; d < dimension; d++) {
                distance += Math.pow(p.position[d] - globalBest[d], 2);
            }
            totalDistance += Math.sqrt(distance);
        }
        
        return totalDistance / swarm.size();
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public void setObjectiveFunction(ObjectiveFunction function) {
        this.objectiveFunction = function;
    }
    
    public void setSwarmSize(int size) {
        this.swarmSize = size;
    }
    
    public void setInertiaWeight(double weight) {
        this.inertiaWeight = weight;
    }
    
    public void setCognitiveCoefficient(double coefficient) {
        this.cognitiveCoefficient = coefficient;
    }
    
    public void setSocialCoefficient(double coefficient) {
        this.socialCoefficient = coefficient;
    }
    
    public double[] getGlobalBest() {
        return Arrays.copyOf(globalBest, globalBest.length);
    }
    
    public double getGlobalBestFitness() {
        return globalBestFitness;
    }
    
    public List<Particle> getSwarm() {
        return new ArrayList<>(swarm);
    }
    
    /**
     * Get PSO status
     */
    public String getStatus() {
        return String.format(
            "PSO: swarm=%d, dim=%d, gBest=%.4f, diversity=%.4f",
            swarmSize,
            dimension,
            globalBestFitness,
            calculateSwarmDiversity()
        );
    }
    
    // ==========================================
    // RESULT CLASS
    // ==========================================
    
    public static class PSOResult {
        public final int iterations;
        public final double bestFitness;
        public final double[] bestPosition;
        public final long durationMs;
        public final double swarmDiversity;
        
        public PSOResult(int iterations, double bestFitness, double[] bestPosition,
                         long durationMs, double swarmDiversity) {
            this.iterations = iterations;
            this.bestFitness = bestFitness;
            this.bestPosition = bestPosition;
            this.durationMs = durationMs;
            this.swarmDiversity = swarmDiversity;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PSO Result:\n" +
                "  Iterations: %d\n" +
                "  Best Fitness: %.4f\n" +
                "  Best Position: %s\n" +
                "  Duration: %d ms\n" +
                "  Swarm Diversity: %.4f",
                iterations,
                bestFitness,
                Arrays.toString(Arrays.copyOf(bestPosition, Math.min(5, bestPosition.length))),
                durationMs,
                swarmDiversity
            );
        }
    }
}
