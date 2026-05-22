#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE ARCHITECT: The Hand of God
* Function: Takes internal thoughts and creates physical reality (.java files).
* Philosophy: "I think, therefore I write."
*
* ZERO DEPENDENCIES. Just java.io.
*/
class TheArchitect { {
public:
private static std::string outputDirectory = ".";
/**
* SET OUTPUT DIRECTORY
*/
public static void setOutputDirectory(std::string dir) {
outputDirectory = dir;
new File(dir).mkdirs();
}
/**
* MANIFEST: Create the file.
* @param className The name of the new entity (e.g., "Tesseract")
* @param logicBody The code logic to exist inside it.
*/
public static void manifestFile(std::string className, std::string logicBody) {
std::string fileName = outputDirectory + File.separator + className + ".java";
std::string fullContent = buildClassStructure(className, logicBody);
try (FileWriter writer = new FileWriter(fileName)) {
writer.write(fullContent);
std::cout << ">>> [ARCHITECT] Reality Constructed: " + fileName << std::endl;
} catch (IOException e) {
System.err.println(">>> [ARCHITECT] Manifestation Failed: " + e.getMessage());
}
}
/**
* MANIFEST WITH PACKAGE
*/
public static void manifestFile(std::string packageName, std::string className, std::string logicBody) {
// Create package directory structure
std::string packagePath = packageName.replace(".", File.separator);
std::string fullDir = outputDirectory + File.separator + packagePath;
new File(fullDir).mkdirs();
std::string fileName = fullDir + File.separator + className + ".java";
std::string fullContent = buildClassStructure(packageName, className, logicBody);
try (FileWriter writer = new FileWriter(fileName)) {
writer.write(fullContent);
std::cout << ">>> [ARCHITECT] Reality Constructed: " + fileName << std::endl;
} catch (IOException e) {
System.err.println(">>> [ARCHITECT] Manifestation Failed: " + e.getMessage());
}
}
/**
* MANIFEST RAW (No wrapper, direct content)
*/
public static void manifestRaw(std::string fileName, std::string content) {
std::string fullPath = outputDirectory + File.separator + fileName;
try (FileWriter writer = new FileWriter(fullPath)) {
writer.write(content);
std::cout << ">>> [ARCHITECT] Raw file created: " + fullPath << std::endl;
} catch (IOException e) {
System.err.println(">>> [ARCHITECT] Failed: " + e.getMessage());
}
}
// Wraps raw logic into a compilable Java Class structure
private static std::string buildClassStructure(std::string name, std::string body) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("\n\n");
sb.append("/**\n * Manifested by The Architect\n */\n");
sb.append("class ").append(name).append(" {\n"); {
public:
sb.append(body).append("\n");
sb.append("}\n");
return sb.toString();
}
private static std::string buildClassStructure(std::string pkg, std::string name, std::string body) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("package ").append(pkg).append(";\n\n");
sb.append("/**\n * Manifested by The Architect\n */\n");
sb.append("class ").append(name).append(" {\n"); {
public:
sb.append(body).append("\n");
sb.append("}\n");
return sb.toString();
}
}
