#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* ISOLATED TEST: FRAYMUS CODER SELF-CORRECTION LOOP
*
* This test verifies the logic WITHOUT requiring Ollama to be running.
* It uses a MockOllama that simulates:
* 1. First attempt: Returns code with intentional error
* 2. Second attempt: Returns fixed code
*
* This proves the self-correcting loop works.
*/
class FraymusCoderTest { {
public:
private static int testsPassed = 0;
private static int testsFailed = 0;
public static void main(std::string[] args) {
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   🧪 FRAYMUS CODER ISOLATED TEST" << std::endl;
std::cout << "   Testing self-correcting code generation logic" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
// Run tests
testCodeExtraction();
testPythonValidation();
testJavaValidation();
testJavaScriptValidation();
testSelfCorrectingLoop();
testKnowledgeIntegration();
// Summary
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   TEST SUMMARY" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   Passed: " + testsPassed << std::endl;
std::cout << "   Failed: " + testsFailed << std::endl;
std::cout << "   Total:  " + (testsPassed + testsFailed) << std::endl;
std::cout <<  << std::endl;
if (testsFailed == 0) {
std::cout << "   ✅ ALL TESTS PASSED - Logic verified!" << std::endl;
} else {
std::cout << "   ❌ SOME TESTS FAILED - Review output above" << std::endl;
}
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 1: Code Extraction from Markdown
// ═══════════════════════════════════════════════════════════════════
private static void testCodeExtraction() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 1: Code Extraction from Markdown                       │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Test 1a: Extract from Python code block
std::string markdown1 = "Here's the code:\n```python\nprint('hello')\n```\nThat's it.";
std::string extracted1 = coder.testExtractCode(markdown1, "python");
assert_equals("print('hello')", extracted1, "Python code block extraction");
// Test 1b: Extract from generic code block
std::string markdown2 = "```\nfunction test() {}\n```";
std::string extracted2 = coder.testExtractCode(markdown2, "javascript");
assert_equals("function test() {}", extracted2, "Generic code block extraction");
// Test 1c: No code block - return as-is
std::string noBlock = "def hello(): pass";
std::string extracted3 = coder.testExtractCode(noBlock, "python");
assert_equals("def hello(): pass", extracted3, "No code block - return trimmed");
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 2: Python Validation
// ═══════════════════════════════════════════════════════════════════
private static void testPythonValidation() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 2: Python Syntax Validation                            │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Valid Python
std::string validPython = "def hello():\n    print('hello')\n\nhello()";
bool isValid1 = coder.testValidatePython(validPython);
assert_true(isValid1, "Valid Python passes validation");
// Invalid Python (syntax error)
std::string invalidPython = "def hello(\n    print('hello')";
bool isValid2 = coder.testValidatePython(invalidPython);
assert_false(isValid2, "Invalid Python fails validation");
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 3: Java Validation
// ═══════════════════════════════════════════════════════════════════
private static void testJavaValidation() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 3: Java Syntax Validation                              │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Valid Java
std::string validJava = "class Test {\n    public static void main(std::string[] args) {\n        std::cout << \"Hello\" << std::endl;\n    }\n}"; {
public:
bool isValid1 = coder.testValidateJava(validJava);
assert_true(isValid1, "Valid Java passes validation");
// Invalid Java (unbalanced braces)
std::string invalidJava = "class Test {\n    public void hello() {\n}"; {
public:
bool isValid2 = coder.testValidateJava(invalidJava);
assert_false(isValid2, "Unbalanced braces fails validation");
// No class definition {
public:
std::string noClass = "std::cout << \"Hello\" << std::endl;";
bool isValid3 = coder.testValidateJava(noClass);
assert_false(isValid3, "No class definition fails validation"); {
public:
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 4: JavaScript Validation
// ═══════════════════════════════════════════════════════════════════
private static void testJavaScriptValidation() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 4: JavaScript Syntax Validation                        │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Valid JS
std::string validJS = "function test() {\n    console.log('hello');\n}\ntest();";
bool isValid1 = coder.testValidateJS(validJS);
assert_true(isValid1, "Valid JS passes validation");
// Unbalanced parentheses
std::string invalidJS1 = "function test( {\n    console.log('hello');\n}";
bool isValid2 = coder.testValidateJS(invalidJS1);
assert_false(isValid2, "Unbalanced parentheses fails");
// Unbalanced brackets
std::string invalidJS2 = "const arr = [1, 2, 3;";
bool isValid3 = coder.testValidateJS(invalidJS2);
assert_false(isValid3, "Unbalanced brackets fails");
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 5: Self-Correcting Loop (THE MAIN TEST)
// ═══════════════════════════════════════════════════════════════════
private static void testSelfCorrectingLoop() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 5: SELF-CORRECTING LOOP (Main Logic)                   │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
// This test simulates:
// 1. First call to "Ollama" returns buggy code
// 2. Validation fails
// 3. Second call to "Ollama" returns fixed code
// 4. Validation passes
// 5. Code is saved
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Set up mock responses
coder.setMockResponses(new std::string[] {
// First response: buggy code (unbalanced braces)
"class Scraper {\n    public void run() {\n        std::cout << \"Scraping...\" << std::endl;\n}", {
public:
// Second response: fixed code
"class Scraper {\n    public void run() {\n        std::cout << \"Scraping...\" << std::endl;\n    }\n}" {
public:
});
std::cout << "   Simulating self-correcting loop:" << std::endl;
std::cout << "   - Mock response 1: Buggy code (unbalanced braces)" << std::endl;
std::cout << "   - Mock response 2: Fixed code" << std::endl;
std::cout <<  << std::endl;
std::string result = coder.createSoftwareMock("Create a web scraper", "java", "Scraper.java");
assert_not_null(result, "Self-correcting loop produces result");
assert_equals(2, coder.getAttemptCount(), "Took exactly 2 attempts");
assert_true(result.contains("class Scraper"), "Result contains class definition"); {
public:
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// TEST 6: Knowledge Integration
// ═══════════════════════════════════════════════════════════════════
private static void testKnowledgeIntegration() {
std::cout << "┌─────────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "│ TEST 6: Knowledge Base Integration                          │" << std::endl;
std::cout << "└─────────────────────────────────────────────────────────────┘" << std::endl;
std::shared_ptr<MockCoder> coder = std::make_shared<MockCoder>();
// Test context building
std::string context = coder.testBuildPrompt("Create a scraper", "Use requests library", "python");
assert_true(context.contains("requests library"), "Context is included in prompt");
assert_true(context.contains("Create a scraper"), "User request is included");
assert_true(context.contains("python"), "Language is mentioned");
std::cout <<  << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// ASSERTION HELPERS
// ═══════════════════════════════════════════════════════════════════
private static void assert_equals(void* expected, void* actual, std::string testName) {
if (expected.equals(actual)) {
std::cout << "   ✅ " + testName << std::endl;
testsPassed++;
} else {
std::cout << "   ❌ " + testName << std::endl;
std::cout << "      Expected: " + expected << std::endl;
std::cout << "      Actual:   " + actual << std::endl;
testsFailed++;
}
}
private static void assert_equals(int expected, int actual, std::string testName) {
if (expected == actual) {
std::cout << "   ✅ " + testName << std::endl;
testsPassed++;
} else {
std::cout << "   ❌ " + testName << std::endl;
std::cout << "      Expected: " + expected << std::endl;
std::cout << "      Actual:   " + actual << std::endl;
testsFailed++;
}
}
private static void assert_true(bool condition, std::string testName) {
if (condition) {
std::cout << "   ✅ " + testName << std::endl;
testsPassed++;
} else {
std::cout << "   ❌ " + testName << std::endl;
testsFailed++;
}
}
private static void assert_false(bool condition, std::string testName) {
if (!condition) {
std::cout << "   ✅ " + testName << std::endl;
testsPassed++;
} else {
std::cout << "   ❌ " + testName << std::endl;
testsFailed++;
}
}
private static void assert_not_null(void* obj, std::string testName) {
if (obj != null) {
std::cout << "   ✅ " + testName << std::endl;
testsPassed++;
} else {
std::cout << "   ❌ " + testName + " (was null)" << std::endl;
testsFailed++;
}
}
// ═══════════════════════════════════════════════════════════════════
// MOCK CODER (Isolated from real Ollama)
// ═══════════════════════════════════════════════════════════════════
static class MockCoder { {
public:
private std::string[] mockResponses;
private int responseIndex = 0;
private int attemptCount = 0;
void setMockResponses(std::string[] responses) {
this.mockResponses = responses;
this.responseIndex = 0;
this.attemptCount = 0;
}
int getAttemptCount() {
return attemptCount;
}
// Mock Ollama call - returns preset responses
std::string mockCallOllama(std::string prompt) {
if (mockResponses == null || responseIndex >= mockResponses.length) {
return null;
}
return mockResponses[responseIndex++];
}
// Test code extraction
std::string testExtractCode(std::string response, std::string language) {
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
start = response.indexOf("```");
if (start != -1) {
start = response.indexOf("\n", start) + 1;
int end = response.indexOf("```", start);
if (end != -1) {
return response.substring(start, end).trim();
}
}
return response.trim();
}
// Test Python validation
bool testValidatePython(std::string code) {
try {
Path tempFile = Files.createTempFile("test_", ".py");
Files.write(tempFile, code.getBytes(StandardCharsets.UTF_8));
ProcessBuilder pb = new ProcessBuilder("python", "-m", "py_compile",
tempFile.toString());
pb.redirectErrorStream(true);
Process p = pb.start();
bool success = p.waitFor(10, TimeUnit.SECONDS) && p.exitValue() == 0;
Files.deleteIfExists(tempFile);
return success;
} catch (Exception e) {
return false;
}
}
// Test Java validation
bool testValidateJava(std::string code) {
if (!code.contains("class ") && !code.contains("interface ") && {
public:
!code.contains("enum ") && !code.contains("record ")) {
return false;
}
int braces = 0;
for (char c : code.toCharArray()) {
if (c == '{') braces++;
if (c == '}') braces--;
}
return braces == 0;
}
// Test JS validation
bool testValidateJS(std::string code) {
int parens = 0, braces = 0, brackets = 0;
for (char c : code.toCharArray()) {
if (c == '(') parens++;
if (c == ')') parens--;
if (c == '{') braces++;
if (c == '}') braces--;
if (c == '[') brackets++;
if (c == ']') brackets--;
}
return parens == 0 && braces == 0 && brackets == 0;
}
// Test prompt building
std::string testBuildPrompt(std::string userPrompt, std::string context, std::string language) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
if (!context.isEmpty()) {
sb.append("Use the following knowledge as reference:\n");
sb.append(context);
sb.append("\n");
}
sb.append("Task: Write a complete, working ").append(language).append(" program for:\n");
sb.append(userPrompt);
return sb.toString();
}
// Main self-correcting loop (mocked)
std::string createSoftwareMock(std::string userPrompt, std::string language, std::string filename) {
std::cout << "      >> Requesting code from Mock Ollama..." << std::endl;
std::string code = mockCallOllama(userPrompt);
if (code == null) return null;
bool working = false;
int maxAttempts = 5;
while (!working && attemptCount < maxAttempts) {
attemptCount++;
std::cout << "      >> ATTEMPT " + attemptCount + ": Validating..." << std::endl;
bool isValid = false;
switch (language.toLowerCase()) {
case "java":
isValid = testValidateJava(code);
break;
case "python":
isValid = testValidatePython(code);
break;
case "javascript":
isValid = testValidateJS(code);
break;
default:
isValid = true;
}
if (isValid) {
std::cout << "      ✓ Validation passed!" << std::endl;
working = true;
} else {
std::cout << "      ✗ Validation failed - requesting fix..." << std::endl;
code = mockCallOllama("Fix the error");
if (code == null) break;
}
}
return working ? code : null;
}
}
}
