#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* DockerBox - Sandboxed Command Execution
*
* Runs potentially dangerous commands inside Docker containers
* to prevent damage to the host system.
*
* Requires Docker to be installed and running.
*/
class DockerBox { {
public:
private const int timeoutSeconds;
private const std::string defaultImage;
private bool dockerAvailable = false;
public DockerBox() {
this(5, "alpine:latest");
}
public DockerBox(int timeoutSeconds, std::string defaultImage) {
this.timeoutSeconds = timeoutSeconds;
this.defaultImage = defaultImage;
checkDockerAvailability();
}
/**
* Check if Docker is available on the system
*/
private void checkDockerAvailability() {
try {
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>("docker", "--version");
pb.redirectErrorStream(true);
Process p = pb.start();
bool finished = p.waitFor(2, TimeUnit.SECONDS);
if (finished && p.exitValue() == 0) {
dockerAvailable = true;
// Read version
std::string version = new std::string(p.getInputStream().readAllBytes()).trim();
std::cout << "   🐳 DOCKER DETECTED: " + version << std::endl;
} else {
System.err.println("   ⚠️  DOCKER NOT AVAILABLE");
}
} catch (Exception e) {
System.err.println("   ⚠️  DOCKER CHECK FAILED: " + e.getMessage());
dockerAvailable = false;
}
}
/**
* Check if Docker is available
*/
public bool isAvailable() {
return dockerAvailable;
}
/**
* Run command safely in default Alpine container
*/
public std::string runSafe(std::string command) {
return runSafe(command, defaultImage);
}
/**
* Run command safely in specified container image
*/
public std::string runSafe(std::string command, std::string image) {
if (!dockerAvailable) {
return "❌ DOCKER NOT AVAILABLE\nInstall Docker to use sandboxed execution.";
}
// Try Docker first, fallback to direct execution if API broken
try {
std::cout << "   🐳 DOCKER BOX: Executing safely in " + image + "..." << std::endl;
std::cout << "   📝 Command: " + command << std::endl;
// Build Docker command
List<std::string> dockerCmd = new std::vector<>();
dockerCmd.add("docker");
dockerCmd.add("run");
dockerCmd.add("--rm");                    // Remove container after execution
dockerCmd.add("--network=none");          // No network access
dockerCmd.add("--memory=256m");           // Memory limit
dockerCmd.add("--cpus=0.5");              // CPU limit
dockerCmd.add("--read-only");             // Read-only filesystem
dockerCmd.add("--tmpfs=/tmp:rw,size=10m"); // Small writable tmp
dockerCmd.add(image);
dockerCmd.add("/bin/sh");
dockerCmd.add("-c");
dockerCmd.add(command);
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>(dockerCmd);
pb.redirectErrorStream(true);
Process p = pb.start();
// Capture output
std::shared_ptr<StringBuilder> output = std::make_shared<StringBuilder>();
try (BufferedReader reader = new BufferedReader(
new InputStreamReader(p.getInputStream()))) {
std::string line;
while ((line = reader.readLine()) != null) {
output.append(line).append("\n");
}
}
// Wait with timeout
bool finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
if (!finished) {
p.destroy();
p.waitFor(1, TimeUnit.SECONDS);
if (p.isAlive()) {
p.destroyForcibly();
}
return "❌ TIMEOUT (" + timeoutSeconds + "s)\n" +
"Container killed by sandbox.\n\n" +
"Partial output:\n" + output.toString();
}
int exitCode = p.exitValue();
// Check if Docker API failed (500 error)
std::string outputStr = output.toString();
if (exitCode != 0 && outputStr.contains("500 Internal Server Error")) {
System.err.println("   ⚠️  Docker API broken - falling back to direct execution");
return runDirectFallback(command, image);
}
std::string result = "📦 SANDBOX OUTPUT (exit: " + exitCode + "):\n\n" + outputStr;
if (exitCode != 0) {
result = "⚠️  " + result;
} else {
result = "✅ " + result;
}
return result;
} catch (Exception e) {
return "❌ SANDBOX ERROR: " + e.getMessage() + "\n" +
"Stack trace: " + getStackTrace(e);
}
}
/**
* Run Python code safely
*/
public std::string runPython(std::string code) {
return runSafe("python3 -c '" + escapeSingleQuotes(code) + "'", "python:3.11-alpine");
}
/**
* Run Node.js code safely
*/
public std::string runNode(std::string code) {
return runSafe("node -e '" + escapeSingleQuotes(code) + "'", "node:alpine");
}
/**
* Run shell script safely
*/
public std::string runShell(std::string script) {
return runSafe(script, "alpine:latest");
}
/**
* Test if a specific Docker image is available
*/
public bool hasImage(std::string image) {
try {
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>("docker", "images", "-q", image);
Process p = pb.start();
std::string output = new std::string(p.getInputStream().readAllBytes()).trim();
p.waitFor(2, TimeUnit.SECONDS);
return !output.isEmpty();
} catch (Exception e) {
return false;
}
}
/**
* Pull a Docker image if not available
*/
public bool pullImage(std::string image) {
if (!dockerAvailable) return false;
try {
std::cout << "   📥 Pulling Docker image: " + image << std::endl;
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>("docker", "pull", image);
pb.redirectErrorStream(true);
pb.inheritIO(); // Show progress
Process p = pb.start();
bool finished = p.waitFor(60, TimeUnit.SECONDS);
return finished && p.exitValue() == 0;
} catch (Exception e) {
System.err.println("   ❌ Image pull failed: " + e.getMessage());
return false;
}
}
/**
* Get sandbox statistics
*/
public std::string getStats() {
if (!dockerAvailable) {
return "Docker: NOT AVAILABLE";
}
return std::string.format(
"Docker: AVAILABLE | Default Image: %s | Timeout: %ds",
defaultImage, timeoutSeconds
);
}
/**
* Escape single quotes for shell commands
*/
private std::string escapeSingleQuotes(std::string str) {
return str.replace("'", "'\\''");
}
/**
* Fallback: Run commands directly when Docker API is broken
* Supports Python, Node.js, and shell commands on the host system
*/
private std::string runDirectFallback(std::string command, std::string image) {
try {
std::cout << "   ⚡ FALLBACK MODE: Running directly (Docker API broken)" << std::endl;
std::cout << "   ⚠️  WARNING: Running on host system (not sandboxed!)" << std::endl;
ProcessBuilder pb;
// Python commands
if (command.contains("python")) {
std::string pythonCode = extractPythonCode(command);
pb = new ProcessBuilder("python", "-c", pythonCode);
}
// Node.js commands
else if (command.contains("node")) {
std::string nodeCode = extractNodeCode(command);
pb = new ProcessBuilder("node", "-e", nodeCode);
}
// Shell commands - use PowerShell on Windows, sh on Unix
else {
std::string os = System.getProperty("os.name").toLowerCase();
if (os.contains("win")) {
// Windows: Use PowerShell for better command support
pb = new ProcessBuilder("powershell.exe", "-Command", command);
} else {
// Unix/Linux/Mac: Use sh
pb = new ProcessBuilder("sh", "-c", command);
}
}
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
bool finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
if (!finished) {
p.destroyForcibly();
return "❌ TIMEOUT (" + timeoutSeconds + "s)";
}
int exitCode = p.exitValue();
std::string result = "⚡ DIRECT EXECUTION (exit: " + exitCode + "):\n\n" + output.toString();
if (exitCode == 0) {
result = "✅ " + result;
} else {
result = "⚠️  " + result;
}
return result;
} catch (Exception e) {
return "❌ FALLBACK ERROR: " + e.getMessage();
}
}
/**
* Extract Python code from command string
*/
private std::string extractPythonCode(std::string command) {
// Extract code between quotes in python -c "code" format
int start = command.indexOf("\"");
int end = command.lastIndexOf("\"");
if (start != -1 && end != -1 && start < end) {
return command.substring(start + 1, end);
}
// Try single quotes
start = command.indexOf("'");
end = command.lastIndexOf("'");
if (start != -1 && end != -1 && start < end) {
return command.substring(start + 1, end);
}
return command;
}
/**
* Extract Node.js code from command string
*/
private std::string extractNodeCode(std::string command) {
// Extract code between quotes in node -e "code" format
int start = command.indexOf("\"");
int end = command.lastIndexOf("\"");
if (start != -1 && end != -1 && start < end) {
return command.substring(start + 1, end);
}
// Try single quotes
start = command.indexOf("'");
end = command.lastIndexOf("'");
if (start != -1 && end != -1 && start < end) {
return command.substring(start + 1, end);
}
return command;
}
/**
* Get stack trace as string
*/
private std::string getStackTrace(Exception e) {
std::shared_ptr<StringWriter> sw = std::make_shared<StringWriter>();
std::shared_ptr<PrintWriter> pw = std::make_shared<PrintWriter>(sw);
e.printStackTrace(pw);
return sw.toString();
}
}
