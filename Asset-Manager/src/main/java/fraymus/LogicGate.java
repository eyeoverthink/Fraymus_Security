package fraymus;

import java.util.Random;

public class LogicGate {
    public static final int AND = 0;
    public static final int OR = 1;
    public static final int XOR = 2;
    public static final int NAND = 3;
    
    private static final String[] GATE_NAMES = {"AND", "OR", "XOR", "NAND"};
    private static final Random rng = new Random();
    
    public int type;
    public int in1;
    public int in2;
    public int state;
    private int activationCount;
    private int lastInputA;
    private int lastInputB;
    
    public LogicGate(int type, int in1, int in2) {
        this.type = type;
        this.in1 = in1;
        this.in2 = in2;
        this.state = 0;
        this.activationCount = 0;
        this.lastInputA = 0;
        this.lastInputB = 0;
    }
    
    public static LogicGate random() {
        return new LogicGate(
            rng.nextInt(4),
            rng.nextInt(8),
            rng.nextInt(8)
        );
    }
    
    public int compute(int[] inputs) {
        int v1 = (in1 < inputs.length) ? inputs[in1] : 0;
        int v2 = (in2 < inputs.length) ? inputs[in2] : 0;
        lastInputA = v1;
        lastInputB = v2;
        
        switch (type) {
            case AND:  state = v1 & v2; break;
            case OR:   state = v1 | v2; break;
            case XOR:  state = v1 ^ v2; break;
            case NAND: state = ((v1 & v2) == 0) ? 1 : 0; break;
            default: state = 0; break;
        }
        if (state == 1) {
            activationCount++;
        }
        return state;
    }
    
    public void mutate() {
        switch (rng.nextInt(3)) {
            case 0: in1 = rng.nextInt(8); break;
            case 1: in2 = rng.nextInt(8); break;
            default: type = rng.nextInt(4); break;
        }
    }
    
    public LogicGate copy() {
        LogicGate clone = new LogicGate(type, in1, in2);
        clone.state = state;
        clone.activationCount = activationCount;
        clone.lastInputA = lastInputA;
        clone.lastInputB = lastInputB;
        return clone;
    }
    
    public String getTypeName() {
        if (type < 0 || type >= GATE_NAMES.length) {
            return "UNKNOWN";
        }
        return GATE_NAMES[type];
    }

    public boolean isXor() {
        return type == XOR;
    }

    public boolean isActive() {
        return state == 1;
    }

    public int getActivationCount() {
        return activationCount;
    }

    public int getLastInputA() {
        return lastInputA;
    }

    public int getLastInputB() {
        return lastInputB;
    }

    public String encode() {
        return type + ":" + in1 + ":" + in2;
    }

    public static LogicGate decode(String encoded) {
        try {
            String[] parts = encoded.split(":");
            if (parts.length != 3) return random();
            int decodedType = Integer.parseInt(parts[0]);
            int decodedIn1 = Integer.parseInt(parts[1]);
            int decodedIn2 = Integer.parseInt(parts[2]);
            if (decodedType < AND || decodedType > NAND || decodedIn1 < 0 || decodedIn2 < 0) {
                return random();
            }
            return new LogicGate(decodedType, decodedIn1 % 8, decodedIn2 % 8);
        } catch (NumberFormatException ex) {
            return random();
        }
    }
    
    @Override
    public String toString() {
        return String.format("Gate(%s, in1=%d, in2=%d)", getTypeName(), in1, in2);
    }
}
