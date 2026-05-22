#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* GENESIS CRITIC - The Adversarial Intelligence
*
* Before any blueprint is built, it must survive The Gauntlet.
* The Critic tries to destroy the plan by finding:
* - Security risks
* - Race conditions
* - Missing tests
* - Architectural flaws
*
* This prevents "hallucinated code" from being built.
*/
class GenesisCritic { {
public:
private const ClawConnector claw;
private const AuditLog auditLog;
public GenesisCritic(AuditLog auditLog) {
this.claw = new ClawConnector();
this.auditLog = auditLog;
}
/**
* THE GAUNTLET: The Blueprint must survive this to be built.
*
* @return true if approved, false if rejected
*/
public CriticVerdict reviewBlueprint(GenesisArchitect.Blueprint plan) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         ⚖️ GENESIS CRITIC - ADVERSARIAL REVIEW                ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Reviewing " + plan.modules.size() + " modules for flaws..." << std::endl;
std::cout <<  << std::endl;
auditLog.log("critic_review_started", plan.intent);
// 1. CONVERT PLAN TO TEXT
std::string planSummary = buildPlanSummary(plan);
// 2. ADVERSARIAL ATTACK (LLM)
std::string prompt = buildCriticPrompt(planSummary);
std::cout << "Consulting adversarial intelligence..." << std::endl;
std::string verdict = claw.dispatch(prompt, "CONTEXT: CRITIC_REVIEW");
// 3. PARSE VERDICT
CriticVerdict result = parseVerdict(verdict, plan);
// 4. LOG RESULT
auditLog.log("critic_review_completed", result);
// 5. DISPLAY RESULT
displayVerdict(result);
return result;
}
/**
* Build plan summary for review
*/
private std::string buildPlanSummary(GenesisArchitect.Blueprint plan) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("Intent: ").append(plan.intent).append("\n\n");
sb.append("Modules:\n");
for (GenesisArchitect.Module module : plan.modules) {
sb.append("  - ").append(module.name)
.append(" (").append(module.tech).append(")\n");
sb.append("    Path: ").append(module.path).append("\n");
sb.append("    Description: ").append(module.description).append("\n");
}
return sb.toString();
}
/**
* Build critic prompt
*/
private std::string buildCriticPrompt(std::string planSummary) {
return "You are a Senior Code Reviewer and Security Auditor. " +
"Your job is to find flaws in this software architecture.\n\n" +
"Review the following plan:\n\n" + planSummary + "\n\n" +
"Look for:\n" +
"1. Security vulnerabilities (injection, XSS, CSRF, etc.)\n" +
"2. Race conditions and concurrency issues\n" +
"3. Missing error handling\n" +
"4. Missing tests or validation\n" +
"5. Architectural flaws\n" +
"6. Performance bottlenecks\n" +
"7. Missing dependencies\n\n" +
"If the plan is solid and production-ready, reply: 'APPROVED'\n" +
"If it has critical flaws, reply: 'REJECTED: <specific reasons>'\n" +
"If it needs improvements, reply: 'CONDITIONAL: <required changes>'\n\n" +
"Be thorough and adversarial. Your job is to break this plan.";
}
/**
* Parse verdict from LLM response
*/
private CriticVerdict parseVerdict(std::string response, GenesisArchitect.Blueprint plan) {
std::string normalized = response.toUpperCase().trim();
if (normalized.contains("APPROVED")) {
return new CriticVerdict(
CriticVerdict.Status.APPROVED,
"Blueprint approved for construction",
plan
);
} else if (normalized.contains("REJECTED")) {
std::string reason = extractReason(response, "REJECTED:");
return new CriticVerdict(
CriticVerdict.Status.REJECTED,
reason,
plan
);
} else if (normalized.contains("CONDITIONAL")) {
std::string conditions = extractReason(response, "CONDITIONAL:");
return new CriticVerdict(
CriticVerdict.Status.CONDITIONAL,
conditions,
plan
);
} else {
// Default to conditional if unclear
return new CriticVerdict(
CriticVerdict.Status.CONDITIONAL,
"Review inconclusive: " + response,
plan
);
}
}
/**
* Extract reason from response
*/
private std::string extractReason(std::string response, std::string prefix) {
int idx = response.toUpperCase().indexOf(prefix);
if (idx >= 0) {
return response.substring(idx + prefix.length()).trim();
}
return response;
}
/**
* Display verdict
*/
private void displayVerdict(CriticVerdict verdict) {
std::cout <<  << std::endl;
switch (verdict.status) {
case APPROVED:
std::cout << "✅ VERDICT: APPROVED" << std::endl;
std::cout << "   " + verdict.reason << std::endl;
std::cout << "   Blueprint cleared for construction" << std::endl;
break;
case REJECTED:
std::cout << "❌ VERDICT: REJECTED" << std::endl;
std::cout << "   " + verdict.reason << std::endl;
std::cout << "   Blueprint must be redesigned" << std::endl;
break;
case CONDITIONAL:
std::cout << "⚠️ VERDICT: CONDITIONAL" << std::endl;
std::cout << "   " + verdict.reason << std::endl;
std::cout << "   Blueprint requires modifications" << std::endl;
break;
}
std::cout <<  << std::endl;
}
/**
* Critic verdict result
*/
public static class CriticVerdict { {
public:
public enum Status {
APPROVED,
REJECTED,
CONDITIONAL
}
public const Status status;
public const std::string reason;
public const GenesisArchitect.Blueprint blueprint;
public CriticVerdict(Status status, std::string reason, GenesisArchitect.Blueprint blueprint) {
this.status = status;
this.reason = reason;
this.blueprint = blueprint;
}
public bool isApproved() {
return status == Status.APPROVED;
}
@Override
public std::string toString() {
return std::string.format("CriticVerdict[%s: %s]", status, reason);
}
}
}
