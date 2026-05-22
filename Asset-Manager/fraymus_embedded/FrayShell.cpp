#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🐚 FRAYSH - The Hyper-Dimensional Shell
* "Intent-based command line interface"
*
* This replaces traditional shells (bash/zsh/powershell) with an associative interface.
*
* Traditional Shell:
* - Exact syntax required: "ls -la"
* - Miss one character → command fails
* - No learning or adaptation
*
* FrayShell:
* - Natural language: "show me files" or "list stuff"
* - Vector resonance matching
* - One-shot learning: teach new phrases instantly
*
* Example:
* - You type: "blast it"
* - You bind it to: git push --force
* - Later you type: "launch it"
* - System: Vectors resonate → executes git push
*
* This is an Associative Command Line.
*/
class FrayShell { {
public:
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🐚 FRAY-SH [HYPER-DIMENSIONAL SHELL]                  ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Type 'help' for commands. Type 'exit' to quit." << std::endl;
std::cout <<  << std::endl;
// 1. INIT BRAIN
std::shared_ptr<HyperFormer> brain = std::make_shared<HyperFormer>();
std::shared_ptr<IntentRegistry> registry = std::make_shared<IntentRegistry>();
// 2. BOOTSTRAP SKILLS (Seed the memory)
// We embed specific phrases to specific actions
std::cout << "Bootstrapping muscle memory..." << std::endl;
registry.register("ls", brain.embed("list files"), SystemSkills::listFiles);
registry.register("ls_alt1", brain.embed("show files"), SystemSkills::listFiles);
registry.register("ls_alt2", brain.embed("show me files"), SystemSkills::listFiles);
registry.register("ls_alt3", brain.embed("what files"), SystemSkills::listFiles);
registry.register("pwd", brain.embed("where am i"), SystemSkills::printWorkingDir);
registry.register("pwd_alt1", brain.embed("current location"), SystemSkills::printWorkingDir);
registry.register("pwd_alt2", brain.embed("current directory"), SystemSkills::printWorkingDir);
registry.register("cat", brain.embed("read file"), SystemSkills::cat);
registry.register("cat_alt1", brain.embed("show file"), SystemSkills::cat);
registry.register("cat_alt2", brain.embed("display file"), SystemSkills::cat);
registry.register("echo", brain.embed("say this"), SystemSkills::echo);
registry.register("echo_alt1", brain.embed("print this"), SystemSkills::echo);
registry.register("help", brain.embed("help me"), SystemSkills::help);
registry.register("help_alt1", brain.embed("show help"), SystemSkills::help);
std::cout << "✓ Loaded " + registry.size() + " intent mappings" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<Scanner> scanner = std::make_shared<Scanner>(System.in);
while (true) {
std::cout << "ff> ";
std::string input = scanner.nextLine().trim();
if (input.isEmpty()) continue;
if (input.equalsIgnoreCase("exit")) {
std::cout <<  << std::endl;
std::cout << "👋 Goodbye!" << std::endl;
break;
}
// 3. ENCODE INTENT
// We break the input into words and bind them into a single Thought Vector
std::string[] words = input.split("\\s+");
std::shared_ptr<HyperVector> thought = std::make_shared<HyperVector>();
for (std::string w : words) {
// Bundle every word's vector to create a "Sentence Concept"
thought = thought.bundle(brain.embed(w));
}
// 4. RESOLVE ACTION
// We set a lower threshold (0.51) to allow for "fuzzy" matching
var action = registry.resolve(thought, 0.51);
if (action != null) {
action.accept(input);
} else {
std::cout << "❓ Intent not recognized. Teach me?" << std::endl;
std::cout << "   (Type 'bind [ls/pwd/cat] <phrase>' to associate)" << std::endl;
std::cout <<  << std::endl;
// 5. ONE-SHOT LEARNING
if (input.startsWith("bind ")) {
std::string[] parts = input.split(" ", 3);
if (parts.length == 3) {
std::string cmd = parts[1];
std::string phrase = parts[2];
// Create vector for new phrase
std::shared_ptr<HyperVector> newVec = std::make_shared<HyperVector>();
for (std::string w : phrase.split("\\s+")) {
newVec = newVec.bundle(brain.embed(w));
}
// Map it to existing logic
std::string learnedName = "learned_" + cmd + "_" + System.currentTimeMillis();
if (cmd.equals("ls")) {
registry.register(learnedName, newVec, SystemSkills::listFiles);
} else if (cmd.equals("pwd")) {
registry.register(learnedName, newVec, SystemSkills::printWorkingDir);
} else if (cmd.equals("cat")) {
registry.register(learnedName, newVec, SystemSkills::cat);
} else if (cmd.equals("echo")) {
registry.register(learnedName, newVec, SystemSkills::echo);
} else {
std::cout << "❌ Unknown command: " + cmd << std::endl;
continue;
}
std::cout << "✨ LEARNED: '" + phrase + "' now triggers " + cmd << std::endl;
std::cout <<  << std::endl;
} else {
std::cout << "❌ Usage: bind <command> <your phrase>" << std::endl;
std::cout <<  << std::endl;
}
}
}
}
scanner.close();
}
}
