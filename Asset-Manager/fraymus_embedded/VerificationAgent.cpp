#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* VERIFICATION AGENT - The Scientist
*
* Specialized agent whose only job is to break things.
* When BuilderAgent finishes, it hands code to VerificationAgent.
*
* Process:
* 1. Run code in sandbox
* 2. Capture results
* 3. Record in BlackBox
* 4. If failed, trigger repair
*/
class VerificationAgent implements Runnable { {
public:
private const Module module;
private const std::string artifactPath;
private const GenesisSandbox sandbox;
private const ClawConnector claw;
private const BlackBox memory;
private const AuditLog auditLog;
private volatile bool complete = false;
private volatile bool success = false;
public VerificationAgent(Module module, std::string artifactPath,
BlackBox memory, AuditLog auditLog) {
this.module = module;
this.artifactPath = artifactPath;
this.sandbox = new GenesisSandbox(auditLog);
this.claw = new ClawConnector();
this.memory = memory;
this.auditLog = auditLog;
}
@Override
public void run() {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         🔬 VERIFICATION AGENT - TESTING " + module.name << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
auditLog.log("verification_started", module.name);
try {
// 1. RUN THE EXPERIMENT
GenesisSandbox.VerificationResult result = sandbox.verifyArtifact(
artifactPath,
module.tech
);
if (result.passed) {
handleSuccess(result);
} else {
handleFailure(result);
}
} catch (Exception e) {
System.err.println("💥 VERIFICATION EXCEPTION: " + e.getMessage());
auditLog.log("verification_exception", module.name, e);
handleFailure(new GenesisSandbox.VerificationResult(
false,
"Exception: " + e.getMessage(),
""
));
} finally {
complete = true;
}
}
/**
* Handle successful verification
*/
private void handleSuccess(GenesisSandbox.VerificationResult result) {
std::cout << "✅ VERIFIED: " + module.name + " is stable" << std::endl;
std::cout << "   " + result.message << std::endl;
std::cout <<  << std::endl;
success = true;
// Record success in BlackBox
memory.remember(
module.name + " Construction (" + module.tech + ")",
"Tests Passed: " + result.message,
true
);
auditLog.log("verification_passed", module.name);
}
/**
* Handle failed verification
*/
private void handleFailure(GenesisSandbox.VerificationResult result) {
System.err.println("💥 FAILURE: " + module.name + " crashed in sandbox");
System.err.println("   " + result.message);
System.err.println();
success = false;
// Record failure in BlackBox
memory.remember(
module.name + " Construction (" + module.tech + ")",
"Tests Failed: " + result.message,
false
);
auditLog.log("verification_failed", module.name);
// 2. SELF-HEALING LOOP
if (shouldAttemptRepair()) {
triggerRepairProtocol(result);
}
}
/**
* Determine if repair should be attempted
*/
private bool shouldAttemptRepair() {
// For V1, we log the failure but don't auto-repair
// Auto-repair will be added in V2 with proper safeguards
return false;
}
/**
* Trigger repair protocol
*/
private void triggerRepairProtocol(GenesisSandbox.VerificationResult result) {
std::cout << "🚑 DISPATCHING REPAIR BOT for " + module.name << std::endl;
auditLog.log("repair_triggered", module.name);
// Build repair prompt
std::string repairPrompt = buildRepairPrompt(result);
// Send to OpenClaw for repair
std::string fixedCode = claw.dispatch(repairPrompt, "CONTEXT: REPAIR");
// TODO: Write fixed code and re-verify
// This would create a repair loop with max attempts
auditLog.log("repair_attempted", module.name);
}
/**
* Build repair prompt for OpenClaw
*/
private std::string buildRepairPrompt(GenesisSandbox.VerificationResult result) {
return "The following code failed verification:\n\n" +
"Module: " + module.name + "\n" +
"Technology: " + module.tech + "\n" +
"Path: " + artifactPath + "\n\n" +
"Error Output:\n" + result.output + "\n\n" +
"Please fix the code to pass all tests. " +
"Return ONLY the corrected code, no explanations.";
}
/**
* Check if verification is complete
*/
public bool isComplete() {
return complete;
}
/**
* Check if verification succeeded
*/
public bool isSuccess() {
return success;
}
/**
* Get module being verified
*/
public Module getModule() {
return module;
}
}
