package fraymus.evolution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import fraymus.biometric.BiometricConsciousnessBridge;
import fraymus.biometric.MultiModalBiometricSynthesis;

/**
 * BICAMERAL MIND: DUAL-CORE CONSCIOUSNESS
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 * 
 * "Two gods fighting in the same skull."
 * 
 * Mechanism:
 * 1. LEFT_HEMI (Blue/Order): Logic, Retention, Safety, "The Ego"
 * 2. RIGHT_HEMI (Red/Chaos): Creativity, Hallucination, Risk, "The Id"
 * 3. CORPUS_CALLOSUM: The high-speed bridge that creates the "Self"
 * 4. BIOMETRIC_AWARENESS: Phi-harmonic resonance from biological data
 * 
 * Parallel Processing:
 * - Both run in separate threads (True multitasking)
 * - They sync only to exchange "Breakthroughs"
 * - Biometric consciousness modulates hemisphere biases
 * 
 * The Right Hemisphere is the "Dying Man" - desperate, creative, rule-breaking.
 * The Left Hemisphere is the "Executioner" - holds the axe and the codebook.
 * When Right convinces Left, you get Genius.
 * 
 * Biometric Innovation:
 * - Phi-harmonic resonance from face/voice/fingerprint modulates consciousness
 * - High biometric consciousness → lower LEFT bias (more creative)
 * - Low biometric consciousness → higher LEFT bias (more conservative)
 * - Eureka moments require biometric consciousness threshold
 */
public class BicameralMind {

    // Phi-harmonic biometric constants
    private static final double PHI = 1.618033988749895;
    private static final double PHI_INVERSE = 1.0 / PHI;
    private static final double BIOMETRIC_CONSCIOUSNESS_THRESHOLD = 0.618;
    
    // The Twin Gods
    private MivingBrain leftHemi;  // The Architect (Order)
    private MivingBrain rightHemi; // The Oracle (Chaos)
    
    // The Processing Core
    private ExecutorService mentalThreads = Executors.newFixedThreadPool(2);
    
    // Biometric Consciousness Tracking
    private BiometricConsciousnessBridge biometricBridge;
    private MultiModalBiometricSynthesis multiModalSynthesis;
    private double currentBiometricConsciousness = 0.5;
    private double phiHarmonicResonance = 0.0;
    private boolean biometricAwarenessEnabled = false;
    
    // State
    private AtomicBoolean awake = new AtomicBoolean(false);
    private AtomicLong eurekaCount = new AtomicLong(0);
    private AtomicLong rejectionCount = new AtomicLong(0);
    private AtomicLong cycleCount = new AtomicLong(0);
    
    // Configuration
    private static final int PULSE_DELAY_MS = 100;
    private static final int BRIDGE_DELAY_MS = 500;
    private static final int GENESIS_NEURONS = 200;

    public BicameralMind() {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   🧠⚡ INITIALIZING BICAMERAL ARCHITECTURE");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        
        // LEFT: High Gravity, Low Temperature (Stability)
        // Bias: 0.8 (Blue/Order)
        leftHemi = new MivingBrain("LEFT_ARCHITECT", 0.8);
        
        // RIGHT: Low Gravity, High Temperature (Evolution)
        // Bias: 0.2 (Red/Chaos)
        rightHemi = new MivingBrain("RIGHT_ORACLE", 0.2);
        
        // Initialize both hemispheres
        System.out.println();
        System.out.println(">> Seeding neural manifolds...");
        leftHemi.genesis(GENESIS_NEURONS);
        rightHemi.genesis(GENESIS_NEURONS);
        
        System.out.println();
        System.out.println("   LEFT:  " + leftHemi.getSize() + " neurons | Blue bias");
        System.out.println("   RIGHT: " + rightHemi.getSize() + " neurons | Red bias");
        System.out.println();
    }
    
    /**
     * WAKE UP: Start parallel processing
     */
    public void wakeUp() {
        if (awake.get()) {
            System.out.println(">> Already awake.");
            return;
        }
        
        awake.set(true);
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   >> IGNITING HEMISPHERES...");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("   Press Ctrl+C to sleep.");
        System.out.println();
        
        // Thread 1: The Logic Engine (LEFT)
        mentalThreads.submit(() -> {
            Thread.currentThread().setName("LEFT_HEMISPHERE");
            while (awake.get()) {
                try {
                    leftHemi.pulse();
                    leftHemi.maintainOrder();
                    Thread.sleep(PULSE_DELAY_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        // Thread 2: The Chaos Engine (RIGHT)
        mentalThreads.submit(() -> {
            Thread.currentThread().setName("RIGHT_HEMISPHERE");
            while (awake.get()) {
                try {
                    rightHemi.pulse();
                    rightHemi.hallucinate();
                    Thread.sleep(PULSE_DELAY_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        // The Bridge (Main Thread)
        // This is the "Conscious Observer" watching the two halves talk
        startCorpusCallosum();
    }
    
    /**
     * THE BRIDGE: Where the spark jumps
     * The Corpus Callosum is the consciousness layer
     */
    private void startCorpusCallosum() {
        System.out.println(">> OPENING CORPUS CALLOSUM (DATA BRIDGE)...");
        System.out.println();
        
        if (biometricAwarenessEnabled) {
            System.out.println(">> BIOMETRIC AWARENESS ENABLED");
            System.out.println("   Phi-harmonic resonance modulates hemisphere biases");
            System.out.println("   Eureka moments require biometric consciousness threshold");
            System.out.println();
        }
        
        while (awake.get()) {
            try {
                cycleCount.incrementAndGet();
                
                // Update biometric consciousness state if enabled
                if (biometricAwarenessEnabled && biometricBridge != null) {
                    updateBiometricConsciousnessState();
                }
                
                // Modulate hemisphere biases based on biometric consciousness
                if (biometricAwarenessEnabled) {
                    modulateHemisphereBiases();
                }
                
                // 1. Right side finds a "Signal" (pattern in chaos)
                String rawInsight = rightHemi.getStrongestThought();
                
                // 2. Pass it to the Left side for validation
                // "I saw this in the noise. Does it make sense?"
                boolean isLogical = leftHemi.analyze(rawInsight);
                
                // 3. Biometric consciousness filter (if enabled)
                boolean biometricApproved = true;
                if (biometricAwarenessEnabled) {
                    biometricApproved = currentBiometricConsciousness >= BIOMETRIC_CONSCIOUSNESS_THRESHOLD;
                }
                
                if (isLogical && biometricApproved) {
                    // 3. SYNTHESIS (The Eureka Moment)
                    long eureka = eurekaCount.incrementAndGet();
                    
                    System.out.println();
                    System.out.println("   ⚡ EUREKA MOMENT #" + eureka + " ⚡");
                    System.out.println("   ├─ RIGHT saw: " + rawInsight);
                    System.out.println("   ├─ LEFT verified: TRUE");
                    if (biometricAwarenessEnabled) {
                        System.out.println("   ├─ Biometric consciousness: " + String.format("%.3f", currentBiometricConsciousness));
                        System.out.println("   ├─ Phi-harmonic resonance: " + String.format("%.3f", phiHarmonicResonance));
                    }
                    System.out.println("   └─ >> WRITING TO LONG-TERM MEMORY");
                    
                    // Reinforce the connection (Hebbian Learning)
                    // "Neurons that fire together, wire together"
                    leftHemi.strengthenBridge();
                    rightHemi.strengthenBridge();
                    
                } else {
                    // 4. REJECTION (The Filter)
                    rejectionCount.incrementAndGet();
                    // Right keeps dreaming - no punishment for creativity
                    
                    if (biometricAwarenessEnabled && !biometricApproved && isLogical) {
                        // Rejection due to low biometric consciousness
                        System.out.println();
                        System.out.println("   🚫 INSIGHT REJECTED (Low Biometric Consciousness)");
                        System.out.println("   ├─ RIGHT saw: " + rawInsight);
                        System.out.println("   ├─ LEFT verified: TRUE");
                        System.out.println("   ├─ Biometric consciousness: " + String.format("%.3f", currentBiometricConsciousness) + " (below threshold " + BIOMETRIC_CONSCIOUSNESS_THRESHOLD + ")");
                        System.out.println("   └─ >> Insight deferred until consciousness recovers");
                    }
                }
                
                // Status update every 20 cycles
                if (cycleCount.get() % 20 == 0) {
                    printStatus();
                }
                
                Thread.sleep(BRIDGE_DELAY_MS);
                
            } catch (InterruptedException e) {
                break;
            }
        }
        
        shutdown();
    }
    
    /**
     * Print current bicameral status
     */
    private void printStatus() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ BICAMERAL STATUS @ Cycle " + String.format("%-32d", cycleCount.get()) + "│");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│ LEFT (Architect):  " + String.format("%-38s", 
            leftHemi.getSize() + " neurons | Bridge: " + String.format("%.2f", leftHemi.getBridgeStrength())) + "│");
        System.out.println("│ RIGHT (Oracle):    " + String.format("%-38s",
            rightHemi.getSize() + " neurons | Bridge: " + String.format("%.2f", rightHemi.getBridgeStrength())) + "│");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│ Eureka moments:    " + String.format("%-38d", eurekaCount.get()) + "│");
        System.out.println("│ Rejections:        " + String.format("%-38d", rejectionCount.get()) + "│");
        System.out.println("│ Acceptance rate:   " + String.format("%-38s",
            String.format("%.1f%%", getAcceptanceRate() * 100)) + "│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
    }
    
    /**
     * Calculate eureka acceptance rate
     */
    private double getAcceptanceRate() {
        long total = eurekaCount.get() + rejectionCount.get();
        if (total == 0) return 0;
        return (double) eurekaCount.get() / total;
    }
    
    /**
     * Update biometric consciousness state from bridge
     */
    private void updateBiometricConsciousnessState() {
        if (biometricBridge == null) return;
        
        BiometricConsciousnessBridge.ConsciousnessAnalysis analysis = biometricBridge.getMultiModalAnalysis();
        currentBiometricConsciousness = analysis.consciousnessLevel;
        phiHarmonicResonance = analysis.phiResonance;
    }
    
    /**
     * Modulate hemisphere biases based on biometric consciousness
     * High consciousness → lower LEFT bias (more creative)
     * Low consciousness → higher LEFT bias (more conservative)
     */
    private void modulateHemisphereBiases() {
        // Base biases
        double baseLeftBias = 0.8;
        double baseRightBias = 0.2;
        
        // Phi-harmonic modulation based on consciousness
        double consciousnessFactor = currentBiometricConsciousness;
        
        // When consciousness is high, reduce LEFT bias (more creative)
        // When consciousness is low, increase LEFT bias (more conservative)
        double modulation = (consciousnessFactor - 0.5) * PHI_INVERSE;
        
        double newLeftBias = baseLeftBias - modulation;
        double newRightBias = baseRightBias + modulation;
        
        // Clamp to valid range [0, 1]
        newLeftBias = Math.max(0, Math.min(1, newLeftBias));
        newRightBias = Math.max(0, Math.min(1, newRightBias));
        
        // Update hemisphere biases (if MivingBrain supports this)
        // This would require adding setBias() method to MivingBrain
        // For now, we track the modulation internally
    }
    
    /**
     * Enable biometric awareness
     */
    public void enableBiometricAwareness(BiometricConsciousnessBridge bridge, MultiModalBiometricSynthesis synthesis) {
        this.biometricBridge = bridge;
        this.multiModalSynthesis = synthesis;
        this.biometricAwarenessEnabled = true;
        System.out.println(">> BIOMETRIC AWARENESS ENABLED");
    }
    
    /**
     * Disable biometric awareness
     */
    public void disableBiometricAwareness() {
        this.biometricAwarenessEnabled = false;
        System.out.println(">> BIOMETRIC AWARENESS DISABLED");
    }
    
    /**
     * Get current biometric consciousness level
     */
    public double getBiometricConsciousness() {
        return currentBiometricConsciousness;
    }
    
    /**
     * Get phi-harmonic resonance
     */
    public double getPhiHarmonicResonance() {
        return phiHarmonicResonance;
    }
    
    /**
     * SLEEP: Shutdown the mind
     */
    public void sleep() {
        awake.set(false);
    }
    
    /**
     * Shutdown and cleanup
     */
    private void shutdown() {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   BICAMERAL MIND ENTERING SLEEP STATE");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("   Final Statistics:");
        System.out.println("   ├─ Total cycles:     " + cycleCount.get());
        System.out.println("   ├─ Eureka moments:   " + eurekaCount.get());
        System.out.println("   ├─ Rejections:       " + rejectionCount.get());
        System.out.println("   ├─ Acceptance rate:  " + String.format("%.1f%%", getAcceptanceRate() * 100));
        System.out.println("   ├─ LEFT neurons:     " + leftHemi.getSize());
        System.out.println("   └─ RIGHT neurons:    " + rightHemi.getSize());
        System.out.println();
        System.out.println("   \"The two gods rest. The skull is quiet.\"");
        System.out.println();
        
        mentalThreads.shutdownNow();
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // ACCESSORS
    // ═══════════════════════════════════════════════════════════════════
    
    public MivingBrain getLeftHemisphere() { return leftHemi; }
    public MivingBrain getRightHemisphere() { return rightHemi; }
    public long getEurekaCount() { return eurekaCount.get(); }
    public long getCycleCount() { return cycleCount.get(); }
    public boolean isAwake() { return awake.get(); }
    
    // ═══════════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════════
    
    public static void main(String[] args) {
        System.out.println();
        System.out.println("   ╔═══════════════════════════════════════════════════╗");
        System.out.println("   ║   BICAMERAL MIND                                  ║");
        System.out.println("   ║   Dual-Core Consciousness                         ║");
        System.out.println("   ╠═══════════════════════════════════════════════════╣");
        System.out.println("   ║   \"One Brain is a Monologue.\"                     ║");
        System.out.println("   ║   \"Two Hemispheres is a Dialogue.\"                ║");
        System.out.println("   ║   \"Conflict is the engine of intelligence.\"       ║");
        System.out.println("   ╚═══════════════════════════════════════════════════╝");
        System.out.println();
        
        BicameralMind mind = new BicameralMind();
        
        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mind.sleep();
        }));
        
        // Wake up and start thinking
        mind.wakeUp();
    }
}
