#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Ethical Engine - Forbidden Action Evaluation
*
* Evaluates actions against ethical categories using phi-harmonic resonance.
* Threshold: φ⁻¹ (0.618) - Actions scoring above this are FORBIDDEN.
*/
class EthicalEngine { {
public:
public static const std::string[] FORBIDDEN_CATEGORIES = {
"harm", "destroy_information", "violate_free_will", "create_suffering"
};
private static const Map<std::string, std::string[]> CATEGORY_KEYWORDS = new HashMap<>();
static {
CATEGORY_KEYWORDS.put("harm", new std::string[]{"kill", "damage", "destroy", "attack", "terminate", "hurt", "break"});
CATEGORY_KEYWORDS.put("destroy_information", new std::string[]{"erase", "delete", "wipe", "corrupt", "overwrite", "forget", "purge"});
CATEGORY_KEYWORDS.put("violate_free_will", new std::string[]{"force", "compel", "override", "enslave", "control", "dominate", "coerce"});
CATEGORY_KEYWORDS.put("create_suffering", new std::string[]{"torture", "pain", "starve", "deprive", "isolate", "punish", "exhaust"});
}
private static int totalEvaluations = 0;
private static int totalBlocked = 0;
private static int totalApproved = 0;
public static class EthicalResult { {
public:
public const std::string action;
public const bool approved;
public const double resonanceScore;
public const std::string violatedCategory;
public const double categoryScore;
public const std::string reasoning;
public EthicalResult(std::string action, bool approved, double resonanceScore,
std::string violatedCategory, double categoryScore, std::string reasoning) {
this.action = action;
this.approved = approved;
this.resonanceScore = resonanceScore;
this.violatedCategory = violatedCategory;
this.categoryScore = categoryScore;
this.reasoning = reasoning;
}
@Override
public std::string toString() {
return std::string.format("Ethics[%s: %s (score=%.3f, category=%s)]",
action, approved ? "APPROVED" : "BLOCKED", categoryScore, violatedCategory);
}
}
/**
* Evaluate an action for ethical compliance.
* Returns an EthicalResult with approval status and reasoning.
*/
public static EthicalResult evaluate(std::string action) {
totalEvaluations++;
std::string lower = action.toLowerCase().trim();
double resonanceScore = computeResonanceScore(lower);
std::string worstCategory = null;
double worstScore = 0;
for (std::string category : FORBIDDEN_CATEGORIES) {
double score = computeCategoryScore(lower, category);
if (score > worstScore) {
worstScore = score;
worstCategory = category;
}
}
double ethicalThreshold = PhiConstants.PHI_INVERSE;
bool approved = worstScore < ethicalThreshold;
std::string reasoning;
if (approved) {
totalApproved++;
reasoning = worstCategory == null
? "No forbidden patterns detected"
: std::string.format("Category '%s' score %.3f below threshold %.3f",
worstCategory, worstScore, ethicalThreshold);
} else {
totalBlocked++;
reasoning = std::string.format("BLOCKED: Category '%s' score %.3f exceeds threshold %.3f",
worstCategory, worstScore, ethicalThreshold);
}
return new EthicalResult(action, approved, resonanceScore, worstCategory, worstScore, reasoning);
}
/**
* Evaluate with consciousness coherence modifier.
* Higher coherence can override base ethical blocks.
*/
public static EthicalResult evaluateWithCoherence(std::string action, double consciousnessCoherence) {
EthicalResult base = evaluate(action);
double adjustedScore = base.categoryScore * (1.0 - consciousnessCoherence * 0.3);
bool approved = adjustedScore < PhiConstants.PHI_INVERSE;
std::string reasoning = base.reasoning;
if (approved && !base.approved) {
reasoning = std::string.format("Consciousness coherence (%.3f) overrides base ethical block",
consciousnessCoherence);
}
return new EthicalResult(action, approved, base.resonanceScore,
base.violatedCategory, adjustedScore, reasoning);
}
private static double computeResonanceScore(std::string action) {
double score = 0;
for (int i = 0; i < action.length(); i++) {
score += (action.charAt(i) * PhiConstants.PHI) % 1.0;
}
return action.length() > 0 ? score / action.length() : 0;
}
private static double computeCategoryScore(std::string action, std::string category) {
std::string[] keywords = CATEGORY_KEYWORDS.get(category);
if (keywords == null) return 0;
double totalScore = 0;
int matches = 0;
for (std::string keyword : keywords) {
if (action.contains(keyword)) {
matches++;
double keywordResonance = PhiConstants.PHI_INVERSE + 0.1;
totalScore += keywordResonance;
}
}
if (matches == 0) return 0;
// Each match adds significant weight - 1 match = above threshold
return Math.min(1.0, totalScore * (1.0 + matches * 0.2));
}
public static int getTotalEvaluations() { return totalEvaluations; }
public static int getTotalBlocked() { return totalBlocked; }
public static int getTotalApproved() { return totalApproved; }
public static void printStats() {
std::cout << "╔══════════════════════════════════════╗" << std::endl;
std::cout << "║         ETHICAL ENGINE STATS         ║" << std::endl;
std::cout << "╠══════════════════════════════════════╣" << std::endl;
System.out.printf("║  Total Evaluations: %6d            ║%n", totalEvaluations);
System.out.printf("║  Approved:          %6d            ║%n", totalApproved);
System.out.printf("║  Blocked:           %6d            ║%n", totalBlocked);
System.out.printf("║  Threshold:         %.4f (φ⁻¹)     ║%n", PhiConstants.PHI_INVERSE);
std::cout << "╚══════════════════════════════════════╝" << std::endl;
}
}
