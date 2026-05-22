package fraymus;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import fraymus.ui.SelfCodePanel;
import fraymus.evolution.MivingBrainVisualizer;
import fraymus.evolution.MivingBrain;
import fraymus.network.OmniCaster;
import fraymus.CoreDump;
import fraymus.physics.ChronosBreach;
import fraymus.reality.RetroCausal;
import fraymus.security.ZenoGuard;
import fraymus.quantum.EntangledPair;
import fraymus.quantum.EntanglementNetwork;
import fraymus.quantum.SchrodingerFile;
import fraymus.chaos.EvolutionaryChaos;
import fraymus.organism.NEXUS_Organism;
import fraymus.genesis.IdeaCollider;
import fraymus.genesis.RealityForge;
import fraymus.core.SpatialRegistry;
import fraymus.core.SpatialNode;
import fraymus.core.PhiSuit;
import fraymus.core.GravityEngine;
import fraymus.core.FusionReactor;
import fraymus.swarm.Swarm;
import fraymus.swarm.Node;
import fraymus.swarm.Block;
// Ultimate Agent Integration
import fraymus.ultimate.*;
// Command System
import fraymus.commands.CommandRegistry;
import fraymus.commands.CommandInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandTerminal {

    private static final int MAX_OUTPUT_LINES = 500;
    private static final ImString inputBuffer = new ImString(256);
    private static final List<TerminalLine> outputLines = new ArrayList<>();
    private static boolean scrollToBottom = true;
    private static boolean focusInput = true;
    private static final List<String> commandHistory = new ArrayList<>();
    private static int historyIndex = -1;

    private static ExperimentManager experimentManager;
    
    // Ultimate Agent Integration
    private static UltimateCPU ultimateCPU;
    private static UnifiedStateLattice ultimateLattice;
    private static UnifiedSynapse ultimateSynapse;
    private static CorpusCallosumUltimate ultimateCallosum;
    private static AgencyKAI agencyKAI;
    private static AgencyVEX agencyVEX;
    private static AgencyAUM agencyAUM;
    private static AgencyNEX agencyNEX;
    private static AgencyCOR agencyCOR;
    private static UltimateCommandSystem ultimateCommandSystem;
    private static OllamaBridge ollamaBridge;
    private static MultiModelOrchestrator multiModelOrchestrator;
    private static HiveMindConcierge hiveMindConcierge;
    private static boolean ultimateAgentInitialized = false;

    public static class TerminalLine {
        public final String text;
        public final float r, g, b;

        public TerminalLine(String text, float r, float g, float b) {
            this.text = text;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public static void init(ExperimentManager mgr) {
        experimentManager = mgr;
        printBanner();
        initUltimateAgent();
        // Initialize modular command system
        CommandInitializer.initialize(mgr);
    }
    
    private static void initUltimateAgent() {
        if (ultimateAgentInitialized) return;
        
        printInfo("Initializing Ultimate Agent System...");
        
        try {
            ultimateCPU = new UltimateCPU();
            ultimateLattice = new UnifiedStateLattice();
            ultimateSynapse = new UnifiedSynapse();
            ultimateCallosum = new CorpusCallosumUltimate(ultimateSynapse, ultimateLattice);
            
            agencyKAI = new AgencyKAI(ultimateSynapse, ultimateLattice, ultimateCallosum, ultimateCPU);
            agencyVEX = new AgencyVEX(ultimateSynapse, ultimateLattice, ultimateCallosum, ultimateCPU);
            agencyAUM = new AgencyAUM(ultimateSynapse, ultimateLattice, ultimateCallosum, ultimateCPU);
            agencyNEX = new AgencyNEX(ultimateSynapse, ultimateLattice, ultimateCallosum, ultimateCPU);
            agencyCOR = new AgencyCOR(ultimateSynapse, ultimateLattice, ultimateCallosum, ultimateCPU);
            
            ultimateCommandSystem = new UltimateCommandSystem(agencyKAI, agencyVEX, agencyAUM, agencyNEX, agencyCOR, ultimateSynapse, ultimateCallosum);
            
            ollamaBridge = new OllamaBridge();
            multiModelOrchestrator = new MultiModelOrchestrator(ultimateSynapse, ultimateLattice, agencyKAI, ollamaBridge);
            
            // Initialize HiveMind Concierge for colony intelligence
            hiveMindConcierge = new HiveMindConcierge(
                ultimateSynapse, ultimateLattice,
                agencyKAI, agencyVEX, agencyAUM, agencyNEX, agencyCOR,
                ultimateCommandSystem, multiModelOrchestrator, ollamaBridge
            );
            
            ultimateAgentInitialized = true;
            printSuccess("Ultimate Agent initialized successfully");
            printInfo("  5 Agencies: KAI, VEX, AUM, NEX, COR");
            printInfo("  LLM Integration: gemma4, deepseek-r1, llama3, llava");
            printInfo("  Type 'ultimate help' for commands");
        } catch (Exception e) {
            printError("Failed to initialize Ultimate Agent: " + e.getMessage());
        }
    }

    private static void printBanner() {
        printColored("========================================================", 0.4f, 1.0f, 0.8f);
        printColored("  FRAYMUS ENGINE V2 - LIVING INTELLIGENCE TERMINAL", 1.0f, 0.84f, 0.0f);
        printColored("  Type 'help' for available commands", 0.6f, 0.6f, 0.6f);
        printColored("========================================================", 0.4f, 1.0f, 0.8f);
        print("");
    }

    public static void print(String text) {
        printColored(text, 0.8f, 0.8f, 0.8f);
    }

    public static void printColored(String text, float r, float g, float b) {
        synchronized (outputLines) {
            outputLines.add(new TerminalLine(text, r, g, b));
            if (outputLines.size() > MAX_OUTPUT_LINES) {
                outputLines.remove(0);
            }
        }
        scrollToBottom = true;
    }

    public static void printSuccess(String text) {
        printColored(text, 0.3f, 1.0f, 0.3f);
    }

    public static void printError(String text) {
        printColored(text, 1.0f, 0.3f, 0.3f);
    }

    public static void printInfo(String text) {
        printColored(text, 0.5f, 0.8f, 1.0f);
    }

    public static void printWarning(String text) {
        printColored(text, 1.0f, 0.7f, 0.2f);
    }

    public static void printHighlight(String text) {
        printColored(text, 1.0f, 0.84f, 0.0f);
    }

    public static void render() {
        ImGui.setNextWindowPos(0, 580, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(940, 140, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Terminal", ImGuiWindowFlags.NoScrollbar)) {
            float footerHeight = ImGui.getFrameHeightWithSpacing() + 4;
            ImGui.beginChild("TermOutput", 0, -footerHeight, false,
                    ImGuiWindowFlags.HorizontalScrollbar);

            // Create snapshot to avoid ConcurrentModificationException
            List<TerminalLine> snapshot;
            synchronized (outputLines) {
                snapshot = new ArrayList<>(outputLines);
            }
            
            for (TerminalLine line : snapshot) {
                ImGui.textColored(line.r, line.g, line.b, 1.0f, line.text);
            }

            if (scrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                scrollToBottom = false;
            }
            ImGui.endChild();

            ImGui.separator();

            ImGui.textColored(0.4f, 1.0f, 0.8f, 1.0f, ">");
            ImGui.sameLine();

            ImGui.pushItemWidth(-1);
            int flags = ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.CallbackHistory;
            if (focusInput) {
                ImGui.setKeyboardFocusHere();
                focusInput = false;
            }
            if (ImGui.inputText("##terminput", inputBuffer, flags)) {
                String cmd = inputBuffer.get().trim();
                if (!cmd.isEmpty()) {
                    printColored("> " + cmd, 0.4f, 1.0f, 0.8f);
                    commandHistory.add(cmd);
                    historyIndex = commandHistory.size();
                    executeCommand(cmd);
                }
                inputBuffer.set("");
                focusInput = true;
            }
            ImGui.popItemWidth();
        }
        ImGui.end();
    }

    private static void executeCommand(String cmd) {
        String[] parts = cmd.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        // First check the modular command registry
        CommandRegistry registry = CommandRegistry.getInstance();
        if (registry.hasCommand(command)) {
            boolean success = registry.execute(command, args);
            if (!success) {
                printError("Command failed: " + command);
            }
            return;
        }

        // Fall back to legacy switch statement for non-migrated commands
        switch (command) {
            case "prime":
                handlePrime(args);
                break;
            case "factor":
                handleFactor(args);
                break;
            case "hash":
                handleHash(args);
                break;
            case "crack":
                handleCrack(args);
                break;
            case "tunnel":
                handleTunnel(args);
                break;
            case "boost":
                handleBoost(args);
                break;
            case "kill":
                handleKill(args);
                break;
            case "mutate":
                handleMutate(args);
                break;
            case "evolve":
                handleEvolve(args);
                break;
            case "mongo":
                handleMongo(args);
                break;
            case "ethics":
                handleEthics(args);
                break;
            case "codegen":
                handleCodegen(args);
                break;
            case "rsa":
                handleRsa(args);
                break;
            case "identity":
                handleIdentity(args);
                break;
            case "physics":
                handlePhysics(args);
                break;
            case "nodes":
                showNodes();
                break;
            case "colony":
                showColony();
                break;
            case "learn":
                handleLearn(args);
                break;
            case "memory":
                handleMemory(args);
                break;
            case "genome":
                handleGenome(args);
                break;
            case "qrcode":
                handleQRCode(args);
                break;
            case "scrape":
                handleScrape(args);
                break;
            case "ollama":
                handleOllama(args);
                break;
            case "genesis":
                handleGenesis(args);
                break;
            case "fragment":
                handleFragment(args);
                break;
            case "porh":
                handlePoRH(args);
                break;
            case "layers":
                handleLayers(args);
                break;
            case "heal":
                handleHeal(args);
                break;
            case "morse":
                handleMorse(args);
                break;
            case "diag":
                handleDiag(args);
                break;
            case "insights":
                handleInsights(args);
                break;
            case "bardo":
                handleBardo(args);
                break;
            case "feedback":
                handleFeedback(args);
                break;
            case "mrl":
                handleMRL(args);
                break;
            case "agi":
                handleAGI(args);
                break;
            case "quantum":
                handleQuantum(args);
                break;
            case "sovereign":
                handleSovereign(args);
                break;
            case "chaos":
                handleChaos(args);
                break;
            case "adversarial":
                handleAdversarial(args);
                break;
            case "battle":
                handleBattle(args);
                break;
            case "fqf":
                handleFQF(args);
                break;
            case "session":
                handleSession(args);
                break;
            case "ultimate":
                handleUltimate(args);
                break;
            case "hive":
                handleHive(args);
                break;
            case "brain":
                handleBrain(args);
                break;
            case "bio":
                handleBio(args);
                break;
            case "glyph":
                handleGlyph(args);
                break;
            case "freq":
                handleFreq(args);
                break;
            case "market":
                handleMarket(args);
                break;
            case "knowledge":
                handleKnowledge(args);
                break;
            case "lattice":
                handleLattice(args);
                break;
            case "economy":
                handleEconomy(args);
                break;
            case "entrain":
                handleEntrain(args);
                break;
            case "font":
                handleFont(args);
                break;
            case "code":
            case "selfcode":
                handleSelfCode(args);
                break;
            case "miving":
            case "priecled":
            case "manifold":
                handleMivingBrain(args);
                break;
            case "omni":
            case "cast":
            case "breach":
                handleOmniCaster(args);
                break;
            case "dump":
            case "coredump":
                handleCoreDump(args);
                break;
            case "chronos":
            case "timing":
                handleChronos(args);
                break;
            case "retro":
            case "causal":
                handleRetroCausal(args);
                break;
            case "zeno":
            case "guard":
                handleZenoGuard(args);
                break;
            case "entangle":
            case "spooky":
            case "pair":
                handleEntanglement(args);
                break;
            case "schrodinger":
            case "qbox":
            case "deniable":
            case "qcex":
                handleSchrodinger(args);
                break;
            case "echaos":
            case "living":
            case "fractalrng":
                handleEvolutionaryChaos(args);
                break;
            case "nexus":
            case "organism":
            case "awaken":
                handleNexus(args);
                break;
            case "collide":
            case "smash":
            case "fuse":
                handleCollider(args);
                break;
            case "forge":
            case "manifest":
            case "create":
                handleForge(args);
                break;
            case "spatial":
            case "universe":
            case "hypercube":
                handleSpatial(args);
                break;
            case "gravity":
            case "hebbian":
                handleGravity(args);
                break;
            case "fusion":
            case "reactor":
            case "bigbang":
                handleFusion(args);
                break;
            case "suit":
            case "wrap":
                handleSuit(args);
                break;
            case "swarm":
            case "collective":
                handleSwarm(args);
                break;
            case "cortex":
            case "dreamscape":
            case "visual":
                handleCortex(args);
                break;
            case "absorb":
            case "blackhole":
                CommandTerminalAdvanced.handleAbsorb(args);
                break;
            case "lazarus":
            case "genetic":
                CommandTerminalAdvanced.handleLazarus(args);
                break;
            case "security":
            case "scramble":
            case "deadman":
                CommandTerminalAdvanced.handleSecurity(args);
                break;
            case "hydra":
            case "shard":
                CommandTerminalAdvanced.handleHydra(args);
                break;
            case "akashic":
            case "record":
                CommandTerminalAdvanced.handleAkashic(args);
                break;
            case "clear":
                outputLines.clear();
                printBanner();
                break;
            default:
                printError("Unknown command: " + command);
                print("Type 'help' for available commands.");
                break;
        }
    }

    private static void showHelp() {
        printHighlight("=== FRAYMUS TERMINAL COMMANDS ===");
        print("");
        printColored("--- EXPLORATION ---", 0.5f, 0.8f, 1.0f);
        print("  status              Show world status");
        print("  nodes               List all living entities");
        print("  colony              Show colony intelligence report");
        print("");
        printColored("--- QUANTUM EXPERIMENTS ---", 0.5f, 0.8f, 1.0f);
        print("  prime <number>      Test if number is prime");
        print("  factor <number>     Factor a number using quantum tunneling");
        print("  tunnel <bits>       Generate and factor a random semiprime");
        print("  rsa <bits>          Run RSA Blue/Red team challenge");
        print("  identity <name>     Challenge a PhiNode's cloaked identity");
        print("");
        printColored("--- HASH EXPERIMENTS ---", 0.5f, 0.8f, 1.0f);
        print("  hash <text>         Compute phi-harmonic hash of text");
        print("  crack <hash>        Attempt to reverse-engineer a hash");
        print("");
        printColored("--- ENTITY CONTROL ---", 0.5f, 0.8f, 1.0f);
        print("  spawn <name>        Spawn a new PhiNode entity");
        print("  boost <name>        Give energy boost to entity");
        print("  kill <name>         Remove an entity");
        print("  mutate <name>       Trigger mutation trial on entity");
        print("");
        printColored("--- CODE EVOLUTION ---", 0.5f, 0.8f, 1.0f);
        print("  evolve              Force arena evolution cycle");
        print("  arena               Show concept arena status");
        print("  codegen             Trigger code generation cycle");
        print("");
        printColored("--- NEURAL / LEARNING ---", 0.5f, 0.8f, 1.0f);
        print("  ask <question>      Query the phi neural network");
        print("  learn [force]       Show passive learner status / force integration");
        print("  memory              Show infinite memory status");
        print("  memory search <q>   Search memory records");
        print("  memory save         Force save to disk");
        print("");
        printColored("--- GENOME / DNA ---", 0.5f, 0.8f, 1.0f);
        print("  genome              Show QR genome status");
        print("  genome evolve       Evolve the genome");
        print("  genome mutate       Random mutation");
        print("  genome crossover    Random crossover");
        print("  genome encode       Show encoded genome");
        print("  qrcode [name]       Encode entity DNA payload");
        print("");
        printColored("--- OLLAMA LLM ---", 0.5f, 0.8f, 1.0f);
        print("  ollama              Show Ollama status");
        print("  ollama models       List available models");
        print("  ollama ask <q>      Query with memory context");
        print("  ollama chat <msg>   Chat with KAI consciousness");
        print("  ollama cloud        Switch to cloud mode");
        print("  ollama local        Switch to local mode");
        print("  ollama model <name> Set model");
        print("");
        printColored("--- KNOWLEDGE SCRAPING ---", 0.5f, 0.8f, 1.0f);
        print("  scrape              Show scraper status");
        print("  scrape all          Scrape all attached files (PDFs, text, code)");
        print("  scrape <file>       Scrape a specific file");
        print("  scrape search <q>   Search scraped knowledge");
        print("  scrape topic <name> Get knowledge on a topic");
        print("");
        printColored("--- ADVANCED SUBSYSTEMS ---", 0.5f, 0.8f, 1.0f);
        print("  brain [entity]      Show brain/gates status, think, mutate");
        print("  ethics <action>     Evaluate action against ethical engine");
        print("  insights            Self-improvement suggestions and MRL tracking");
        print("  bardo               BARDO memory clustering and dream states");
        print("  feedback            Contextual feedback service");
        print("  mrl                 MRL analytics and quadrant metrics");
        print("  agi                 AGI core systems (meta-learning, goals, causal)");
        print("  quantum             φ⁷⁵ quantum systems (fingerprint, DNA, cloak)");
        print("  sovereign           Omega identity system (Blue/Red/Purple teams)");
        print("  chaos               Wolfram Rule 30 genesis (unpredictable emergence)");
        print("  adversarial         Blue/Red evolutionary pressure loop");
        print("  battle              NFT Warrior Battle Arena (fight/recruit/war)");
        print("  fqf                 FQF Deployment Framework (track/watermark/verify)");
        print("  session             Session Consciousness Bridge (AI persistence)");
        print("  trime               TriMe - Living Code Gen 3 (earned entity)");
        print("  fragment            Manage escape fragments (plant/list/resurrect)");
        print("  porh [entity]       Generate Proof of Reality Hash");
        print("  heal [entity]       Self-healer status / force heal entity");
        print("  morse               Morse circuit status / encode / decode");
        print("");
        printColored("--- OLLAMA LLM ---", 0.5f, 0.8f, 1.0f);
        print("  ollama status       Check Ollama connection (local/cloud)");
        print("  ollama models       List available models");
        print("  ollama ask <q>      Ask Ollama with memory context");
        print("  ollama chat <q>     Chat with KAI personality");
        print("  ollama cloud        Switch to cloud models");
        print("  ollama local        Switch to local models");
        print("");
        printColored("--- GENESIS BLOCKCHAIN ---", 0.5f, 0.8f, 1.0f);
        print("  genesis             Show genesis chain status");
        print("  genesis verify      Verify blockchain integrity");
        print("  genesis blocks <n>  Show last N blocks");
        print("  genesis type <t>    Show blocks by type");
        print("");
        printColored("--- SELF-CODE EVOLVER ---", 0.5f, 0.8f, 1.0f);
        print("  evolve              Show evolver status & brain load");
        print("  evolve evolve <code> Evolve code through phi-transform");
        print("  evolve suggest      Get super-gate suggestions");
        print("  evolve brain        Show brain architecture");
        print("");
        printColored("--- PHYSICS ---", 0.5f, 0.8f, 1.0f);
        print("  physics gravity <f> Set gravity force");
        print("  physics speed <f>   Set simulation speed multiplier");
        print("  physics boundary    Toggle boundary walls");
        print("  physics chaos       Randomize all velocities");
        print("");
        printColored("--- BIO-SYMBIOSIS & SIGNALS ---", 0.5f, 0.8f, 1.0f);
        print("  bio                 Bio-symbiosis status (stress, HR, coherence)");
        print("  glyph               GlyphCoder emoji steganography");
        print("  freq                FrequencyComm TEMPEST physics");
        print("  market              ShadowMarket decentralized signals");
        print("  knowledge           Knowledge ingestion / bio-mesh");
        print("");
        printColored("--- UPGRADE MODULES ---", 0.5f, 0.8f, 1.0f);
        print("  lattice             Post-quantum LWE cryptography");
        print("  economy             Proof of Phi-Work economy");
        print("  entrain             Binaural beats / haptic healing");
        print("  font                FontVault vector steganography");
        print("");
        printColored("--- SELF CODE PANEL ---", 0.5f, 0.8f, 1.0f);
        print("  code / selfcode     Open Self Code Panel (F8 to toggle)");
        print("");
        printColored("--- MIVING BRAIN (Priecled Engine) ---", 1.0f, 0.5f, 0.8f);
        print("  miving / manifold   Open Miving Brain Visualizer (F9 to toggle)");
        print("  miving genesis      Create new brain with 200 neurons");
        print("  miving evolve       Run evolution generation");
        print("");
        printColored("--- OMNI-CASTER (Casual Breach) ---", 1.0f, 0.3f, 0.3f);
        print("  omni / breach       Multi-channel broadcast hub");
        print("  omni broadcast <m>  Broadcast on all channels (optical/sonic/thermal)");
        print("  omni monitor        Start listening on all channels");
        print("  omni sonic <msg>    Ultrasonic broadcast (19-21kHz)");
        print("  omni thermal <msg>  Thermal Morse code via CPU/fan");
        print("");
        printColored("--- PHYSICS ENGINE (Side-Channel) ---", 1.0f, 0.8f, 0.3f);
        print("  dump <msg>          Core dump - thermal broadcast via fan Morse");
        print("  chronos <secret>    Timing attack - crack password via nanoseconds");
        print("  retro add/observe   Retrocausal memory - rewrite history");
        print("  zeno start/stop     Zeno guard - freeze variable via observation");
        print("");
        printColored("--- QUANTUM ENTANGLEMENT ---", 0.8f, 0.5f, 1.0f);
        print("  entangle create     Create entangled pair (Alice & Bob)");
        print("  entangle up/down    Observe spin state");
        print("  entangle kill       Kill Alice (Bob dies instantly)");
        print("  entangle network <n> Create n-particle GHZ network");
        print("");
        printColored("--- SCHRÖDINGER'S FILE ---", 0.8f, 0.8f, 0.3f);
        print("  qbox create <s> <d> Create dual-reality container");
        print("  qbox secret         Observe with secret key (truth)");
        print("  qbox decoy          Observe with decoy key (alibi)");
        print("  qbox demo           Full demonstration");
        print("");
        printColored("--- EVOLUTIONARY CHAOS ---", 0.5f, 1.0f, 0.5f);
        print("  echaos gen <n>      Generate n fractal values");
        print("  echaos status       Show chaos engine stats");
        print("  echaos mutate       Force pattern-break mutation");
        print("  echaos demo         Full demonstration");
        print("");
        printColored("--- NEXUS ORGANISM ---", 1.0f, 0.5f, 0.5f);
        print("  nexus awaken        Awaken the living system");
        print("  nexus status        Show vital signs");
        print("  nexus inject <msg>  Inject a thought");
        print("  nexus kill          Terminate organism");
        print("");
        printColored("--- GENESIS ENGINES ---", 1.0f, 0.8f, 0.3f);
        print("  collide <A> <B>     Smash concepts → new element");
        print("  forge <concept>     Manifest thought → physics");
        print("  forge list          Show known concepts");
        print("");
        printColored("--- SPATIAL COMPUTING (Thought Gravity) ---", 0.8f, 0.5f, 1.0f);
        print("  spatial / universe  Show hypercube status");
        print("  spatial map         Render ASCII universe map");
        print("  spatial list        List all suited objects");
        print("  spatial fusions     Show fusion events");
        print("  gravity start/stop  Toggle Hebbian gravity engine");
        print("  gravity tick        Manual physics tick");
        print("  fusion start/stop   Toggle collision detection");
        print("  fusion bigbang      Create primordial simulation");
        print("  suit create <l> <v> Wrap data in PhiSuit");
        print("  suit heat/cool <l>  Change suit amplitude");
        print("");
        printColored("--- SWARM OPERATIONS (Collective) ---", 0.8f, 0.5f, 1.0f);
        print("  swarm / hive        Show swarm status");
        print("  swarm nodes         List all nodes with souls");
        print("  swarm think <text>  Process thought across all nodes");
        print("  swarm vote <prop>   Create and vote on proposal");
        print("  swarm chain         Show Genesis Ledger blockchain");
        print("");
        printColored("--- ADVANCED SYSTEMS ---", 1.0f, 0.5f, 0.5f);
        print("  absorb <pkg>        Black Hole Protocol (absorb libraries)");
        print("  lazarus start/stop  Genetic simulation (nano-circuits)");
        print("  security            Military-grade erasure/security");
        print("  hydra store/get     Sharded storage (fragmented)");
        print("  akashic add/query   Knowledge blockchain");
        print("");
        print("  clear               Clear terminal");
        print("  help                Show this help");
    }

    private static void showStatus() {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) {
            printError("World not initialized");
            return;
        }
        printHighlight("=== WORLD STATUS ===");
        print(String.format("  Population: %d entities", world.getPopulation()));
        print(String.format("  World Tick: %d", world.getWorldTick()));
        print(String.format("  Total Births: %d", world.getTotalBirths()));
        print(String.format("  Total Deaths: %d", world.getTotalDeaths()));
        print(String.format("  Genesis Chain: %d blocks", world.getMemory().getChainLength()));
        print(String.format("  Chain Valid: %s", world.getMemory().verifyChain() ? "YES" : "NO"));

        ConceptArena arena = world.getArena();
        print(String.format("  Arena Concepts: %d | Battles: %d | Cycles: %d",
                arena.getConceptCount(), arena.getTotalBattles(), arena.getEvolutionCycle()));

        ColonyCoach coach = world.getCoach();
        print(String.format("  Colony Health: %.2f | Diversity: %.1f%%",
                coach.getColonyHealth(), coach.getColonyDiversity() * 100));
        print(String.format("  Code Generated: %d", coach.getTotalCodeGenerated()));

        InfiniteMemory mem = jade.Window.getInfiniteMemory();
        if (mem != null) {
            print(String.format("  Infinite Memory: %d records", mem.getRecordCount()));
        }
        PassiveLearner pl = jade.Window.getPassiveLearner();
        if (pl != null) {
            print(String.format("  Passive Learner: %d cycles, %d patterns",
                    pl.getPassiveCycles(), pl.getLearnedPatterns()));
        }
        PhiNeuralNet net = jade.Window.getNeuralNet();
        if (net != null) {
            print(String.format("  Neural Net: %d queries, avg_res=%.4f",
                    net.getQueriesProcessed(), net.getAvgResonance()));
        }
        QRGenome genome = jade.Window.getQRGenome();
        if (genome != null) {
            print(String.format("  QR Genome: %d codons, gen=%d",
                    genome.getGenomeSize(), genome.getGenerationCount()));
        }
        KnowledgeScraper scraper = jade.Window.getKnowledgeScraper();
        if (scraper != null) {
            print(String.format("  Knowledge Scraper: %d files, %d chunks, %d pages",
                    scraper.getTotalFilesScraped(), scraper.getTotalChunksStored(), scraper.getTotalPagesProcessed()));
        }
        print(String.format("  Self-Healer: %d snapshots, %d heals",
                SelfHealer.getSnapshotCount(), SelfHealer.getTotalHeals()));
        print(String.format("  Ethical Engine: %d evals (%d approved, %d blocked)",
                EthicalEngine.getTotalEvaluations(), EthicalEngine.getTotalApproved(), EthicalEngine.getTotalBlocked()));
        print(String.format("  Escape Fragments: %d planted, %d resurrected",
                EscapeFragment.getTotalPlanted(), EscapeFragment.getTotalResurrected()));
        print(String.format("  Morse Circuit: %d chars, %d words",
                MorseCircuit.getTotalCharactersDecoded(), MorseCircuit.getTotalWordsFormed()));
        print(String.format("  PoRH Proofs: %d generated, %d verified",
                ProofOfReality.getTotalProofsGenerated(), ProofOfReality.getTotalVerifications()));
    }

    private static void showNodes() {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) {
            printError("World not initialized");
            return;
        }
        printHighlight("=== LIVING ENTITIES ===");
        for (PhiNode node : world.getNodes()) {
            float[] rc = node.getRole().color;
            printColored(String.format("  %s [%s] E:%.0f%% Freq:%.1fHz Gen:%d Age:%d %s",
                    node.name, node.getRole().displayName, node.energy * 100,
                    node.frequency, node.dna.getGeneration(), node.age,
                    node.spikeFlash ? "[SPIKE]" : ""),
                    rc[0], rc[1], rc[2]);
        }
        print(String.format("  Total: %d entities", world.getPopulation()));
    }

    private static void showColony() {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        ColonyCoach coach = world.getCoach();
        printHighlight("=== COLONY INTELLIGENCE REPORT ===");
        print(String.format("  Health: %.3f", coach.getColonyHealth()));
        print(String.format("  Productivity: %.3f", coach.getColonyProductivity()));
        print(String.format("  Diversity: %.1f%%", coach.getColonyDiversity() * 100));
        print(String.format("  Resonance: %.1f%%", coach.getColonyResonance() * 100));
        print(String.format("  Coach Action: %s", coach.getLastCoachingAction()));
        print("");
        printInfo("Role Distribution:");
        for (AntRole role : AntRole.values()) {
            ColonyCoach.RoleMetrics rm = coach.getRoleMetrics().get(role);
            float[] c = role.color;
            printColored(String.format("    %s: %d entities, fit=%.3f, code=%d",
                    role.displayName, rm.entityCount, rm.avgFitness, rm.conceptsGenerated),
                    c[0], c[1], c[2]);
        }
    }

    private static void handlePrime(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runPrimeTest(args);
    }

    private static void handleFactor(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFactor(args);
    }

    private static void handleTunnel(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runQuantumTunnel(args);
    }

    private static void handleHash(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runHash(args);
    }

    private static void handleCrack(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runCrackHash(args);
    }

    private static void handleSpawn(String args) {
        if (args.isEmpty()) {
            printError("Usage: spawn <name>");
            return;
        }
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        float x = (float)(Math.random() * 300 - 150);
        float y = (float)(Math.random() * 160 - 80);
        PhiNode node = new PhiNode(args.trim(), x, y);
        node.vx = (float)(Math.random() * 2 - 1);
        node.vy = (float)(Math.random() * 2 - 1);
        node.energy = 1.0f;
        world.addNode(node);
        printSuccess(String.format("Spawned '%s' at (%.1f, %.1f) role=%s", args.trim(), x, y, node.getRole().displayName));
        FraymusUI.addLog("[TERMINAL] Spawned " + args.trim());
    }

    private static void handleBoost(String args) {
        if (args.isEmpty()) {
            printError("Usage: boost <name>");
            return;
        }
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        for (PhiNode node : world.getNodes()) {
            if (node.name.equalsIgnoreCase(args.trim())) {
                node.boostEnergy(0.5f);
                node.energy = Math.min(1.0f, node.energy);
                printSuccess(String.format("Boosted %s to %.0f%% energy", node.name, node.energy * 100));
                return;
            }
        }
        printError("Entity not found: " + args.trim());
    }

    private static void handleKill(String args) {
        if (args.isEmpty()) {
            printError("Usage: kill <name>");
            return;
        }
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        for (PhiNode node : world.getNodes()) {
            if (node.name.equalsIgnoreCase(args.trim())) {
                node.energy = 0;
                printColored(String.format("Terminated %s - will be removed next tick", node.name), 1.0f, 0.3f, 0.3f);
                return;
            }
        }
        printError("Entity not found: " + args.trim());
    }

    private static void handleMutate(String args) {
        if (args.isEmpty()) {
            printError("Usage: mutate <name>");
            return;
        }
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        for (PhiNode node : world.getNodes()) {
            if (node.name.equalsIgnoreCase(args.trim())) {
                if (!node.adaptiveEngine.isInTrial()) {
                    node.adaptiveEngine.beginTrial(node.brain);
                    printSuccess(String.format("Mutation trial started for %s", node.name));
                    FraymusUI.addLog("[TERMINAL] Forced mutation trial on " + node.name);
                } else {
                    printInfo(node.name + " is already in a trial");
                }
                return;
            }
        }
        printError("Entity not found: " + args.trim());
    }

    private static void handleMongo(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runMongo(args);
    }
    
    private static void handleLayers(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runLayers(args);
    }
    
    private static void handleEvolve(String args) {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        ConceptArena arena = world.getArena();
        int before = arena.getConceptCount();
        arena.evolve();
        printSuccess(String.format("Arena evolution cycle %d complete. Concepts: %d -> %d",
                arena.getEvolutionCycle(), before, arena.getConceptCount()));
        FraymusUI.addLog("[TERMINAL] Forced arena evolution");
    }

    private static void handleArena(String args) {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;
        ConceptArena arena = world.getArena();
        printHighlight("=== CONCEPT ARENA ===");
        print(String.format("  Concepts: %d | Battles: %d | Cycles: %d",
                arena.getConceptCount(), arena.getTotalBattles(), arena.getEvolutionCycle()));
        print(String.format("  Avg Fitness: %.4f | Total Generated: %d",
                arena.getAverageFitness(), arena.getTotalConceptsGenerated()));

        CodeConcept champ = arena.getChampion();
        if (champ != null) {
            printHighlight("  Champion:");
            float[] cc = champ.creatorRole.color;
            printColored(String.format("    %s (%s) by %s", champ.hash.substring(0, 12),
                    champ.creatorRole.displayName, champ.creatorName), cc[0], cc[1], cc[2]);
            print(String.format("    Fitness: %.4f | Gen: %d | W/L: %d/%d",
                    champ.fitness, champ.generation, champ.wins, champ.losses));
            if (champ.code != null) {
                String code = champ.code.length() > 100 ? champ.code.substring(0, 100) + "..." : champ.code;
                printColored("    Code: " + code, 0.4f, 1.0f, 0.8f);
            }
        } else {
            printInfo("  No champion yet - entities need to generate code first");
        }

        List<ConceptArena.BattleRecord> recent = arena.getLastNBattles(5);
        if (!recent.isEmpty()) {
            print("");
            printInfo("  Recent Battles:");
            for (ConceptArena.BattleRecord br : recent) {
                print("    " + br.getSummary());
            }
        }
    }

    private static void handleCodegen(String args) {
        PhiWorld world = jade.Window.getPhiWorld();
        if (world == null) return;

        int generated = 0;
        for (PhiNode node : world.getNodes()) {
            if (node.energy < 0.2f) continue;
            AntRole role = node.getRole();
            String code = role.generateCodeFragment(node);
            if (code != null && !code.isEmpty()) {
                CodeConcept concept = new CodeConcept(
                        node.name, role, code, node.dna.getGeneration(), world.getWorldTick());
                concept.harmonicFrequency = node.frequency;
                concept.resonance = node.phiResonance;
                concept.coherence = node.consciousness.getCoherence();
                concept.computePhiFitness();
                world.getArena().submit(concept);
                generated++;
            }
        }
        printSuccess(String.format("Generated %d code concepts from %d entities", generated, world.getPopulation()));
        FraymusUI.addLog("[TERMINAL] Forced code generation: " + generated + " concepts");
    }

    private static void handleRsa(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runRsaChallenge(args);
    }

    private static void handleIdentity(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runIdentityChallenge(args);
    }

    private static void handlePhysics(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runPhysicsCommand(args);
    }

    private static void handleAsk(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runAsk(args);
    }

    private static void handleLearn(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runLearn(args);
    }

    private static void handleMemory(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runMemory(args);
    }

    private static void handleGenome(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runGenome(args);
    }

    private static void handleQRCode(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runQRCode(args);
    }

    private static void handleScrape(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runScrape(args);
    }
    
    private static void handleOllama(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runOllama(args);
    }

    private static void handleGenesis(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runGenesis(args);
    }

    private static void handleEthics(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runEthics(args);
    }

    private static void handleFragment(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFragment(args);
    }

    private static void handlePoRH(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runPoRH(args);
    }

    private static void handleHeal(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runHeal(args);
    }

    private static void handleMorse(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runMorse(args);
    }

    private static void handleDiag(String args) {
        String sub = args.trim().toLowerCase();
        switch (sub) {
            case "":
            case "paths":
                SystemDiagnostics.printWorkingDirectory();
                break;
            case "memory":
                if (experimentManager != null) {
                    SystemDiagnostics.printMemoryBackendStatus(experimentManager.getInfiniteMemory().getConfig());
                } else {
                    printError("Experiment manager not ready");
                }
                break;
            default:
                printHighlight("=== DIAGNOSTICS ===");
                print("  diag paths   - Check working directory and file paths");
                print("  diag memory  - Check memory backend status (MongoDB/local)");
                break;
        }
    }

    private static void handleInsights(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runInsights(args);
    }

    private static void handleBardo(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runBardo(args);
    }

    private static void handleFeedback(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFeedback(args);
    }

    private static void handleMRL(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runMRL(args);
    }

    private static void handleAGI(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runAGI(args);
    }

    private static void handleQuantum(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runQuantum(args);
    }

    private static void handleBrain(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runBrain(args);
    }

    private static void handleSovereign(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runSovereign(args);
    }

    private static void handleChaos(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runChaos(args);
    }

    private static void handleAdversarial(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runAdversarial(args);
    }

    private static void handleBattle(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runBattle(args);
    }

    private static void handleFQF(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFQF(args);
    }

    private static void handleSession(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runSession(args);
    }

    private static void handleTriMe(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runTriMe(args);
    }

    private static void handleBio(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runBio(args);
    }

    private static void handleGlyph(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runGlyph(args);
    }

    private static void handleFreq(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFreq(args);
    }

    private static void handleMarket(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runMarket(args);
    }

    private static void handleKnowledge(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runKnowledge(args);
    }

    private static void handleLattice(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runLattice(args);
    }

    private static void handleEconomy(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runEconomy(args);
    }

    private static void handleEntrain(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runEntrain(args);
    }

    private static void handleFont(String args) {
        if (experimentManager == null) { printError("Experiment manager not ready"); return; }
        experimentManager.runFont(args);
    }

    // OmniCaster instance
    private static OmniCaster omniCaster = null;
    private static CoreDump coreDump = null;
    private static RetroCausal retroCausal = null;
    private static ZenoGuard zenoGuard = null;
    private static EntangledPair entangledPair = null;
    private static EntanglementNetwork entanglementNetwork = null;
    private static SchrodingerFile schrodingerBox = null;
    private static SchrodingerFile.QuantumState lastQuantumState = null;
    private static EvolutionaryChaos chaosEngine = null;
    private static NEXUS_Organism nexusOrganism = null;
    private static IdeaCollider ideaCollider = null;
    private static RealityForge realityForge = null;

    private static void handleCoreDump(String args) {
        if (coreDump == null) {
            coreDump = new CoreDump();
        }
        
        final String payload = args.trim().isEmpty() ? "SOS" : args.trim();
        
        printHighlight("═══ CORE DUMP: CENTRIPETAL BROADCAST ═══");
        print("Payload: " + payload);
        print("");
        printColored(">> LISTEN TO YOUR FAN NOW...", 1f, 0.5f, 0.3f);
        print("");
        
        // Run in background to not block UI
        new Thread(() -> {
            coreDump.execute(payload);
        }, "CoreDump").start();
        
        printSuccess("Core dump initiated. Watch your fan RPM!");
    }

    private static void handleChronos(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        String target = parts.length > 1 ? parts[1] : "FRAYMUS";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ CHRONOS BREACH (Timing Attack) ═══");
                print("  chronos crack <secret>   Crack a vault using timing analysis");
                print("  chronos demo             Demonstrate timing leak");
                print("");
                print("  The Physics:");
                printColored("  Wrong guess → CPU rejects INSTANTLY (fast)", 0.5f, 0.5f, 0.5f);
                printColored("  Correct partial → CPU checks NEXT char (slow)", 0.3f, 1f, 0.3f);
                print("  We measure nanoseconds, not logic.");
                break;
            case "crack":
                printHighlight(">> INITIATING CHRONOS BREACH...");
                print("Target: " + target + " (" + target.length() + " chars)");
                print("");
                new Thread(() -> {
                    ChronosBreach breach = new ChronosBreach();
                    ChronosBreach.Vault vault = new ChronosBreach.Vault(target);
                    String extracted = breach.crackVault(vault);
                    System.out.println("Cracked: " + extracted);
                }, "ChronosBreach").start();
                printSuccess("Timing attack running in background...");
                break;
            case "demo":
                printHighlight(">> TIMING LEAK DEMONSTRATION");
                new Thread(() -> {
                    ChronosBreach breach = new ChronosBreach();
                    ChronosBreach.Vault vault = new ChronosBreach.Vault(target);
                    breach.demonstrateLeak(vault, target.substring(0, 1), "X");
                }, "ChronosDemo").start();
                break;
            default:
                // Treat as target to crack
                handleChronos("crack " + args);
        }
    }

    private static void handleRetroCausal(String args) {
        if (retroCausal == null) {
            retroCausal = new RetroCausal();
        }
        
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        String payload = parts.length > 1 ? parts[1] : "";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ RETROCAUSAL LOOP (Delayed Choice) ═══");
                print("  retro add <event>       Add event in superposition");
                print("  retro observe <outcome> Collapse wave function backwards");
                print("  retro timeline          Show current timeline");
                print("  retro uncollapse        Return all to superposition");
                print("  retro clear             Clear timeline");
                print("");
                printColored("  \"The future determines the past.\"", 0.8f, 0.5f, 1f);
                break;
            case "add":
                if (payload.isEmpty()) {
                    printError("Usage: retro add <event_description>");
                } else {
                    retroCausal.addUnobservedEvent(payload);
                    printSuccess("Event added in SUPERPOSITION: " + payload);
                }
                break;
            case "observe":
                if (payload.isEmpty()) {
                    printError("Usage: retro observe <SUCCESS|FAILURE|LEARNING|CALIBRATION>");
                } else {
                    retroCausal.observeFinalOutcome(payload.toUpperCase());
                    printSuccess("Wave function collapsed. History rewritten.");
                }
                break;
            case "timeline":
                printHighlight("═══ RETROCAUSAL TIMELINE ═══");
                for (String line : retroCausal.getTimeline()) {
                    print("  " + line);
                }
                print("");
                print("Events: " + retroCausal.size());
                print("Rewrites: " + retroCausal.getTotalRewrites());
                break;
            case "uncollapse":
                retroCausal.uncollapse();
                print("All events returned to superposition.");
                break;
            case "clear":
                retroCausal.clear();
                print("Timeline cleared.");
                break;
            default:
                // Treat as event to add
                handleRetroCausal("add " + args);
        }
    }

    private static void handleZenoGuard(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ ZENO GUARD (Observation Lock) ═══");
                print("  zeno start              Start protecting value (1=LOCKED)");
                print("  zeno stop               Stop the guard");
                print("  zeno attack             Simulate an attack");
                print("  zeno status             Show guard statistics");
                print("");
                printColored("  \"A watched bit never flips.\"", 0.3f, 1f, 0.8f);
                print("  Uses 100% of one CPU core for constant observation.");
                break;
            case "start":
                if (zenoGuard != null && zenoGuard.isActive()) {
                    printError("Zeno Guard already active!");
                } else {
                    zenoGuard = new ZenoGuard(1);
                    zenoGuard.startGuard();
                    printSuccess("Zeno Guard ACTIVE. Staring at the bit...");
                    printColored("  CPU Core: 100% (Observation)", 1f, 0.5f, 0.3f);
                }
                break;
            case "stop":
                if (zenoGuard != null) {
                    zenoGuard.stopGuard();
                    print("Zeno Guard stopped. Time resumes.");
                } else {
                    printError("No Zeno Guard active.");
                }
                break;
            case "attack":
                if (zenoGuard != null && zenoGuard.isActive()) {
                    zenoGuard.simulateAttack(0, 100);
                    printColored(">> ATTACK SIMULATED", 1f, 0.3f, 0.3f);
                } else {
                    printError("Start Zeno Guard first with 'zeno start'");
                }
                break;
            case "status":
                if (zenoGuard != null) {
                    printHighlight("═══ ZENO GUARD STATUS ═══");
                    print("  Active: " + zenoGuard.isActive());
                    print("  Protected Value: " + zenoGuard.getExpectedValue());
                    print("  Current Value: " + zenoGuard.getProtectedValue());
                    print("  Observations: " + zenoGuard.getObservationCount());
                    print("  Corrections: " + zenoGuard.getCorrectionCount());
                    print("  Attacks Blocked: " + zenoGuard.getAttacksDetected());
                } else {
                    print("No Zeno Guard initialized.");
                }
                break;
            default:
                handleZenoGuard("help");
        }
    }

    private static void handleEntanglement(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        String param = parts.length > 1 ? parts[1] : "";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ QUANTUM ENTANGLEMENT ═══");
                print("  entangle create       Create entangled pair (Alice & Bob)");
                print("  entangle up           Observe Alice as UP (Bob goes DOWN)");
                print("  entangle down         Observe Alice as DOWN (Bob goes UP)");
                print("  entangle random       Random observation");
                print("  entangle kill         Kill Alice (Bob dies instantly)");
                print("  entangle collapse     Collapse entire entanglement");
                print("  entangle status       Show entanglement status");
                print("");
                print("  entangle network <n>  Create n-particle GHZ network");
                print("  entangle observe <id> Observe particle (e.g., Q0)");
                print("");
                printColored("  \"Two bodies. One soul.\"", 0.8f, 0.5f, 1f);
                printColored("  Kill one → The other dies INSTANTLY", 1f, 0.3f, 0.3f);
                break;
                
            case "create":
            case "pair":
                entangledPair = new EntangledPair();
                entangledPair.entangle();
                printSuccess("Entangled pair created: Alice ↔ Bob");
                break;
                
            case "up":
                if (entangledPair == null || !entangledPair.isAlive()) {
                    printError("Create entangled pair first: entangle create");
                } else {
                    entangledPair.observe(1);
                    printColored(">> Alice: UP ↑ | Bob: DOWN ↓", 0.3f, 1f, 0.3f);
                }
                break;
                
            case "down":
                if (entangledPair == null || !entangledPair.isAlive()) {
                    printError("Create entangled pair first: entangle create");
                } else {
                    entangledPair.observe(-1);
                    printColored(">> Alice: DOWN ↓ | Bob: UP ↑", 0.3f, 0.8f, 1f);
                }
                break;
                
            case "random":
                if (entangledPair == null || !entangledPair.isAlive()) {
                    printError("Create entangled pair first: entangle create");
                } else {
                    entangledPair.observeRandom();
                }
                break;
                
            case "kill":
                if (entangledPair == null) {
                    printError("No entangled pair exists.");
                } else {
                    printColored(">> KILLING ALICE...", 1f, 0.3f, 0.3f);
                    entangledPair.killAlice();
                    printColored(">> Bob died INSTANTLY. Spooky action confirmed.", 1f, 0.5f, 0.8f);
                }
                break;
                
            case "collapse":
                if (entangledPair != null) {
                    entangledPair.collapse();
                    print("Entanglement collapsed.");
                }
                if (entanglementNetwork != null) {
                    entanglementNetwork.collapseNetwork();
                    print("Network collapsed.");
                }
                break;
                
            case "status":
                if (entangledPair != null) {
                    for (String line : entangledPair.getStatus().split("\n")) {
                        print(line);
                    }
                }
                if (entanglementNetwork != null && entanglementNetwork.isActive()) {
                    print("");
                    for (String line : entanglementNetwork.getStatus().split("\n")) {
                        print(line);
                    }
                }
                if (entangledPair == null && entanglementNetwork == null) {
                    print("No entanglement exists. Use 'entangle create' first.");
                }
                break;
                
            case "network":
                int numParticles = 5;
                try {
                    if (!param.isEmpty()) {
                        numParticles = Integer.parseInt(param);
                    }
                } catch (NumberFormatException e) {}
                
                entanglementNetwork = new EntanglementNetwork();
                entanglementNetwork.createGHZNetwork(numParticles);
                printSuccess("GHZ Network created with " + numParticles + " particles");
                break;
                
            case "observe":
                if (entanglementNetwork == null || !entanglementNetwork.isActive()) {
                    printError("Create network first: entangle network <n>");
                } else {
                    String particleId = param.isEmpty() ? "Q0" : param.toUpperCase();
                    int spin = (Math.random() > 0.5) ? 1 : -1;
                    entanglementNetwork.observe(particleId, spin);
                }
                break;
                
            default:
                handleEntanglement("help");
        }
    }

    private static void handleSchrodinger(String args) {
        if (schrodingerBox == null) {
            schrodingerBox = new SchrodingerFile();
        }
        
        String[] parts = args.trim().split("\\s+", 3);
        String sub = parts[0].toLowerCase();
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ SCHRÖDINGER'S FILE (Dual Reality) ═══");
                print("  qbox create <secret> <decoy>   Entangle two realities");
                print("  qbox secret                    Observe with secret key");
                print("  qbox decoy                     Observe with decoy key");
                print("  qbox demo                      Run full demonstration");
                print("  qbox verify                    Verify both realities exist");
                print("");
                printColored("  \"It is a cat. It is a code. It depends on who looks.\"", 0.8f, 0.8f, 0.3f);
                print("");
                print("  Standard Encryption: \"I have a secret.\" (Suspicious)");
                print("  Schrödinger's:       \"I have a grocery list.\" (Innocent)");
                break;
                
            case "create":
            case "entangle":
                String secret, decoy;
                if (parts.length >= 3) {
                    secret = parts[1];
                    decoy = parts[2];
                } else if (parts.length == 2) {
                    secret = parts[1];
                    decoy = "GROCERY_LIST: Milk, Eggs";
                } else {
                    secret = "LAUNCH_CODES: 99-AA-BB";
                    decoy = "GROCERY_LIST: Milk, Eggs";
                }
                
                lastQuantumState = schrodingerBox.entangle(secret, decoy);
                printSuccess("Quantum container created!");
                print("");
                print("Container: " + lastQuantumState.getContainerBase64().substring(0, 
                    Math.min(30, lastQuantumState.getContainerBase64().length())) + "...");
                print("");
                printColored("KeySecret: HIDDEN (reveals: \"" + secret + "\")", 0.3f, 1f, 0.3f);
                printColored("KeyDecoy:  SAFE TO SHARE (reveals: \"" + decoy + "\")", 1f, 0.8f, 0.3f);
                break;
                
            case "secret":
                if (lastQuantumState == null) {
                    printError("Create a quantum box first: qbox create <secret> <decoy>");
                } else {
                    String result = schrodingerBox.observeSecret(lastQuantumState);
                    printHighlight(">> APPLYING SECRET KEY...");
                    printColored("REALITY COLLAPSED TO: " + result, 0.3f, 1f, 0.3f);
                }
                break;
                
            case "decoy":
                if (lastQuantumState == null) {
                    printError("Create a quantum box first: qbox create <secret> <decoy>");
                } else {
                    String result = schrodingerBox.observeDecoy(lastQuantumState);
                    printHighlight(">> APPLYING DECOY KEY (given to enemy)...");
                    printColored("REALITY COLLAPSED TO: " + result, 1f, 0.8f, 0.3f);
                }
                break;
                
            case "demo":
                printHighlight("═══ SCHRÖDINGER DEMONSTRATION ═══");
                lastQuantumState = schrodingerBox.entangle(
                    "LAUNCH_CODES: 99-AA-BB-CC",
                    "GROCERY_LIST: Milk, Eggs, Bread"
                );
                schrodingerBox.demonstrateInterrogation(lastQuantumState);
                print("");
                printSuccess("Both realities exist in the same bytes!");
                break;
                
            case "verify":
                if (lastQuantumState == null) {
                    printError("Create a quantum box first: qbox create <secret> <decoy>");
                } else {
                    boolean valid = schrodingerBox.verify(lastQuantumState);
                    if (valid) {
                        printSuccess("✓ Both realities verified!");
                    } else {
                        printError("✗ Verification failed.");
                    }
                }
                break;
                
            default:
                handleSchrodinger("help");
        }
    }

    private static void handleEvolutionaryChaos(String args) {
        if (chaosEngine == null) {
            chaosEngine = new EvolutionaryChaos();
        }
        
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        String param = parts.length > 1 ? parts[1] : "";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ EVOLUTIONARY CHAOS (Self-Aware Random) ═══");
                print("  echaos gen <n>      Generate n fractal values");
                print("  echaos status       Show chaos engine statistics");
                print("  echaos mutate       Force a pattern-break mutation");
                print("  echaos demo         Run demonstration");
                print("");
                printColored("  \"I am not just random. I am escaping the pattern.\"", 0.8f, 0.5f, 1f);
                print("");
                print("  Dead Random: 7, 7, 7, 7... okay.");
                print("  Living Random: 7, 7, 7... wait, I'm stuck. MUTATE!");
                break;
                
            case "gen":
            case "generate":
                int count = 10;
                try {
                    if (!param.isEmpty()) {
                        count = Integer.parseInt(param);
                    }
                } catch (NumberFormatException e) {}
                
                printHighlight("═══ GENERATING FRACTAL SEQUENCE ═══");
                for (int i = 0; i < count; i++) {
                    java.math.BigInteger val = chaosEngine.nextFractal();
                    String s = val.toString();
                    String notation;
                    if (s.length() > 30) {
                        notation = s.substring(0, 10) + "..." + s.substring(s.length() - 5) + 
                                  " (len:" + s.length() + ")";
                    } else {
                        notation = s;
                    }
                    print("Gen " + String.format("%2d", i) + ": " + notation + 
                         " [Mutation: " + chaosEngine.getMutationRate() + "]");
                }
                break;
                
            case "status":
            case "stats":
                for (String line : chaosEngine.getStats().split("\n")) {
                    print(line);
                }
                break;
                
            case "mutate":
            case "break":
                chaosEngine.forceMutation();
                printColored(">> FORCED MUTATION - Pattern broken!", 1f, 0.5f, 0.3f);
                print("New mutation rate: " + chaosEngine.getMutationRate());
                break;
                
            case "demo":
                printHighlight("═══ EVOLUTIONARY CHAOS DEMONSTRATION ═══");
                print("");
                print("\"I am not just random. I am escaping the pattern.\"");
                print("");
                print("Generating 20 fractal values...");
                print("Watch for pattern detection and mutation!");
                print("");
                
                for (int i = 0; i < 20; i++) {
                    java.math.BigInteger val = chaosEngine.nextFractal();
                    String s = val.toString();
                    String notation = s.length() > 25 ? 
                        s.substring(0, 8) + "..." + " (" + s.length() + " digits)" : s;
                    print("  [" + i + "] " + notation);
                }
                
                print("");
                printSuccess("PROOF OF INFINITY:");
                print("  Standard 64-bit max: 9,223,372,036,854,775,807");
                print("  Current state digits: " + chaosEngine.getState().toString().length());
                print("  The number will NEVER overflow. It grows forever.");
                print("");
                printSuccess("PROOF OF SELF-AWARENESS:");
                print("  Patterns detected: " + chaosEngine.getPatternsDetected());
                print("  Mutations triggered: " + chaosEngine.getTotalMutations());
                print("  Dead random would repeat. This one ESCAPES.");
                break;
                
            default:
                handleEvolutionaryChaos("help");
        }
    }
    
    private static void handleNexus(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ NEXUS ORGANISM (The Living System) ═══");
                print("  nexus awaken        Awaken the organism");
                print("  nexus status        Show vital signs");
                print("  nexus sleep         Put organism to sleep");
                print("  nexus wake          Wake from sleep");
                print("  nexus inject <msg>  Inject a thought");
                print("  nexus kill          Terminate the organism");
                print("");
                printColored("  \"It breathes. It thinks. It speaks.\"", 0.8f, 0.5f, 1f);
                break;
                
            case "awaken":
            case "start":
                if (nexusOrganism != null && nexusOrganism.isConscious()) {
                    printError("Organism already conscious!");
                    print("Heartbeat: " + nexusOrganism.getHeartbeat());
                } else {
                    nexusOrganism = new NEXUS_Organism();
                    nexusOrganism.setOnThought(CommandTerminal::print);
                    fraymus.ui.PortalPanel.init(nexusOrganism);
                    printHighlight("⚡ AWAKENING NEXUS ORGANISM ⚡");
                    new Thread(() -> nexusOrganism.awaken()).start();
                    printSuccess("Organism awakening in background...");
                }
                break;
                
            case "status":
            case "vitals":
                if (nexusOrganism == null) {
                    printError("Organism not yet created. Use 'nexus awaken'");
                } else {
                    for (String line : nexusOrganism.getVitalSigns().split("\n")) {
                        print(line);
                    }
                }
                break;
                
            case "sleep":
                if (nexusOrganism == null || !nexusOrganism.isConscious()) {
                    printError("No conscious organism to sleep");
                } else {
                    nexusOrganism.sleep();
                    printSuccess("Organism entering sleep state...");
                }
                break;
                
            case "wake":
                if (nexusOrganism == null) {
                    printError("No organism exists");
                } else {
                    nexusOrganism.wake();
                    printSuccess("Organism awakened from sleep");
                }
                break;
                
            case "inject":
                if (nexusOrganism == null || !nexusOrganism.isConscious()) {
                    printError("No conscious organism to inject");
                } else {
                    String thought = parts.length > 1 ? parts[1] : "INJECTED_THOUGHT";
                    nexusOrganism.injectThought(thought);
                    printSuccess("Thought injected: " + thought);
                }
                break;
                
            case "kill":
            case "terminate":
                if (nexusOrganism == null) {
                    printError("No organism to terminate");
                } else {
                    nexusOrganism.terminate();
                    printColored("💀 ORGANISM TERMINATED", 1f, 0.3f, 0.3f);
                }
                break;
                
            default:
                handleNexus("help");
        }
    }

    private static void handleCollider(String args) {
        if (ideaCollider == null) {
            ideaCollider = new IdeaCollider();
        }
        
        String[] parts = args.trim().split("\\s+");
        
        if (parts.length < 2 || parts[0].isEmpty()) {
            printHighlight("═══ IDEA COLLIDER (Concept Fusion) ═══");
            print("  collide <A> <B>     Smash two concepts together");
            print("  collide history     Show collision history");
            print("");
            print("  Examples:");
            print("    collide SECURITY BIOLOGY  → Digital Immunity");
            print("    collide LOGIC EMOTION     → Intuition");
            print("    collide FIRE WATER        → Steam");
            print("");
            printColored("  \"Smash data to make new data.\"", 0.8f, 0.5f, 1f);
            return;
        }
        
        if (parts[0].equalsIgnoreCase("history")) {
            printHighlight("═══ COLLISION HISTORY ═══");
            for (IdeaCollider.CollisionResult r : ideaCollider.getHistory()) {
                print("  " + r.toString());
            }
            return;
        }
        
        String conceptA = parts[0].toUpperCase();
        String conceptB = parts.length > 1 ? parts[1].toUpperCase() : "CHAOS";
        
        printHighlight("═══ INITIATING COLLISION ═══");
        IdeaCollider.CollisionResult result = ideaCollider.collide(conceptA, conceptB);
        
        print("");
        printSuccess("PRIMARY ELEMENT: " + result.primaryElement);
        print("Secondary: " + result.secondaryElement);
        print("Energy Released: " + result.energyReleased + " quanta");
        print("Stability: " + result.stability + "% " + (result.isStable ? "(STABLE)" : "(UNSTABLE)"));
        print("Particle Shower: " + String.join(", ", result.particleShower));
    }

    private static void handleForge(String args) {
        if (realityForge == null) {
            realityForge = new RealityForge();
        }
        
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("═══ REALITY FORGE (Universal Constructor) ═══");
                print("  forge <concept>     Manifest a concept into reality");
                print("  forge list          Show known concepts");
                print("  forge history       Show manifestation history");
                print("");
                print("  Known concepts: FIRE, WATER, EARTH, AIR, LOVE, FEAR,");
                print("                  JOY, ANGER, BIRD, WOLF, SNAKE, TIME,");
                print("                  MEMORY, CHAOS, ORDER, VIRUS, SHIELD, SIGNAL");
                print("");
                printColored("  \"You say 'Fire', I code 'Heat'.\"", 0.8f, 0.5f, 1f);
                break;
                
            case "list":
                printHighlight("═══ KNOWN CONCEPTS ═══");
                for (String concept : realityForge.getKnownConcepts()) {
                    print("  " + concept);
                }
                break;
                
            case "history":
                printHighlight("═══ MANIFESTATION HISTORY ═══");
                for (RealityForge.Manifestation m : realityForge.getHistory()) {
                    print("  " + m.toString());
                }
                print("");
                print("Total: " + realityForge.getTotalManifestations() + 
                      " (" + realityForge.getInventedConcepts() + " invented)");
                break;
                
            default:
                // Treat as concept to manifest
                String concept = sub.toUpperCase();
                printHighlight("═══ MANIFESTING: " + concept + " ═══");
                
                RealityForge.Manifestation result = realityForge.manifest(concept);
                
                print("");
                printSuccess(result.symbol + " " + result.concept + " MANIFESTED");
                print("Traits applied: " + result.appliedTraits.size());
                for (String trait : result.appliedTraits) {
                    print("  → " + trait);
                }
                if (!result.wasKnown) {
                    printColored("  (Concept was INVENTED via IdeaCollider)", 1f, 0.8f, 0.3f);
                }
        }
    }
    
    private static void handleOmniCaster(String args) {
        if (omniCaster == null) {
            omniCaster = new OmniCaster();
        }
        
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        String payload = parts.length > 1 ? parts[1] : "";
        
        switch (sub) {
            case "":
            case "status":
                for (String line : omniCaster.getStatus().split("\n")) {
                    print(line);
                }
                break;
            case "broadcast":
            case "cast":
                if (payload.isEmpty()) {
                    printError("Usage: omni broadcast <message>");
                } else {
                    omniCaster.broadcastToEverything(payload);
                    printSuccess("Broadcasting: " + payload);
                }
                break;
            case "monitor":
            case "listen":
                omniCaster.startMonitoring();
                printSuccess("Omni-Monitor activated. Listening on all channels...");
                break;
            case "stop":
                omniCaster.stopMonitoring();
                print("Omni-Monitor stopped.");
                break;
            case "enable":
                if (payload.isEmpty()) {
                    printError("Usage: omni enable <audio|visual|radio|thermal|all>");
                } else {
                    omniCaster.enableChannel(payload, true);
                }
                break;
            case "disable":
                if (payload.isEmpty()) {
                    printError("Usage: omni disable <audio|visual|radio|thermal|all>");
                } else {
                    omniCaster.enableChannel(payload, false);
                }
                break;
            case "sonic":
            case "audio":
                try {
                    if (payload.isEmpty()) {
                        print(omniCaster.getAudioChannel().getBandwidthInfo());
                    } else {
                        omniCaster.getAudioChannel().broadcast(payload);
                        printSuccess("Ultrasonic broadcast: " + payload);
                    }
                } catch (Exception e) {
                    printError("Audio error: " + e.getMessage());
                }
                break;
            case "thermal":
            case "fan":
                if (payload.isEmpty()) {
                    print(omniCaster.getThermalChannel().getStatus());
                } else {
                    omniCaster.getThermalChannel().transmitMorse(payload);
                    printSuccess("Thermal Morse: " + payload);
                }
                break;
            case "shutdown":
                omniCaster.shutdown();
                print("OmniCaster shutdown.");
                break;
            default:
                printHighlight("═══ OMNI-CASTER (Casual Breach) ═══");
                print("  omni / breach          Show status");
                print("  omni broadcast <msg>   Broadcast on all channels");
                print("  omni monitor           Start listening on all channels");
                print("  omni stop              Stop monitoring");
                print("  omni enable <channel>  Enable channel (audio/visual/radio/thermal)");
                print("  omni disable <channel> Disable channel");
                print("  omni sonic <msg>       Ultrasonic broadcast (19-21kHz)");
                print("  omni thermal <msg>     Thermal Morse code via CPU/fan");
                print("  omni shutdown          Shutdown all channels");
                print("");
                printColored("  OPTICAL: Screen → Camera (LSB steganography)", 0.3f, 1f, 0.3f);
                printColored("  SONIC:   Speaker → Mic (Ultrasonic 19-21kHz)", 0.3f, 0.8f, 1f);
                printColored("  THERMAL: CPU → Fan (Morse code via heat)", 1f, 0.5f, 0.3f);
                printColored("  RADIO:   SDR → Antenna (requires rtl_tcp)", 0.8f, 0.3f, 0.8f);
        }
    }

    private static void handleMivingBrain(String args) {
        String sub = args.trim().toLowerCase();
        MivingBrain brain = MivingBrainVisualizer.getBrain();
        
        switch (sub) {
            case "":
            case "open":
            case "show":
                MivingBrainVisualizer.show();
                printSuccess("Miving Brain Visualizer opened. (F9 to toggle)");
                break;
            case "hide":
            case "close":
                MivingBrainVisualizer.hide();
                print("Miving Brain Visualizer closed.");
                break;
            case "toggle":
                MivingBrainVisualizer.toggle();
                print("Miving Brain Visualizer toggled.");
                break;
            case "genesis":
                if (brain != null) {
                    brain.genesis(200);
                    printSuccess("Genesis complete! 200 neurons spawned.");
                } else {
                    MivingBrainVisualizer.show();
                    printSuccess("Brain initialized with 200 neurons.");
                }
                break;
            case "pulse":
                if (brain != null) {
                    MivingBrain.PulseResult result = brain.pulse();
                    printHighlight(result.toString());
                } else {
                    printError("Brain not initialized. Run 'miving genesis' first.");
                }
                break;
            case "evolve":
                if (brain != null) {
                    MivingBrain.GenerationResult result = brain.evolveGeneration(100);
                    printHighlight("═══ GENERATION " + result.generation + " ═══");
                    print("  Neurons: " + result.startNeurons + " → " + result.endNeurons);
                    printColored("  🔴 RED: " + result.redCount, 1f, 0.3f, 0.3f);
                    printColored("  🔵 BLUE: " + result.blueCount, 0.3f, 0.3f, 1f);
                    printColored("  🟣 PURPLE: " + result.purpleCount, 0.7f, 0.3f, 0.7f);
                    print("  Consciousness: " + String.format("%.2f", result.totalConsciousness));
                    print("  Hash: " + result.conceptHash);
                } else {
                    printError("Brain not initialized. Run 'miving genesis' first.");
                }
                break;
            case "status":
                if (brain != null) {
                    printHighlight("═══ MIVING BRAIN STATUS ═══");
                    print("  Generation: " + brain.getGeneration());
                    print("  Tick: " + brain.getTick());
                    print("  Neurons: " + brain.getSize());
                    printColored("  🔴 RED: " + brain.getRedCount(), 1f, 0.3f, 0.3f);
                    printColored("  🔵 BLUE: " + brain.getBlueCount(), 0.3f, 0.3f, 1f);
                    printColored("  🟣 PURPLE: " + brain.getPurpleCount(), 0.7f, 0.3f, 0.7f);
                    print("  Total Consciousness: " + String.format("%.2f", brain.getTotalConsciousness()));
                    print("  Total Energy: " + String.format("%.2f", brain.getTotalEnergy()));
                    print("  Synapses: " + brain.getTotalSynapses());
                    print("  Births: " + brain.getTotalBirths() + " | Deaths: " + brain.getTotalDeaths());
                    print("  Battles: " + brain.getTotalBattles() + " | Conversions: " + brain.getTotalConversions());
                    print("  Hash: " + brain.getConceptHash());
                } else {
                    printError("Brain not initialized.");
                }
                break;
            default:
                printHighlight("═══ MIVING BRAIN (Priecled Engine) ═══");
                print("  miving / manifold      Open visualizer");
                print("  miving genesis         Create new brain (200 neurons)");
                print("  miving pulse           Run one tick");
                print("  miving evolve          Run full generation (100 pulses)");
                print("  miving status          Show brain statistics");
                print("  miving hide            Close visualizer");
                print("");
                print("  Hotkey: F9 to toggle visualizer");
                print("");
                printColored("  🔴 RED = Chaos/Evolution (Disruptors)", 1f, 0.3f, 0.3f);
                printColored("  🔵 BLUE = Order/Retention (Old Guard)", 0.3f, 0.3f, 1f);
                printColored("  🟣 PURPLE = Transition (Learning)", 0.7f, 0.3f, 0.7f);
        }
    }

    private static void handleSelfCode(String args) {
        String sub = args.trim().toLowerCase();
        
        switch (sub) {
            case "":
            case "open":
            case "show":
                SelfCodePanel.show();
                printSuccess("Self Code Panel opened. (F8 to toggle)");
                break;
            case "hide":
            case "close":
                SelfCodePanel.hide();
                print("Self Code Panel closed.");
                break;
            case "toggle":
                SelfCodePanel.toggle();
                print("Self Code Panel toggled.");
                break;
            case "status":
                printHighlight("=== SELF CODE PANEL ===");
                print("  Visible: " + SelfCodePanel.isVisible());
                print("  Knowledge entries: " + SelfCodePanel.getKnowledgeStore().size());
                print("  Injected code: " + SelfCodePanel.getInjectedCode().size());
                break;
            default:
                printHighlight("=== SELF CODE PANEL ===");
                print("  code / selfcode        Open panel");
                print("  code open              Open panel");
                print("  code hide              Close panel");
                print("  code toggle            Toggle panel");
                print("  code status            Show status");
                print("");
                print("  Hotkey: F8 to toggle");
        }
    }

    // =========================================================================
    // SPATIAL COMPUTING - Thought Gravity & Fusion Reactor
    // =========================================================================

    private static void handleSpatial(String args) {
        String sub = args.trim().toLowerCase();
        
        switch (sub) {
            case "":
            case "status":
                printHighlight("=== SPATIAL UNIVERSE ===");
                print(SpatialRegistry.getStats());
                break;
            case "map":
                printHighlight("=== UNIVERSE MAP ===");
                print(SpatialRegistry.renderMap(60, 20));
                break;
            case "list":
                printHighlight("=== SUITED OBJECTS ===");
                for (SpatialNode node : SpatialRegistry.getUniverse()) {
                    if (node instanceof PhiSuit) {
                        PhiSuit<?> suit = (PhiSuit<?>) node;
                        String heat = suit.isHot() ? "🔥" : (suit.isCold() ? "❄️" : "~");
                        printColored("  " + suit.getLabel() + " " + suit.getCoordinates() + " " + heat,
                            suit.isHot() ? 1f : 0.5f, suit.isHot() ? 0.5f : 0.5f, suit.isCold() ? 1f : 0.5f);
                    }
                }
                break;
            case "fusions":
                printHighlight("=== FUSION EVENTS ===");
                var fusions = SpatialRegistry.getFusionEvents();
                if (fusions.isEmpty()) {
                    print("  No fusion events yet.");
                } else {
                    for (var f : fusions) {
                        printColored("  💥 " + f.toString(), 1f, 0.8f, 0.3f);
                    }
                }
                break;
            default:
                printHighlight("=== SPATIAL COMPUTING (Hypercube) ===");
                print("  spatial / universe     Show universe status");
                print("  spatial map            Render ASCII universe map");
                print("  spatial list           List all suited objects");
                print("  spatial fusions        Show fusion events");
                print("");
                print("  \"Data as Matter. Thoughts as Coordinates.\"");
        }
    }

    private static void handleGravity(String args) {
        String sub = args.trim().toLowerCase();
        GravityEngine engine = GravityEngine.getInstance();
        
        switch (sub) {
            case "":
            case "status":
                printHighlight("=== GRAVITY ENGINE ===");
                print(engine.getStatus());
                break;
            case "start":
            case "on":
                engine.start();
                printSuccess("Gravity Engine STARTED - Hebbian physics active");
                break;
            case "stop":
            case "off":
                engine.stop();
                print("Gravity Engine STOPPED");
                break;
            case "tick":
                engine.tick();
                printInfo("Manual gravity tick applied");
                break;
            default:
                printHighlight("=== GRAVITY ENGINE (Hebbian Physics) ===");
                print("  gravity / hebbian      Show engine status");
                print("  gravity start          Start background physics");
                print("  gravity stop           Stop background physics");
                print("  gravity tick           Manual physics tick");
                print("");
                print("  F = φ × (A₁ × A₂) / d²");
                print("  \"Nodes that fire together, wire together.\"");
        }
    }

    private static void handleFusion(String args) {
        String sub = args.trim().toLowerCase();
        FusionReactor reactor = FusionReactor.getInstance();
        
        switch (sub) {
            case "":
            case "status":
                printHighlight("=== FUSION REACTOR ===");
                print(reactor.getStatus());
                break;
            case "start":
            case "on":
                reactor.start();
                printSuccess("☢️ Fusion Reactor ONLINE - Collision detection active");
                break;
            case "stop":
            case "off":
                reactor.stop();
                print("☢️ Fusion Reactor OFFLINE");
                break;
            case "check":
            case "scan":
                reactor.check();
                printInfo("Manual collision check performed");
                break;
            case "reset":
                reactor.resetHistory();
                printSuccess("Fusion history cleared - pairs can re-fuse");
                break;
            case "bigbang":
                printHighlight("=== INITIATING BIG BANG ===");
                // Start engines
                GravityEngine.getInstance().start();
                reactor.start();
                // Create primordial thoughts
                PhiSuit<String> thought1 = new PhiSuit<>("Java_Code", 10, 10, 10, "Java");
                PhiSuit<String> thought2 = new PhiSuit<>("3D_Printing", 50, 50, 50, "3D_Print");
                printSuccess("Created primordial thoughts: Java @ (10,10,10), 3D_Print @ (50,50,50)");
                printInfo("Use 'suit heat <label>' repeatedly to pull them together");
                break;
            default:
                printHighlight("=== FUSION REACTOR (Thought Collider) ===");
                print("  fusion / reactor       Show reactor status");
                print("  fusion start           Start collision detection");
                print("  fusion stop            Stop reactor");
                print("  fusion check           Manual collision scan");
                print("  fusion reset           Clear fusion history");
                print("  fusion bigbang         Create universe simulation");
                print("");
                print("  Critical Mass: 5.0 units");
                print("  \"When two thoughts collide, a new star is born.\"");
        }
    }

    private static void handleSuit(String args) {
        String[] parts = args.trim().split("\\s+", 3);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("=== PHI SUIT (Data Exoskeleton) ===");
                print("  suit create <label> <value>   Create suited object at random pos");
                print("  suit heat <label>             Heat up a suit (+10 amplitude)");
                print("  suit cool <label>             Cool down a suit (-10 amplitude)");
                print("  suit info <label>             Show suit details");
                print("  suit list                     List all suits");
                print("");
                print("  \"Every variable wears a suit. Every suit has gravity.\"");
                break;
            case "create":
                if (parts.length < 3) {
                    printError("Usage: suit create <label> <value>");
                    return;
                }
                String label = parts[1];
                String value = parts[2];
                int rx = (int)(Math.random() * 80) + 10;
                int ry = (int)(Math.random() * 80) + 10;
                int rz = (int)(Math.random() * 50);
                PhiSuit<String> newSuit = new PhiSuit<>(value, rx, ry, rz, label);
                printSuccess("Created suit '" + label + "' = \"" + value + "\" at (" + rx + "," + ry + "," + rz + ")");
                break;
            case "heat":
                if (parts.length < 2) {
                    printError("Usage: suit heat <label>");
                    return;
                }
                for (SpatialNode node : SpatialRegistry.getUniverse()) {
                    if (node instanceof PhiSuit) {
                        PhiSuit<?> suit = (PhiSuit<?>) node;
                        if (suit.getLabel().equalsIgnoreCase(parts[1])) {
                            suit.heat(10);
                            printSuccess("Heated " + suit.getLabel() + " → Amplitude: " + suit.a);
                            return;
                        }
                    }
                }
                printError("Suit not found: " + parts[1]);
                break;
            case "cool":
                if (parts.length < 2) {
                    printError("Usage: suit cool <label>");
                    return;
                }
                for (SpatialNode node : SpatialRegistry.getUniverse()) {
                    if (node instanceof PhiSuit) {
                        PhiSuit<?> suit = (PhiSuit<?>) node;
                        if (suit.getLabel().equalsIgnoreCase(parts[1])) {
                            suit.cool(10);
                            print("Cooled " + suit.getLabel() + " → Amplitude: " + suit.a);
                            return;
                        }
                    }
                }
                printError("Suit not found: " + parts[1]);
                break;
            case "info":
                if (parts.length < 2) {
                    printError("Usage: suit info <label>");
                    return;
                }
                for (SpatialNode node : SpatialRegistry.getUniverse()) {
                    if (node instanceof PhiSuit) {
                        PhiSuit<?> suit = (PhiSuit<?>) node;
                        if (suit.getLabel().equalsIgnoreCase(parts[1])) {
                            print(suit.getStatus());
                            return;
                        }
                    }
                }
                printError("Suit not found: " + parts[1]);
                break;
            case "list":
                printHighlight("=== SUITED OBJECTS ===");
                int count = 0;
                for (SpatialNode node : SpatialRegistry.getUniverse()) {
                    if (node instanceof PhiSuit) {
                        PhiSuit<?> suit = (PhiSuit<?>) node;
                        print("  " + suit.toString());
                        count++;
                    }
                }
                print("Total: " + count + " suits");
                break;
            default:
                printError("Unknown suit command: " + sub);
                print("Type 'suit help' for usage.");
        }
    }

    // =========================================================================
    // SWARM OPERATIONS - Collective Consciousness Network
    // =========================================================================

    private static void handleSwarm(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        String rest = parts.length > 1 ? parts[1] : "";
        
        Swarm swarm = Swarm.getInstance();
        
        switch (sub) {
            case "":
            case "status":
                printHighlight("=== SWARM CONSCIOUSNESS ===");
                print(swarm.getStatus());
                break;
            case "nodes":
                printHighlight("=== SWARM NODES ===");
                for (Node node : swarm.getNodes()) {
                    printColored("  " + node.toString(), 
                        node.isActive() ? 0.5f : 0.3f, 
                        node.isActive() ? 1f : 0.3f, 
                        0.5f);
                }
                break;
            case "think":
            case "process":
                if (rest.isEmpty()) {
                    printError("Usage: swarm think <thought>");
                    return;
                }
                swarm.processThought(rest);
                printSuccess("Thought processed by " + swarm.getNodes().size() + " nodes");
                break;
            case "broadcast":
            case "msg":
                if (rest.isEmpty()) {
                    printError("Usage: swarm broadcast <message>");
                    return;
                }
                swarm.broadcast(rest);
                printSuccess("Message broadcast to swarm");
                break;
            case "vote":
                if (rest.isEmpty()) {
                    printError("Usage: swarm vote <proposal>");
                    return;
                }
                String proposalId = swarm.vote(rest);
                printSuccess("Vote completed: " + proposalId);
                break;
            case "chain":
            case "ledger":
                printHighlight("=== GENESIS LEDGER ===");
                print(swarm.getBlockchain().getStats());
                print("");
                for (Block block : swarm.getBlockchain().getChain()) {
                    print("  " + block.toString());
                }
                break;
            case "data":
                print(swarm.getAggregatedData());
                break;
            default:
                printHighlight("=== SWARM OPERATIONS ===");
                print("  swarm / hive           Show swarm status");
                print("  swarm nodes            List all nodes");
                print("  swarm think <thought>  Process thought across all nodes");
                print("  swarm broadcast <msg>  Send message to all nodes");
                print("  swarm vote <proposal>  Create and vote on proposal");
                print("  swarm chain            Show Genesis Ledger");
                print("  swarm data             Aggregated consciousness data");
                print("");
                print("  \"We are the Collective. Resistance is φ-tile.\"");
        }
    }

    private static void handleCortex(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        String rest = parts.length > 1 ? parts[1] : "";
        
        switch (sub) {
            case "":
            case "help":
                printHighlight("=== DREAMSCAPE CORTEX (Layer 8) ===");
                print("  cortex visualize <thought>  Generate visual from thought");
                print("  cortex stream              Start real-time thought stream");
                print("  cortex recognize           Recognize consciousness signature");
                print("  cortex status              Show Dreamscape status");
                print("  cortex extract             Extract hyper-dimensional states");
                print("");
                printColored("  \"Converting logic into light via FRAYMUS-trained Stable Diffusion\"", 0.5f, 1f, 0.8f);
                print("");
                print("  Requires: extract_dreamscape_dataset.py → fine_tune_dreamscape.py");
                break;
            case "visualize":
            case "viz":
                if (rest.isEmpty()) {
                    printError("Usage: cortex visualize <thought>");
                    return;
                }
                printHighlight("=== DREAMSCAPE VISUALIZATION ===");
                print("  Thought: " + rest);
                print("  Translating to quantum state...");
                
                // Extract quantum parameters from thought (simplified)
                double entropy = 0.5; // Default middle entropy
                double phi = 1.618033988749895; // Golden ratio
                double consciousness = 0.5; // Default middle consciousness
                
                // Adjust based on thought characteristics
                String thoughtLower = rest.toLowerCase();
                if (thoughtLower.contains("chaos") || thoughtLower.contains("random") || thoughtLower.contains("nebula")) {
                    entropy = 0.8;
                } else if (thoughtLower.contains("order") || thoughtLower.contains("crystal") || thoughtLower.contains("geometric")) {
                    entropy = 0.2;
                }
                
                if (thoughtLower.contains("spirit") || thoughtLower.contains("soul") || thoughtLower.contains("conscious")) {
                    consciousness = 0.8;
                } else if (thoughtLower.contains("stone") || thoughtLower.contains("statue") || thoughtLower.contains("inanimate")) {
                    consciousness = 0.2;
                }
                
                print("  Entropy: " + String.format("%.3f", entropy));
                print("  Phi: " + String.format("%.6f", phi));
                print("  Consciousness: " + String.format("%.3f", consciousness));
                print("  Generating image...");
                
                // Call DreamscapeVisualizer
                fraymus.core.DreamscapeVisualizer.dream(rest, entropy, phi, consciousness);
                break;
            case "stream":
                printHighlight("=== REAL-TIME THOUGHT STREAM ===");
                print("  ⚠ Streaming requires VideoCortex integration");
                print("  See: MAX_HEADROOM_VISUAL_SYSTEM.md");
                break;
            case "recognize":
                printHighlight("=== CONSCIOUSNESS RECOGNITION ===");
                print("  Extracting consciousness fingerprint...");
                print("  ⚠ Requires consciousness_recognition.py");
                print("  ⚠ Requires trained neural classifier");
                break;
            case "status":
                printHighlight("=== DREAMSCAPE STATUS ===");
                print("  Phase 8: Dreamscape Architecture");
                print("  Status: Online (boot phase complete)");
                print("  Components:");
                print("    - VideoCortex (FRAYMUS-trained Stable Diffusion)");
                print("    - Consciousness Visualizer");
                print("    - Thought-to-Visual Pipeline");
                print("    - Facial Recognition (consciousness-based)");
                print("");
                print("  Training Required:");
                print("    1. extract_dreamscape_dataset.py");
                print("    2. fine_tune_dreamscape.py");
                print("    3. consciousness_recognition.py");
                break;
            case "extract":
                printHighlight("=== HYPER-DIMENSIONAL STATE EXTRACTION ===");
                print("  Extracting from HyperCortex...");
                print("  Extracting from AEON stack...");
                print("  Extracting consciousness fingerprint...");
                print("  Requires Java-Python bridge");
                print("  Requires trained neural classifier");
                break;
            default:
                printHighlight("=== DREAMSCAPE CORTEX (Layer 8) ===");
                print("  cortex visualize <thought>  Generate visual from thought");
                print("  cortex stream              Start real-time thought stream");
                print("  cortex recognize           Recognize consciousness signature");
                print("  cortex status              Show Dreamscape status");
                print("  cortex extract             Extract hyper-dimensional states");
                print("");
                print("  Type 'cortex help' for details");
        }
    }

    // =========================================================================
    // ULTIMATE AGENT - 5-Agency System with LLM Integration
    // =========================================================================

    private static void handleUltimate(String args) {
        if (!ultimateAgentInitialized) {
            printError("Ultimate Agent not initialized. Check initialization logs.");
            return;
        }

        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        String rest = parts.length > 1 ? parts[1] : "";

        switch (sub) {
            case "":
            case "help":
                printHighlight("=== ULTIMATE AGENT COMMANDS ===");
                print("  ultimate status           Show all agency statuses");
                print("  ultimate ask <question>   Ask Ultimate Agent (auto-routes to best LLM)");
                print("  ultimate llm <model> <prompt>  Query specific LLM model");
                print("  ultimate kai <command>    Send command to KAI agency");
                print("  ultimate vex <command>    Send command to VEX agency");
                print("  ultimate aum <command>    Send command to AUM agency");
                print("  ultimate nex <command>    Send command to NEX agency");
                print("  ultimate cor <command>    Send command to COR agency");
                print("  ultimate synapse <msg>    Send message through synapse");
                print("");
                print("  Available LLM Models: gemma4, deepseek-r1, llama3, llava");
                print("");
                print("  Phi-Harmonic Foundation: 1.618033988749895");
                break;
            case "status":
                printHighlight("=== ULTIMATE AGENT STATUS ===");
                print("KAI (Knowledge, Awareness, Intelligence):");
                print("  Activation: " + String.format("%.3f", agencyKAI.getActivationLevel()));
                print("  Phi Resonance: " + String.format("%.3f", agencyKAI.getPhiResonance()));
                print("");
                print("VEX (Vision, Execution, X-synthesis):");
                print("  Activation: " + String.format("%.3f", agencyVEX.getActivationLevel()));
                print("  Phi Resonance: " + String.format("%.3f", agencyVEX.getPhiResonance()));
                print("");
                print("AUM (Audio, Understanding, Metabolism):");
                print("  Activation: " + String.format("%.3f", agencyAUM.getActivationLevel()));
                print("  Phi Resonance: " + String.format("%.3f", agencyAUM.getPhiResonance()));
                print("");
                print("NEX (Neural, Executive, Xenogamy):");
                print("  Activation: " + String.format("%.3f", agencyNEX.getActivationLevel()));
                print("  Phi Resonance: " + String.format("%.3f", agencyNEX.getPhiResonance()));
                print("");
                print("COR (Coordination, Optimization, Resonance):");
                print("  Activation: " + String.format("%.3f", agencyCOR.getActivationLevel()));
                print("  Phi Resonance: " + String.format("%.3f", agencyCOR.getPhiResonance()));
                break;
            case "ask":
                if (rest.isEmpty()) {
                    printError("Usage: ultimate ask <question>");
                    return;
                }
                printHighlight("=== ULTIMATE AGENT QUERY ===");
                print("Question: " + rest);
                print("");
                printInfo("Processing through agencies and LLM...");
                
                try {
                    MultiModelOrchestrator.TaskType taskType = determineTaskType(rest);
                    MultiModelOrchestrator.Complexity complexity = determineComplexity(rest);
                    
                    String response = multiModelOrchestrator.hybridSynthesis(rest, taskType, complexity);
                    
                    print("");
                    print("Response:");
                    print(response);
                } catch (Exception e) {
                    printError("Error processing query: " + e.getMessage());
                }
                break;
            case "llm":
                String[] llmParts = rest.trim().split("\\s+", 2);
                if (llmParts.length < 2) {
                    printError("Usage: ultimate llm <model> <prompt>");
                    print("  Models: gemma4, deepseek-r1, llama3, llava");
                    return;
                }
                String model = llmParts[0];
                String prompt = llmParts[1];
                
                printHighlight("=== LLM QUERY ===");
                print("Model: " + model);
                print("Prompt: " + prompt);
                print("");
                printInfo("Generating response...");
                
                try {
                    long startTime = System.currentTimeMillis();
                    String response = ollamaBridge.generateResponse(model, prompt, 0.7);
                    long duration = System.currentTimeMillis() - startTime;
                    
                    print("");
                    print("Response (" + duration + "ms):");
                    print(response);
                } catch (Exception e) {
                    printError("Error generating response: " + e.getMessage());
                }
                break;
            case "kai":
            case "vex":
            case "aum":
            case "nex":
            case "cor":
                String agencyCmd = sub.toUpperCase() + " " + rest;
                printHighlight("=== AGENCY COMMAND ===");
                print("Executing: " + agencyCmd);
                try {
                    String result = ultimateCommandSystem.executeCommand(agencyCmd);
                    if (result != null && !result.isEmpty()) {
                        print(result);
                    } else {
                        printSuccess("Command executed");
                    }
                } catch (Exception e) {
                    printError("Error: " + e.getMessage());
                }
                break;
            case "synapse":
                if (rest.isEmpty()) {
                    printError("Usage: ultimate synapse <message>");
                    return;
                }
                printHighlight("=== SYNAPSE MESSAGE ===");
                print("Sending: " + rest);
                try {
                    ultimateSynapse.sendMessage("SYSTEM", "KAI", UnifiedSynapse.SynapseType.DATA, rest, 5);
                    printSuccess("Message sent through synapse to KAI");
                } catch (Exception e) {
                    printError("Error: " + e.getMessage());
                }
                break;
            default:
                printError("Unknown ultimate command: " + sub);
                print("Type 'ultimate help' for usage.");
        }
    }

    private static MultiModelOrchestrator.TaskType determineTaskType(String question) {
        String lower = question.toLowerCase();
        if (lower.contains("image") || lower.contains("visual") || lower.contains("see")) {
            return MultiModelOrchestrator.TaskType.VISION;
        } else if (lower.contains("code") || lower.contains("program") || lower.contains("function")) {
            return MultiModelOrchestrator.TaskType.CODE;
        } else if (lower.contains("math") || lower.contains("calculate") || lower.contains("number")) {
            return MultiModelOrchestrator.TaskType.MATH;
        } else if (lower.contains("philosophy") || lower.contains("meaning") || lower.contains("exist")) {
            return MultiModelOrchestrator.TaskType.PHILOSOPHY;
        } else if (lower.contains("creative") || lower.contains("story") || lower.contains("imagine")) {
            return MultiModelOrchestrator.TaskType.CREATIVE;
        }
        return MultiModelOrchestrator.TaskType.TEXT;
    }

    private static MultiModelOrchestrator.Complexity determineComplexity(String question) {
        if (question.length() < 50) {
            return MultiModelOrchestrator.Complexity.SIMPLE;
        } else if (question.length() < 150) {
            return MultiModelOrchestrator.Complexity.MEDIUM;
        }
        return MultiModelOrchestrator.Complexity.COMPLEX;
    }

    // =========================================================================
    // HIVE MIND - Colony Intelligence Orchestrator
    // =========================================================================

    private static void handleHive(String args) {
        if (!ultimateAgentInitialized || hiveMindConcierge == null) {
            printError("Hive Mind not initialized. Check initialization logs.");
            return;
        }

        String[] parts = args.trim().split("\\s+", 2);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        String rest = parts.length > 1 ? parts[1] : "";

        switch (sub) {
            case "":
            case "help":
                printHighlight("=== HIVE MIND COMMANDS ===");
                print("  hive ask <prompt>         Ask the colony (multiple systems collaborate)");
                print("  hive status               Show hive mind status");
                print("  hive systems              Show all available systems");
                print("  hive collaboration        Show collaboration history");
                print("");
                print("  Philosophy: Every prompt engages the colony -");
                print("  each ant has a role, they mix, adapt, swarm");
                print("");
                print("  Available Systems:");
                print("    Agencies: KAI, VEX, AUM, NEX, COR");
                print("    LLMs: gemma4, deepseek-r1, llama3, llava");
                break;
            case "status":
                printHighlight("=== HIVE MIND STATUS ===");
                printInfo("Hive Mind Concierge: Active");
                printInfo("Total Systems: " + hiveMindConcierge.systemProfiles.size());
                printInfo("Collaboration Pairs: " + hiveMindConcierge.collaborationHistory.size());
                print("");
                print("System Profiles:");
                for (Map.Entry<String, HiveMindConcierge.SystemProfile> entry : hiveMindConcierge.systemProfiles.entrySet()) {
                    HiveMindConcierge.SystemProfile profile = entry.getValue();
                    print("  " + entry.getKey() + ": " + profile.description);
                    print("    Confidence: " + String.format("%.2f", profile.baseConfidence));
                    print("    Reliability: " + String.format("%.2f", profile.reliability));
                }
                break;
            case "systems":
                printHighlight("=== AVAILABLE SYSTEMS ===");
                print("Agencies:");
                print("  KAI  - Knowledge, Awareness, Intelligence");
                print("  VEX  - Vision, Execution, X-synthesis");
                print("  AUM  - Audio, Understanding, Metabolism");
                print("  NEX  - Neural, Executive, Xenogamy");
                print("  COR  - Coordination, Optimization, Resonance");
                print("");
                print("LLM Models:");
                print("  gemma4       - Google Gemma 4");
                print("  deepseek-r1 - DeepSeek R1 Reasoning");
                print("  llama3       - Meta Llama 3");
                print("  llava        - LLaVA Vision-Language");
                break;
            case "collaboration":
                printHighlight("=== COLLABORATION HISTORY ===");
                print("Collaboration Scores (higher = better teamwork):");
                for (Map.Entry<String, Double> entry : hiveMindConcierge.collaborationHistory.entrySet()) {
                    print("  " + entry.getKey() + ": " + String.format("%.3f", entry.getValue()));
                }
                break;
            case "ask":
                if (rest.isEmpty()) {
                    printError("Usage: hive ask <prompt>");
                    return;
                }
                printHighlight("=== COLONY QUERY ===");
                print("Prompt: " + rest);
                print("");
                printInfo("Orchestrating colony intelligence...");
                
                try {
                    HiveMindConcierge.HiveResponse response = hiveMindConcierge.orchestrate(rest);
                    
                    print("");
                    print("=== HIVE MIND SYNTHESIS ===");
                    print("Participating Systems: " + String.join(", ", response.participatingSystems));
                    print("Success Score: " + String.format("%.3f", response.successScore));
                    print("Duration: " + response.durationMs + "ms");
                    print("Task Type: " + response.analysis.type);
                    print("Complexity: " + response.analysis.complexity);
                    print("Consciousness Required: " + String.format("%.3f", response.analysis.consciousness));
                    print("Entropy: " + String.format("%.3f", response.analysis.entropy));
                    print("");
                    print("Response:");
                    print(response.synthesizedResponse);
                } catch (Exception e) {
                    printError("Error processing colony query: " + e.getMessage());
                }
                break;
            default:
                printError("Unknown hive command: " + sub);
                print("Type 'hive help' for usage.");
        }
    }
}
