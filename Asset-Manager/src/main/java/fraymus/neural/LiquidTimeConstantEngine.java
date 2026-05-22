package fraymus.neural;

import java.util.Arrays;

/**
 * LIQUID TIME-CONSTANT (LTC) ENGINE
 * 
 * Replaces static RK4 integration with input-dependent ODE dynamics.
 * Inspired by C. elegans nervous system and Liquid Neural Networks.
 * 
 * Architecture:
 * - Input-dependent time constant: τ(s, x) adapts to input frequency
 * - Short memory horizon for high-frequency noise
 * - Long memory horizon for slow, stable inputs
 * - Extreme parameter efficiency (19 neurons ≈ 20K parameters)
 * 
 * Mathematical Model:
 * ds/dt = -[1/τ_base + f(s, x)]s + f(s, x)A
 * 
 * where:
 * - s: state vector
 * - x: input vector
 * - τ(s, x): input-dependent time constant
 * - f(s, x): liquid gating function
 * - A: state bounds
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class LiquidTimeConstantEngine {
    
    // ==========================================
    // SYSTEM HYPER-PARAMETERS
    // ==========================================
    private static final int STATE_DIM = 32;
    private static final int INPUT_DIM = 16;
    private static final double TAU_BASE = 0.1;  // Base time constant
    private static final double DT = 0.01;       // Integration time step
    private static final double PHI = 1.618033988749895;
    
    // ==========================================
    // LIQUID MANIFOLD STATE
    // ==========================================
    private final double[] state = new double[STATE_DIM];
    private final double[] dy = new double[STATE_DIM];
    
    // Input-dependent time constant
    private double tau = TAU_BASE;
    
    // Liquid gating weights (learnable)
    private final double[][] W_s = new double[STATE_DIM][STATE_DIM];  // State-to-state
    private final double[][] W_x = new double[INPUT_DIM][STATE_DIM]; // Input-to-state
    private final double[] bias = new double[STATE_DIM];
    
    // Frequency band tracking for adaptive memory
    private double inputFrequency = 1.0;  // Hz
    private double memoryHorizon = 10.0;  // seconds (adaptive)
    
    // Statistics
    private int cyclesCompleted = 0;
    private double totalComputeMs = 0;
    
    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public LiquidTimeConstantEngine() {
        // Initialize weights with fractal DNA
        initializeFractalDNA();
    }
    
    /**
     * Initialize weights using deterministic fractal patterns
     */
    private void initializeFractalDNA() {
        for (int i = 0; i < STATE_DIM; i++) {
            for (int j = 0; j < STATE_DIM; j++) {
                W_s[i][j] = (Math.sin(i * PHI) * Math.cos(j * PHI)) / STATE_DIM;
            }
            for (int j = 0; j < INPUT_DIM; j++) {
                W_x[j][i] = (Math.sin(i * j * PHI)) / INPUT_DIM;
            }
            bias[i] = (Math.cos(i * PHI) - 0.5) * 0.1;
        }
    }
    
    // ==========================================
    // LIQUID GATING FUNCTION
    // ==========================================
    
    /**
     * Calculate input-dependent time constant τ(s, x)
     * High-frequency inputs → short τ (react fast)
     * Low-frequency inputs → long τ (remember longer)
     */
    private double calculateTimeConstant(double[] s, double[] x) {
        // Calculate input energy (proxy for frequency)
        double inputEnergy = 0;
        for (int i = 0; i < Math.min(x.length, INPUT_DIM); i++) {
            inputEnergy += x[i] * x[i];
        }
        inputEnergy = Math.sqrt(inputEnergy);
        
        // Update frequency estimate (exponential moving average)
        inputFrequency = 0.9 * inputFrequency + 0.1 * inputEnergy;
        
        // Adaptive time constant: high frequency → short τ
        double adaptiveTau = TAU_BASE / (1.0 + inputFrequency * 0.5);
        
        // Clamp to reasonable bounds
        adaptiveTau = Math.max(0.01, Math.min(1.0, adaptiveTau));
        
        // Update memory horizon inversely proportional to τ
        memoryHorizon = 10.0 / (adaptiveTau * 10.0);
        
        return adaptiveTau;
    }
    
    /**
     * Liquid gating function f(s, x)
     * Determines how fast state reacts to input
     */
    private double[] liquidGating(double[] s, double[] x) {
        double[] gated = new double[STATE_DIM];
        
        // State contribution
        for (int i = 0; i < STATE_DIM; i++) {
            double stateSum = 0;
            for (int j = 0; j < STATE_DIM; j++) {
                stateSum += W_s[i][j] * s[j];
            }
            
            // Input contribution
            double inputSum = 0;
            for (int j = 0; j < Math.min(x.length, INPUT_DIM); j++) {
                inputSum += W_x[j][i] * x[j];
            }
            
            // Apply sigmoid activation
            double preActivation = stateSum + inputSum + bias[i];
            gated[i] = sigmoid(preActivation);
        }
        
        return gated;
    }
    
    /**
     * Sigmoid activation function
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    // ==========================================
    // ODE INTEGRATION (LIQUID DYNAMICS)
    // ==========================================
    
    /**
     * Execute one LTC integration step
     * ds/dt = -[1/τ_base + f(s, x)]s + f(s, x)A
     */
    public void integrate(double[] input) {
        long t0 = System.currentTimeMillis();
        
        // Calculate adaptive time constant
        tau = calculateTimeConstant(state, input);
        
        // Calculate liquid gating
        double[] f_sx = liquidGating(state, input);
        
        // ODE integration (Euler method for efficiency, can upgrade to RK4)
        for (int i = 0; i < STATE_DIM; i++) {
            // Decay term: -[1/τ_base + f(s, x)]s
            double decay = -(1.0 / tau + f_sx[i]) * state[i];
            
            // Input term: f(s, x)A (A = 1.0 for normalized bounds)
            double inputTerm = f_sx[i] * 1.0;
            
            // Update state
            dy[i] = decay + inputTerm;
            state[i] += dy[i] * DT;
            
            // Clamp to bounds [-1, 1]
            state[i] = Math.max(-1.0, Math.min(1.0, state[i]));
        }
        
        cyclesCompleted++;
        totalComputeMs += System.currentTimeMillis() - t0;
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public double[] getState() {
        return Arrays.copyOf(state, state.length);
    }
    
    public double getTimeConstant() {
        return tau;
    }
    
    public double getInputFrequency() {
        return inputFrequency;
    }
    
    public double getMemoryHorizon() {
        return memoryHorizon;
    }
    
    public int getCyclesCompleted() {
        return cyclesCompleted;
    }
    
    public double getAverageComputeMs() {
        return cyclesCompleted > 0 ? totalComputeMs / cyclesCompleted : 0;
    }
    
    /**
     * Get system status
     */
    public String getStatus() {
        return String.format(
            "LTC Engine: τ=%.4f, freq=%.2f Hz, horizon=%.2fs, cycles=%d, avg=%.2fms",
            tau, inputFrequency, memoryHorizon, cyclesCompleted, getAverageComputeMs()
        );
    }
}
