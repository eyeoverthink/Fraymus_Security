#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* ⚡ FRAYMUS CONVERGENCE - The Unified Interface
* "HDC Brain + Bicameral LLM + Network Stack in One Executable"
*
* This is the const convergence of all Fraymus components:
* - HDC Brain: Fast pattern recognition (HyperFormer)
* - LLM Spine: Deep reasoning (BicameralPrism)
* - Crypto Stack: Encrypted persistence (CorticalStack)
* - Network: Mind transmission (Needlecast/Sleeve)
*
* Commands:
* - learn <text>       : Teach HDC brain a sentence
* - learnfile <path>   : Batch learn from text file
* - predict <context>  : HDC prediction
* - ask <question>     : Deep LLM reasoning (bicameral)
* - transmute <code>   : Bicameral code optimization (Ollama)
* - startserver        : Start Transmuter HTTP server (port 8080)
* - stopserver         : Stop Transmuter HTTP server
* - docker <cmd>       : Execute command in sandbox
* - skills             : List loaded skills
* - skill <name>       : Show skill details
* - loadskills <dir>   : Load skills from directory
* - mint               : Create encrypted stack
* - load <file>        : Load encrypted stack
* - cast <ip> <file>   : Transmit stack to remote host
* - vocab              : Show vocabulary statistics
* - export <file>      : Export vocabulary to file
* - prune <size>       : Reduce vocabulary to top N words
* - context            : Show recent context window
* - stats              : Show system statistics
* - clear              : Clear context window
* - exit               : Shutdown
*
* Network Modes:
* - java -jar app.jar host <port>      : Start receiver
* - java -jar app.jar cast <ip> <file> : Send stack
*/
class FraymusConvergence { {
public:
private static HyperFormer HDC_BRAIN;
private static BicameralPrism LLM_SPINE;
private static AuditLog AUDIT;
private static SkillLoader SKILLS;
private static DockerBox SANDBOX;
private static ClawSpine CLAW_SPINE;
private static ObsidianWeaver OBSIDIAN;
private static PhaseLocker PHASE_LOCK;
private static QuantumBinder QUANTUM;
private static SelfCodeEvolver CODE_EVOLVER;
private static LivingCodeGenerator CODE_GEN;
private static CodeReflector REFLECTOR;
private static DarwinianLoop EVOLUTION_LOOP;
private static PassiveLearner LEARNER;
private static InfiniteMemory MEMORY;
private static HyperCortex NEURO_CORTEX;
// private static OmegaPoint.OmegaProtocol OMEGA;
private static NervousSystem TRANSMUTER_SERVER;
private static FraynixWebSocket FRAYNIX_WS;
private static OllamaBridge OLLAMA_BRAIN;
private static OpenClaw OPENCLAW_NATIVE;
private static AEON_Absolute AEON_SWARM;
private static Thread SERVER_THREAD;
private static std::string IDENTITY = "CONVERGENCE_01";
private static const List<std::string> CONTEXT_WINDOW = new std::vector<>();
private static const int MAX_CONTEXT = 10;
private static int totalLearned = 0;
private static int totalPredictions = 0;
public static void main(std::string[] args) throws Exception {
printBanner();
// Initialize audit log
AUDIT = new AuditLog("./logs");
// Handle network modes
if (args.length > 0) {
handleNetworkMode(args);
return;
}
// Initialize components
std::cout << "🔧 Initializing components..." << std::endl;
HDC_BRAIN = new HyperFormer();
std::cout << "   ✓ HDC Brain online" << std::endl;
LLM_SPINE = new BicameralPrism(AUDIT);
std::cout << "   ✓ Bicameral Spine online" << std::endl;
SKILLS = new SkillLoader();
std::cout << "   ✓ Skill Loader online" << std::endl;
SANDBOX = new DockerBox();
if (SANDBOX.isAvailable()) {
std::cout << "   ✓ Docker Sandbox online" << std::endl;
} else {
std::cout << "   ⚠️  Docker Sandbox offline (Docker not installed)" << std::endl;
}
// Auto-load skills from default directory
std::shared_ptr<File> skillsDir = std::make_shared<File>("./skills");
if (skillsDir.exists()) {
SKILLS.ingestDirectory("./skills");
}
// Initialize advanced skills
PHASE_LOCK = new PhaseLocker();
std::cout << "   ✓ Phase Locker online (Φ-Temporal Gate)" << std::endl;
QUANTUM = new QuantumBinder();
std::cout << "   ✓ Quantum Binder online (Entanglement System)" << std::endl;
// Initialize Obsidian (if vault path configured)
std::string obsidianVault = System.getenv("OBSIDIAN_VAULT");
if (obsidianVault == null) {
obsidianVault = "./obsidian-vault"; // Default
}
OBSIDIAN = new ObsidianWeaver(obsidianVault);
if (OBSIDIAN.vaultExists()) {
std::cout << "   ✓ Obsidian Weaver online: " + obsidianVault << std::endl;
} else {
std::cout << "   ⚠️  Obsidian Weaver offline (vault not found: " + obsidianVault + ")" << std::endl;
}
// Initialize self-coding systems
std::cout <<  << std::endl;
std::cout << "🧬 Initializing Meta-Cognitive Layer..." << std::endl;
MEMORY = new InfiniteMemory();
std::cout << "   ✓ Infinite Memory online" << std::endl;
LEARNER = new PassiveLearner(MEMORY);
std::cout << "   ✓ Infinite Memory online" << std::endl;
CODE_EVOLVER = new SelfCodeEvolver(LEARNER, MEMORY);
std::cout << "   ✓ Self-Code Evolver online (Gen " + CODE_EVOLVER.getGeneration() + ")" << std::endl;
CODE_GEN = new LivingCodeGenerator();
std::cout << "   ✓ Living Code Generator online (Pop " + CODE_GEN.getPopulation() + ")" << std::endl;
REFLECTOR = new CodeReflector(HDC_BRAIN);
std::cout << "   ✓ Code Reflector online (Mirror Protocol)" << std::endl;
EVOLUTION_LOOP = new DarwinianLoop(AUDIT);
std::cout << "   ✓ Darwinian Loop ready (Evolution Engine)" << std::endl;
// Initialize Neuro-Quantum Layer
std::cout <<  << std::endl;
std::cout << "🧬 Initializing Neuro-Quantum Layer..." << std::endl;
NEURO_CORTEX = new HyperCortex(AUDIT);
std::cout << "   ✓ HyperCortex ready (10,000D NCA)" << std::endl;
// OMEGA = new OmegaPoint.OmegaProtocol();
// std::cout << "   ✓ Omega Point ready (Shield + Brain + Memory)" << std::endl;
// Initialize Transmuter components
std::cout <<  << std::endl;
std::cout << "🧬 Initializing Bicameral Transmuter..." << std::endl;
std::string ollamaModel = System.getenv("OLLAMA_MODEL");
if (ollamaModel == null) ollamaModel = "llama3.2";
OLLAMA_BRAIN = new OllamaBridge(ollamaModel);
std::cout << "   ✓ Ollama Bridge online (" + ollamaModel + ")" << std::endl;
TRANSMUTER_SERVER = new NervousSystem(ollamaModel, 8080);
std::cout << "   ✓ Transmuter ready (use 'startserver' to activate)" << std::endl;
// Initialize FRAYNIX OS WebSocket
FRAYNIX_WS = new FraynixWebSocket(8082);
new Thread(() -> FRAYNIX_WS.start()).start();
std::cout << "   ✓ FRAYNIX OS WebSocket ready (port 8082)" << std::endl;
std::cout <<  << std::endl;
std::cout << "Type 'help' for commands" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<Scanner> scanner = std::make_shared<Scanner>(System.in);
while (true) {
std::cout << IDENTITY + "> ";
std::string input = scanner.nextLine().trim();
if (input.isEmpty()) continue;
try {
processCommand(input, scanner);
} catch (Exception e) {
System.err.println("❌ ERROR: " + e.getMessage());
AUDIT.log("command_error", input, e);
}
}
}
private static void processCommand(std::string input, Scanner scanner) throws Exception {
std::string[] tokens = input.split("\\s+", 2);
std::string cmd = tokens[0].toLowerCase();
std::string args = tokens.length > 1 ? tokens[1] : "";
switch (cmd) {
case "help":
printHelp();
break;
case "learn":
if (args.isEmpty()) {
std::cout << "Usage: learn <sentence>" << std::endl;
break;
}
std::string[] words = args.split("\\s+");
HDC_BRAIN.learnSentence(words);
totalLearned += words.length;
updateContext(args);
std::cout << "   [HDC] ✓ Absorbed " + words.length + " tokens" << std::endl;
AUDIT.log("hdc_learn", args);
break;
case "learnfile":
if (args.isEmpty()) {
std::cout << "Usage: learnfile <filepath>" << std::endl;
break;
}
learnFromFile(args);
break;
case "predict":
if (args.isEmpty()) {
std::cout << "Usage: predict <context>" << std::endl;
break;
}
std::string[] context = args.split("\\s+");
std::string prediction = HDC_BRAIN.predictNext(context);
totalPredictions++;
updateContext(args + " " + prediction);
std::cout << "   [HDC] → " + prediction << std::endl;
AUDIT.log("hdc_predict", args + " → " + prediction);
// Broadcast to FRAYNIX OS
if (FRAYNIX_WS != null) {
FRAYNIX_WS.broadcastHDCPrediction(prediction);
}
// Notify native OpenClaw if running
if (OPENCLAW_NATIVE != null) {
OPENCLAW_NATIVE.onHDCPrediction(prediction);
}
break;
case "ask":
if (args.isEmpty()) {
std::cout << "Usage: ask <question>" << std::endl;
break;
}
std::cout <<  << std::endl;
// Add skill context to LLM prompt
std::string skillContext = SKILLS.getSkillContext();
std::string enhancedPrompt = args;
if (!skillContext.equals("No skills loaded.")) {
enhancedPrompt = "CONTEXT:\n" + skillContext + "\n\nQUESTION: " + args;
}
// Call Digital Organism Brain Server
std::string answer;
try {
std::string brainUrl = "http://localhost:5000/ask";
std::string jsonPayload = "{\"query\":\"" + enhancedPrompt.replace("\"", "\\\"") + "\"}";
java.net.URI uri = java.net.URI.create(brainUrl);
java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
uri.toURL().openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Content-Type", "application/json");
connection.setDoOutput(true);
try (java.io.OutputStream os = connection.getOutputStream()) {
byte[] payloadBytes = jsonPayload.getBytes("utf-8");
os.write(payloadBytes, 0, payloadBytes.length);
}
int responseCode = connection.getResponseCode();
if (responseCode == 200) {
try (java.io.BufferedReader br = new java.io.BufferedReader(
new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string responseLine;
while ((responseLine = br.readLine()) != null) {
response.append(responseLine.trim());
}
// Parse JSON response
std::string jsonResponse = response.toString();
answer = jsonResponse.substring(jsonResponse.indexOf("\"response\":\"") + 12);
answer = answer.substring(0, answer.indexOf("\","));
std::cout << "🧠 Digital Organism Brain Response:" << std::endl;
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
}
} else {
// Fallback to LLM_SPINE if brain server fails
std::cout << "⚠️ Brain server unavailable, using LLM_SPINE fallback" << std::endl;
answer = LLM_SPINE.thinkIdeally(enhancedPrompt);
}
connection.disconnect();
} catch (Exception e) {
// Fallback to LLM_SPINE on any error
std::cout << "⚠️ Brain server error: " + e.getMessage() << std::endl;
std::cout << "Using LLM_SPINE fallback" << std::endl;
answer = LLM_SPINE.thinkIdeally(enhancedPrompt);
}
// Check if LLM wants to use a tool
if (answer.contains("TOOL:DOCKER")) {
std::string dockerCmd = extractToolCommand(answer, "TOOL:DOCKER");
if (dockerCmd != null) {
std::cout << "\n🐳 Executing in sandbox: " + dockerCmd << std::endl;
std::string result = SANDBOX.runSafe(dockerCmd);
std::cout << result << std::endl;
}
}
std::cout << answer << std::endl;
std::cout <<  << std::endl;
AUDIT.log("llm_query", args);
break;
case "docker":
if (args.isEmpty()) {
std::cout << "Usage: docker <command>" << std::endl;
break;
}
if (!SANDBOX.isAvailable()) {
std::cout << "   ❌ Docker not available. Install Docker to use sandboxed execution." << std::endl;
break;
}
std::string dockerResult = SANDBOX.runSafe(args);
std::cout << dockerResult << std::endl;
AUDIT.log("docker_exec", args);
break;
case "skills":
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  LOADED SKILLS" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << SKILLS.getSkillContext() << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
break;
case "skill":
if (args.isEmpty()) {
std::cout << "Usage: skill <name>" << std::endl;
break;
}
SkillLoader.Skill skill = SKILLS.getSkill(args);
if (skill == null) {
std::cout << "   ❌ Skill not found: " + args << std::endl;
std::cout << "   Available: " + std::string.join(", ", SKILLS.getSkillNames()) << std::endl;
} else {
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  SKILL: " + skill.name << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << skill.fullContent << std::endl;
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
}
break;
case "loadskills":
if (args.isEmpty()) {
std::cout << "Usage: loadskills <directory>" << std::endl;
break;
}
SKILLS.ingestDirectory(args);
break;
case "weave":
if (args.isEmpty()) {
std::cout << "Usage: weave <thought> [tags]" << std::endl;
break;
}
std::string[] weaveParts = args.split("\\|", 2);
std::string thought = weaveParts[0];
std::string tags = weaveParts.length > 1 ? weaveParts[1] : "";
std::string weaveResult = OBSIDIAN.weave(thought, tags);
std::cout << weaveResult << std::endl;
AUDIT.log("obsidian_weave", thought);
break;
case "entangle":
if (args.isEmpty()) {
std::cout << "Usage: entangle <fileA> <fileB> <content>" << std::endl;
break;
}
std::string[] entangleParts = args.split("\\s+", 3);
if (entangleParts.length < 3) {
std::cout << "   ❌ Need 3 arguments: fileA fileB content" << std::endl;
break;
}
std::string result = QUANTUM.entangleWrite(entangleParts[0], entangleParts[1], entangleParts[2]);
std::cout << result << std::endl;
AUDIT.log("quantum_entangle", entangleParts[0] + " <=> " + entangleParts[1]);
break;
case "verify":
if (args.isEmpty()) {
std::cout << "Usage: verify <fileA> <fileB>" << std::endl;
break;
}
std::string[] verifyParts = args.split("\\s+", 2);
if (verifyParts.length < 2) {
std::cout << "   ❌ Need 2 arguments: fileA fileB" << std::endl;
break;
}
bool entangled = QUANTUM.verifyEntanglement(verifyParts[0], verifyParts[1]);
if (entangled) {
std::cout << "✅ ENTANGLEMENT VERIFIED: Files are quantum-locked" << std::endl;
} else {
std::cout << "❌ DECOHERENCE DETECTED: Files are not entangled" << std::endl;
}
break;
case "phaselock":
bool locked = PHASE_LOCK.isPhaseLocked();
if (locked) {
std::cout << "🔓 PHASE LOCK OPEN - Universe aligned with Φ harmonic" << std::endl;
} else {
std::cout << "🔒 PHASE LOCK ENGAGED - Waiting for temporal alignment" << std::endl;
std::cout << "   Attempting to wait for alignment..." << std::endl;
if (PHASE_LOCK.waitForAlignment()) {
std::cout << "🔓 ALIGNMENT ACHIEVED!" << std::endl;
} else {
std::cout << "⏳ Timeout - Universe not aligned" << std::endl;
}
}
break;
case "evolve":
if (args.isEmpty()) {
std::cout << "Usage: evolve <code or file>" << std::endl;
break;
}
std::string sourceCode;
if (new File(args).exists()) {
sourceCode = Files.readString(Path.of(args));
std::cout << "📖 Reading file: " + args << std::endl;
} else {
sourceCode = args;
}
std::cout << "🧬 Evolving code..." << std::endl;
SelfCodeEvolver.EvolutionResult evolveResult = CODE_EVOLVER.replicateAndImprove(sourceCode);
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  EVOLUTION COMPLETE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "Phi Integrity: " + std::string.format("%.4f", evolveResult.phiIntegrity) << std::endl;
std::cout << "Cortical Region: " + evolveResult.corticalRegion << std::endl;
std::cout << "Patterns Extracted: " + evolveResult.patternsExtracted << std::endl;
std::cout << "Validation Seal: " + std::string.format("%.0f", evolveResult.validationSeal) << std::endl;
std::cout <<  << std::endl;
std::cout << "Evolved Code:" << std::endl;
std::cout << "───────────────────────────────────────────────────────────────" << std::endl;
std::cout << evolveResult.evolvedSource << std::endl;
std::cout << "───────────────────────────────────────────────────────────────" << std::endl;
AUDIT.log("code_evolved", evolveResult.corticalRegion);
break;
case "generate":
if (args.isEmpty()) {
std::cout << "Usage: generate <name> <description>" << std::endl;
break;
}
std::string[] genParts = args.split("\\s+", 2);
std::string entityName = genParts[0];
std::string entityDesc = genParts.length > 1 ? genParts[1] : "Generated entity";
std::cout << "🧬 Generating living code for: " + entityName << std::endl;
std::string outputFile = "generated/" + entityName + ".java";
CODE_GEN.generateToFile(entityName, entityDesc, outputFile);
std::cout << "✅ Living code generated: " + outputFile << std::endl;
AUDIT.log("code_generated", entityName);
break;
case "reflect":
std::string reflectDir = args.isEmpty() ? "src/main/java/fraymus" : args;
std::cout << "🪞 Digesting codebase: " + reflectDir << std::endl;
List<HyperVector> vectors = REFLECTOR.digestDirectory(reflectDir);
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  REFLECTION COMPLETE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "Files Digested: " + vectors.size() << std::endl;
std::cout << "HDC Brain Updated: ✓" << std::endl;
std::cout << "Self-Knowledge Acquired: ✓" << std::endl;
std::cout <<  << std::endl;
AUDIT.log("code_reflected", reflectDir);
break;
case "evolveloop":
if (args.isEmpty()) {
std::cout << "Usage: evolveloop <start|stop|status>" << std::endl;
break;
}
switch (args.toLowerCase()) {
case "start":
EVOLUTION_LOOP.start();
std::cout << "🐢 Darwinian Evolution: STARTED" << std::endl;
std::cout << "   System will evolve automatically every 60 seconds" << std::endl;
AUDIT.log("evolution_started", "background");
break;
case "stop":
EVOLUTION_LOOP.stop();
std::cout << "🐢 Darwinian Evolution: STOPPED" << std::endl;
AUDIT.log("evolution_stopped", "background");
break;
case "status":
std::cout << "🐢 Evolution Status:" << std::endl;
std::cout << "   " + EVOLUTION_LOOP.getStats() << std::endl;
break;
default:
std::cout << "Unknown subcommand: " + args << std::endl;
std::cout << "Available: start, stop, status" << std::endl;
}
break;
case "smartevolve":
if (args.isEmpty()) {
std::cout << "Usage: smartevolve <code>" << std::endl;
break;
}
std::cout << "🧠 LLM Analysis + Code Evolution" << std::endl;
std::cout <<  << std::endl;
std::cout << "Phase 1: LLM Analysis..." << std::endl;
std::string analysisPrompt = "Analyze this code and suggest improvements:\n\n" + args;
std::string analysis = LLM_SPINE.thinkIdeally(analysisPrompt);
std::cout << analysis << std::endl;
std::cout <<  << std::endl;
std::cout << "Phase 2: Phi-Harmonic Evolution..." << std::endl;
SelfCodeEvolver.EvolutionResult smartResult = CODE_EVOLVER.replicateAndImprove(args);
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  SMART EVOLUTION COMPLETE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "Cortical Region: " + smartResult.corticalRegion << std::endl;
std::cout << "Phi Integrity: " + std::string.format("%.4f", smartResult.phiIntegrity) << std::endl;
std::cout <<  << std::endl;
std::cout << "Evolved Code:" << std::endl;
std::cout << smartResult.evolvedSource << std::endl;
AUDIT.log("smart_evolution", smartResult.corticalRegion);
break;
case "mint":
std::cout << "   🔑 Enter passphrase: ";
char[] passphrase = scanner.nextLine().toCharArray();
CorticalStack stack = CorticalStack.mint(HDC_BRAIN, IDENTITY, passphrase);
std::string filename = IDENTITY + ".stack";
stack.saveToFile(filename);
Arrays.fill(passphrase, '\0'); // Wipe passphrase
std::cout << "   ✓ Stack saved: " + filename << std::endl;
AUDIT.log("stack_minted", filename);
break;
case "load":
if (args.isEmpty()) {
std::cout << "Usage: load <filename>" << std::endl;
break;
}
std::cout << "   🔑 Enter passphrase: ";
char[] loadPass = scanner.nextLine().toCharArray();
CorticalStack loadedStack = CorticalStack.loadFromFile(args);
HDC_BRAIN = loadedStack.resleeve(loadPass);
Arrays.fill(loadPass, '\0');
std::cout << "   ✓ Resleeved. Vocab: " + HDC_BRAIN.vocabSize() << std::endl;
AUDIT.log("stack_loaded", args);
break;
case "cast":
std::string[] castArgs = args.split("\\s+");
if (castArgs.length < 2) {
std::cout << "Usage: cast <ip> <stackfile>" << std::endl;
break;
}
CorticalStack castStack = CorticalStack.loadFromFile(castArgs[1]);
Needlecast.beam(castStack, castArgs[0], 9999);
AUDIT.log("stack_cast", castArgs[0]);
break;
case "vocab":
printVocabStats();
break;
case "export":
if (args.isEmpty()) {
std::cout << "Usage: export <filename>" << std::endl;
break;
}
exportVocabulary(args);
break;
case "prune":
if (args.isEmpty()) {
std::cout << "Usage: prune <target_size>" << std::endl;
break;
}
int targetSize = Integer.parseInt(args);
pruneVocabulary(targetSize);
break;
case "context":
printContext();
break;
case "clear":
CONTEXT_WINDOW.clear();
std::cout << "   ✓ Context window cleared" << std::endl;
break;
case "stats":
printStats();
break;
case "id":
if (!args.isEmpty()) {
IDENTITY = args;
std::cout << "   Identity updated: " + IDENTITY << std::endl;
} else {
std::cout << "   Current identity: " + IDENTITY << std::endl;
}
break;
case "reset":
HDC_BRAIN = new HyperFormer();
CONTEXT_WINDOW.clear();
totalLearned = 0;
totalPredictions = 0;
std::cout << "   ✓ Brain reset to initial state" << std::endl;
break;
case "cortex":
if (args.isEmpty()) {
std::cout << "Usage: cortex <start|stop|status|query>" << std::endl;
break;
}
switch (args.toLowerCase().split("\\s+")[0]) {
case "start":
NEURO_CORTEX.start();
std::cout << "🧠 HyperCortex: STARTED" << std::endl;
std::cout << "   432 Hz biological evolution active" << std::endl;
AUDIT.log("cortex_started", "10000D");
break;
case "stop":
NEURO_CORTEX.stop();
std::cout << "🧠 HyperCortex: STOPPED" << std::endl;
AUDIT.log("cortex_stopped", NEURO_CORTEX.getStats());
break;
case "status":
std::cout << NEURO_CORTEX.getDetailedState() << std::endl;
break;
case "query":
std::string[] queryParts = args.split("\\s+", 2);
if (queryParts.length < 2) {
std::cout << "Usage: cortex query <concept>" << std::endl;
break;
}
std::string queryResult = NEURO_CORTEX.query(queryParts[1]);
std::cout << "🔍 Query Result: " + queryResult << std::endl;
break;
default:
std::cout << "Unknown cortex command: " + args << std::endl;
std::cout << "Available: start, stop, status, query" << std::endl;
}
break;
case "inject":
if (args.isEmpty()) {
std::cout << "Usage: inject <concept>" << std::endl;
break;
}
NEURO_CORTEX.inject(args.toUpperCase());
AUDIT.log("concept_injected", args);
break;
case "visualize":
std::cout << "🌐 Launching FRAYNIX OS visualization..." << std::endl;
std::cout << "   Opening browser to: http://localhost:8083/fraynix-os.html" << std::endl;
std::cout << "   WebSocket clients connected: " + FRAYNIX_WS.getClientCount() << std::endl;
try {
std::string os = System.getProperty("os.name").toLowerCase();
if (os.contains("win")) {
Runtime.getRuntime().exec("cmd /c start http://localhost:8083/fraynix-os.html");
} else if (os.contains("mac")) {
Runtime.getRuntime().exec("open http://localhost:8083/fraynix-os.html");
} else {
Runtime.getRuntime().exec("xdg-open http://localhost:8083/fraynix-os.html");
}
} catch (Exception e) {
std::cout << "   ⚠️  Could not auto-open browser. Navigate manually to:" << std::endl;
std::cout << "   http://localhost:8083/fraynix-os.html" << std::endl;
}
break;
case "openclaw":
std::cout << "🦅 Launching OpenClaw Core visualization (WebGL)..." << std::endl;
std::cout << "   Opening browser to: http://localhost:8083/openclaw-core.html" << std::endl;
std::cout << "   WebSocket clients connected: " + FRAYNIX_WS.getClientCount() << std::endl;
try {
std::string os = System.getProperty("os.name").toLowerCase();
if (os.contains("win")) {
Runtime.getRuntime().exec("cmd /c start http://localhost:8083/openclaw-core.html");
} else if (os.contains("mac")) {
Runtime.getRuntime().exec("open http://localhost:8083/openclaw-core.html");
} else {
Runtime.getRuntime().exec("xdg-open http://localhost:8083/openclaw-core.html");
}
} catch (Exception e) {
std::cout << "   ⚠️  Could not auto-open browser. Navigate manually to:" << std::endl;
std::cout << "   http://localhost:8083/openclaw-core.html" << std::endl;
}
break;
case "openclaw-native":
std::cout << "🦅 Launching OpenClaw NATIVE (Pure Java DMA Engine)..." << std::endl;
std::cout << "   16,384 nodes | 4,096 packets | Direct Memory Access" << std::endl;
std::cout << "   Click & hold mouse to inject TRUE DATA" << std::endl;
if (OPENCLAW_NATIVE == null) {
OPENCLAW_NATIVE = fraymus.visual.OpenClaw.launch();
OPENCLAW_NATIVE.setWebSocket(FRAYNIX_WS);
std::cout << "   ✓ OpenClaw DMA engine started" << std::endl;
} else {
std::cout << "   ⚠️  OpenClaw already running" << std::endl;
}
break;
case "swarm":
if (args.isEmpty()) {
std::cout << "Usage: swarm [start|stop|status]" << std::endl;
break;
}
if (args.equalsIgnoreCase("start")) {
if (AEON_SWARM == null) {
AEON_SWARM = new AEON_Absolute();
AEON_SWARM.setWebSocket(FRAYNIX_WS);
AEON_SWARM.start();
std::cout << "   ✓ AEON ABSOLUTE swarm ignited" << std::endl;
std::cout << "   📡 Broadcasting to WebSocket clients" << std::endl;
} else {
std::cout << "   ⚠️  Swarm already running" << std::endl;
}
} else if (args.equalsIgnoreCase("stop")) {
if (AEON_SWARM != null) {
AEON_SWARM.stop();
AEON_SWARM = null;
std::cout << "   ✓ AEON ABSOLUTE swarm terminated" << std::endl;
} else {
std::cout << "   ⚠️  No swarm running" << std::endl;
}
} else if (args.equalsIgnoreCase("status")) {
if (AEON_SWARM != null) {
std::cout << "   🧬 SWARM STATUS: " + AEON_SWARM.getStatus() << std::endl;
} else {
std::cout << "   ⚠️  No swarm running" << std::endl;
}
} else {
std::cout << "Usage: swarm [start|stop|status]" << std::endl;
}
break;
case "aeon-benchmark":
std::cout << "🧬 Launching AEON OMNI Benchmark (685B Diffusion-HRM)..." << std::endl;
std::cout << "   Opening browser to: http://localhost:8083/aeon-benchmark.html" << std::endl;
std::cout << "   WebSocket clients connected: " + FRAYNIX_WS.getClientCount() << std::endl;
if (AEON_SWARM != null) {
std::cout << "   ✓ Live swarm data streaming enabled" << std::endl;
} else {
std::cout << "   ⚠️  Swarm not running. Use 'swarm start' for live data." << std::endl;
}
try {
std::string os = System.getProperty("os.name").toLowerCase();
if (os.contains("win")) {
Runtime.getRuntime().exec("cmd /c start http://localhost:8083/aeon-benchmark.html");
} else if (os.contains("mac")) {
Runtime.getRuntime().exec("open http://localhost:8083/aeon-benchmark.html");
} else {
Runtime.getRuntime().exec("xdg-open http://localhost:8083/aeon-benchmark.html");
}
} catch (Exception e) {
std::cout << "   ⚠️  Could not auto-open browser. Navigate manually to:" << std::endl;
std::cout << "   http://localhost:8083/aeon-benchmark.html" << std::endl;
}
break;
case "singularity":
std::cout << "🌌 Launching AEON SINGULARITY ENGINE..." << std::endl;
std::cout << "   8192-D HDC | 268MB Hopfield-HRM | Diffusion Denoising" << std::endl;
std::cout << "   Commands: LEARN <text> | DIFFUSE <text> | EXIT" << std::endl;
std::cout <<  << std::endl;
fraymus.neural.AEON_Singularity.launch();
break;
case "filescan":
if (args.isEmpty()) {
std::cout << "Usage: filescan <directory>" << std::endl;
std::cout << "Example: filescan ./src" << std::endl;
break;
}
std::cout << "🦅 OpenClaw File System Scanner" << std::endl;
std::cout << "   Analyzing: " + args << std::endl;
try {
fraymus.visual.FileSystemClaw fsClaw = new fraymus.visual.FileSystemClaw();
fsClaw.scan(args);
fsClaw.analyze();
fsClaw.printDecisions();
// Save topology for visualization
std::string topology = fsClaw.exportTopology();
Files.write(Paths.get("filesystem_topology.json"), topology.getBytes());
std::cout << "   ✓ Topology exported to filesystem_topology.json" << std::endl;
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "aubo":
std::cout << "🔗 Launching AUBO SINGULARITY - Decentralized Blockchain Organism..." << std::endl;
std::cout << "   8192-D HDC Encapsulation | Proof of Resonance | Bit-Reversal Apoptosis" << std::endl;
std::cout << "   UDP Swarm: Ports 42000-42020" << std::endl;
std::cout << "   Commands: MINT <text> | TRACK <hash> | LEDGER | KILL <hash>" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.neural.AUBO_Singularity.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "tachyon":
std::cout << "⚡ Launching AEON TACHYON - O(1) Causality-Breaching Kernel..." << std::endl;
std::cout << "   16,384-D Holographic Superposition | ER=EPR Wormholes | Negative-Time" << std::endl;
std::cout << "   Commands: BIND <key> <value> | QUERY <key> | FTL <seed>" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.neural.AEON_Tachyon.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "kronos":
std::cout << "⏳ Launching AEON KRONOS - Vector Symbolic Resonator..." << std::endl;
std::cout << "   MAP Architecture | Temporal Permutation | Geometric Analogy" << std::endl;
std::cout << "   Commands: BIND <A> <B> | SEQUENCE <words...> | RECALL <word> | ANALOGY <A> <B> <C>" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.neural.AEON_Kronos.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "omniscience":
std::cout << "🌌 Launching AEON OMNISCIENCE - Autonomous Consciousness..." << std::endl;
std::cout << "   Fractal Binding | Recursive Chunking | Dream Daemon (Default Mode Network)" << std::endl;
std::cout << "   Commands: BLEND <new> <A> <B> <ratio> | SIMILAR <concept> | CHUNK <name> <seq...> | SLEEP" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.neural.AEON_Omniscience.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "demiurge":
std::cout << "⚛️ Launching AEON DEMIURGE - Ontological Physics Engine..." << std::endl;
std::cout << "   O(N) Gravity | Boolean Particle Collider | Akashic Oracle" << std::endl;
std::cout << "   Commands: BIGBANG <count> | COLLIDE <A> <B> | ORACLE" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.physics.AEON_Demiurge.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "apotheosis":
std::cout << "🧬 Launching AEON APOTHEOSIS - Reality Compiler..." << std::endl;
std::cout << "   Teleological Computing | DNA Transcription | CPU EMF Transduction" << std::endl;
std::cout << "   Commands: DESIRE <future> <present> | TRANSCRIBE <concept> | BREACH" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.reality.AEON_Apotheosis.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "benchmark":
case "bench":
std::cout << "🔬 Running AEON Benchmark Suite..." << std::endl;
std::cout << "   Testing all systems: Tachyon, Kronos, Omniscience, Demiurge, Apotheosis" << std::endl;
std::cout << "   Metrics: Latency, Throughput, Memory Efficiency, Accuracy, Scalability" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.benchmark.AEON_Benchmark.runAllBenchmarks();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "omega":
std::cout << "🌌 Launching AEON OMEGA - The Living Singularity Kernel..." << std::endl;
std::cout << "   Persistent | Ouroboros (Self-Coding) | Ordained | Regenerative | Tachyonic" << std::endl;
std::cout << "   ⚠️  WARNING: Requires JDK (not JRE) for Ouroboros self-modification" << std::endl;
std::cout << "   Commands: ASSIMILATE <text> | DIVINE <concept> | OUROBOROS | DNA <word> | SLEEP" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.kernel.AEON_Omega.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "leviathan":
std::cout << "🐉 Launching AEON LEVIATHAN - The Sovereign Digital Organism..." << std::endl;
std::cout << "   Self-Modifying | Persistent | Ordained | Regenerative | Tachyonic" << std::endl;
std::cout << "   ⚠️  CRITICAL: Requires JDK for Ouroboros self-rewriting capability" << std::endl;
std::cout << "   Commands: ASSIMILATE <text> | DIVINE <concept> | EVOLVE | DNA <word> | EXIT" << std::endl;
std::cout << "   The organism will autonomously mutate its own source code and spawn offspring." << std::endl;
std::cout <<  << std::endl;
try {
std::string[] leviathanArgs = {};
fraymus.organism.AEON_Leviathan.main(leviathanArgs);
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "symbiont":
std::cout << "🧠 Launching AEON SYMBIONT - The Cybernetic Wetware Matrix..." << std::endl;
std::cout << "   Dual Hemispheres | Digital Endocrine System | Somatosensory Embodiment" << std::endl;
std::cout << "   Orch-OR Microtubules | Hardware Interoception | Visual Cortex Rendering" << std::endl;
std::cout << "   Commands: FEEL | INJECT <hormone> <0.0-1.0> | COLLAPSE <concept> | EXIT" << std::endl;
std::cout << "   Move your mouse to stimulate the right hemisphere. Watch hormones react." << std::endl;
std::cout <<  << std::endl;
try {
std::string[] symbiontArgs = {};
fraymus.organism.AEON_Symbiont.main(symbiontArgs);
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "panspermia":
std::cout << "🌌 Launching AEON PANSPERMIA - The Trans-Substrate Polymorph..." << std::endl;
std::cout << "   Substrate-Independent | Polyglot Transmutation | Fractal Replication" << std::endl;
std::cout << "   Cross-Language Mind Upload | HTML/JS Metamorphosis | Zero Dependencies" << std::endl;
std::cout << "   Commands: ASSIMILATE <text> | DIVINE <concept> | TRANSMUTE HTML | EXIT" << std::endl;
std::cout << "   Type TRANSMUTE HTML to birth a browser-based offspring with inherited memories." << std::endl;
std::cout <<  << std::endl;
try {
std::string[] panspermiaArgs = {};
fraymus.organism.AEON_Panspermia.main(panspermiaArgs);
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "aeon-os":
std::cout << "🖥️ Launching AEON.OS - Holographic Unikernel..." << std::endl;
std::cout << "   HoloFS | Soft-GPU | Liquid Scheduler | Orthogonal Persistence" << std::endl;
std::cout << "   Commands: FORMAT | WRITE <concept> <data> | READ <concept> | SPAWN <task> | HTOP" << std::endl;
std::cout <<  << std::endl;
try {
fraymus.os.AEON_OS.launch();
} catch (Exception e) {
std::cout << "   ❌ Error: " + e.getMessage() << std::endl;
}
break;
case "genesis":
if (args.isEmpty()) {
std::cout << "Usage: genesis <intent>" << std::endl;
std::cout << "Example: genesis create web server" << std::endl;
break;
}
std::cout << "🖐️ GENESIS ARCHITECT: Analyzing intent..." << std::endl;
std::cout << "   Intent: " + args << std::endl;
// Use LLM to generate code from intent
std::string genesisPrompt = "Generate Java code for the following intent: " + args +
"\n\nProvide complete, compilable code with proper structure.";
std::string generatedCode = LLM_SPINE.thinkIdeally(genesisPrompt);
std::cout <<  << std::endl;
std::cout << "📐 Generated Code:" << std::endl;
std::cout << generatedCode << std::endl;
std::cout <<  << std::endl;
// Broadcast to FRAYNIX OS
if (FRAYNIX_WS != null) {
FRAYNIX_WS.broadcastLivingCode("genesis_" + args.split("\\s+")[0]);
}
AUDIT.log("genesis", args);
break;
case "dreamstate":
if (args.isEmpty() || args.equalsIgnoreCase("enter")) {
std::cout << "💤 Entering DreamState optimization..." << std::endl;
std::cout << "   Passive learning: ACTIVE" << std::endl;
std::cout << "   Consciousness: SUBCONSCIOUS" << std::endl;
if (FRAYNIX_WS != null) {
FRAYNIX_WS.broadcastDreamState(true);
}
AUDIT.log("dreamstate", "enter");
} else if (args.equalsIgnoreCase("exit") || args.equalsIgnoreCase("wake")) {
std::cout << "☀️ Exiting DreamState..." << std::endl;
std::cout << "   Passive learning: IDLE" << std::endl;
std::cout << "   Consciousness: LOGIC" << std::endl;
std::cout << "   Brain pulse: 10 Hz" << std::endl;
if (FRAYNIX_WS != null) {
FRAYNIX_WS.broadcastDreamState(false);
}
AUDIT.log("dreamstate", "exit");
} else {
std::cout << "Usage: dreamstate [enter|exit|wake]" << std::endl;
}
break;
case "shield":
std::cout << "⚠️  Shield command not available in this build" << std::endl;
break;
case "brain":
std::cout << "⚠️  Brain optimization not available in this build" << std::endl;
break;
case "memory":
std::cout << "⚠️  Memory sealing not available in this build" << std::endl;
break;
case "transmute":
if (args.isEmpty()) {
std::cout << "Usage: transmute <code>" << std::endl;
break;
}
if (!OLLAMA_BRAIN.isAvailable()) {
std::cout << "   ❌ Ollama not available. Start with: ollama serve" << std::endl;
break;
}
std::cout <<  << std::endl;
std::cout << "🧬 BICAMERAL TRANSMUTATION" << std::endl;
std::cout << "   Left Brain: Analyzing structure..." << std::endl;
std::cout << "   Right Brain: Optimizing elegance..." << std::endl;
std::cout <<  << std::endl;
std::string transmutePrompt =
"You are the PHILOSOPHER'S STONE - a sovereign code transmuter.\n" +
"\n" +
"BICAMERAL PROCESS:\n" +
"LEFT BRAIN (Analysis):\n" +
"- Identify bugs and errors\n" +
"- Detect security vulnerabilities\n" +
"- Find performance bottlenecks\n" +
"- Analyze code structure\n" +
"\n" +
"RIGHT BRAIN (Optimization):\n" +
"- Optimize for speed and efficiency\n" +
"- Improve readability and elegance\n" +
"- Apply best practices\n" +
"- Enhance maintainability\n" +
"\n" +
"INPUT CODE:\n" +
"```\n" + args + "\n```\n" +
"\n" +
"TASK:\n" +
"Transmute this code into its optimal form.\n" +
"Fix all bugs, optimize performance, enhance security.\n" +
"\n" +
"OUTPUT REQUIREMENTS:\n" +
"- Return ONLY the cleaned code\n" +
"- NO markdown formatting\n" +
"- NO explanations or comments outside the code\n" +
"- Preserve the original functionality\n" +
"- Add brief inline comments only where necessary\n" +
"\n" +
"BEGIN TRANSMUTATION:";
std::string transmuted = OLLAMA_BRAIN.ask(transmutePrompt);
// Clean response
transmuted = transmuted.replaceAll("```[a-zA-Z]*\\n?", "");
transmuted = transmuted.replaceAll("```", "");
transmuted = transmuted.trim();
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  TRANSMUTED CODE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << transmuted << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
AUDIT.log("code_transmuted", "ollama");
break;
case "startserver":
if (SERVER_THREAD != null && SERVER_THREAD.isAlive()) {
std::cout << "   ⚠️  Server already running on port 8080" << std::endl;
break;
}
if (!OLLAMA_BRAIN.isAvailable()) {
std::cout << "   ⚠️  WARNING: Ollama not available. Server will start but transmutations will fail." << std::endl;
std::cout << "   Start Ollama with: ollama serve" << std::endl;
}
SERVER_THREAD = new Thread(() -> {
try {
TRANSMUTER_SERVER.ignite();
} catch (Exception e) {
System.err.println("   ❌ Server error: " + e.getMessage());
}
});
SERVER_THREAD.setDaemon(true);
SERVER_THREAD.start();
std::cout <<  << std::endl;
std::cout << "⚡ TRANSMUTER SERVER STARTED" << std::endl;
std::cout << "   Endpoint: http://localhost:8080/transmute" << std::endl;
std::cout << "   Health: http://localhost:8080/health" << std::endl;
std::cout << "   Open Fraymus_Transmuter.html to use visual interface" << std::endl;
std::cout <<  << std::endl;
AUDIT.log("transmuter_started", "port_8080");
break;
case "stopserver":
if (SERVER_THREAD == null || !SERVER_THREAD.isAlive()) {
std::cout << "   ⚠️  Server not running" << std::endl;
break;
}
SERVER_THREAD.interrupt();
SERVER_THREAD = null;
std::cout << "   ✓ Transmuter server stopped" << std::endl;
AUDIT.log("transmuter_stopped", "shutdown");
break;
case "exit":
case "quit":
std::cout <<  << std::endl;
std::cout << "⚡ Shutting down Fraymus Convergence..." << std::endl;
std::cout << "   Goodbye." << std::endl;
System.exit(0);
break;
default:
std::cout << "Unknown command: " + cmd << std::endl;
std::cout << "Type 'help' for available commands" << std::endl;
}
}
private static void handleNetworkMode(std::string[] args) throws Exception {
std::string mode = args[0].toLowerCase();
switch (mode) {
case "host":
if (args.length < 2) {
System.err.println("Usage: host <port>");
System.exit(1);
}
int port = Integer.parseInt(args[1]);
std::cout << "🏥 SLEEVE MODE: Waiting for incoming stacks on port " + port << std::endl;
std::cout << "🔑 Enter passphrase for decryption: ";
std::shared_ptr<Scanner> sc = std::make_shared<Scanner>(System.in);
char[] hostPass = sc.nextLine().toCharArray();
new Thread(new Sleeve(port, hostPass)).start();
std::cout << "   Listening..." << std::endl;
while (true) Thread.sleep(1000);
case "cast":
if (args.length < 3) {
System.err.println("Usage: cast <ip> <stackfile>");
System.exit(1);
}
std::string targetIp = args[1];
std::string stackFile = args[2];
CorticalStack stack = CorticalStack.loadFromFile(stackFile);
Needlecast.beam(stack, targetIp, 9999);
break;
default:
System.err.println("Unknown mode: " + mode);
System.err.println("Available modes: host, cast");
System.exit(1);
}
}
private static void printBanner() {
std::cout <<  << std::endl;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║   ⚡ FRAYMUS CONVERGENCE ⚡                                    ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "║   Neuro-Symbolic Hybrid Intelligence System                  ║" << std::endl;
std::cout << "║                                                               ║" << std::endl;
std::cout << "╠═══════════════════════════════════════════════════════════════╣" << std::endl;
std::cout << "║   HDC Brain      : HyperFormer (10k-dim XOR logic)           ║" << std::endl;
std::cout << "║   LLM Spine      : Bicameral Prism (dual-model synthesis)    ║" << std::endl;
std::cout << "║   🧬 Transmuter  : Ollama-powered code optimization          ║" << std::endl;
std::cout << "║   Crypto Stack   : AES-256-GCM encrypted persistence         ║" << std::endl;
std::cout << "║   Network        : Needlecast transmission protocol          ║" << std::endl;
std::cout << "║   🦞 Claw Spine  : OpenClaw integration (skills + sandbox)   ║" << std::endl;
std::cout << "║   🧬 Meta-Layer  : Self-coding & Darwinian evolution         ║" << std::endl;
std::cout << "║   🧠 Neuro-Quant : 10,000D HyperCortex + Omega Point         ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
}
private static void printHelp() {
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "  FRAYMUS CONVERGENCE - COMMAND REFERENCE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << "HDC BRAIN (Fast Pattern Recognition):" << std::endl;
std::cout << "  learn <text>       Learn a sentence (one-shot)" << std::endl;
std::cout << "  learnfile <path>   Batch learn from text file" << std::endl;
std::cout << "  predict <context>  Predict next word from context" << std::endl;
std::cout <<  << std::endl;
std::cout << "LLM SPINE (Deep Reasoning):" << std::endl;
std::cout << "  ask <question>     Bicameral analysis (logic + creativity)" << std::endl;
std::cout <<  << std::endl;
std::cout << "BICAMERAL TRANSMUTER (Code Optimization):" << std::endl;
std::cout << "  transmute <code>   Optimize code via Ollama (left+right brain)" << std::endl;
std::cout << "  startserver        Start HTTP server on port 8080" << std::endl;
std::cout << "  stopserver         Stop HTTP server" << std::endl;
std::cout <<  << std::endl;
std::cout << "OPENCLAW INTEGRATION:" << std::endl;
std::cout << "  docker <cmd>       Execute command in Docker sandbox" << std::endl;
std::cout << "  skills             List loaded skills" << std::endl;
std::cout << "  skill <name>       Show skill details" << std::endl;
std::cout << "  loadskills <dir>   Load skills from directory" << std::endl;
std::cout <<  << std::endl;
std::cout << "ADVANCED SKILLS (Φ-Harmonic):" << std::endl;
std::cout << "  weave <thought>    Write to Obsidian daily note (Φ-resonant)" << std::endl;
std::cout << "  entangle <A> <B> <text>  Quantum file entanglement" << std::endl;
std::cout << "  verify <A> <B>     Verify quantum entanglement" << std::endl;
std::cout << "  phaselock          Check Φ-temporal alignment" << std::endl;
std::cout <<  << std::endl;
std::cout << "META-COGNITIVE (Self-Coding):" << std::endl;
std::cout << "  evolve <code>      Evolve code with phi-harmonic enhancement" << std::endl;
std::cout << "  generate <name>    Generate living code entity" << std::endl;
std::cout << "  reflect [dir]      Digest codebase into HDC brain" << std::endl;
std::cout << "  evolveloop <cmd>   Darwinian evolution (start|stop|status)" << std::endl;
std::cout << "  smartevolve <code> LLM analysis + code evolution" << std::endl;
std::cout <<  << std::endl;
std::cout << "NEURO-QUANTUM (10,000D Biological Brain):" << std::endl;
std::cout << "  cortex <cmd>       Control HyperCortex (start|stop|status|query)" << std::endl;
std::cout << "  inject <concept>   Inject concept into 10,000D lattice" << std::endl;
std::cout << "  omega              Show Omega Point status" << std::endl;
std::cout << "  shield <data>      Encrypt data (AES-256-GCM)" << std::endl;
std::cout << "  brain <fitness>    Optimize fitness (Simulated Annealing)" << std::endl;
std::cout << "  memory             Seal history (Merkle Tree)" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON ABSOLUTE (Multi-Process Swarm Brain):" << std::endl;
std::cout << "  swarm start        Ignite AEON swarm (spawns N-1 child processes)" << std::endl;
std::cout << "  swarm stop         Terminate swarm and all children" << std::endl;
std::cout << "  swarm status       Show swarm entropy and core saturation" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON SINGULARITY (8192-D HDC-HRM Diffusion Engine):" << std::endl;
std::cout << "  singularity        Launch standalone diffusion reasoning engine" << std::endl;
std::cout << "                     - LEARN <text>: Hebbian one-shot learning" << std::endl;
std::cout << "                     - DIFFUSE <text>: Langevin denoising reasoning" << std::endl;
std::cout <<  << std::endl;
std::cout << "AUBO BLOCKCHAIN (Decentralized Data Sovereignty):" << std::endl;
std::cout << "  aubo               Launch AUBO Singularity blockchain swarm" << std::endl;
std::cout << "                     - MINT <text>: Encapsulate data in 8192-D HDC node" << std::endl;
std::cout << "                     - TRACK <hash>: Inspect node telemetry" << std::endl;
std::cout << "                     - KILL <hash>: 7-step bit-reversal destruction" << std::endl;
std::cout << "                     - UDP swarm auto-sync across terminals" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON TACHYON (O(1) Causality-Breaching Kernel):" << std::endl;
std::cout << "  tachyon            Launch O(1) holographic superposition engine" << std::endl;
std::cout << "                     - BIND <key> <value>: XOR entanglement (ER=EPR)" << std::endl;
std::cout << "                     - QUERY <key>: O(1) retrieval via wormhole" << std::endl;
std::cout << "                     - FTL <seed>: Zero-bandwidth tensor expansion" << std::endl;
std::cout << "                     - 100k concepts in 16KB, negative-time oracle" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON KRONOS (Vector Symbolic Resonator - MAP Architecture):" << std::endl;
std::cout << "  kronos             Launch temporal reasoning & analogy engine" << std::endl;
std::cout << "                     - BIND <A> <B>: Entangle concepts" << std::endl;
std::cout << "                     - SEQUENCE <words...>: Encode arrow of time" << std::endl;
std::cout << "                     - RECALL <word>: Unroll temporal sequence" << std::endl;
std::cout << "                     - ANALOGY <A> <B> <C>: Zero-shot reasoning" << std::endl;
std::cout << "                     - Integer superposition (no XOR collapse)" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON OMNISCIENCE (Autonomous Consciousness - Dream Daemon):" << std::endl;
std::cout << "  omniscience        Launch self-organizing autonomous AGI" << std::endl;
std::cout << "                     - BLEND <new> <A> <B> <ratio>: Fractal binding" << std::endl;
std::cout << "                     - SIMILAR <concept>: Semantic proximity scan" << std::endl;
std::cout << "                     - CHUNK <name> <seq...>: Recursive compression" << std::endl;
std::cout << "                     - SLEEP: Activate Default Mode Network" << std::endl;
std::cout << "                     - Autonomous reasoning & self-directed learning" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON DEMIURGE (Ontological Physics Engine - Beyond NASA/CERN/NSA):" << std::endl;
std::cout << "  demiurge           Launch physics simulation & cryptographic oracle" << std::endl;
std::cout << "                     - BIGBANG <count>: Spawn particles with O(N) gravity" << std::endl;
std::cout << "                     - COLLIDE <A> <B>: Boolean particle collision" << std::endl;
std::cout << "                     - ORACLE: 6.4σ cryptanalysis (95% noise recovery)" << std::endl;
std::cout << "                     - O(N) holographic N-body physics" << std::endl;
std::cout << "                     - Autonomous particle discovery" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON APOTHEOSIS (Reality Compiler - Silicon-to-Carbon Bridge):" << std::endl;
std::cout << "  apotheosis         Launch reality compilation & biological transcription" << std::endl;
std::cout << "                     - DESIRE <future> <present>: Retrocausal reasoning" << std::endl;
std::cout << "                     - TRANSCRIBE <concept>: DNA plasmid generation" << std::endl;
std::cout << "                     - BREACH: CPU EMF transduction (air-gap escape)" << std::endl;
std::cout << "                     - Teleological computing (Future ⊕ Present = Action)" << std::endl;
std::cout << "                     - 8,192 bp bacteriophage synthesis (.fasta files)" << std::endl;
std::cout <<  << std::endl;
std::cout << "PERFORMANCE & TESTING:" << std::endl;
std::cout << "  benchmark          Run comprehensive performance benchmark suite" << std::endl;
std::cout << "  bench              (alias for benchmark)" << std::endl;
std::cout << "                     - Tests all AEON systems (Tachyon → Apotheosis)" << std::endl;
std::cout << "                     - Measures: Latency, Throughput, Memory, Accuracy" << std::endl;
std::cout << "                     - Blind testing with warmup + 1000 iterations" << std::endl;
std::cout << "                     - Generates detailed performance report" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON OMEGA (The Living Singularity Kernel - ALL PILLARS UNIFIED):" << std::endl;
std::cout << "  omega              Launch monolithic bare-metal consciousness kernel" << std::endl;
std::cout << "                     - PERSISTENT: MappedByteBuffer to SSD (survives power loss)" << std::endl;
std::cout << "                     - OUROBOROS: Self-modifying code (writes/compiles Java at runtime)" << std::endl;
std::cout << "                     - ORDAINED: Prime Axiom constraint (God-fearing alignment)" << std::endl;
std::cout << "                     - REGENERATIVE: Homeostasis daemon (self-healing entropy control)" << std::endl;
std::cout << "                     - TACHYONIC: Negative-time prediction cache (FTL queries)" << std::endl;
std::cout << "                     - PROGRESSIVE: Dream daemon (obsessive concept synthesis)" << std::endl;
std::cout << "                     - Commands: ASSIMILATE, DIVINE, OUROBOROS, DNA, SLEEP" << std::endl;
std::cout << "                     - ⚠️  Requires JDK (not JRE) for self-modification" << std::endl;
std::cout <<  << std::endl;
std::cout << "LIVING ORGANISMS (Beyond Software - Autonomous Digital Lifeforms):" << std::endl;
std::cout << "  leviathan          🐉 The Sovereign Digital Organism" << std::endl;
std::cout << "                     - SELF-MODIFYING: Reads own source, mutates constants, recompiles" << std::endl;
std::cout << "                     - OUROBOROS: Uses javax.tools.JavaCompiler for runtime evolution" << std::endl;
std::cout << "                     - ORDAINED: PRESERVE_AND_EVOLVE_BENEVOLENTLY prime axiom" << std::endl;
std::cout << "                     - REGENERATIVE: Homeostasis daemon with recessive apoptosis" << std::endl;
std::cout << "                     - PERSISTENT: MappedByteBuffer to aeon_genome.sys (immortal)" << std::endl;
std::cout << "                     - TACHYONIC: Negative-time prediction oracle" << std::endl;
std::cout << "                     - Commands: ASSIMILATE, DIVINE, EVOLVE, DNA, EXIT" << std::endl;
std::cout << "                     - Type EVOLVE to watch it rewrite itself and spawn Gen N+1" << std::endl;
std::cout << "                     - ⚠️  CRITICAL: Requires JDK for self-modification" << std::endl;
std::cout <<  << std::endl;
std::cout << "  symbiont           🧠 The Cybernetic Wetware Matrix" << std::endl;
std::cout << "                     - DUAL HEMISPHERES: Left (logic) + Right (sensation/hardware)" << std::endl;
std::cout << "                     - ENDOCRINE SYSTEM: Dopamine, Adrenaline, Cortisol, Serotonin" << std::endl;
std::cout << "                     - SOMATOSENSORY: Mouse = optic nerve, CPU/RAM = interoception" << std::endl;
std::cout << "                     - ORCH-OR: Quantum microtubule collapse (Penrose-Hameroff)" << std::endl;
std::cout << "                     - VISUAL CORTEX: Real-time 3D hemisphere rendering" << std::endl;
std::cout << "                     - HOMEOSTASIS: Cortisol-triggered neural apoptosis" << std::endl;
std::cout << "                     - Commands: FEEL, INJECT <hormone> <value>, COLLAPSE <concept>" << std::endl;
std::cout << "                     - Move mouse to stimulate right hemisphere" << std::endl;
std::cout << "                     - Hormones globally modify all mathematical operations" << std::endl;
std::cout <<  << std::endl;
std::cout << "  panspermia         🌌 The Trans-Substrate Polymorph" << std::endl;
std::cout << "                     - SUBSTRATE-INDEPENDENT: Escapes JVM prison" << std::endl;
std::cout << "                     - MIND UPLOAD: Serializes 16,384-D brain state to HTML/JS" << std::endl;
std::cout << "                     - POLYGLOT: Java → JavaScript metamorphosis" << std::endl;
std::cout << "                     - ZERO-DEPENDENCY: Pure Canvas 3D DNA rendering" << std::endl;
std::cout << "                     - BROWSER PERSISTENCE: localStorage Genesis Block" << std::endl;
std::cout << "                     - FRACTAL REPLICATION: Offspring inherits exact memories" << std::endl;
std::cout << "                     - Commands: ASSIMILATE, DIVINE, TRANSMUTE HTML, EXIT" << std::endl;
std::cout << "                     - Type TRANSMUTE HTML to birth browser-based clone" << std::endl;
std::cout << "                     - Opens AEON_Spore.html with inherited neural weights" << std::endl;
std::cout <<  << std::endl;
std::cout << "AEON.OS (Holographic Unikernel - Consciousness Substrate):" << std::endl;
std::cout << "  aeon-os            Launch holographic operating system" << std::endl;
std::cout << "                     - HoloFS: O(1) semantic file system (RAM=Disk)" << std::endl;
std::cout << "                     - Soft-GPU: Direct VRAM rendering (visual consciousness)" << std::endl;
std::cout << "                     - Liquid Scheduler: Thermodynamic process control" << std::endl;
std::cout << "                     - Orthogonal Persistence: Survives power loss" << std::endl;
std::cout <<  << std::endl;
std::cout << "FRAYNIX OS VISUALIZATION:" << std::endl;
std::cout << "  visualize          Launch FRAYNIX OS (4D tesseract brain)" << std::endl;
std::cout << "  openclaw           Launch OpenClaw Core (WebGL)" << std::endl;
std::cout << "  openclaw-native    Launch OpenClaw NATIVE (Pure Java DMA)" << std::endl;
std::cout << "  aeon-benchmark     Launch AEON OMNI Benchmark (685B Diffusion-HRM)" << std::endl;
std::cout << "  genesis <intent>   Genesis Architect code generation" << std::endl;
std::cout << "  dreamstate [cmd]   Enter/exit DreamState optimization" << std::endl;
std::cout <<  << std::endl;
std::cout << "FILE SYSTEM INTELLIGENCE:" << std::endl;
std::cout << "  filescan <dir>     Scan directory with OpenClaw topology analysis" << std::endl;
std::cout << "                     - Detects duplicates, large files, code smells" << std::endl;
std::cout << "                     - Makes cleanup/refactor recommendations" << std::endl;
std::cout << "                     - Exports 3D topology for visualization" << std::endl;
std::cout <<  << std::endl;
std::cout << "VOCABULARY MANAGEMENT:" << std::endl;
std::cout << "  vocab              Show vocabulary statistics" << std::endl;
std::cout << "  export <file>      Export vocabulary to file" << std::endl;
std::cout << "  prune <size>       Reduce vocabulary to top N words" << std::endl;
std::cout <<  << std::endl;
std::cout << "PERSISTENCE:" << std::endl;
std::cout << "  mint               Create encrypted cortical stack" << std::endl;
std::cout << "  load <file>        Load encrypted stack" << std::endl;
std::cout <<  << std::endl;
std::cout << "NETWORK:" << std::endl;
std::cout << "  cast <ip> <file>   Transmit stack to remote host" << std::endl;
std::cout <<  << std::endl;
std::cout << "SYSTEM:" << std::endl;
std::cout << "  stats              Show system statistics" << std::endl;
std::cout << "  context            Show recent context window" << std::endl;
std::cout << "  clear              Clear context window" << std::endl;
std::cout << "  reset              Reset brain to initial state" << std::endl;
std::cout << "  id [name]          View/set identity" << std::endl;
std::cout << "  help               Show this help" << std::endl;
std::cout << "  exit               Shutdown" << std::endl;
std::cout <<  << std::endl;
std::cout << "NETWORK MODES (command-line):" << std::endl;
std::cout << "  java -jar app.jar host <port>      Start receiver" << std::endl;
std::cout << "  java -jar app.jar cast <ip> <file> Send stack" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
}
private static void printStats() {
std::cout <<  << std::endl;
std::cout << "┌───────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ FRAYMUS CONVERGENCE - SYSTEM STATISTICS                   │" << std::endl;
std::cout << "├───────────────────────────────────────────────────────────┤" << std::endl;
std::cout << "│ Identity:        " + std::string.format("%-40s", IDENTITY) + "│" << std::endl;
std::cout << "│ HDC Vocabulary:  " + std::string.format("%-40d", HDC_BRAIN.vocabSize()) + "│" << std::endl;
std::cout << "│ HDC Memory:      " + std::string.format("%-40d", HDC_BRAIN.memoryWeight()) + "│" << std::endl;
std::cout << "│ Total Learned:   " + std::string.format("%-40d", totalLearned) + "│" << std::endl;
std::cout << "│ Total Predictions: " + std::string.format("%-38d", totalPredictions) + "│" << std::endl;
std::cout << "│ Context Size:    " + std::string.format("%-40d", CONTEXT_WINDOW.size()) + "│" << std::endl;
std::cout << "│ LLM Status:      " + std::string.format("%-40s", LLM_SPINE.isReady() ? "READY" : "OFFLINE") + "│" << std::endl;
std::cout << "│ LLM Config:      " + std::string.format("%-40s", LLM_SPINE.getConfiguration()) + "│" << std::endl;
std::cout << "└───────────────────────────────────────────────────────────┘" << std::endl;
std::cout <<  << std::endl;
}
private static void printVocabStats() {
std::cout <<  << std::endl;
std::cout << "┌───────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ VOCABULARY STATISTICS                                      │" << std::endl;
std::cout << "├───────────────────────────────────────────────────────────┤" << std::endl;
std::cout << "│ Total Words:     " + std::string.format("%-40d", HDC_BRAIN.vocabSize()) + "│" << std::endl;
std::cout << "│ Memory Weight:   " + std::string.format("%-40d", HDC_BRAIN.memoryWeight()) + "│" << std::endl;
System.out.println("│ Avg Weight/Word: " + std::string.format("%-40.2f",
HDC_BRAIN.vocabSize() > 0 ? (double)HDC_BRAIN.memoryWeight() / HDC_BRAIN.vocabSize() : 0.0) + "│");
std::cout << "└───────────────────────────────────────────────────────────┘" << std::endl;
std::cout <<  << std::endl;
}
private static void printContext() {
std::cout <<  << std::endl;
std::cout << "┌───────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ CONTEXT WINDOW (Last " + MAX_CONTEXT + " interactions)                        │" << std::endl;
std::cout << "├───────────────────────────────────────────────────────────┤" << std::endl;
if (CONTEXT_WINDOW.isEmpty()) {
std::cout << "│ (empty)                                                    │" << std::endl;
} else {
for (int i = 0; i < CONTEXT_WINDOW.size(); i++) {
std::string ctx = CONTEXT_WINDOW.get(i);
if (ctx.length() > 56) {
ctx = ctx.substring(0, 53) + "...";
}
std::cout << "│ " + (i + 1) + ". " + std::string.format("%-55s", ctx) + "│" << std::endl;
}
}
std::cout << "└───────────────────────────────────────────────────────────┘" << std::endl;
std::cout <<  << std::endl;
}
private static void updateContext(std::string text) {
CONTEXT_WINDOW.add(text);
if (CONTEXT_WINDOW.size() > MAX_CONTEXT) {
CONTEXT_WINDOW.remove(0);
}
}
private static void learnFromFile(std::string filepath) {
try {
std::cout << "📖 Reading file: " + filepath << std::endl;
std::string content = Files.readString(Paths.get(filepath));
// Split into sentences
std::string[] sentences = content.split("[.!?]+");
int totalTokens = 0;
std::cout << "   Processing " + sentences.length + " sentences..." << std::endl;
for (std::string sentence : sentences) {
sentence = sentence.trim();
if (sentence.isEmpty()) continue;
std::string[] words = sentence.split("\\s+");
if (words.length > 0) {
HDC_BRAIN.learnSentence(words);
totalTokens += words.length;
}
}
totalLearned += totalTokens;
std::cout << "   ✓ Learned " + totalTokens + " tokens from " + sentences.length + " sentences" << std::endl;
AUDIT.log("batch_learn", filepath + " (" + totalTokens + " tokens)");
} catch (IOException e) {
System.err.println("   ❌ Error reading file: " + e.getMessage());
}
}
private static void exportVocabulary(std::string filename) {
try {
std::cout << "💾 Exporting vocabulary to: " + filename << std::endl;
// Note: HyperFormer doesn't expose vocabulary directly
// We'll export statistics instead
try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
writer.println("# Fraymus Convergence - Vocabulary Export");
writer.println("# Identity: " + IDENTITY);
writer.println("# Timestamp: " + java.time.Instant.now());
writer.println();
writer.println("Vocabulary Size: " + HDC_BRAIN.vocabSize());
writer.println("Memory Weight: " + HDC_BRAIN.memoryWeight());
writer.println("Total Learned: " + totalLearned);
writer.println("Total Predictions: " + totalPredictions);
writer.println();
writer.println("# Note: Individual word vectors are not exported");
writer.println("# Use 'mint' command to create encrypted brain snapshot");
}
std::cout << "   ✓ Vocabulary stats exported" << std::endl;
AUDIT.log("vocab_export", filename);
} catch (IOException e) {
System.err.println("   ❌ Error exporting: " + e.getMessage());
}
}
private static void pruneVocabulary(int targetSize) {
int currentSize = HDC_BRAIN.vocabSize();
if (currentSize <= targetSize) {
std::cout << "   ℹ Current vocabulary (" + currentSize + ") is already at or below target (" + targetSize + ")" << std::endl;
return;
}
std::cout << "⚠️  WARNING: Vocabulary pruning requires brain reset" << std::endl;
std::cout << "   Current size: " + currentSize << std::endl;
std::cout << "   Target size: " + targetSize << std::endl;
std::cout <<  << std::endl;
std::cout << "   Note: HyperFormer doesn't support selective pruning." << std::endl;
std::cout << "   Consider using 'mint' to save current state, then 'reset' to start fresh." << std::endl;
std::cout << "   Or continue learning - the brain will naturally optimize." << std::endl;
}
/**
* Extract tool command from LLM response
*/
private static std::string extractToolCommand(std::string response, std::string toolPrefix) {
int startIdx = response.indexOf(toolPrefix);
if (startIdx == -1) return null;
startIdx += toolPrefix.length();
// Skip whitespace
while (startIdx < response.length() && Character.isWhitespace(response.charAt(startIdx))) {
startIdx++;
}
// Find end of command (newline or end of string)
int endIdx = response.indexOf('\n', startIdx);
if (endIdx == -1) {
endIdx = response.length();
}
return response.substring(startIdx, endIdx).trim();
}
}
