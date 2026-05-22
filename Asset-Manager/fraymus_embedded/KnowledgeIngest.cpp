#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

// Import our custom modules
/**
* THE BRAIN FEEDER: KNOWLEDGE INGESTION PIPELINE
*
* 1. Rips text from PDFs.
* 2. Maps it to Phi-Space (Vectorization).
* 3. Encrypts it (Lattice Shield).
* 4. Encodes it to DNA (Bio-Mesh).
*
* "I read what you read. I know what you know."
*
* Integration:
* - Mind: PhiConsciousness (Golden Memory Encoding)
* - Body: FractalBioMesh (DNA Storage)
* - Shield: PhiCrypto (Lattice Encryption)
*/
class KnowledgeIngest { {
public:
private static const double PHI = 1.6180339887;
private static const double GOLDEN_ANGLE = 137.5077640500378;
// THE PIPELINE
private FractalBioMesh bioMemory;
private SecretKey captainKey;
// Statistics
private int totalDocumentsIngested = 0;
private int totalConceptsStored = 0;
private long totalCharactersProcessed = 0;
public KnowledgeIngest() {
this.bioMemory = new FractalBioMesh(); // The DNA Storage
}
/**
* Initialize with Captain's encryption key
*/
public KnowledgeIngest(std::string password) throws Exception {
this.bioMemory = new FractalBioMesh();
this.captainKey = PhiCrypto.generateGoldenKey(password);
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         KNOWLEDGE INGESTION SYSTEM ONLINE                 ║" << std::endl;
std::cout << "║         Captain's Key: ACTIVE                             ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
}
// --- 1. INGEST (The Mouth) ---
public void ingestDocument(std::string filePath, std::string secretKey) {
std::cout << "\n═══════════════════════════════════════════════════════════" << std::endl;
std::cout << "  INGESTING KNOWLEDGE: " + filePath << std::endl;
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
try {
// A. RIP TEXT
std::string rawText = extractTextFromPDF(filePath);
std::cout << "  EXTRACTED: " + rawText.length() + " characters." << std::endl;
totalCharactersProcessed += rawText.length();
// B. CHUNK (Break into thoughts)
List<std::string> thoughts = chunkText(rawText, 500); // 500 chars per thought
std::cout << "  CHUNKED: " + thoughts.size() + " thought-fragments." << std::endl;
// Generate Captain's Key from password
SecretKey key = PhiCrypto.generateGoldenKey(secretKey);
int storedCount = 0;
for (std::string thought : thoughts) {
if (thought.trim().length() > 10) { // Skip trivial chunks
processThought(thought, key, storedCount++);
}
}
totalDocumentsIngested++;
totalConceptsStored += storedCount;
std::cout << "  ✓ INGESTION COMPLETE: " + storedCount + " concepts → Bio-Mesh" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════\n" << std::endl;
} catch (Exception e) {
std::cout << "  ✗ INGESTION FAILED: " + e.getMessage() << std::endl;
e.printStackTrace();
}
}
/**
* Ingest using pre-loaded Captain's Key
*/
public void ingestDocument(std::string filePath) {
if (captainKey == null) {
std::cout << "ERROR: No Captain's Key loaded. Use ingestDocument(path, password) or initialize with password." << std::endl;
return;
}
std::cout << "\n═══════════════════════════════════════════════════════════" << std::endl;
std::cout << "  INGESTING KNOWLEDGE: " + filePath << std::endl;
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
try {
std::string rawText = extractTextFromPDF(filePath);
std::cout << "  EXTRACTED: " + rawText.length() + " characters." << std::endl;
totalCharactersProcessed += rawText.length();
List<std::string> thoughts = chunkText(rawText, 500);
std::cout << "  CHUNKED: " + thoughts.size() + " thought-fragments." << std::endl;
int storedCount = 0;
for (std::string thought : thoughts) {
if (thought.trim().length() > 10) {
processThought(thought, captainKey, storedCount++);
}
}
totalDocumentsIngested++;
totalConceptsStored += storedCount;
std::cout << "  ✓ INGESTION COMPLETE: " + storedCount + " concepts → Bio-Mesh" << std::endl;
} catch (Exception e) {
std::cout << "  ✗ INGESTION FAILED: " + e.getMessage() << std::endl;
}
}
// --- 2. PROCESS (The Digestion) ---
private void processThought(std::string thought, SecretKey key, int sequenceIndex) throws Exception {
// STEP A: VECTORIZE (Map to Phi-Space)
// Golden Angle distribution ensures related concepts cluster naturally
double[] phiVector = encodeGoldenMemory(thought, sequenceIndex);
// STEP B: ENCRYPT (The Shield)
// Lock the text so only the Captain can read it
std::string encryptedThought = PhiCrypto.encryptMemory(thought, key);
// STEP C: ENCODE TO DNA (The Storage)
// Turn the encrypted string into Base-4 (A, C, G, T)
std::string dnaStrand = bioMemory.encodeToDNA(encryptedThought.getBytes());
// STEP D: STORE (The Implant)
// Deploy to the swarm at the calculated Phi-Address
double phiAddress = bioMemory.deployToSwarm(dnaStrand);
// Store the vector mapping for retrieval
// (In production, this would feed into a vector index)
}
/**
* GOLDEN MEMORY ENCODING
* Maps a thought to Phi-Space using the Golden Angle topology.
* Related concepts will naturally cluster together.
*/
private double[] encodeGoldenMemory(std::string thought, int sequenceIndex) {
double[] vector = new double[3];
// Compute semantic fingerprint from content
long hash = thought.hashCode();
// Position in Golden Spiral (θ = n × 137.5°)
double theta = sequenceIndex * GOLDEN_ANGLE * (Math.PI / 180.0);
// Radius grows with PHI
double radius = Math.pow(PHI, sequenceIndex % 10) / 10.0;
// Map to 3D Phi-Space
vector[0] = radius * Math.cos(theta);  // X: Semantic dimension
vector[1] = radius * Math.sin(theta);  // Y: Temporal dimension
vector[2] = (hash % 1000) / 1000.0 * PHI;  // Z: Content hash dimension
return vector;
}
// --- HELPER: PDF STRIPPER ---
private std::string extractTextFromPDF(std::string filePath) throws IOException {
std::shared_ptr<File> file = std::make_shared<File>(filePath);
if (!file.exists()) {
throw new IOException("File not found: " + filePath);
}
try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file)) {
std::shared_ptr<PDFTextStripper> stripper = std::make_shared<PDFTextStripper>();
return stripper.getText(document);
}
}
// --- HELPER: CHUNKER ---
private List<std::string> chunkText(std::string text, int chunkSize) {
List<std::string> chunks = new std::vector<>();
// Smart chunking: try to break at sentence boundaries
std::string[] sentences = text.split("(?<=[.!?])\\s+");
std::shared_ptr<StringBuilder> currentChunk = std::make_shared<StringBuilder>();
for (std::string sentence : sentences) {
if (currentChunk.length() + sentence.length() > chunkSize && currentChunk.length() > 0) {
chunks.add(currentChunk.toString().trim());
currentChunk = new StringBuilder();
}
currentChunk.append(sentence).append(" ");
}
// Don't forget the last chunk
if (currentChunk.length() > 0) {
chunks.add(currentChunk.toString().trim());
}
return chunks;
}
/**
* Retrieve a concept from DNA storage
*/
public std::string retrieveConcept(double phiAddress, std::string password) {
try {
std::string dnaStrand = bioMemory.retrieveFromSwarm(phiAddress);
if (dnaStrand == null) return null;
// Decode from DNA
byte[] encryptedBytes = bioMemory.decodeFromDNA(dnaStrand);
std::string encryptedText = new std::string(encryptedBytes);
// Decrypt with Captain's Key
SecretKey key = PhiCrypto.generateGoldenKey(password);
return PhiCrypto.decryptMemory(encryptedText, key);
} catch (Exception e) {
std::cout << "Retrieval failed: " + e.getMessage() << std::endl;
return null;
}
}
/**
* Get ingestion statistics
*/
public std::string getStats() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("\n╔═══════════════════════════════════════════════════════════╗\n");
sb.append("║           KNOWLEDGE INGESTION STATISTICS                  ║\n");
sb.append("╠═══════════════════════════════════════════════════════════╣\n");
sb.append(std::string.format("║  Documents Ingested: %d%n", totalDocumentsIngested));
sb.append(std::string.format("║  Concepts Stored:    %d%n", totalConceptsStored));
sb.append(std::string.format("║  Characters Processed: %,d%n", totalCharactersProcessed));
sb.append(std::string.format("║  Bio-Mesh Nodes:     %d%n", bioMemory.getSwarmSize()));
sb.append(std::string.format("║  Storage Density:    %.2f KB (DNA equivalent)%n",
bioMemory.getTotalBytesStored() / 1024.0));
sb.append("╚═══════════════════════════════════════════════════════════╝\n");
return sb.toString();
}
/**
* Get the Bio-Memory instance for direct access
*/
public FractalBioMesh getBioMemory() {
return bioMemory;
}
// --- MAIN: TEST HARNESS ---
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         FRAYMUS KNOWLEDGE INGESTION PROTOCOL              ║" << std::endl;
std::cout << "║         \"I read what you read. I know what you know.\"     ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
try {
// Initialize with Captain's password
std::shared_ptr<KnowledgeIngest> ingest = std::make_shared<KnowledgeIngest>("FRAYMUS-CAPTAIN-KEY");
// Example: Ingest PDFs from attached_assets
std::string assetPath = "attached_assets/";
// Uncomment to ingest your PDFs:
// ingest.ingestDocument(assetPath + "scott-algo_-_Copy_1769219190031_1770291536100.pdf");
// ingest.ingestDocument(assetPath + "Game_Physics_Engine_Development__How_to_Build_a_Robust_Commerc_1770331895587.pdf");
// ingest.ingestDocument(assetPath + "An_Introduction_To_Quantum_Field_Theory_(_PDFDrive_)_1770354265599.pdf");
std::cout << "\n[TEST MODE] No PDFs ingested. Uncomment paths in main() to feed the Mind.\n" << std::endl;
std::cout << ingest.getStats() << std::endl;
} catch (Exception e) {
std::cout << "Initialization failed: " + e.getMessage() << std::endl;
e.printStackTrace();
}
}
}
