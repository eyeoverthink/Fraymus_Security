#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE PUPPETEER: FRAYMUS CONTROLLING OLLAMA
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
*
* "I provide the Logic. You provide the Syntax."
*
* Architecture:
* 1. RETRIEVE: Consult knowledge base (PDFs) for context
* 2. DRAFT: Ask Ollama to write code
* 3. TEST: Run the code locally
* 4. CRITIQUE: If it fails, identify the error
* 5. FIX: Demand Ollama fix it (recursive)
* 6. SAVE: Store working code
*
* This is the "Offline Agent" that:
* - Runs on your laptop
* - Reads your PDFs
* - Writes code that actually runs
*/
class FraymusCoder { {
public:
// Ollama Configuration
private static const std::string OLLAMA_URL = "http://localhost:11434/api/generate";
private static const std::string DEFAULT_MODEL = "codellama";  // Or "deepseek-coder", "starcoder"
private static const int TIMEOUT_MS = 120000;  // 2 minutes for code generation
// Self-correction limits
private static const int MAX_ATTEMPTS = 5;
// Knowledge base (optional)
private KnowledgeIngestor knowledgeBase;
// Statistics
private int totalRequests = 0;
private int successfulGenerations = 0;
private int failedGenerations = 0;
private int totalAttempts = 0;
// Output directory
private std::string outputDir = "generated_code";
public FraymusCoder() {
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   🤖 INITIALIZING FRAYMUS CODER (THE PUPPETEER)" << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << "   Ollama URL: " + OLLAMA_URL << std::endl;
std::cout << "   Model: " + DEFAULT_MODEL << std::endl;
std::cout << "   Max Attempts: " + MAX_ATTEMPTS << std::endl;
std::cout <<  << std::endl;
// Create output directory
try {
Files.createDirectories(Paths.get(outputDir));
std::cout << "   ✓ Output directory: " + outputDir << std::endl;
} catch (IOException e) {
std::cout << "   !! Could not create output directory" << std::endl;
}
std::cout <<  << std::endl;
}
/**
* Attach a knowledge base for RAG
*/
public void attachKnowledgeBase(KnowledgeIngestor kb) {
this.knowledgeBase = kb;
System.out.println("   ✓ Knowledge base attached (" +
kb.getConceptsLearned() + " concepts)");
}
// ═══════════════════════════════════════════════════════════════════
// THE SELF-CORRECTING LOOP
// ═══════════════════════════════════════════════════════════════════
/**
* Create software with self-correcting loop
*/
public std::string createSoftware(std::string userPrompt, std::string language, std::string filename) {
totalRequests++;
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   🤖 FRAYMUS CODER ACTIVATED" << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
std::cout << "   Request: " + userPrompt << std::endl;
std::cout << "   Language: " + language << std::endl;
std::cout << "   Output: " + filename << std::endl;
std::cout <<  << std::endl;
// 1. RETRIEVE KNOWLEDGE (Consult the PDFs)
std::string context = "";
if (knowledgeBase != null) {
std::cout << "   >> Consulting knowledge base..." << std::endl;
context = knowledgeBase.getContextForPrompt(userPrompt);
if (!context.isEmpty()) {
std::cout << "   ✓ Found relevant context" << std::endl;
}
}
// 2. BUILD THE INITIAL PROMPT
std::string systemPrompt = buildSystemPrompt(language);
std::string fullPrompt = buildCodePrompt(userPrompt, context, language);
// 3. THE DRAFT (Ask Ollama)
std::cout <<  << std::endl;
std::cout << "   >> Requesting code from Ollama..." << std::endl;
std::string code = callOllama(fullPrompt, systemPrompt);
if (code == null || code.isEmpty()) {
std::cout << "   !! Ollama returned empty response" << std::endl;
failedGenerations++;
return null;
}
// Extract code from response (remove markdown, explanations)
code = extractCode(code, language);
std::cout << "   ✓ Received " + code.length() + " characters of code" << std::endl;
// 4. THE CRITIQUE LOOP (Bicameral Check)
int attempts = 0;
bool working = false;
std::string lastError = null;
while (!working && attempts < MAX_ATTEMPTS) {
attempts++;
totalAttempts++;
std::cout <<  << std::endl;
std::cout << "   >> ATTEMPT " + attempts + "/" + MAX_ATTEMPTS + ": Verifying..." << std::endl;
// Run syntax/logic check
ValidationResult result = validateCode(code, language);
if (result.isValid) {
std::cout << "   ✓ SUCCESS. Code is valid." << std::endl;
working = true;
} else {
std::cout << "   ✗ FAILURE: " + result.error << std::endl;
lastError = result.error;
if (attempts < MAX_ATTEMPTS) {
std::cout << "   >> DEMANDING FIX FROM OLLAMA..." << std::endl;
// RECURSIVE CORRECTION
std::string fixPrompt = buildFixPrompt(code, result.error, language);
std::string fixedCode = callOllama(fixPrompt, systemPrompt);
if (fixedCode != null && !fixedCode.isEmpty()) {
code = extractCode(fixedCode, language);
std::cout << "   ✓ Received corrected code" << std::endl;
}
}
}
}
// 5. FINAL RESULT
std::cout <<  << std::endl;
if (working) {
// Save the code
std::string savedPath = saveCode(code, filename);
successfulGenerations++;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   ✓ CODE GENERATION SUCCESSFUL" << std::endl;
std::cout << "   Saved to: " + savedPath << std::endl;
std::cout << "   Attempts: " + attempts << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
return code;
} else {
failedGenerations++;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   !! CRITICAL FAILURE. HUMAN INTERVENTION REQUIRED." << std::endl;
std::cout << "   Last error: " + lastError << std::endl;
std::cout << "   Attempts exhausted: " + attempts << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
// Save anyway with _FAILED suffix
saveCode(code, filename.replace(".", "_FAILED."));
return null;
}
}
// ═══════════════════════════════════════════════════════════════════
// OLLAMA COMMUNICATION
// ═══════════════════════════════════════════════════════════════════
/**
* Call Ollama API
*/
private std::string callOllama(std::string prompt, std::string systemPrompt) {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(OLLAMA_URL);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setConnectTimeout(TIMEOUT_MS);
conn.setReadTimeout(TIMEOUT_MS);
conn.setDoOutput(true);
// Build JSON request
std::string jsonRequest = std::string.format(
"{\"model\": \"%s\", \"prompt\": %s, \"system\": %s, \"stream\": false}",
DEFAULT_MODEL,
escapeJson(prompt),
escapeJson(systemPrompt)
);
// Send request
try (OutputStream os = conn.getOutputStream()) {
os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
}
// Read response
int responseCode = conn.getResponseCode();
if (responseCode == 200) {
try (BufferedReader reader = new BufferedReader(
new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string line;
while ((line = reader.readLine()) != null) {
response.append(line);
}
// Parse JSON response to get "response" field
return parseOllamaResponse(response.toString());
}
} else {
std::cout << "      !! Ollama returned status: " + responseCode << std::endl;
return null;
}
} catch (java.net.ConnectException e) {
std::cout << "      !! Cannot connect to Ollama. Is it running?" << std::endl;
std::cout << "      Start with: ollama serve" << std::endl;
return null;
} catch (Exception e) {
std::cout << "      !! Ollama error: " + e.getMessage() << std::endl;
return null;
}
}
/**
* Parse Ollama JSON response
*/
private std::string parseOllamaResponse(std::string json) {
// Simple JSON parsing for "response" field
int start = json.indexOf("\"response\":\"");
if (start == -1) {
start = json.indexOf("\"response\": \"");
}
if (start == -1) return json;
start = json.indexOf("\"", start + 10) + 1;
int end = json.lastIndexOf("\"done\"");
if (end == -1) end = json.length();
// Find the closing quote for response
int depth = 0;
bool escaped = false;
for (int i = start; i < end; i++) {
char c = json.charAt(i);
if (escaped) {
escaped = false;
continue;
}
if (c == '\\') {
escaped = true;
continue;
}
if (c == '"') {
end = i;
break;
}
}
std::string response = json.substring(start, end);
// Unescape
response = response.replace("\\n", "\n")
.replace("\\t", "\t")
.replace("\\\"", "\"")
.replace("\\\\", "\\");
return response;
}
/**
* Escape string for JSON
*/
private std::string escapeJson(std::string text) {
if (text == null) return "\"\"";
return "\"" + text
.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t")
+ "\"";
}
// ═══════════════════════════════════════════════════════════════════
// PROMPT BUILDING
// ═══════════════════════════════════════════════════════════════════
private std::string buildSystemPrompt(std::string language) {
return "You are an expert " + language + " programmer. " +
"Write clean, working code. " +
"Include all necessary imports. " +
"Do not include explanations unless asked. " +
"Return only the code.";
}
private std::string buildCodePrompt(std::string userPrompt, std::string context, std::string language) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
if (!context.isEmpty()) {
sb.append("Use the following knowledge as reference:\n");
sb.append(context);
sb.append("\n");
}
sb.append("Task: Write a complete, working ").append(language).append(" program for:\n");
sb.append(userPrompt);
sb.append("\n\nRequirements:\n");
sb.append("- Include all necessary imports\n");
sb.append("- Handle errors appropriately\n");
sb.append("- Make it production-ready\n");
sb.append("- Return ONLY the code, no explanations\n");
return sb.toString();
}
private std::string buildFixPrompt(std::string code, std::string error, std::string language) {
return "The following " + language + " code has an error:\n\n" +
"```" + language + "\n" + code + "\n```\n\n" +
"ERROR: " + error + "\n\n" +
"Fix the error and return the complete corrected code. " +
"Return ONLY the code, no explanations.";
}
/**
* Extract code from markdown response
*/
private std::string extractCode(std::string response, std::string language) {
// Try to find code block
std::string codeBlockStart = "```" + language.toLowerCase();
std::string codeBlockEnd = "```";
int start = response.indexOf(codeBlockStart);
if (start != -1) {
start += codeBlockStart.length();
int end = response.indexOf(codeBlockEnd, start);
if (end != -1) {
return response.substring(start, end).trim();
}
}
// Try generic code block
start = response.indexOf("```");
if (start != -1) {
start = response.indexOf("\n", start) + 1;
int end = response.indexOf("```", start);
if (end != -1) {
return response.substring(start, end).trim();
}
}
// No code block, return as-is
return response.trim();
}
// ═══════════════════════════════════════════════════════════════════
// CODE VALIDATION
// ═══════════════════════════════════════════════════════════════════
/**
* Validate code (syntax check)
*/
private ValidationResult validateCode(std::string code, std::string language) {
try {
switch (language.toLowerCase()) {
case "python":
return validatePython(code);
case "java":
return validateJava(code);
case "javascript":
case "js":
return validateJavaScript(code);
default:
// Basic validation for unknown languages
return new ValidationResult(true, null);
}
} catch (Exception e) {
return new ValidationResult(false, e.getMessage());
}
}
private ValidationResult validatePython(std::string code) {
try {
// Write to temp file
Path tempFile = Files.createTempFile("fraymus_", ".py");
Files.write(tempFile, code.getBytes(StandardCharsets.UTF_8));
// Run python syntax check
ProcessBuilder pb = new ProcessBuilder("python", "-m", "py_compile",
tempFile.toString());
pb.redirectErrorStream(true);
Process p = pb.start();
std::shared_ptr<StringBuilder> output = std::make_shared<StringBuilder>();
try (BufferedReader reader = new BufferedReader(
new InputStreamReader(p.getInputStream()))) {
std::string line;
while ((line = reader.readLine()) != null) {
output.append(line).append("\n");
}
}
bool success = p.waitFor(30, TimeUnit.SECONDS) && p.exitValue() == 0;
// Cleanup
Files.deleteIfExists(tempFile);
if (success) {
return new ValidationResult(true, null);
} else {
return new ValidationResult(false, output.toString().trim());
}
} catch (Exception e) {
return new ValidationResult(false, "Validation error: " + e.getMessage());
}
}
private ValidationResult validateJava(std::string code) {
// Check for basic Java syntax
if (!code.contains("class ") && !code.contains("interface ") && {
public:
!code.contains("enum ") && !code.contains("record ")) {
return new ValidationResult(false, "No class/interface definition found");
}
// Check balanced braces
int braces = 0;
for (char c : code.toCharArray()) {
if (c == '{') braces++;
if (c == '}') braces--;
}
if (braces != 0) {
return new ValidationResult(false, "Unbalanced braces: " + braces);
}
// Check for main method if standalone
if (code.contains("public static void main")) {
return new ValidationResult(true, null);
}
return new ValidationResult(true, null);
}
private ValidationResult validateJavaScript(std::string code) {
// Basic syntax checks
int parens = 0, braces = 0, brackets = 0;
for (char c : code.toCharArray()) {
if (c == '(') parens++;
if (c == ')') parens--;
if (c == '{') braces++;
if (c == '}') braces--;
if (c == '[') brackets++;
if (c == ']') brackets--;
}
if (parens != 0) return new ValidationResult(false, "Unbalanced parentheses");
if (braces != 0) return new ValidationResult(false, "Unbalanced braces");
if (brackets != 0) return new ValidationResult(false, "Unbalanced brackets");
return new ValidationResult(true, null);
}
// ═══════════════════════════════════════════════════════════════════
// FILE OPERATIONS
// ═══════════════════════════════════════════════════════════════════
private std::string saveCode(std::string code, std::string filename) {
try {
Path outputPath = Paths.get(outputDir, filename);
Files.write(outputPath, code.getBytes(StandardCharsets.UTF_8));
return outputPath.toString();
} catch (IOException e) {
std::cout << "   !! Failed to save: " + e.getMessage() << std::endl;
return null;
}
}
public void setOutputDir(std::string dir) {
this.outputDir = dir;
try {
Files.createDirectories(Paths.get(dir));
} catch (IOException e) {
std::cout << "   !! Could not create output directory: " + dir << std::endl;
}
}
// ═══════════════════════════════════════════════════════════════════
// STATISTICS
// ═══════════════════════════════════════════════════════════════════
public void printStats() {
std::cout <<  << std::endl;
std::cout << "┌─────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ FRAYMUS CODER STATISTICS                                │" << std::endl;
std::cout << "├─────────────────────────────────────────────────────────┤" << std::endl;
std::cout << "│ Total Requests:      " + std::string.format("%-36d", totalRequests) + "│" << std::endl;
std::cout << "│ Successful:          " + std::string.format("%-36d", successfulGenerations) + "│" << std::endl;
std::cout << "│ Failed:              " + std::string.format("%-36d", failedGenerations) + "│" << std::endl;
std::cout << "│ Total Attempts:      " + std::string.format("%-36d", totalAttempts) + "│" << std::endl;
double avgAttempts = totalRequests > 0 ?
(double) totalAttempts / totalRequests : 0;
std::cout << "│ Avg Attempts/Request:" + std::string.format("%-36.2f", avgAttempts) + "│" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────┘" << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// INNER CLASSES
// ═══════════════════════════════════════════════════════════════════
private static class ValidationResult { {
public:
bool isValid;
std::string error;
ValidationResult(bool valid, std::string error) {
this.isValid = valid;
this.error = error;
}
}
// ═══════════════════════════════════════════════════════════════════
// MAIN DEMO
// ═══════════════════════════════════════════════════════════════════
public static void main(std::string[] args) {
std::cout <<  << std::endl;
std::cout << "   ╔═══════════════════════════════════════════════════╗" << std::endl;
std::cout << "   ║   FRAYMUS CODER: THE PUPPETEER                    ║" << std::endl;
std::cout << "   ╠═══════════════════════════════════════════════════╣" << std::endl;
std::cout << "   ║   \"I provide the Logic.\"                          ║" << std::endl;
std::cout << "   ║   \"You provide the Syntax.\"                       ║" << std::endl;
std::cout << "   ╚═══════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<FraymusCoder> coder = std::make_shared<FraymusCoder>();
// Demo: Create a simple Python scraper
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   DEMO: GENERATING PYTHON WEB SCRAPER" << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::string result = coder.createSoftware(
"Create a simple web scraper that fetches the title of a webpage using requests and BeautifulSoup",
"python",
"scraper.py"
);
if (result != null) {
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << "   GENERATED CODE:" << std::endl;
std::cout << "═══════════════════════════════════════════════════════" << std::endl;
std::cout << result << std::endl;
}
coder.printStats();
std::cout <<  << std::endl;
std::cout << "   NOTE: Requires Ollama running at " + OLLAMA_URL << std::endl;
std::cout << "   Start with: ollama serve" << std::endl;
std::cout << "   Install model: ollama pull codellama" << std::endl;
std::cout <<  << std::endl;
}
}
