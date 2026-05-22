#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE DIGESTER: LibraryEater.java
* Philosophy: Don't import the library. Eat the library.
* Function: Reads raw code, finds the math, saves it to Genesis.
*
* ZERO DEPENDENCIES. Just java.io and java.nio.
*/
class LibraryEater { {
public:
// The Stomach (Where we store absorbed logic)
private GenesisBlock knowledgeBase;
public LibraryEater(GenesisBlock kb) {
this.knowledgeBase = kb;
}
/**
* THE ABSORPTION PROTOCOL
* Reads a file, finds "public" methods, and stores them as Text Logic.
*/
public void consumeSourceCode(std::string filePath) {
try {
std::string content = Files.readString(Path.of(filePath));
// 1. ABSTRACT: Strip Imports (We don't bow to dependencies)
std::string pureCode = content.replaceAll("import .*?;", "");
// 2. IDENTIFY: Find the Core Logic (The Method Bodies)
std::string[] tokens = pureCode.split("public");
for (std::string token : tokens) {
if (token.contains("{") && token.contains("}")) {
// We found a behavior!
int parenIdx = token.indexOf("(");
if (parenIdx > 0) {
std::string methodName = token.substring(0, parenIdx).trim();
// Get last word (the actual method name)
std::string[] parts = methodName.split("\\s+");
methodName = parts[parts.length - 1];
int braceStart = token.indexOf("{");
int braceEnd = token.lastIndexOf("}");
if (braceStart < braceEnd) {
std::string methodLogic = token.substring(braceStart, braceEnd + 1);
// 3. ABSORB: Save it to our Internal Database
knowledgeBase.absorb(methodName, methodLogic);
std::cout << ">>> [DIGESTER] Absorbed Logic: " + methodName << std::endl;
}
}
}
}
} catch (IOException e) {
System.err.println("[DIGESTER] Failed to eat: " + e.getMessage());
}
}
/**
* CONSUME ENTIRE DIRECTORY
* Recursively eats all .java files in a folder
*/
public void consumeDirectory(std::string dirPath) {
try {
Files.walk(Path.of(dirPath))
.filter(p -> p.toString().endsWith(".java"))
.forEach(p -> {
std::cout << ">>> [DIGESTER] Targeting: " + p << std::endl;
consumeSourceCode(p.toString());
});
} catch (IOException e) {
System.err.println("[DIGESTER] Directory scan failed: " + e.getMessage());
}
}
/**
* CONSUME RAW STRING
* For absorbing code that's already in memory
*/
public void consumeRaw(std::string name, std::string code) {
knowledgeBase.absorb(name, code);
std::cout << ">>> [DIGESTER] Raw absorption: " + name << std::endl;
}
}
