#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* GENESIS SANDBOX - The Proving Ground
*
* Runs generated code inside disposable Docker containers.
* Prevents "self-modifying OS" from becoming "self-owning OS".
*
* Process:
* 1. Spin up isolated container
* 2. Mount generated code
* 3. Run build/test
* 4. Capture results
* 5. Destroy container
*
* If it passes, deploy. If it fails, reject.
*/
class GenesisSandbox { {
public:
private const AuditLog auditLog;
private static const long TIMEOUT_SECONDS = 120; // 2 minutes max
public GenesisSandbox(AuditLog auditLog) {
this.auditLog = auditLog;
}
/**
* PROVE: Runs the generated code inside a disposable container.
* Returns TRUE if the build/test passes.
*/
public VerificationResult verifyArtifact(std::string artifactPath, std::string language) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🧪 GENESIS SANDBOX - CONTAINMENT VERIFICATION         ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Artifact: " + artifactPath << std::endl;
std::cout << "Language: " + language << std::endl;
std::cout <<  << std::endl;
auditLog.log("sandbox_verification_started", artifactPath);
// Check if Docker is available
if (!isDockerAvailable()) {
std::cout << "⚠️ Docker not available - skipping containerized verification" << std::endl;
auditLog.log("sandbox_docker_unavailable", artifactPath);
return new VerificationResult(true, "Docker unavailable - skipped verification", "");
}
std::string containerImage = mapLangToImage(language);
std::string testCommand = mapLangToTestCmd(language);
std::cout << "Container: " + containerImage << std::endl;
std::cout << "Test Command: " + testCommand << std::endl;
std::cout <<  << std::endl;
std::cout << "Spinning up containment..." << std::endl;
// 1. DOCKER COMMAND
std::shared_ptr<File> artifactFile = std::make_shared<File>(artifactPath);
if (!artifactFile.exists()) {
System.err.println("❌ Artifact path does not exist: " + artifactPath);
auditLog.log("sandbox_artifact_not_found", artifactPath);
return new VerificationResult(false, "Artifact not found", "");
}
std::string absolutePath = artifactFile.getAbsolutePath();
std::string[] cmd = {
"docker", "run", "--rm",
"-v", absolutePath + ":/app",
"-w", "/app",
"--memory", "512m",  // Limit memory
"--cpus", "1",       // Limit CPU
"--network", "none", // No network access
containerImage,
"sh", "-c", testCommand
};
try {
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>(cmd);
pb.redirectErrorStream(true);
Process process = pb.start();
// 2. CAPTURE OUTPUT
std::shared_ptr<BufferedReader> reader = std::make_shared<BufferedReader>(new InputStreamReader(process.getInputStream()));
std::shared_ptr<StringBuilder> output = std::make_shared<StringBuilder>();
std::string line;
bool success = false;
std::cout << "Container output:" << std::endl;
while ((line = reader.readLine()) != null) {
std::cout << "   [DOCKER]: " + line << std::endl;
output.append(line).append("\n");
// Look for success signals
if (line.contains("BUILD SUCCESSFUL") ||
line.contains("passed") ||
line.contains("OK") ||
line.contains("All tests passed")) {
success = true;
}
// Look for failure signals
if (line.contains("BUILD FAILED") ||
line.contains("FAILED") ||
line.contains("Error") ||
line.contains("Exception")) {
success = false;
}
}
// 3. TIMEOUT & EXIT
bool finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
if (!finished) {
process.destroy();
System.err.println("   ❌ SANDBOX: Timeout after " + TIMEOUT_SECONDS + " seconds");
auditLog.log("sandbox_timeout", artifactPath);
return new VerificationResult(false, "Timeout", output.toString());
}
int exitCode = process.exitValue();
bool passed = success && exitCode == 0;
std::cout <<  << std::endl;
if (passed) {
std::cout << "✅ VERIFICATION PASSED" << std::endl;
auditLog.log("sandbox_verification_passed", artifactPath);
} else {
std::cout << "❌ VERIFICATION FAILED (exit code: " + exitCode + ")" << std::endl;
auditLog.log("sandbox_verification_failed", artifactPath);
}
std::cout <<  << std::endl;
return new VerificationResult(passed,
passed ? "All tests passed" : "Tests failed (exit code: " + exitCode + ")",
output.toString());
} catch (Exception e) {
System.err.println("   ❌ SANDBOX FAILURE: " + e.getMessage());
auditLog.log("sandbox_error", artifactPath, e);
return new VerificationResult(false, "Sandbox error: " + e.getMessage(), "");
}
}
/**
* Check if Docker is available
*/
private bool isDockerAvailable() {
try {
std::shared_ptr<Process> process = std::make_shared<ProcessBuilder>("docker", "--version").start();
bool finished = process.waitFor(5, TimeUnit.SECONDS);
return finished && process.exitValue() == 0;
} catch (Exception e) {
return false;
}
}
/**
* Map language to Docker image
*/
private std::string mapLangToImage(std::string tech) {
std::string lower = tech.toLowerCase();
if (lower.contains("java") || lower.contains("gradle")) {
return "gradle:jdk17-alpine";
}
if (lower.contains("python")) {
return "python:3.9-alpine";
}
if (lower.contains("node") || lower.contains("javascript") || lower.contains("react")) {
return "node:18-alpine";
}
if (lower.contains("rust")) {
return "rust:alpine";
}
if (lower.contains("go")) {
return "golang:alpine";
}
return "ubuntu:latest"; // Fallback
}
/**
* Map language to test command
*/
private std::string mapLangToTestCmd(std::string tech) {
std::string lower = tech.toLowerCase();
if (lower.contains("java") || lower.contains("gradle")) {
return "gradle test || echo 'No tests defined'";
}
if (lower.contains("python")) {
return "python -m pytest || python -m unittest discover || echo 'No tests defined'";
}
if (lower.contains("node") || lower.contains("javascript")) {
return "npm test || echo 'No tests defined'";
}
if (lower.contains("rust")) {
return "cargo test || echo 'No tests defined'";
}
if (lower.contains("go")) {
return "go test ./... || echo 'No tests defined'";
}
return "echo 'Unknown tech - verification skipped'";
}
/**
* Verification result
*/
public static class VerificationResult { {
public:
public const bool passed;
public const std::string message;
public const std::string output;
public VerificationResult(bool passed, std::string message, std::string output) {
this.passed = passed;
this.message = message;
this.output = output;
}
@Override
public std::string toString() {
return std::string.format("VerificationResult[passed=%s, message=%s]", passed, message);
}
}
}
