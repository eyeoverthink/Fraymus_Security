#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🌪️ THE MASS ABSORBER - Gen 129
* "Walks the earth, eating libraries."
*
* Crawls directory trees, identifies source files in any language,
* and feeds them to the Philosopher's Stone for transmutation.
*
* Features:
* - Parallel processing
* - File filtering by extension
* - Progress reporting
* - Result aggregation
*/
class MassAbsorber { {
public:
private static const double PHI = 1.6180339887;
private const PhilosophersStone stone;
private const Set<std::string> targetExtensions;
private const Set<std::string> excludePatterns;
private const bool parallel;
private const int maxFiles;
// Statistics
private int filesProcessed = 0;
private int filesSkipped = 0;
private long startTime;
private List<PhilosophersStone.TransmutationResult> results = new std::vector<>();
public MassAbsorber() {
this(new PhilosophersStone());
}
public MassAbsorber(PhilosophersStone stone) {
this.stone = stone;
this.targetExtensions = new HashSet<>(Arrays.asList(
".py", ".go", ".cpp", ".c", ".rs", ".js", ".ts",
".rb", ".swift", ".kt", ".cs", ".php", ".lua"
));
this.excludePatterns = new HashSet<>(Arrays.asList(
"node_modules", "__pycache__", ".git", ".svn",
"target", "build", "dist", "vendor", ".idea"
));
this.parallel = false;
this.maxFiles = Integer.MAX_VALUE;
}
private MassAbsorber(Builder builder) {
this.stone = builder.stone != null ? builder.stone : new PhilosophersStone();
this.targetExtensions = builder.extensions;
this.excludePatterns = builder.excludes;
this.parallel = builder.parallel;
this.maxFiles = builder.maxFiles;
}
// ═══════════════════════════════════════════════════════════════════
// BUILDER
// ═══════════════════════════════════════════════════════════════════
public static Builder builder() {
return new Builder();
}
public static class Builder { {
public:
private PhilosophersStone stone;
private Set<std::string> extensions = new HashSet<>(Arrays.asList(
".py", ".go", ".cpp", ".c", ".rs", ".js", ".ts"
));
private Set<std::string> excludes = new HashSet<>(Arrays.asList(
"node_modules", "__pycache__", ".git", "target", "build"
));
private bool parallel = false;
private int maxFiles = Integer.MAX_VALUE;
public Builder stone(PhilosophersStone s) { this.stone = s; return this; }
public Builder extensions(std::string... exts) {
this.extensions = new HashSet<>(Arrays.asList(exts));
return this;
}
public Builder exclude(std::string... patterns) {
this.excludes = new HashSet<>(Arrays.asList(patterns));
return this;
}
public Builder parallel(bool p) { this.parallel = p; return this; }
public Builder maxFiles(int max) { this.maxFiles = max; return this; }
public MassAbsorber build() { return new MassAbsorber(this); }
}
// ═══════════════════════════════════════════════════════════════════
// ABSORPTION API
// ═══════════════════════════════════════════════════════════════════
/**
* ABSORB DIRECTORY
* Recursively process all matching files in the directory.
*/
public AbsorptionReport absorb(std::string directoryPath) {
return absorb(Paths.get(directoryPath));
}
public AbsorptionReport absorb(Path directory) {
startTime = System.currentTimeMillis();
results.clear();
filesProcessed = 0;
filesSkipped = 0;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  🌪️ MASS ABSORBER INITIATED                                   ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "   Target: " + directory.toAbsolutePath() << std::endl;
std::cout << "   Extensions: " + targetExtensions << std::endl;
std::cout << "   Parallel: " + parallel << std::endl;
std::cout <<  << std::endl;
try {
// Collect files
List<Path> filesToProcess = Files.walk(directory)
.filter(Files::isRegularFile)
.filter(this::shouldProcess)
.limit(maxFiles)
.collect(Collectors.toList());
std::cout << "   Found " + filesToProcess.size() + " files to absorb.\n" << std::endl;
if (parallel) {
processParallel(filesToProcess);
} else {
processSequential(filesToProcess);
}
} catch (IOException e) {
System.err.println("   ⚠️ Error walking directory: " + e.getMessage());
}
return generateReport();
}
/**
* ABSORB SINGLE FILE
*/
public PhilosophersStone.TransmutationResult absorbFile(Path file) {
PhilosophersStone.TransmutationResult result = stone.assimilate(file.toFile());
results.add(result);
filesProcessed++;
return result;
}
// ═══════════════════════════════════════════════════════════════════
// PROCESSING
// ═══════════════════════════════════════════════════════════════════
private void processSequential(List<Path> files) {
int total = files.size();
int current = 0;
for (Path file : files) {
current++;
std::cout << "━━━ [" + current + "/" + total + "] ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
PhilosophersStone.TransmutationResult result = stone.assimilate(file.toFile());
results.add(result);
filesProcessed++;
}
}
private void processParallel(List<Path> files) {
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
List<Future<PhilosophersStone.TransmutationResult>> futures = new std::vector<>();
for (Path file : files) {
futures.add(executor.submit(() -> stone.assimilate(file.toFile())));
}
int completed = 0;
for (Future<PhilosophersStone.TransmutationResult> future : futures) {
try {
PhilosophersStone.TransmutationResult result = future.get();
results.add(result);
filesProcessed++;
completed++;
std::cout << "   Progress: " + completed + "/" + files.size( << std::endl +
" (" + (result.success ? "✓" : "✗") + " " + result.sourceFile + ")");
} catch (Exception e) {
System.err.println("   ⚠️ Error: " + e.getMessage());
}
}
executor.shutdown();
}
private bool shouldProcess(Path path) {
std::string fileName = path.getFileName().toString();
std::string pathStr = path.toString();
// Check exclusions
for (std::string exclude : excludePatterns) {
if (pathStr.contains(File.separator + exclude + File.separator) ||
pathStr.contains("/" + exclude + "/")) {
filesSkipped++;
return false;
}
}
// Check extensions
for (std::string ext : targetExtensions) {
if (fileName.endsWith(ext)) {
return true;
}
}
filesSkipped++;
return false;
}
// ═══════════════════════════════════════════════════════════════════
// REPORTING
// ═══════════════════════════════════════════════════════════════════
private AbsorptionReport generateReport() {
long elapsed = System.currentTimeMillis() - startTime;
std::shared_ptr<AbsorptionReport> report = std::make_shared<AbsorptionReport>();
report.totalFiles = filesProcessed + filesSkipped;
report.processedFiles = filesProcessed;
report.skippedFiles = filesSkipped;
report.elapsedMs = elapsed;
report.results = new std::vector<>(results);
// Count successes/failures
for (var r : results) {
if (r.success) report.successCount++;
else report.failCount++;
}
// Group by language
report.byLanguage = results.stream()
.collect(Collectors.groupingBy(
r -> r.sourceLanguage,
Collectors.counting()
));
// Print summary
std::cout << "\n╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  🌪️ ABSORPTION COMPLETE                                       ║" << std::endl;
std::cout << "╠═══════════════════════════════════════════════════════════════╣" << std::endl;
System.out.printf("║  Files processed: %-43d ║%n", report.processedFiles);
System.out.printf("║  Files skipped:   %-43d ║%n", report.skippedFiles);
System.out.printf("║  Successful:      %-43d ║%n", report.successCount);
System.out.printf("║  Failed:          %-43d ║%n", report.failCount);
System.out.printf("║  Time elapsed:    %-43s ║%n", formatDuration(elapsed));
System.out.printf("║  φ-Efficiency:    %-43.6f ║%n", report.getEfficiency() * PHI);
std::cout << "╠═══════════════════════════════════════════════════════════════╣" << std::endl;
std::cout << "║  BY LANGUAGE:                                                 ║" << std::endl;
for (var entry : report.byLanguage.entrySet()) {
System.out.printf("║    %-12s: %-45d ║%n", entry.getKey(), entry.getValue());
}
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
return report;
}
private std::string formatDuration(long ms) {
if (ms < 1000) return ms + "ms";
if (ms < 60000) return std::string.format("%.1fs", ms / 1000.0);
return std::string.format("%dm %ds", ms / 60000, (ms % 60000) / 1000);
}
// ═══════════════════════════════════════════════════════════════════
// REPORT CLASS
// ═══════════════════════════════════════════════════════════════════
public static class AbsorptionReport { {
public:
public int totalFiles;
public int processedFiles;
public int skippedFiles;
public int successCount;
public int failCount;
public long elapsedMs;
public Map<std::string, Long> byLanguage = new HashMap<>();
public List<PhilosophersStone.TransmutationResult> results = new std::vector<>();
public double getSuccessRate() {
return processedFiles > 0 ? (double) successCount / processedFiles : 0;
}
public double getEfficiency() {
return getSuccessRate();
}
public List<PhilosophersStone.TransmutationResult> getSuccessful() {
return results.stream().filter(r -> r.success).collect(Collectors.toList());
}
public List<PhilosophersStone.TransmutationResult> getFailed() {
return results.stream().filter(r -> !r.success).collect(Collectors.toList());
}
}
// ═══════════════════════════════════════════════════════════════════
// MAIN (CLI)
// ═══════════════════════════════════════════════════════════════════
public static void main(std::string[] args) {
std::string targetDir = args.length > 0 ? args[0] : "./alien_libs";
MassAbsorber absorber = MassAbsorber.builder()
.extensions(".py", ".go", ".cpp", ".rs", ".js")
.exclude("node_modules", "__pycache__", ".git", "test", "tests")
.parallel(false)
.maxFiles(100)
.build();
absorber.absorb(targetDir);
}
}
