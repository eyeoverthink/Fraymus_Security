package fraymus.core;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FRAY ORCHESTRATOR
 * A lightweight natural-language router that turns intent into a concrete
 * Fraynix shell command.
 */
public class FrayOrchestrator {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final String[] BUILDER_KEYS = {
        "shell", "desktop", "gpu", "net", "compiler", "vga", "mem", "fs",
        "arcade", "doom", "gameserver", "identity", "llm", "explorer", "omniverse",
        "phi", "solver"
    };
    private static FrayOrchestrator instance;

    public static FrayOrchestrator getInstance() {
        if (instance == null) {
            instance = new FrayOrchestrator();
        }
        return instance;
    }

    public RouteDecision routeIntent(String intent) {
        String original = intent == null ? "" : intent.trim();
        if (original.isEmpty()) {
            return RouteDecision.none(IntentType.UNKNOWN, "No intent provided.");
        }

        String lower = original.toLowerCase(Locale.ROOT);

        if (containsAny(lower, "help", "what can you do", "show commands", "show menu")) {
            return routeHelpIntent(lower);
        }

        if (containsAny(lower, "builder", "builders")) {
            if (containsAny(lower, "run", "launch", "execute")) {
                String builderName = extractBuilderName(lower);
                if (builderName != null) {
                    return new RouteDecision(IntentType.BUILDERS, "builders run " + builderName,
                            "The request targets a specific builder entrypoint.", 0.96);
                }
            }
            if (containsAny(lower, "list builder", "builder list", "show builder", "builder status", "builders status")) {
                String command = containsAny(lower, "status", "pipeline") ? "builders status" : "builders list";
                return new RouteDecision(IntentType.BUILDERS, command, "The request is about the builder layer.", 0.95);
            }
            // "create a new builder", "build me a builder" etc. → AI handles it
        }

        if (containsAny(lower, "status", "system health", "telemetry", "how are you doing")) {
            return new RouteDecision(IntentType.STATUS, "status", "The request asks for system state or telemetry.", 0.88);
        }

        // Check for creative/build requests BEFORE simple phi keyword matching
        if (containsAny(lower, "breakthrough", "novel", "innovative", "nobel", "create", "propose", "design", "invent")
                || (containsAny(lower, "algorithm", "method", "framework") && containsAny(lower, "new", "novel", "breakthrough"))) {
            // Route to CODE_GENERATION for creative generation, not static handlers
            return new RouteDecision(IntentType.CODE_GENERATION, "creative",
                    "The request asks for novel creative work or algorithm design.", 0.95);
        }

        if (containsAny(lower, "golden ratio", "phi explorer", "phi harmonic", "phi-harmonic")
                || (lower.contains("phi") && containsAny(lower, "explore", "constant", "fibonacci", "resonance"))) {
            return new RouteDecision(IntentType.MATHEMATICS, "phi",
                    "The request asks for Phi constants, ratios, or resonance exploration.", 0.88);
        }

        if (looksLikeEquation(lower)
                || containsAny(lower, "linear equation", "quadratic equation", "algebra solver", "solve equation")) {
            return new RouteDecision(IntentType.MATHEMATICS, "solve " + original,
                    "The request maps to the algebra solver surface.", 0.89);
        }

        if (containsAny(lower, "bare metal", "bootable image", "os image", "generate image", "build the os")) {
            return new RouteDecision(IntentType.BUILDERS, "generate", "The request maps to the bare-metal builder pipeline.", 0.90);
        }

        if (containsAny(lower, "transmute", "translate to", "convert to", "port to", "babel")) {
            return routeBabelIntent(original, lower);
        }

        if (containsAny(lower, "quantum", "qubit", "entangle", "superposition", "hadamard", "measure", "resonance", "willow")) {
            return routeWillowIntent(original, lower);
        }

        if (containsAny(lower, "self improve", "self-improve", "improve your own code", "rewrite yourself",
                "self code", "self-code", "self coding", "self-coding", "mutate yourself", "evolve yourself",
                "self improvement", "self-improvement", "ouroboros")) {
            return new RouteDecision(IntentType.SELF_MODIFICATION, "omega ouroboros",
                    "The request is asking for self-modification or recursive improvement.", 0.97);
        }

        if (containsAny(lower, "remember", "memorize", "store this", "save this thought", "note this thought")) {
            return new RouteDecision(IntentType.MEMORY, "think " + stripLeadingCue(original),
                    "The request is about storing a thought in memory.", 0.87);
        }

        if (containsAny(lower, "learn", "assimilate", "teach yourself", "one-shot", "holographic memory")) {
            return routeLearningIntent(original, lower);
        }

        // Only route to code gen for EXPLICIT code requests — "write code", "generate code", "codegen"
        // Vague "build me X", "create X" → AI decides what to do
        if (containsAny(lower, "generate code", "write code", "codegen", "write a function",
                "write a class", "write a script", "refactor", "debug", "fix this code")) {
            return new RouteDecision(IntentType.CODE_GENERATION, "code " + original,
                    "The request explicitly asks for code generation.", 0.84);
        }

        // Everything else → AI. The LLM is the brain. Let it think.
        return new RouteDecision(IntentType.GENERAL_AI, "ai " + original,
                "Route to the AI layer — let the LLM decide what to do.", 0.70);
    }

    public void manifestIntent(String intent) {
        RouteDecision decision = routeIntent(intent);
        System.out.println("ORCHESTRATOR PROCESSING...");
        System.out.println("   Intent: \"" + intent + "\"");
        System.out.println("   Type: " + decision.getType());
        System.out.println("   Route: " + decision.getCommand());
        System.out.println("   Why: " + decision.getReason());
        System.out.println("   Confidence: " + decision.getConfidence());
    }

    private RouteDecision routeBabelIntent(String original, String lower) {
        String language = extractLanguage(lower);
        String command = "babel " + original + (language != null ? " " + language : "");
        return new RouteDecision(IntentType.TRANSMUTATION, command,
                "The request sounds like substrate translation or polyglot code generation.", 0.93);
    }

    private RouteDecision routeHelpIntent(String lower) {
        if (containsAny(lower, "builder", "builders")) {
            return new RouteDecision(IntentType.HELP, "help builders",
                    "The request asks for builder-specific command guidance.", 0.99);
        }
        if (containsAny(lower, "quantum", "qubit", "willow", "tachyon", "kronos")) {
            return new RouteDecision(IntentType.HELP, "help quantum",
                    "The request asks for quantum or temporal command guidance.", 0.99);
        }
        if (containsAny(lower, "ai", "omega", "cortex", "aeon", "omniscience", "absolute", "memory")) {
            return new RouteDecision(IntentType.HELP, "help ai",
                    "The request asks for AI, cognition, or memory command guidance.", 0.99);
        }
        if (containsAny(lower, "visual", "visualization", "desktop", "openclaw", "hrm", "demiurge", "babel")) {
            return new RouteDecision(IntentType.HELP, "help visualization",
                    "The request asks for visualization or simulation command guidance.", 0.99);
        }
        if (containsAny(lower, "reality", "physics", "emf", "retro", "dna", "apotheosis", "exodus")) {
            return new RouteDecision(IntentType.HELP, "help reality",
                    "The request asks for reality, physics, or embodiment command guidance.", 0.99);
        }
        if (containsAny(lower, "advanced", "aether", "braid", "agora", "singularity", "aubo", "swarm")) {
            return new RouteDecision(IntentType.HELP, "help advanced",
                    "The request asks for advanced bridge, swarm, or ledger command guidance.", 0.99);
        }
        if (containsAny(lower, "route", "routing")) {
            return new RouteDecision(IntentType.HELP, "help routing",
                    "The request asks for natural-language routing guidance.", 0.99);
        }
        return new RouteDecision(IntentType.HELP, "help",
                "The request asks for command guidance.", 0.98);
    }

    private RouteDecision routeWillowIntent(String original, String lower) {
        int[] numbers = extractNumbers(lower);
        if (lower.contains("entangle") && numbers.length >= 2) {
            return new RouteDecision(IntentType.QUANTUM, "willow entangle " + numbers[0] + " " + numbers[1],
                    "The request explicitly asks for entanglement.", 0.95);
        }
        if (lower.contains("measure") && numbers.length >= 1) {
            return new RouteDecision(IntentType.QUANTUM, "willow measure " + numbers[0],
                    "The request explicitly asks for a qubit measurement.", 0.94);
        }
        if ((lower.contains("hadamard") || lower.contains("superposition")) && numbers.length >= 1) {
            return new RouteDecision(IntentType.QUANTUM, "willow hadamard " + numbers[0],
                    "The request is asking for superposition via Hadamard.", 0.94);
        }
        if (lower.contains("echo")) {
            return new RouteDecision(IntentType.QUANTUM, "willow echo",
                    "The request maps to Willow echo correction.", 0.91);
        }
        if (containsAny(lower, "spectrum", "telemetry")) {
            return new RouteDecision(IntentType.QUANTUM, "willow spectrum",
                    "The request is asking for Willow telemetry.", 0.88);
        }
        return new RouteDecision(IntentType.QUANTUM, "willow",
                "The request is quantum-themed but not specific enough for a subcommand.", 0.72);
    }

    private RouteDecision routeLearningIntent(String original, String lower) {
        String payload = stripLeadingCue(original);
        if (containsAny(lower, "one-shot", "holographic", "absolute")) {
            return new RouteDecision(IntentType.LEARNING, "absolute learn " + payload,
                    "The request sounds like holographic or one-shot learning.", 0.90);
        }
        return new RouteDecision(IntentType.LEARNING, "omega assimilate " + payload,
                "The request is best served by Omega concept assimilation.", 0.89);
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String stripLeadingCue(String text) {
        String trimmed = text.trim();
        String[] cues = {
            "learn this concept:", "learn this concept", "learn about", "learn",
            "remember this:", "remember", "memorize", "store this thought:", "store this", "save this thought:",
            "assimilate", "teach yourself"
        };

        String lower = trimmed.toLowerCase(Locale.ROOT);
        for (String cue : cues) {
            if (lower.startsWith(cue)) {
                return trimmed.substring(cue.length()).trim();
            }
        }
        return trimmed;
    }

    private String extractLanguage(String lower) {
        Map<String, String> languages = new LinkedHashMap<>();
        languages.put("python", "PYTHON");
        languages.put("javascript", "JS");
        languages.put(" js", "JS");
        languages.put("golang", "GO");
        languages.put(" go", "GO");
        languages.put("assembly", "ASM");
        languages.put(" asm", "ASM");
        languages.put(" c ", "C");

        for (Map.Entry<String, String> entry : languages.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private int[] extractNumbers(String text) {
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        int[] values = new int[4];
        int count = 0;
        while (matcher.find() && count < values.length) {
            values[count++] = Integer.parseInt(matcher.group());
        }
        int[] result = new int[count];
        System.arraycopy(values, 0, result, 0, count);
        return result;
    }

    private String extractBuilderName(String lower) {
        if (lower.contains("phi explorer") || lower.contains("phi builder")) {
            return "phi";
        }
        if (lower.contains("algebra solver") || lower.contains("solver builder")) {
            return "solver";
        }
        for (String builderKey : BUILDER_KEYS) {
            if (lower.contains(builderKey)) {
                return builderKey;
            }
        }
        return null;
    }

    private boolean looksLikeEquation(String lower) {
        return lower.contains("=")
                && lower.contains("x")
                && containsAny(lower, "solve", "equation", "quadratic", "linear", "algebra");
    }

    public enum IntentType {
        HELP,
        STATUS,
        BUILDERS,
        CODE_GENERATION,
        TRANSMUTATION,
        QUANTUM,
        SELF_MODIFICATION,
        LEARNING,
        MEMORY,
        MATHEMATICS,
        GENERAL_AI,
        UNKNOWN
    }

    public static final class RouteDecision {
        private final IntentType type;
        private final String command;
        private final String reason;
        private final double confidence;

        private RouteDecision(IntentType type, String command, String reason, double confidence) {
            this.type = type;
            this.command = command;
            this.reason = reason;
            this.confidence = confidence;
        }

        public static RouteDecision none(IntentType type, String reason) {
            return new RouteDecision(type, "", reason, 0.0);
        }

        public IntentType getType() {
            return type;
        }

        public String getCommand() {
            return command;
        }

        public String getReason() {
            return reason;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isRunnable() {
            return command != null && !command.isBlank();
        }
    }

    public static void main(String[] args) {
        FrayOrchestrator orchestrator = FrayOrchestrator.getInstance();
        orchestrator.manifestIntent("build a python parser");
        System.out.println();
        orchestrator.manifestIntent("improve your own code");
        System.out.println();
        orchestrator.manifestIntent("entangle qubits 2 and 7");
    }
}
