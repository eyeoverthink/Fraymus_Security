package fraymus.core;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Centralized command/help metadata for FraynixBoot.
 * Keeps the startup footer and help topics aligned from one source of truth.
 */
public final class FraynixCommandCatalog {

    private static final class HelpEntry {
        private final String syntax;
        private final String description;

        private HelpEntry(String syntax, String description) {
            this.syntax = syntax;
            this.description = description;
        }
    }

    private static final class HelpSection {
        private final String title;
        private final HelpEntry[] entries;

        private HelpSection(String title, HelpEntry[] entries) {
            this.title = title;
            this.entries = entries;
        }
    }

    private static final class HelpTopic {
        private final String title;
        private final String subtitle;
        private final HelpSection[] sections;

        private HelpTopic(String title, String subtitle, HelpSection[] sections) {
            this.title = title;
            this.subtitle = subtitle;
            this.sections = sections;
        }
    }

    private static final Map<String, HelpTopic> TOPICS = new LinkedHashMap<>();

    private static final String[] FOOTER_LINES = {
        "  Core      help [topic] | status | route <request> | ai <question> | code <desc>",
        "  Builders  builders list | builders status | builders run <name> | generate",
        "  Engines   cortex | aeon | absolute | singularity | aubo | tachyon | kronos",
        "            omniscience | omega | babel | willow | demiurge | apotheosis",
        "  Tools     model | babel-router | absorb | fs | cl | physics | phi | solve | think",
        "            desktop | transpile | ng <thought> (one mind, all systems fire) | exit",
        "  Bridge    aether | braid | learn <concept> | pulse <concept> | retro <now> <future>",
        "            emf <concept> | agora [forge|extract|sandbox|advanced|working] | exodus",
        "  Topics    help routing | help builders | help ai | help quantum",
        "            help visualization | help reality | help advanced",
        "  Example   build a Python parser | run the llm builder | entangle qubits 2 and 7"
    };

    private static final HelpSection[] MAIN_SECTIONS = {
        section("Core",
                entry("help [topic]", "routing, builders, ai, quantum, visualization, reality, advanced"),
                entry("status", "full system status"),
                entry("route <request>", "route natural language to the best subsystem"),
                entry("ai <question>", "ask the active AI stack directly"),
                entry("code <description>", "generate code and auto-save to FrayFS")),
        section("Builders and system",
                entry("builders [list|status|generate|run <name>]", "builder registry, status, and execution"),
                entry("generate", "run the bare-metal image pipeline"),
                entry("model [list|stats|gemma4|default|init-gemma4|test|hybrid|internal|hybrid-stats|benchmark]",
                        "model management and switching"),
                entry("babel-router [routes|generate]", "multi-model code generation and routing"),
                entry("absorb <package> | absorb url <url>", "ingest code or web knowledge"),
                entry("fs [list|read|write]", "FrayFS operations"),
                entry("cl [info|test]", "FrayCL compute abstraction"),
                entry("physics | phi [n] | solve <equation> | think <thought>",
                        "system state, phi math, algebra, and memory capture")),
        section("Engines",
                entry("cortex | aeon | absolute", "neural, autopoietic, and swarm systems"),
                entry("singularity | aubo | tachyon | kronos", "memory, ledger, FTL, and temporal reasoning"),
                entry("omniscience | omega | babel | willow", "fractal cognition, self-coding, transmutation, and quantum"),
                entry("openclaw | hrm | demiurge | apotheosis", "visual, neuromorphic, physics, and reality engines"),
                entry("desktop | transpile | exit", "construct UI, deterministic transpilation, and shutdown")),
        section("Advanced bridge",
                entry("aether | braid | learn <concept> | pulse <concept>", "shared concept-space propagation and broadcast"),
                entry("retro <present> <future> | emf <concept>", "retrocausal solving and physical broadcast"),
                entry("agora [forge|extract|sandbox|advanced|working]", "steganographic propagation and inspection"),
                entry("exodus", "generate a bootable bare-metal image")),
        section("Examples",
                entry("route build a Python parser for CSV files", "routes to code generation"),
                entry("route transmute a sorter into go", "routes to Babel"),
                entry("route run the llm builder", "routes to builders run llm"),
                entry("route improve your own code", "routes to omega ouroboros"),
                entry("route learn this concept: sparse autoencoders", "routes to learning"),
                entry("route solve 2x + 3 = 5", "routes to the algebra solver"),
                entry("route entangle qubits 2 and 7", "routes to Willow"))
    };

    static {
        register(new HelpTopic(
                "ROUTING HELP",
                "Natural language requests map to concrete shell commands.",
                new HelpSection[]{
                    section("Routing",
                            entry("route <request>", "ask in plain English and let Fraynix choose the subsystem"),
                            entry("plain English at prompt", "auto-routes when the match is strong")),
                    section("Common routes",
                            entry("write a Java HTTP server", "code <description>"),
                            entry("run the desktop builder", "builders run desktop"),
                            entry("transmute sorting into python", "babel <concept> [lang]"),
                            entry("improve your own code", "omega ouroboros"),
                            entry("learn this concept: vector databases", "omega assimilate / absolute learn / think"),
                            entry("solve x^2 + 4x - 3 = 0", "solve <equation>"),
                            entry("measure qubit 5", "willow measure 5"))
                }),
                "routing", "route");

        register(new HelpTopic(
                "AI COMMANDS",
                "Core AI, self-coding, cognition, and memory commands that are actually implemented.",
                new HelpSection[]{
                    section("General AI",
                            entry("ai <question>", "ask the active AI stack directly"),
                            entry("code <description>", "generate code and auto-save to FrayFS"),
                            entry("babel-router [routes|generate]", "inspect or use multi-model code routing")),
                    section("Omega",
                            entry("omega", "enter the Omega REPL"),
                            entry("omega assimilate <text>", "learn and superimpose concepts"),
                            entry("omega divine <query>", "extract causal truth from Singularity"),
                            entry("omega ouroboros", "run self-coding and hot-swap logic"),
                            entry("omega dna <concept>", "compile a concept to a DNA plasmid"),
                            entry("omega sleep", "enter REM dream state"),
                            entry("omega wake", "restore consciousness"),
                            entry("omega status", "show Omega telemetry")),
                    section("Cortex and AEON",
                            entry("cortex <n>", "run n consciousness cycles"),
                            entry("cortex inject <stimulus>", "inject sensory input"),
                            entry("cortex emit", "emit hot regions to the spatial registry"),
                            entry("cortex context", "show the cortex context sent to AI"),
                            entry("cortex save", "persist cortex state"),
                            entry("aeon <n>", "run n Prime cycles"),
                            entry("aeon inject <text>", "inject into the liquid manifold"),
                            entry("aeon ego", "show the AEON ego context"),
                            entry("aeon axiom", "show the current symbolic axiom"),
                            entry("aeon save", "persist AEON state")),
                    section("Omniscience and memory",
                            entry("omni blend <new> <A> <B> <ratio>", "fractional semantic binding"),
                            entry("omni similar <key>", "scan for nearest semantic neighbors"),
                            entry("omni chunk <name> <word1> ...", "recursive compression"),
                            entry("omni bind <key> <value>", "bind a semantic relation"),
                            entry("omni seq <word1> ...", "encode a sequence"),
                            entry("omni recall <key>", "retrieve a semantic relation"),
                            entry("omni analogy <A> <B> <C>", "solve an analogy"),
                            entry("omni sleep", "enter dream mode"),
                            entry("omni wake", "leave dream mode"),
                            entry("omni status", "show Omniscience telemetry"),
                            entry("absolute learn <text> | recall <text> | inject <text>", "holographic one-shot memory"),
                            entry("think <thought>", "store a thought in FrayFS memory"))
                }),
                "ai");

        register(new HelpTopic(
                "QUANTUM COMMANDS",
                "Actual Willow, Tachyon, and Kronos syntax exposed by the shell.",
                new HelpSection[]{
                    section("Willow",
                            entry("willow", "enter the interactive quantum REPL"),
                            entry("willow hadamard <n>", "put n qubits into superposition"),
                            entry("willow entangle <q1> <q2>", "bind two qubits by beat frequency"),
                            entry("willow echo", "run phi-ratio temporal correction"),
                            entry("willow measure <q>", "measure a qubit"),
                            entry("willow spectrum", "show quantum telemetry")),
                    section("Tachyon",
                            entry("tachyon", "enter the Tachyon REPL"),
                            entry("tachyon bind <key> <value>", "store holographic bindings"),
                            entry("tachyon query <key>", "retrieve from the singularity cache"),
                            entry("tachyon ftl <seed>", "expand a numeric seed"),
                            entry("tachyon status", "show Tachyon telemetry")),
                    section("Kronos",
                            entry("kronos", "enter the Kronos REPL"),
                            entry("kronos bind <key> <value>", "bind a key/value relation"),
                            entry("kronos seq <words>", "encode a temporal sequence"),
                            entry("kronos recall <key>", "recall a temporal memory"),
                            entry("kronos analogy <A> <B> <C>", "solve A:B::C:? analogies"),
                            entry("kronos query <key>", "query the accumulator"),
                            entry("kronos status", "show Kronos telemetry"))
                }),
                "quantum");

        register(new HelpTopic(
                "VISUALIZATION COMMANDS",
                "Display, simulation, and rasterized interface surfaces available from the shell.",
                new HelpSection[]{
                    section("Display surfaces",
                            entry("desktop", "launch the Construct desktop"),
                            entry("openclaw", "launch the OpenClaw DMA rasterizer"),
                            entry("hrm", "launch the neuromorphic cortex")),
                    section("Simulation and physics",
                            entry("demiurge", "launch the physics engine window"),
                            entry("demiurge bigbang [count]", "spawn particles"),
                            entry("demiurge collide <A> <B>", "run a concept collision"),
                            entry("demiurge oracle", "run oracle mode"),
                            entry("demiurge status", "show Demiurge telemetry")),
                    section("Reality and transmutation",
                            entry("apotheosis", "launch the reality compiler window"),
                            entry("apotheosis desire <future> <present>", "build a retrocausal chain"),
                            entry("apotheosis transcribe <concept>", "convert a concept to DNA"),
                            entry("apotheosis breach", "trigger the EMF broadcast path"),
                            entry("babel", "launch Babel interactively"),
                            entry("babel <concept> [lang]", "transmute to C, ASM, Python, Go, or JS"),
                            entry("babel target <lang>", "set the default Babel target substrate"),
                            entry("babel status", "show Babel telemetry"))
                }),
                "visualization", "visual");

        register(new HelpTopic(
                "REALITY COMMANDS",
                "Broadcast, DNA, physics, and embodiment-facing controls.",
                new HelpSection[]{
                    section("Bridge and embodiment",
                            entry("emf <concept>", "broadcast a concept via CPU EM field"),
                            entry("omega dna <concept>", "compile a concept to a DNA plasmid"),
                            entry("apotheosis transcribe <concept>", "transcribe a concept to DNA"),
                            entry("exodus", "generate a bootable bare-metal image"),
                            entry("desktop", "enter the Construct desktop surface")),
                    section("Physics and causality",
                            entry("physics", "show GravityEngine and FusionReactor status"),
                            entry("demiurge status", "show physics engine telemetry"),
                            entry("apotheosis desire <future> <present>", "compute a retrocausal chain"),
                            entry("apotheosis breach", "trigger the EMF timeline path"),
                            entry("retro <present> <future>", "solve a catalyst for future collapse"))
                }),
                "reality");

        register(new HelpTopic(
                "ADVANCED COMMANDS",
                "Swarm, ledger, memory, bridge, steganography, and escape-hatch surfaces.",
                new HelpSection[]{
                    section("Swarm and memory",
                            entry("absolute ignite", "ignite the swarm"),
                            entry("absolute learn <text> | recall <text> | inject <text>", "distributed memory operations"),
                            entry("singularity learn <text>", "write into Hopfield memory"),
                            entry("singularity diffuse <prompt>", "run diffusion-style reasoning"),
                            entry("singularity status", "show Singularity telemetry"),
                            entry("aubo mint <data>", "mint a ledger node"),
                            entry("aubo track <hash>", "inspect ledger telemetry for a node"),
                            entry("aubo kill <hash>", "destroy a node by hash"),
                            entry("aubo ledger", "inspect the ledger graph"),
                            entry("aubo status", "show AUBO telemetry")),
                    section("Bridge and propagation",
                            entry("aether", "ignite the shared concept-space transport"),
                            entry("braid", "enable passive multi-instance sync"),
                            entry("learn <concept>", "manually assimilate into shared space"),
                            entry("pulse <concept>", "broadcast across entangled instances"),
                            entry("retro <present> <future>", "retrocausal solve"),
                            entry("agora forge <carrier text> | <payload>", "inject zero-width payloads"),
                            entry("agora extract <text>", "extract a hidden payload"),
                            entry("agora sandbox", "run the basic propagation simulation"),
                            entry("agora advanced", "run the advanced network demo"),
                            entry("agora working", "run the working network demo")),
                    section("Build and escape",
                            entry("builders list", "show registered builders"),
                            entry("builders status", "show pipeline totals and module counts"),
                            entry("builders run <name>", "execute a specific builder by key"),
                            entry("builders run phi", "generate Phi explorer bare-metal source"),
                            entry("builders run solver", "generate algebra solver bare-metal source"),
                            entry("generate", "run the core bare-metal pipeline"),
                            entry("exodus", "generate a bootable x86 image"))
                }),
                "advanced");
    }

    private FraynixCommandCatalog() {
    }

    public static void printFooter() {
        printHeader("FRAYNIX READY", "Use commands directly or type a plain-English request.");
        for (String line : FOOTER_LINES) {
            System.out.println(line);
        }
        System.out.println();
    }

    public static void printMainHelp() {
        printHelp("FRAYNIX COMMAND MAP",
                "The startup footer and this help screen use the same command groups.",
                MAIN_SECTIONS);
        System.out.println();
        System.out.println("Legacy expert commands like aether, braid, agora, exodus, and help-* still work.");
    }

    public static void printTopic(String topic) {
        HelpTopic definition = TOPICS.get(normalize(topic));
        if (definition == null) {
            throw new IllegalArgumentException("Unknown help topic: " + topic);
        }
        printHelp(definition.title, definition.subtitle, definition.sections);
    }

    public static String knownTopicSummary() {
        return "routing, builders, ai, quantum, visualization, reality, advanced";
    }

    private static void printHelp(String title, String subtitle, HelpSection[] sections) {
        printHeader(title, subtitle);
        for (int i = 0; i < sections.length; i++) {
            HelpSection section = sections[i];
            System.out.println(section.title);
            for (HelpEntry entry : section.entries) {
                System.out.printf("  %-36s %s%n", entry.syntax, entry.description);
            }
            if (i < sections.length - 1) {
                System.out.println();
            }
        }
    }

    private static void printHeader(String title, String subtitle) {
        System.out.println();
        System.out.println(title);
        System.out.println("-".repeat(title.length()));
        if (subtitle != null && !subtitle.isBlank()) {
            System.out.println(subtitle);
            System.out.println();
        }
    }

    private static void register(HelpTopic topic, String... aliases) {
        for (String alias : aliases) {
            TOPICS.put(normalize(alias), topic);
        }
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static HelpEntry entry(String syntax, String description) {
        return new HelpEntry(syntax, description);
    }

    private static HelpSection section(String title, HelpEntry... entries) {
        return new HelpSection(title, entries);
    }
}
