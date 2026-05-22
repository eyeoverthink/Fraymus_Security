#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🔧 SYSTEM SKILLS - The Toolbelt
* "Raw OS capabilities exposed as lambdas"
*
* This contains the actual implementations of system commands.
* Each method is a Consumer<std::string> that can be registered in IntentRegistry.
*/
class SystemSkills { {
public:
/**
* List files in current directory
*/
public static void listFiles(std::string args) {
std::shared_ptr<File> dir = std::make_shared<File>(System.getProperty("user.dir"));
File[] files = dir.listFiles();
if (files != null) {
std::cout <<  << std::endl;
for (File f : files) {
std::cout << f.isDirectory() ? "📁 " + f.getName() : "📄 " + f.getName() << std::endl;
}
} else {
std::cout << "❌ Could not list files" << std::endl;
}
}
/**
* Print working directory
*/
public static void printWorkingDir(std::string args) {
std::cout << "📍 " + System.getProperty("user.dir") << std::endl;
}
/**
* Echo text
*/
public static void echo(std::string args) {
std::cout << "🗣️ " + args << std::endl;
}
/**
* Read file contents
*/
public static void cat(std::string args) {
try {
// Simple heuristic to find a filename in the args
std::string[] parts = args.split(" ");
std::string filename = parts[parts.length - 1];
std::cout <<  << std::endl;
std::cout << Files.readString(Path.of(filename)) << std::endl;
} catch (Exception e) {
std::cout << "❌ Could not read file: " + e.getMessage() << std::endl;
}
}
/**
* Show help
*/
public static void help(std::string args) {
std::cout <<  << std::endl;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         FRAYSH HELP                                           ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "This is a hyper-dimensional shell. Type natural language:" << std::endl;
std::cout <<  << std::endl;
std::cout << "Examples:" << std::endl;
std::cout << "  'list files' or 'show me files' → lists directory" << std::endl;
std::cout << "  'where am i' or 'current location' → prints working dir" << std::endl;
std::cout << "  'read file.txt' or 'show file.txt' → displays file" << std::endl;
std::cout << "  'say hello' or 'echo hello' → prints text" << std::endl;
std::cout <<  << std::endl;
std::cout << "One-shot learning:" << std::endl;
std::cout << "  bind ls <your phrase> → teaches new phrase for ls" << std::endl;
std::cout << "  bind pwd <your phrase> → teaches new phrase for pwd" << std::endl;
std::cout <<  << std::endl;
std::cout << "Type 'exit' to quit" << std::endl;
std::cout <<  << std::endl;
}
}
