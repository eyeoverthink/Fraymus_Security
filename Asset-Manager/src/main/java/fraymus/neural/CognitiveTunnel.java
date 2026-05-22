package fraymus.neural;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * THE COGNITIVE TUNNEL — One Mind, One Stream
 *
 * A thought enters. Everything relevant fires. One result exits.
 * No manual routing. No "which module should I use?"
 * The tunnel IS the mind. The components are neurons, not tools.
 *
 * Input: raw thought (string, signal, or vector)
 * Output: unified cognitive result (understanding + action + confidence)
 *
 * Internally activates: SCM, C-ISA, Attractors, PSO, SAHOO, MAP-Elites,
 * Neuroplatform, Dopamine — all as ONE seamless pass.
 */
public class CognitiveTunnel {

    // ==========================================
    // THE COMPONENTS (neurons, not tools)
    // ==========================================
    private final StructuralCausalModel scm;
    private final CustomInstructionSetArchitecture cisa;
    private final CausalAttractorLandscape landscape;
    private final MAPElitesMutator mapElites;
    private final SAHOOFramework sahoo;
    private final AutonomousCISAOptimizer optimizer;
    private final NeuroplatformInterface neuroplatform;
    private final PhysicalDopamineUncaging dopamine;
    private final ParticleSwarmOptimization pso;
    private final DecentralizedSwarmCoordinator swarm;

    // Tunnel state
    private final int dims;
    private double[] cognitiveState;
    private double confidence;
    private double arousal;       // how intensely the mind is engaged
    private double valence;       // positive/negative emotional tone
    private long thoughtCount;
    private long totalLatencyMs;
    private final Map<String, Double> conceptMemory = new ConcurrentHashMap<>();
    private String lastInsight = "";

    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public CognitiveTunnel(int dims,
                           StructuralCausalModel scm,
                           CustomInstructionSetArchitecture cisa,
                           CausalAttractorLandscape landscape,
                           MAPElitesMutator mapElites,
                           SAHOOFramework sahoo,
                           AutonomousCISAOptimizer optimizer,
                           NeuroplatformInterface neuroplatform,
                           PhysicalDopamineUncaging dopamine,
                           ParticleSwarmOptimization pso,
                           DecentralizedSwarmCoordinator swarm) {
        this.dims = dims;
        this.scm = scm;
        this.cisa = cisa;
        this.landscape = landscape;
        this.mapElites = mapElites;
        this.sahoo = sahoo;
        this.optimizer = optimizer;
        this.neuroplatform = neuroplatform;
        this.dopamine = dopamine;
        this.pso = pso;
        this.swarm = swarm;
        this.cognitiveState = new double[dims];
        this.confidence = 0.5;
        this.arousal = 0.3;
        this.valence = 0.0;
    }

    // ==========================================
    // THE ONE ENTRY POINT: think()
    // ==========================================

    /**
     * One thought enters. The whole mind fires. One result exits.
     */
    public ThoughtResult think(String thought) {
        long t0 = System.nanoTime();
        thoughtCount++;

        // ── PHASE 1: ENCODE ──
        // Convert raw thought into the neural substrate
        double[] signal = encode(thought);

        // ── PHASE 2: CAUSAL UNDERSTANDING ──
        // The SCM fires to understand WHY, not just WHAT
        Map<String, Double> causalContext = buildCausalContext(thought, signal);
        double causalViolation = scm.calculateCausalViolationPenalty(causalContext);
        boolean causallySound = causalViolation < 0.3;

        // ── PHASE 3: INSTRUCTION FLOW ──
        // C-ISA processes the signal — excite, inhibit, superpose
        // This is automatic. Not "which instruction?" — ALL relevant ones fire.
        cisa.setFieldState(signal);
        cisa.executeEXC(signal, computeExcitationStrength(thought));
        cisa.executeINH(noise(dims), computeInhibitionStrength(causalViolation));
        cisa.executeSUP(signal, 0.7);

        // Clamp to prevent runaway activation
        cisa.executeCLP(-1.0, 1.0);
        double[] merged = cisa.getFieldState();

        // ── PHASE 4: ATTRACTOR CONVERGENCE ──
        // Let the thought settle into a natural basin
        // Like how your mind "clicks" when it understands something
        double energy = landscape.calculateEnergy(merged);
        CausalAttractorLandscape.AttractorBasin nearestBasin = landscape.findNearestAttractor(merged);
        String nearestAttractor = nearestBasin != null ? nearestBasin.name : "void";
        double[] settled = landscape.settleIntoAttractor(merged, 10);

        // ── PHASE 5: CONFIDENCE & AROUSAL ──
        // How sure are we? How engaged is the mind?
        confidence = computeConfidence(causalViolation, energy, settled);
        arousal = computeArousal(energy, causalViolation);
        valence = computeValence(confidence, causallySound);

        // ── PHASE 6: REWARD & PLASTICITY ──
        // Good thoughts strengthen pathways. Bad ones weaken.
        dopamine.processReward(confidence);

        // ── PHASE 7: OPTIMIZATION (if confidence is low) ──
        // Mind isn't sure? Search harder. Automatically.
        if (confidence < 0.4) {
            pso.setObjectiveFunction(pos -> -landscape.calculateEnergy(pos));
            ParticleSwarmOptimization.PSOResult psoResult = pso.optimize(5);
            if (psoResult.bestFitness > -energy) {
                settled = psoResult.bestPosition;
                confidence = Math.min(1.0, confidence + 0.15);
            }
        }

        // ── PHASE 8: SAFETY CHECK ──
        // SAHOO runs silently. If something feels wrong, it vetoes.
        // You don't consciously think "is this safe?" — your gut does.
        boolean safe = true;
        if (thoughtCount > 1) {
            double[] prevOutput = cognitiveStateSnapshot();
            SAHOOFramework.SAHOOResult safetyCheck = sahoo.validate(
                lastInsight, thought, prevOutput, settled
            );
            safe = safetyCheck.passed;
            if (!safe) {
                // Pull back toward safety — don't crash, just dampen
                for (int i = 0; i < dims; i++) {
                    settled[i] = settled[i] * 0.5 + cognitiveState[i] * 0.5;
                }
                confidence *= 0.7;
            }
        }

        // ── PHASE 9: UPDATE STATE ──
        // The thought becomes part of who we are
        System.arraycopy(settled, 0, cognitiveState, 0, dims);
        lastInsight = thought;

        // Store concept activation
        String[] words = thought.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 2) {
                conceptMemory.merge(word, confidence, (old, v) -> old * 0.8 + v * 0.2);
            }
        }

        long latencyNs = System.nanoTime() - t0;
        long latencyMs = latencyNs / 1_000_000;
        totalLatencyMs += latencyMs;

        // ── RESULT ──
        // One unified output. Not "SCM says X, C-ISA says Y" — just: here's what I think.
        return new ThoughtResult(
            thought,
            confidence,
            arousal,
            valence,
            nearestAttractor,
            energy,
            causallySound,
            safe,
            latencyMs,
            summarize(thought, confidence, nearestAttractor, causallySound, safe)
        );
    }

    // ==========================================
    // ENCODING: thought → neural signal
    // ==========================================
    private double[] encode(String thought) {
        double[] signal = new double[dims];
        long hash = 0;
        for (int i = 0; i < thought.length(); i++) {
            hash = hash * 31 + thought.charAt(i);
        }
        Random rng = new Random(hash);
        for (int i = 0; i < dims; i++) {
            signal[i] = (rng.nextDouble() - 0.5) * 0.8;
        }
        // Blend with current cognitive state (thoughts build on each other)
        for (int i = 0; i < dims; i++) {
            signal[i] = signal[i] * 0.6 + cognitiveState[i] * 0.4;
        }
        // Boost dimensions for known concepts
        String[] words = thought.toLowerCase().split("\\s+");
        for (String word : words) {
            Double prior = conceptMemory.get(word);
            if (prior != null) {
                int idx = Math.abs(word.hashCode()) % dims;
                signal[idx] += prior * 0.3;
            }
        }
        return signal;
    }

    // ==========================================
    // INTERNAL HELPERS (the unconscious)
    // ==========================================
    private Map<String, Double> buildCausalContext(String thought, double[] signal) {
        Map<String, Double> ctx = new HashMap<>();
        ctx.put("perception", magnitude(signal));
        ctx.put("cognition", confidence);
        ctx.put("action", arousal);
        ctx.put("reward", valence);
        return ctx;
    }

    private double computeExcitationStrength(String thought) {
        // Novel thoughts excite more
        boolean novel = !conceptMemory.containsKey(thought.toLowerCase().split("\\s+")[0]);
        return novel ? 0.8 : 0.5;
    }

    private double computeInhibitionStrength(double causalViolation) {
        // More violation → more inhibition (something's wrong, dampen noise)
        return 0.3 + causalViolation * 0.4;
    }

    private double computeConfidence(double causalViolation, double energy, double[] state) {
        double causalConf = 1.0 - causalViolation;
        double normEnergy = energy / dims;  // normalize so 32-D vectors don't crush confidence
        double energyConf = 1.0 / (1.0 + normEnergy);
        double stateNorm = magnitude(state) / Math.sqrt(dims);  // normalize magnitude
        double coherence = stateNorm > 0.01 ? 1.0 / (1.0 + Math.abs(stateNorm - 0.5)) : 0.1;
        return clamp(causalConf * 0.4 + energyConf * 0.35 + coherence * 0.25, 0.05, 0.99);
    }

    private double computeArousal(double energy, double causalViolation) {
        // High energy or high violation = high arousal (mind is working hard)
        // Normalize energy by dims so 32-D vectors don't instantly max out
        double normEnergy = energy / dims;
        return clamp(normEnergy * 0.4 + causalViolation * 0.3 + 0.15, 0.0, 1.0);
    }

    private double computeValence(double confidence, boolean causallySound) {
        // Confident + causally sound = positive. Uncertain + violated = negative.
        double v = (confidence - 0.4) * 1.5;
        if (!causallySound) v -= 0.15;
        return clamp(v, -1.0, 1.0);
    }

    private double[] noise(int size) {
        double[] n = new double[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) n[i] = (r.nextDouble() - 0.5) * 0.1;
        return n;
    }

    private double magnitude(double[] v) {
        double sum = 0;
        for (double x : v) sum += x * x;
        return Math.sqrt(sum);
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private double[] cognitiveStateSnapshot() {
        // Return a small projection for SAHOO comparison
        double[] snap = new double[3];
        snap[0] = confidence;
        snap[1] = arousal;
        snap[2] = valence;
        return snap;
    }

    private String summarize(String thought, double conf, String attractor,
                             boolean causal, boolean safe) {
        StringBuilder sb = new StringBuilder();
        if (conf > 0.7) {
            sb.append("Clear understanding");
        } else if (conf > 0.4) {
            sb.append("Partial grasp");
        } else {
            sb.append("Uncertain - exploring");
        }
        sb.append(" -> ").append(attractor);
        if (!causal) sb.append(" [causal tension detected]");
        if (!safe) sb.append(" [dampened for safety]");
        return sb.toString();
    }

    // ==========================================
    // ACCESSORS
    // ==========================================
    public double getConfidence() { return confidence; }
    public double getArousal() { return arousal; }
    public double getValence() { return valence; }
    public long getThoughtCount() { return thoughtCount; }
    public String getLastInsight() { return lastInsight; }
    public double[] getCognitiveState() { return Arrays.copyOf(cognitiveState, dims); }
    public int getConceptCount() { return conceptMemory.size(); }

    public String getStatus() {
        return String.format(
            "Cognitive Tunnel: thoughts=%d, confidence=%.2f, arousal=%.2f, valence=%+.2f, concepts=%d, avgLatency=%dms",
            thoughtCount, confidence, arousal, valence, conceptMemory.size(),
            thoughtCount > 0 ? totalLatencyMs / thoughtCount : 0
        );
    }

    // ==========================================
    // RESULT: what a thought produces
    // ==========================================
    public static class ThoughtResult {
        public final String thought;
        public final double confidence;
        public final double arousal;
        public final double valence;
        public final String attractor;
        public final double energy;
        public final boolean causallySound;
        public final boolean safe;
        public final long latencyMs;
        public final String insight;

        public ThoughtResult(String thought, double confidence, double arousal,
                             double valence, String attractor, double energy,
                             boolean causallySound, boolean safe,
                             long latencyMs, String insight) {
            this.thought = thought;
            this.confidence = confidence;
            this.arousal = arousal;
            this.valence = valence;
            this.attractor = attractor;
            this.energy = energy;
            this.causallySound = causallySound;
            this.safe = safe;
            this.latencyMs = latencyMs;
            this.insight = insight;
        }

        @Override
        public String toString() {
            return String.format(
                "%s\n  confidence=%.2f  arousal=%.2f  valence=%+.2f  attractor=%s  energy=%.3f  causal=%s  safe=%s  [%dms]",
                insight, confidence, arousal, valence, attractor, energy, causallySound, safe, latencyMs
            );
        }
    }
}
