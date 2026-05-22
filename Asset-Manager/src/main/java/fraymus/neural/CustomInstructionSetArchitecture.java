package fraymus.neural;

import java.util.*;

/**
 * CUSTOM INSTRUCTION SET ARCHITECTURE (C-ISA)
 * 
 * Maps cognitive intentions directly into rapid vector operations.
 * Provides deterministic, programmatic interface for manipulating continuous fields.
 * 
 * Architecture:
 * - Layer 3 of the field-based compute runtime
 * - Maps high-level cognitive intent to optimized vector math
 * - Deterministic execution with predictable latency
 * - Supports causal verification and autonomous evolution
 * 
 * Core Instructions:
 * - EXC (Excite): Vector addition - amplify field state
 * - INH (Inhibit): Vector subtraction - suppress field state
 * - SUP (Support/Merge): Vector averaging - build consensus
 * - CLP (Clamp): Boundary enforcement - constrain to limits
 * - CAU (Causal Verify): Causal-CoT verification - check causality
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 */
public class CustomInstructionSetArchitecture {
    
    // ==========================================
    // INSTRUCTION ENUMERATION
    // ==========================================
    public enum Instruction {
        EXC,  // Excite: vector addition
        INH,  // Inhibit: vector subtraction
        SUP,  // Support/Merge: vector averaging
        CLP,  // Clamp: boundary enforcement
        CAU   // Causal Verify: causal-CoT verification
    }
    
    // ==========================================
    // STATE VECTORS
    // ==========================================
    private final int vectorDim;
    private double[] fieldState;
    private double[] tempBuffer;
    
    // Causal model for CAU instruction
    private StructuralCausalModel causalModel;
    
    // Instruction statistics
    private final Map<Instruction, Long> instructionCounts = new EnumMap<>(Instruction.class);
    private long totalInstructionsExecuted = 0;
    
    // ==========================================
    // CONSTRUCTION
    // ==========================================
    public CustomInstructionSetArchitecture(int vectorDim) {
        this.vectorDim = vectorDim;
        this.fieldState = new double[vectorDim];
        this.tempBuffer = new double[vectorDim];
        
        // Initialize with fractal DNA
        initializeFractalDNA();
        
        // Initialize instruction counts
        for (Instruction instr : Instruction.values()) {
            instructionCounts.put(instr, 0L);
        }
    }
    
    /**
     * Initialize field state with deterministic fractal patterns
     */
    private void initializeFractalDNA() {
        final double PHI = 1.618033988749895;
        for (int i = 0; i < vectorDim; i++) {
            fieldState[i] = Math.sin(i * PHI) * 0.1;
        }
    }
    
    // ==========================================
    // CORE INSTRUCTIONS
    // ==========================================
    
    /**
     * EXC (Excite): Vector addition
     * Amplifies field state by adding input vector
     * 
     * @param input Input vector to add
     * @param strength Excitation strength (0.0 to 1.0)
     */
    public void executeEXC(double[] input, double strength) {
        if (input.length != vectorDim) {
            throw new IllegalArgumentException("Input dimension mismatch");
        }
        
        for (int i = 0; i < vectorDim; i++) {
            fieldState[i] += input[i] * strength;
        }
        
        instructionCounts.put(Instruction.EXC, instructionCounts.get(Instruction.EXC) + 1);
        totalInstructionsExecuted++;
    }
    
    /**
     * INH (Inhibit): Vector subtraction
     * Suppresses field state by subtracting input vector
     * 
     * @param input Input vector to subtract
     * @param strength Inhibition strength (0.0 to 1.0)
     */
    public void executeINH(double[] input, double strength) {
        if (input.length != vectorDim) {
            throw new IllegalArgumentException("Input dimension mismatch");
        }
        
        for (int i = 0; i < vectorDim; i++) {
            fieldState[i] -= input[i] * strength;
        }
        
        instructionCounts.put(Instruction.INH, instructionCounts.get(Instruction.INH) + 1);
        totalInstructionsExecuted++;
    }
    
    /**
     * SUP (Support/Merge): Vector averaging
     * Builds consensus by averaging field state with input vector
     * 
     * @param input Input vector to merge
     * @param weight Merge weight (0.0 to 1.0)
     */
    public void executeSUP(double[] input, double weight) {
        if (input.length != vectorDim) {
            throw new IllegalArgumentException("Input dimension mismatch");
        }
        
        // Phase 1.5: Integrate Causal-CoT verification
        if (causalModel != null) {
            // Convert vectors to variable map for causal verification
            Map<String, Double> stateMap = vectorToMap(fieldState);
            
            // Execute Causal-CoT
            StructuralCausalModel.CausalCoTResult result = 
                causalModel.executeCausalCoT("merge", stateMap);
            
            // If causal violation is high, reduce merge weight
            if (result.violationPenalty > 0.5) {
                weight *= 0.5;  // Penalize non-causal merges
            }
        }
        
        // Perform weighted average
        for (int i = 0; i < vectorDim; i++) {
            fieldState[i] = fieldState[i] * (1.0 - weight) + input[i] * weight;
        }
        
        instructionCounts.put(Instruction.SUP, instructionCounts.get(Instruction.SUP) + 1);
        totalInstructionsExecuted++;
    }
    
    /**
     * CLP (Clamp): Boundary enforcement
     * Constrains field state to specified bounds
     * 
     * @param min Minimum value
     * @param max Maximum value
     */
    public void executeCLP(double min, double max) {
        for (int i = 0; i < vectorDim; i++) {
            fieldState[i] = Math.max(min, Math.min(max, fieldState[i]));
        }
        
        instructionCounts.put(Instruction.CLP, instructionCounts.get(Instruction.CLP) + 1);
        totalInstructionsExecuted++;
    }
    
    /**
     * CAU (Causal Verify): Causal-CoT verification
     * Verifies causal relationships in current field state
     * 
     * @return Causal verification result
     */
    public StructuralCausalModel.CausalCoTResult executeCAU() {
        if (causalModel == null) {
            throw new IllegalStateException("Causal model not initialized");
        }
        
        Map<String, Double> stateMap = vectorToMap(fieldState);
        StructuralCausalModel.CausalCoTResult result = 
            causalModel.executeCausalCoT("verify", stateMap);
        
        instructionCounts.put(Instruction.CAU, instructionCounts.get(Instruction.CAU) + 1);
        totalInstructionsExecuted++;
        
        return result;
    }
    
    // ==========================================
    // CAUSAL INTEGRATION
    // ==========================================
    
    /**
     * Set causal model for CAU instruction and SUP verification
     */
    public void setCausalModel(StructuralCausalModel model) {
        this.causalModel = model;
    }
    
    /**
     * Convert vector to variable map for causal verification
     */
    private Map<String, Double> vectorToMap(double[] vector) {
        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < Math.min(vector.length, 16); i++) {
            map.put("v" + i, vector[i]);
        }
        return map;
    }
    
    // ==========================================
    // BATCH EXECUTION
    // ==========================================
    
    /**
     * Execute a sequence of instructions
     */
    public void executeBatch(List<InstructionOp> operations) {
        for (InstructionOp op : operations) {
            switch (op.instruction) {
                case EXC -> executeEXC(op.vector, op.strength);
                case INH -> executeINH(op.vector, op.strength);
                case SUP -> executeSUP(op.vector, op.strength);
                case CLP -> executeCLP(op.min, op.max);
                case CAU -> executeCAU();
            }
        }
    }
    
    public static class InstructionOp {
        public final Instruction instruction;
        public final double[] vector;
        public final double strength;
        public final double min;
        public final double max;
        
        public InstructionOp(Instruction instruction, double[] vector, double strength) {
            this.instruction = instruction;
            this.vector = vector;
            this.strength = strength;
            this.min = 0;
            this.max = 0;
        }
        
        public InstructionOp(Instruction instruction, double min, double max) {
            this.instruction = instruction;
            this.vector = null;
            this.strength = 0;
            this.min = min;
            this.max = max;
        }
        
        public InstructionOp(Instruction instruction) {
            this.instruction = instruction;
            this.vector = null;
            this.strength = 0;
            this.min = 0;
            this.max = 0;
        }
    }
    
    // ==========================================
    // ACCESSORS
    // ==========================================
    
    public double[] getFieldState() {
        return Arrays.copyOf(fieldState, fieldState.length);
    }
    
    public void setFieldState(double[] state) {
        if (state.length != vectorDim) {
            throw new IllegalArgumentException("State dimension mismatch");
        }
        System.arraycopy(state, 0, fieldState, 0, vectorDim);
    }
    
    public int getVectorDim() {
        return vectorDim;
    }
    
    public long getTotalInstructionsExecuted() {
        return totalInstructionsExecuted;
    }
    
    public Map<Instruction, Long> getInstructionCounts() {
        return new EnumMap<>(instructionCounts);
    }
    
    /**
     * Get C-ISA status
     */
    public String getStatus() {
        return String.format(
            "C-ISA: dim=%d, instr=%d, EXC=%d, INH=%d, SUP=%d, CLP=%d, CAU=%d",
            vectorDim,
            totalInstructionsExecuted,
            instructionCounts.get(Instruction.EXC),
            instructionCounts.get(Instruction.INH),
            instructionCounts.get(Instruction.SUP),
            instructionCounts.get(Instruction.CLP),
            instructionCounts.get(Instruction.CAU)
        );
    }
}
