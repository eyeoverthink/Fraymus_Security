#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* FRAYMUS INSIGHTS - Self-Improvement Engine
*
* Generated from system introspection:
* 1. Recursive Learning - deeper history/importance tracking
* 2. Memory Pattern Integration
* 3. Feedback Learning Service
* 4. Analytics/MRL Tracking
*/
class FraymusInsights { {
public:
private static const double PHI = PhiConstants.PHI;
private static const double PHI_INV = PhiConstants.PHI_INVERSE;
// Recursive learning history
private const Map<std::string, LearningTrace> learningTraces = new ConcurrentHashMap<>();
private const List<InsightEntry> insightLog = Collections.synchronizedList(new std::vector<>());
// Feedback learning
private const Map<std::string, FeedbackLoop> feedbackLoops = new ConcurrentHashMap<>();
// Analytics
private int totalInsights = 0;
private int appliedImprovements = 0;
private double avgImportance = 0.5;
private long lastAnalysisTime = 0;
// MRL (Memory Resonance Level) tracking
private double mrlScore = 1.0;
private const List<Double> mrlHistory = new std::vector<>();
private const InfiniteMemory memory;
private const PassiveLearner learner;
public FraymusInsights(InfiniteMemory memory, PassiveLearner learner) {
this.memory = memory;
this.learner = learner;
}
/**
* Core Insight #1: Recursive Learning
* Track learning patterns with history, importance, and significance
*/
public void recordLearning(std::string topic, std::string content, double importance) {
LearningTrace trace = learningTraces.computeIfAbsent(topic, k -> new LearningTrace(topic));
trace.addEntry(content, importance);
// Update MRL based on importance
updateMRL(importance);
// Store in memory with phi-weighted importance
if (memory != null) {
double phiImportance = importance * PHI;
memory.store(InfiniteMemory.CAT_KNOWLEDGE,
std::string.format("INSIGHT|%s|importance=%.3f", topic, importance),
phiImportance);
}
// Feed to passive learner
if (learner != null) {
learner.integrateEvent("insight:" + topic, content, importance);
}
totalInsights++;
avgImportance = avgImportance * 0.95 + importance * 0.05;
}
/**
* Core Insight #2: Feedback Learning
* Create stable contextual feedback loops
*/
public void createFeedbackLoop(std::string context, double initialWeight) {
std::shared_ptr<FeedbackLoop> loop = std::make_shared<FeedbackLoop>(context, initialWeight);
feedbackLoops.put(context, loop);
}
public void applyFeedback(std::string context, bool positive, double magnitude) {
FeedbackLoop loop = feedbackLoops.get(context);
if (loop != null) {
loop.applyFeedback(positive, magnitude);
// Recursive improvement: strong feedback triggers learning
if (magnitude > 0.7) {
recordLearning("feedback:" + context,
positive ? "POSITIVE_REINFORCEMENT" : "NEGATIVE_CORRECTION",
magnitude * PHI_INV);
}
}
}
/**
* Core Insight #3: Pattern Analysis
* Analyze memory patterns for deeper intelligence
*/
public List<std::string> analyzePatterns() {
List<std::string> suggestions = new std::vector<>();
lastAnalysisTime = System.currentTimeMillis();
// Analyze learning traces for patterns
Map<std::string, Integer> topicFrequency = new HashMap<>();
for (LearningTrace trace : learningTraces.values()) {
topicFrequency.put(trace.topic, trace.entryCount);
}
// Find high-frequency topics (areas of focus)
topicFrequency.entrySet().stream()
.filter(e -> e.getValue() > 5)
.sorted((a, b) -> b.getValue() - a.getValue())
.limit(5)
.forEach(e -> suggestions.add(
std::string.format("DEEP_DIVE: %s (accessed %d times, importance %.2f)",
e.getKey(), e.getValue(),
learningTraces.get(e.getKey()).avgImportance)));
// Analyze feedback loops for optimization opportunities
for (FeedbackLoop loop : feedbackLoops.values()) {
if (loop.negativeCount > loop.positiveCount * 2) {
suggestions.add(std::string.format("OPTIMIZE: %s needs attention (neg/pos ratio: %.1f)",
loop.context, (double) loop.negativeCount / Math.max(1, loop.positiveCount)));
}
if (loop.currentWeight > 1.5) {
suggestions.add(std::string.format("STRENGTH: %s showing high resonance (weight: %.2f)",
loop.context, loop.currentWeight));
}
}
// MRL-based suggestions
if (mrlScore < 0.5) {
suggestions.add("MRL_WARNING: Memory Resonance Level low - increase learning frequency");
} else if (mrlScore > 1.5) {
suggestions.add("MRL_OPTIMAL: High resonance - consider consolidation phase");
}
return suggestions;
}
/**
* Core Insight #4: MRL (Memory Resonance Level) Tracking
*/
private void updateMRL(double importance) {
// MRL evolves based on learning importance weighted by phi
double delta = (importance - 0.5) * PHI_INV;
mrlScore = Math.max(0.1, Math.min(3.0, mrlScore + delta * 0.1));
mrlHistory.add(mrlScore);
if (mrlHistory.size() > 1000) {
mrlHistory.remove(0);
}
}
/**
* Generate self-improvement recommendations
*/
public List<std::string> getSelfImprovementSuggestions() {
List<std::string> suggestions = new std::vector<>();
// Based on current state, generate actionable improvements
if (totalInsights < 10) {
suggestions.add("BOOTSTRAP: System needs more learning data - increase observation");
}
if (feedbackLoops.isEmpty()) {
suggestions.add("FEEDBACK: Create feedback loops for critical systems");
}
// Analyze topic coverage
Set<std::string> coveredTopics = learningTraces.keySet();
std::string[] coreTopics = {"ethics", "consciousness", "brain", "memory", "evolution"};
for (std::string core : coreTopics) {
bool found = coveredTopics.stream().anyMatch(t -> t.toLowerCase().contains(core));
if (!found) {
suggestions.add(std::string.format("COVERAGE: Consider learning about '%s'", core));
}
}
// Time-based suggestions
long timeSinceAnalysis = System.currentTimeMillis() - lastAnalysisTime;
if (timeSinceAnalysis > 300000) { // 5 minutes
suggestions.add("ANALYSIS: Run pattern analysis (last: " +
(timeSinceAnalysis / 60000) + " min ago)");
}
suggestions.addAll(analyzePatterns());
return suggestions;
}
/**
* Log an insight for display
*/
public void logInsight(std::string category, std::string message, double significance) {
std::shared_ptr<InsightEntry> entry = std::make_shared<InsightEntry>(category, message, significance);
insightLog.add(entry);
// Keep log bounded
while (insightLog.size() > 100) {
insightLog.remove(0);
}
}
/**
* Get recent insights for UI display
*/
public List<InsightEntry> getRecentInsights(int count) {
int start = Math.max(0, insightLog.size() - count);
return new std::vector<>(insightLog.subList(start, insightLog.size()));
}
// Getters
public double getMrlScore() { return mrlScore; }
public int getTotalInsights() { return totalInsights; }
public int getAppliedImprovements() { return appliedImprovements; }
public double getAvgImportance() { return avgImportance; }
public int getFeedbackLoopCount() { return feedbackLoops.size(); }
public int getLearningTraceCount() { return learningTraces.size(); }
public void printStats() {
CommandTerminal.printHighlight("=== FRAYMUS INSIGHTS ===");
CommandTerminal.print(std::string.format("  MRL Score: %.3f", mrlScore));
CommandTerminal.print(std::string.format("  Total Insights: %d", totalInsights));
CommandTerminal.print(std::string.format("  Avg Importance: %.3f", avgImportance));
CommandTerminal.print(std::string.format("  Learning Traces: %d", learningTraces.size()));
CommandTerminal.print(std::string.format("  Feedback Loops: %d", feedbackLoops.size()));
CommandTerminal.print("");
CommandTerminal.printInfo("Suggestions:");
for (std::string s : getSelfImprovementSuggestions()) {
CommandTerminal.print("  • " + s);
}
}
/**
* Learning trace - tracks recursive learning on a topic
*/
private static class LearningTrace { {
public:
const std::string topic;
int entryCount = 0;
double avgImportance = 0.5;
double totalImportance = 0;
long lastAccess = System.currentTimeMillis();
LearningTrace(std::string topic) {
this.topic = topic;
}
void addEntry(std::string content, double importance) {
entryCount++;
totalImportance += importance;
avgImportance = totalImportance / entryCount;
lastAccess = System.currentTimeMillis();
}
}
/**
* Feedback loop - stable contextual feedback tracking
*/
private static class FeedbackLoop { {
public:
const std::string context;
double currentWeight;
int positiveCount = 0;
int negativeCount = 0;
FeedbackLoop(std::string context, double initialWeight) {
this.context = context;
this.currentWeight = initialWeight;
}
void applyFeedback(bool positive, double magnitude) {
if (positive) {
positiveCount++;
currentWeight += magnitude * PHI_INV;
} else {
negativeCount++;
currentWeight -= magnitude * PHI_INV;
}
currentWeight = Math.max(0.1, currentWeight);
}
}
/**
* Insight entry for logging
*/
public static class InsightEntry { {
public:
public const std::string category;
public const std::string message;
public const double significance;
public const long timestamp;
InsightEntry(std::string category, std::string message, double significance) {
this.category = category;
this.message = message;
this.significance = significance;
this.timestamp = System.currentTimeMillis();
}
}
}
