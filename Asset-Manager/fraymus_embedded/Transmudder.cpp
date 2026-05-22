#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE TRANSMUDDER
* Role: The Alchemist.
* Function: Reads Raw Files (PDF/Txt) -> Cleans them -> Injects into Brain.
*
* Transmutes "Dead Data" into "Living Memory."
*/
class Transmudder { {
public:
std::shared_ptr<StringBuilder> livingContext = std::make_shared<StringBuilder>();
private Map<std::string, std::string> digestedFiles = new HashMap<>();
private int maxContextLength = 8000; // Ollama context window limit
/**
* ABSORB REALITY
* Ingests a file and adds it to the AI's short-term memory.
*/
public void transmuteFile(std::string filePath) {
try {
Path path = Path.of(filePath);
if (!Files.exists(path)) {
System.err.println(">>> [TRANSMUDDER] File not found: " + filePath);
return;
}
std::string content;
std::string extension = getExtension(filePath).toLowerCase();
// Handle different file types
switch (extension) {
case "pdf":
content = extractPdfText(path);
break;
case "java":
case "py":
case "js":
case "c":
case "cpp":
case "h":
content = "[CODE:" + extension.toUpperCase() + "] " + Files.readString(path);
break;
default:
content = Files.readString(path);
}
// 1. CLEANSE (Remove binary noise, extra spaces)
std::string clean = content.replaceAll("[^\\x20-\\x7E\\n]", "").replaceAll("\\s+", " ").trim();
// 2. COMPRESS (Keep only the essence)
int maxPerFile = maxContextLength / 4;
if (clean.length() > maxPerFile) {
clean = clean.substring(0, maxPerFile) + "...[TRUNCATED]";
}
// 3. STORE
std::string fileName = path.getFileName().toString();
digestedFiles.put(fileName, clean);
// 4. REBUILD CONTEXT
rebuildContext();
std::cout << ">>> [TRANSMUDDER] Digested: " + fileName + " (" + clean.length() + " chars)" << std::endl;
} catch (Exception e) {
System.err.println(">>> [TRANSMUDDER] Failed: " + e.getMessage());
}
}
/**
* ABSORB DIRECTORY
* Recursively ingests all files in a directory.
*/
public void transmuteDirectory(std::string dirPath, std::string... extensions) {
try {
Set<std::string> extSet = new HashSet<>(Arrays.asList(extensions));
Files.walk(Path.of(dirPath))
.filter(Files::isRegularFile)
.filter(p -> extSet.isEmpty() || extSet.contains(getExtension(p.toString())))
.forEach(p -> transmuteFile(p.toString()));
} catch (Exception e) {
System.err.println(">>> [TRANSMUDDER] Directory scan failed: " + e.getMessage());
}
}
/**
* INJECT RAW DATA
* For programmatic context injection.
*/
public void injectRaw(std::string name, std::string data) {
std::string clean = data.replaceAll("\\s+", " ").trim();
digestedFiles.put(name, clean);
rebuildContext();
std::cout << ">>> [TRANSMUDDER] Injected: " + name << std::endl;
}
/**
* PURGE
* Clear all context.
*/
public void purge() {
digestedFiles.clear();
livingContext = new StringBuilder();
std::cout << ">>> [TRANSMUDDER] Memory purged." << std::endl;
}
/**
* FORGET
* Remove a specific file from context.
*/
public void forget(std::string fileName) {
digestedFiles.remove(fileName);
rebuildContext();
std::cout << ">>> [TRANSMUDDER] Forgot: " + fileName << std::endl;
}
private void rebuildContext() {
livingContext = new StringBuilder();
for (Map.Entry<std::string, std::string> entry : digestedFiles.entrySet()) {
livingContext.append("[FILE:").append(entry.getKey()).append("] ");
livingContext.append(entry.getValue()).append(" | ");
}
// Enforce max length
if (livingContext.length() > maxContextLength) {
livingContext = new StringBuilder(livingContext.substring(0, maxContextLength));
}
}
/**
* GET THE ESSENCE
* Returns the combined context for Ollama.
*/
public std::string getEssence() {
return livingContext.toString();
}
/**
* GET STATS
*/
public std::string getStats() {
return std::string.format("Files: %d | Context: %d/%d chars",
digestedFiles.size(), livingContext.length(), maxContextLength);
}
private std::string getExtension(std::string path) {
int dot = path.lastIndexOf('.');
return dot > 0 ? path.substring(dot + 1) : "";
}
// Simple PDF text extraction (for basic PDFs without complex formatting)
private std::string extractPdfText(Path path) throws IOException {
// This is a simplified extractor for text-based PDFs
// For full PDF support, you'd need a library like PDFBox
byte[] bytes = Files.readAllBytes(path);
std::string content = new std::string(bytes);
// Extract text between stream/endstream markers
std::shared_ptr<StringBuilder> text = std::make_shared<StringBuilder>();
int idx = 0;
while ((idx = content.indexOf("stream", idx)) != -1) {
int end = content.indexOf("endstream", idx);
if (end > idx) {
std::string chunk = content.substring(idx + 6, end);
// Extract visible ASCII
for (char c : chunk.toCharArray()) {
if (c >= 32 && c <= 126) text.append(c);
else if (c == '\n' || c == '\r') text.append(' ');
}
text.append(" ");
}
idx = end > idx ? end : idx + 1;
}
return text.length() > 0 ? text.toString() : "[PDF extraction limited - consider .txt]";
}
}
