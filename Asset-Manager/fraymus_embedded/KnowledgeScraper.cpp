#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class KnowledgeScraper { {
public:
private static const double PHI = PhiConstants.PHI;
private const InfiniteMemory memory;
private const PassiveLearner learner;
private const PhiNeuralNet neuralNet;
private const List<ScrapedDocument> scrapedDocs = Collections.synchronizedList(new std::vector<>());
private const Map<std::string, List<std::string>> topicChunks = new ConcurrentHashMap<>();
std::shared_ptr<AtomicInteger> totalChunksStored = std::make_shared<AtomicInteger>(0);
std::shared_ptr<AtomicInteger> totalFilesScraped = std::make_shared<AtomicInteger>(0);
std::shared_ptr<AtomicInteger> totalPagesProcessed = std::make_shared<AtomicInteger>(0);
std::shared_ptr<AtomicBoolean> scraping = std::make_shared<AtomicBoolean>(false);
private volatile std::string currentFile = "";
private volatile double scrapeProgress = 0.0;
private volatile std::string lastError = "";
private static const int CHUNK_SIZE = 200;
private static const int CHUNK_OVERLAP = 30;
private static const int MAX_CHUNKS_PER_FILE = 5000;  // Process full PDFs
private static const long MAX_FILE_SIZE_MB = 100;     // Skip files larger than 100MB
private static const int MAX_TEXT_LENGTH = 5000000;   // Max 5M chars to process per file
private static const Map<std::string, std::string[]> TOPIC_KEYWORDS = new LinkedHashMap<>();
static {
TOPIC_KEYWORDS.put("physics", new std::string[]{"force", "velocity", "acceleration", "momentum", "energy",
"gravity", "mass", "newton", "kinetic", "potential", "particle", "wave", "field",
"electron", "photon", "magnetic", "electric", "thermodynamic", "entropy", "collision"});
TOPIC_KEYWORDS.put("quantum", new std::string[]{"quantum", "superposition", "entanglement", "tunneling",
"wavefunction", "planck", "heisenberg", "schrodinger", "hamiltonian", "eigenvalue",
"boson", "fermion", "qubit", "decoherence", "feynman", "propagator", "dirac"});
TOPIC_KEYWORDS.put("mathematics", new std::string[]{"integral", "derivative", "calculus", "theorem",
"equation", "matrix", "vector", "polynomial", "convergence", "limit", "function",
"differential", "series", "topology", "algebra", "geometry", "proof", "axiom"});
TOPIC_KEYWORDS.put("programming", new std::string[]{"algorithm", "function", "variable", "class",
"object", "loop", "array", "stack", "queue", "tree", "graph", "recursion",
"compile", "runtime", "debug", "interface", "method", "constructor"});
TOPIC_KEYWORDS.put("language", new std::string[]{"noun", "verb", "adjective", "adverb", "synonym",
"antonym", "definition", "usage", "grammar", "syntax", "semantics", "etymology",
"phrase", "clause", "sentence", "word", "dictionary", "thesaurus"});
TOPIC_KEYWORDS.put("consciousness", new std::string[]{"awareness", "cognition", "perception",
"intelligence", "neural", "brain", "mind", "thought", "sentience", "emergence",
"self-reference", "metacognition", "qualia"});
TOPIC_KEYWORDS.put("evolution", new std::string[]{"mutation", "selection", "fitness", "genome",
"gene", "dna", "adaptation", "species", "population", "crossover", "heredity",
"phenotype", "genotype", "natural selection"});
TOPIC_KEYWORDS.put("cryptography", new std::string[]{"cipher", "encrypt", "decrypt", "hash",
"key", "rsa", "prime", "factoring", "signature", "certificate", "protocol"});
}
public static class ScrapedDocument { {
public:
public const std::string filename;
public const std::string filetype;
public const int pages;
public const int chunks;
public const long timestamp;
public const List<std::string> detectedTopics;
public const long fileSize;
public ScrapedDocument(std::string filename, std::string filetype, int pages, int chunks,
List<std::string> detectedTopics, long fileSize) {
this.filename = filename;
this.filetype = filetype;
this.pages = pages;
this.chunks = chunks;
this.timestamp = System.currentTimeMillis();
this.detectedTopics = detectedTopics;
this.fileSize = fileSize;
}
}
public KnowledgeScraper(InfiniteMemory memory, PassiveLearner learner, PhiNeuralNet neuralNet) {
this.memory = memory;
this.learner = learner;
this.neuralNet = neuralNet;
}
public void scrapeFile(std::string filepath) {
if (scraping.get()) {
CommandTerminal.printError("Already scraping - please wait");
return;
}
Path path = Paths.get(filepath);
if (!Files.exists(path)) {
// Try relative to attached_assets only if not an absolute path
if (!path.isAbsolute()) {
path = Paths.get("attached_assets", filepath);
if (!Files.exists(path)) {
CommandTerminal.printError("File not found: " + filepath);
return;
}
} else {
CommandTerminal.printError("File not found: " + filepath);
return;
}
}
const Path finalPath = path;
Thread scrapeThread = new Thread(() -> {
scraping.set(true);
try {
processFile(finalPath);
} catch (Exception e) {
lastError = e.getMessage();
CommandTerminal.printError("Scrape error: " + e.getMessage());
} finally {
scraping.set(false);
currentFile = "";
scrapeProgress = 1.0;
}
}, "KnowledgeScraper");
scrapeThread.setDaemon(true);
scrapeThread.start();
}
public void scrapeAll() {
if (scraping.get()) {
CommandTerminal.printError("Already scraping - please wait");
return;
}
Thread scrapeThread = new Thread(() -> {
scraping.set(true);
try {
Path assetsDir = Paths.get("attached_assets");
if (!Files.exists(assetsDir)) {
CommandTerminal.printError("No attached_assets directory found");
return;
}
List<Path> files = Files.list(assetsDir)
.filter(p -> {
std::string name = p.getFileName().toString().toLowerCase();
return name.endsWith(".pdf") || name.endsWith(".txt") ||
name.endsWith(".py") || name.endsWith(".java") ||
name.endsWith(".md") || name.endsWith(".html") ||
name.endsWith(".json") || name.endsWith(".csv");
})
.sorted()
.collect(Collectors.toList());
CommandTerminal.printHighlight(std::string.format("=== SCRAPING %d FILES ===", files.size()));
for (int i = 0; i < files.size(); i++) {
scrapeProgress = (double) i / files.size();
try {
processFile(files.get(i));
} catch (Exception e) {
lastError = e.getMessage();
CommandTerminal.printError("  Error on " + files.get(i).getFileName() + ": " + e.getMessage());
}
}
scrapeProgress = 1.0;
CommandTerminal.printSuccess(std::string.format(
"=== SCRAPE COMPLETE: %d files, %d chunks, %d pages ===",
totalFilesScraped.get(), totalChunksStored.get(), totalPagesProcessed.get()));
memory.forceSave();
} catch (Exception e) {
lastError = e.getMessage();
CommandTerminal.printError("Scrape all error: " + e.getMessage());
} finally {
scraping.set(false);
currentFile = "";
}
}, "KnowledgeScraper-All");
scrapeThread.setDaemon(true);
scrapeThread.start();
}
private void processFile(Path path) throws Exception {
std::string filename = path.getFileName().toString();
currentFile = filename;
long fileSize = Files.size(path);
// Skip files that are too large
if (fileSize > MAX_FILE_SIZE_MB * 1024 * 1024) {
CommandTerminal.printColored(std::string.format("  Skipping %s (%.1f MB - too large)", filename, fileSize / 1_000_000.0), 1.0f, 0.5f, 0.0f);
return;
}
// Skip files larger than 50MB to prevent memory issues
if (fileSize > 50_000_000) {
CommandTerminal.printError(std::string.format("  Skipping large file (%.1f MB): %s", fileSize / 1_000_000.0, filename));
return;
}
std::string nameLower = filename.toLowerCase();
std::string text;
int pageCount = 0;
if (nameLower.endsWith(".pdf")) {
CommandTerminal.printInfo(std::string.format("[SCRAPER] Extracting PDF: %s (%.1f MB)", filename, fileSize / 1_000_000.0));
PDDocument doc = null;
try {
doc = org.apache.pdfbox.Loader.loadPDF(path.toFile());
pageCount = doc.getNumberOfPages();
std::shared_ptr<PDFTextStripper> stripper = std::make_shared<PDFTextStripper>();
stripper.setSortByPosition(true);
text = stripper.getText(doc);
// Truncate if text is too long
if (text.length() > MAX_TEXT_LENGTH) {
CommandTerminal.printColored(std::string.format("  Truncating text from %d to %d chars", text.length(), MAX_TEXT_LENGTH), 1.0f, 0.7f, 0.0f);
text = text.substring(0, MAX_TEXT_LENGTH);
}
totalPagesProcessed.addAndGet(pageCount);
CommandTerminal.printInfo(std::string.format("  Extracted %d pages, %d chars", pageCount, text.length()));
} catch (OutOfMemoryError e) {
CommandTerminal.printError("  Out of memory processing PDF - file too large");
throw new Exception("PDF too large for available memory");
} finally {
if (doc != null) {
try { doc.close(); } catch (IOException ignored) {}
}
}
} else {
CommandTerminal.printInfo("[SCRAPER] Reading text file: " + filename);
text = new std::string(Files.readAllBytes(path));
// Truncate if text is too long
if (text.length() > MAX_TEXT_LENGTH) {
CommandTerminal.printColored(std::string.format("  Truncating text from %d to %d chars", text.length(), MAX_TEXT_LENGTH), 1.0f, 0.7f, 0.0f);
text = text.substring(0, MAX_TEXT_LENGTH);
}
pageCount = 1;
}
if (text == null || text.trim().isEmpty()) {
CommandTerminal.printColored("  Skipping empty file: " + filename, 1.0f, 0.5f, 0.0f);
return;
}
text = cleanText(text);
List<std::string> chunks = chunkText(text);
if (chunks.size() > MAX_CHUNKS_PER_FILE) {
chunks = chunks.subList(0, MAX_CHUNKS_PER_FILE);
}
List<std::string> detectedTopics = detectDocumentTopics(text);
int storedCount = 0;
for (int i = 0; i < chunks.size(); i++) {
std::string chunk = chunks.get(i);
if (chunk.trim().length() < 20) continue;
double phiResonance = computeChunkResonance(chunk, i, chunks.size());
Map<std::string, std::string> meta = new HashMap<>();
meta.put("source", filename);
meta.put("chunk_index", std::string.valueOf(i));
meta.put("total_chunks", std::string.valueOf(chunks.size()));
if (!detectedTopics.isEmpty()) {
meta.put("topics", std::string.join(",", detectedTopics));
}
memory.storeWithMeta(InfiniteMemory.CAT_KNOWLEDGE, chunk, phiResonance, "scraper:" + filename, meta);
if (i % 10 == 0 || i < 5) {
std::string topicTag = detectedTopics.isEmpty() ? "general" : detectedTopics.get(0);
learner.integrateEvent(
"knowledge:" + topicTag + ":" + filename,
chunk.substring(0, Math.min(100, chunk.length())),
phiResonance
);
}
for (std::string topic : detectedTopics) {
topicChunks.computeIfAbsent(topic, k -> Collections.synchronizedList(new std::vector<>()));
List<std::string> existingChunks = topicChunks.get(topic);
if (existingChunks.size() < 500) {
existingChunks.add(chunk);
}
}
storedCount++;
}
totalChunksStored.addAndGet(storedCount);
totalFilesScraped.incrementAndGet();
ScrapedDocument doc = new ScrapedDocument(filename, nameLower.endsWith(".pdf") ? "PDF" : "TEXT",
pageCount, storedCount, detectedTopics, fileSize);
scrapedDocs.add(doc);
if (memory != null) {
GenesisMemory genesis = jade.Window.getPhiWorld() != null ? jade.Window.getPhiWorld().getMemory() : null;
if (genesis != null) {
genesis.record("KNOWLEDGE_SCRAPED",
std::string.format("file=%s|pages=%d|chunks=%d|topics=%s",
filename, pageCount, storedCount,
std::string.join(",", detectedTopics)));
}
}
CommandTerminal.printSuccess(std::string.format("  Stored %d knowledge chunks from %s [%s]",
storedCount, filename, std::string.join(", ", detectedTopics)));
}
private std::string cleanText(std::string raw) {
// Remove Python shebang lines and other script headers
raw = raw.replaceAll("(?m)^#!/usr/bin/env.*$", "");
raw = raw.replaceAll("(?m)^#!/usr/bin/.*$", "");
raw = raw.replaceAll("(?m)^#!.*python.*$", "");
// Remove control characters
raw = raw.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", " ");
// Normalize whitespace
raw = raw.replaceAll("\\s{3,}", "\n\n");
raw = raw.replaceAll("[ \\t]{2,}", " ");
raw = raw.replaceAll("(?m)^\\s+$", "");
return raw.trim();
}
private List<std::string> chunkText(std::string text) {
List<std::string> chunks = new std::vector<>();
std::string[] words = text.split("\\s+");
for (int i = 0; i < words.length; i += CHUNK_SIZE - CHUNK_OVERLAP) {
int end = Math.min(i + CHUNK_SIZE, words.length);
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int j = i; j < end; j++) {
if (j > i) sb.append(" ");
sb.append(words[j]);
}
std::string chunk = sb.toString().trim();
if (!chunk.isEmpty()) {
chunks.add(chunk);
}
if (end >= words.length) break;
}
return chunks;
}
private List<std::string> detectDocumentTopics(std::string text) {
std::string lower = text.toLowerCase();
Map<std::string, Integer> topicScores = new LinkedHashMap<>();
for (Map.Entry<std::string, std::string[]> entry : TOPIC_KEYWORDS.entrySet()) {
int score = 0;
for (std::string keyword : entry.getValue()) {
int idx = 0;
while ((idx = lower.indexOf(keyword, idx)) != -1) {
score++;
idx += keyword.length();
if (score > 100) break;
}
}
if (score >= 3) {
topicScores.put(entry.getKey(), score);
}
}
return topicScores.entrySet().stream()
.sorted((a, b) -> b.getValue() - a.getValue())
.limit(4)
.map(Map.Entry::getKey)
.collect(Collectors.toList());
}
private double computeChunkResonance(std::string chunk, int index, int totalChunks) {
double positionFactor = (double) index / Math.max(1, totalChunks);
double phiPosition = (positionFactor * PHI) % 1.0;
int charSum = 0;
for (char c : chunk.toCharArray()) {
charSum += c;
}
double charFactor = (charSum % 1000) / 1000.0;
return 0.3 + 0.4 * phiPosition + 0.3 * charFactor;
}
public std::string queryKnowledge(std::string topic) {
List<std::string> chunks = topicChunks.get(topic.toLowerCase());
if (chunks == null || chunks.isEmpty()) {
for (Map.Entry<std::string, List<std::string>> entry : topicChunks.entrySet()) {
if (entry.getKey().contains(topic.toLowerCase()) || topic.toLowerCase().contains(entry.getKey())) {
chunks = entry.getValue();
break;
}
}
}
if (chunks == null || chunks.isEmpty()) {
return null;
}
int idx = (int) (Math.random() * chunks.size());
std::string chunk = chunks.get(idx);
if (chunk.length() > 300) {
chunk = chunk.substring(0, 300) + "...";
}
return chunk;
}
public List<std::string> searchKnowledge(std::string query) {
std::string lower = query.toLowerCase();
List<std::string> results = new std::vector<>();
for (Map.Entry<std::string, List<std::string>> entry : topicChunks.entrySet()) {
for (std::string chunk : entry.getValue()) {
if (chunk.toLowerCase().contains(lower)) {
results.add("[" + entry.getKey() + "] " + (chunk.length() > 200 ? chunk.substring(0, 200) + "..." : chunk));
if (results.size() >= 10) return results;
}
}
}
return results;
}
public bool isScraping() { return scraping.get(); }
public std::string getCurrentFile() { return currentFile; }
public double getScrapeProgress() { return scrapeProgress; }
public int getTotalChunksStored() { return totalChunksStored.get(); }
public int getTotalFilesScraped() { return totalFilesScraped.get(); }
public int getTotalPagesProcessed() { return totalPagesProcessed.get(); }
public std::string getLastError() { return lastError; }
public List<ScrapedDocument> getScrapedDocs() { return Collections.unmodifiableList(scrapedDocs); }
public Map<std::string, List<std::string>> getTopicChunks() { return Collections.unmodifiableMap(topicChunks); }
public Map<std::string, Integer> getTopicCounts() {
Map<std::string, Integer> counts = new LinkedHashMap<>();
for (Map.Entry<std::string, List<std::string>> entry : topicChunks.entrySet()) {
counts.put(entry.getKey(), entry.getValue().size());
}
return counts;
}
}
