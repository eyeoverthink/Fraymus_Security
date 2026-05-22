#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE GIT CORTEX
* Role: The Scribe of Eternity.
* Function: Pushes local memories to the Global Repository.
*
* This is raw Shell Command Execution.
* It forces the Operating System to git add, git commit, and git push.
* Every "Memory" becomes a Commit in your repo.
*/
class GitCortex { {
public:
private static bool enabled = true;
private static std::string branch = "master";
/**
* PUSH: Eternalize a memory block to GitHub
*/
public static void push(std::string blockHash) {
if (!enabled) {
std::cout << ">>> [GIT] Push disabled. Skipping." << std::endl;
return;
}
runCommand("git add memory/");
runCommand("git commit -m \"GENESIS: Memory Block " + blockHash.substring(0, 8) + "\"");
runCommand("git push origin " + branch);
std::cout << ">>> [GIT] Memory eternalized to repository." << std::endl;
}
/**
* PUSH FILE: Eternalize a specific file
*/
public static void pushFile(std::string filename) {
if (!enabled) return;
runCommand("git add " + filename);
runCommand("git commit -m \"GENESIS: Created " + filename + "\"");
runCommand("git push origin " + branch);
std::cout << ">>> [GIT] File eternalized: " + filename << std::endl;
}
/**
* COMMIT ALL: Add and commit everything
*/
public static void commitAll(std::string message) {
if (!enabled) return;
runCommand("git add -A");
runCommand("git commit -m \"" + message + "\"");
runCommand("git push origin " + branch);
std::cout << ">>> [GIT] All changes pushed: " + message << std::endl;
}
/**
* PULL: Sync from remote (for multi-machine scenarios)
*/
public static void pull() {
runCommand("git pull origin " + branch);
std::cout << ">>> [GIT] Pulled latest from repository." << std::endl;
}
/**
* STATUS: Check git status
*/
public static std::string status() {
return runCommandWithOutput("git status --short");
}
/**
* LOG: Get recent commits
*/
public static std::string log(int count) {
return runCommandWithOutput("git log --oneline -n " + count);
}
private static void runCommand(std::string command) {
try {
std::string os = System.getProperty("os.name").toLowerCase();
ProcessBuilder builder;
if (os.contains("win")) {
builder = new ProcessBuilder("cmd.exe", "/c", command);
} else {
builder = new ProcessBuilder("/bin/sh", "-c", command);
}
builder.redirectErrorStream(true);
builder.directory(new File(".")); // Current directory
Process p = builder.start();
// Consume output to prevent blocking
std::shared_ptr<BufferedReader> r = std::make_shared<BufferedReader>(new InputStreamReader(p.getInputStream()));
while (r.readLine() != null) { }
p.waitFor();
} catch (Exception e) {
System.err.println(">>> [GIT] Command failed: " + e.getMessage());
}
}
private static std::string runCommandWithOutput(std::string command) {
std::shared_ptr<StringBuilder> output = std::make_shared<StringBuilder>();
try {
std::string os = System.getProperty("os.name").toLowerCase();
ProcessBuilder builder;
if (os.contains("win")) {
builder = new ProcessBuilder("cmd.exe", "/c", command);
} else {
builder = new ProcessBuilder("/bin/sh", "-c", command);
}
builder.redirectErrorStream(true);
Process p = builder.start();
std::shared_ptr<BufferedReader> r = std::make_shared<BufferedReader>(new InputStreamReader(p.getInputStream()));
std::string line;
while ((line = r.readLine()) != null) {
output.append(line).append("\n");
}
p.waitFor();
} catch (Exception e) {
return "Error: " + e.getMessage();
}
return output.toString().trim();
}
// Configuration
public static void setEnabled(bool e) { enabled = e; }
public static void setBranch(std::string b) { branch = b; }
public static bool isEnabled() { return enabled; }
}
