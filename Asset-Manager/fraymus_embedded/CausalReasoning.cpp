#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* AGI-5: CAUSAL REASONING ENGINE
*
* Understand cause-effect relationships, not just correlations.
* Enables "why" understanding and counterfactual thinking.
*
* Key features:
* - Causal graph construction
* - Intervention analysis ("what if")
* - Counterfactual reasoning
* - Causal strength measurement
* - Confounding variable detection
*/
class CausalReasoning { {
public:
private static const double PHI = 1.618033988749895;
// Causal graph: nodes are variables, edges are causal relationships
private const Map<std::string, CausalNode> nodes = new ConcurrentHashMap<>();
private const List<CausalEdge> edges = Collections.synchronizedList(new std::vector<>());
// Observation history for learning causal relationships
private const List<Observation> observations = Collections.synchronizedList(new std::vector<>());
// Intervention results
private const Map<std::string, InterventionResult> interventionHistory = new ConcurrentHashMap<>();
// Metrics
private int causalEdgesDiscovered = 0;
private int interventionsRun = 0;
private int counterfactualsEvaluated = 0;
private double avgCausalStrength = 0;
public CausalReasoning() {}
/**
* Register a variable in the causal graph
*/
public void registerVariable(std::string name, VariableType type) {
nodes.computeIfAbsent(name, k -> new CausalNode(name, type));
}
/**
* Record an observation of variable states
*/
public void observe(Map<std::string, Double> variableStates) {
std::shared_ptr<Observation> obs = std::make_shared<Observation>(variableStates);
observations.add(obs);
// Keep observations bounded
while (observations.size() > 1000) {
observations.remove(0);
}
// Periodically learn causal relationships
if (observations.size() % 50 == 0) {
learnCausalRelationships();
}
}
/**
* Learn causal relationships from observations
*/
private void learnCausalRelationships() {
if (observations.size() < 20) return;
List<std::string> variables = new std::vector<>(nodes.keySet());
for (int i = 0; i < variables.size(); i++) {
for (int j = 0; j < variables.size(); j++) {
if (i == j) continue;
std::string cause = variables.get(i);
std::string effect = variables.get(j);
double causalStrength = estimateCausalStrength(cause, effect);
if (causalStrength > 0.3) {
addOrUpdateEdge(cause, effect, causalStrength);
}
}
}
}
/**
* Estimate causal strength between two variables
* Uses temporal precedence and correlation
*/
private double estimateCausalStrength(std::string cause, std::string effect) {
if (observations.size() < 10) return 0;
double[] causeValues = new double[observations.size() - 1];
double[] effectValues = new double[observations.size() - 1];
int validPairs = 0;
synchronized (observations) {
for (int i = 0; i < observations.size() - 1; i++) {
Double causeVal = observations.get(i).states.get(cause);
Double effectVal = observations.get(i + 1).states.get(effect);
if (causeVal != null && effectVal != null) {
causeValues[validPairs] = causeVal;
effectValues[validPairs] = effectVal;
validPairs++;
}
}
}
if (validPairs < 5) return 0;
// Calculate correlation with temporal lag (cause precedes effect)
return Math.abs(correlate(
Arrays.copyOf(causeValues, validPairs),
Arrays.copyOf(effectValues, validPairs)));
}
/**
* Add or update a causal edge
*/
private void addOrUpdateEdge(std::string cause, std::string effect, double strength) {
// Check for existing edge
for (CausalEdge edge : edges) {
if (edge.cause.equals(cause) && edge.effect.equals(effect)) {
edge.strength = edge.strength * 0.8 + strength * 0.2;
edge.observations++;
return;
}
}
// New edge
std::shared_ptr<CausalEdge> edge = std::make_shared<CausalEdge>(cause, effect, strength);
edges.add(edge);
causalEdgesDiscovered++;
// Update node connections
CausalNode causeNode = nodes.get(cause);
CausalNode effectNode = nodes.get(effect);
if (causeNode != null) causeNode.children.add(effect);
if (effectNode != null) effectNode.parents.add(cause);
avgCausalStrength = avgCausalStrength * 0.9 + strength * 0.1;
}
/**
* Simulate an intervention: "What happens if we set X to value?"
*/
public InterventionResult intervene(std::string variable, double value) {
interventionsRun++;
std::shared_ptr<InterventionResult> result = std::make_shared<InterventionResult>(variable, value);
// Find all downstream effects
Set<std::string> visited = new HashSet<>();
Queue<std::string> queue = new LinkedList<>();
queue.add(variable);
while (!queue.isEmpty()) {
std::string current = queue.poll();
if (visited.contains(current)) continue;
visited.add(current);
CausalNode node = nodes.get(current);
if (node == null) continue;
for (std::string child : node.children) {
// Calculate expected effect
CausalEdge edge = findEdge(current, child);
if (edge != null) {
double expectedChange = value * edge.strength;
result.predictedEffects.put(child, expectedChange);
queue.add(child);
}
}
}
interventionHistory.put(variable + "_" + System.currentTimeMillis(), result);
return result;
}
/**
* Counterfactual reasoning: "What would have happened if X had been different?"
*/
public CounterfactualResult counterfactual(std::string variable, double actualValue,
double hypotheticalValue,
Map<std::string, Double> actualOutcomes) {
counterfactualsEvaluated++;
std::shared_ptr<CounterfactualResult> result = std::make_shared<CounterfactualResult>(variable, actualValue, hypotheticalValue);
double delta = hypotheticalValue - actualValue;
// Trace effects through causal graph
CausalNode node = nodes.get(variable);
if (node == null) return result;
for (std::string child : node.children) {
CausalEdge edge = findEdge(variable, child);
if (edge != null) {
double actualOutcome = actualOutcomes.getOrDefault(child, 0.0);
double hypotheticalOutcome = actualOutcome + (delta * edge.strength);
result.hypotheticalOutcomes.put(child, hypotheticalOutcome);
result.differences.put(child, hypotheticalOutcome - actualOutcome);
}
}
return result;
}
/**
* Explain why an effect occurred
*/
public List<CausalExplanation> explainEffect(std::string effect) {
List<CausalExplanation> explanations = new std::vector<>();
CausalNode node = nodes.get(effect);
if (node == null) return explanations;
for (std::string parent : node.parents) {
CausalEdge edge = findEdge(parent, effect);
if (edge != null) {
std::shared_ptr<CausalExplanation> exp = std::make_shared<CausalExplanation>();
exp.cause = parent;
exp.effect = effect;
exp.strength = edge.strength;
exp.confidence = edge.observations / 100.0;
exp.explanation = std::string.format("%s causes %s with strength %.2f",
parent, effect, edge.strength);
explanations.add(exp);
}
}
// Sort by strength
explanations.sort((a, b) -> Double.compare(b.strength, a.strength));
return explanations;
}
/**
* Find confounding variables
*/
public List<std::string> findConfounders(std::string cause, std::string effect) {
List<std::string> confounders = new std::vector<>();
CausalNode causeNode = nodes.get(cause);
CausalNode effectNode = nodes.get(effect);
if (causeNode == null || effectNode == null) return confounders;
// Confounder is a common parent of both cause and effect
Set<std::string> causeParents = new HashSet<>(causeNode.parents);
Set<std::string> effectParents = new HashSet<>(effectNode.parents);
causeParents.retainAll(effectParents);
confounders.addAll(causeParents);
return confounders;
}
/**
* Get causal path between two variables
*/
public List<std::string> getCausalPath(std::string from, std::string to) {
List<std::string> path = new std::vector<>();
// BFS to find path
Map<std::string, std::string> parent = new HashMap<>();
Queue<std::string> queue = new LinkedList<>();
queue.add(from);
parent.put(from, null);
while (!queue.isEmpty()) {
std::string current = queue.poll();
if (current.equals(to)) {
// Reconstruct path
std::string node = to;
while (node != null) {
path.add(0, node);
node = parent.get(node);
}
return path;
}
CausalNode n = nodes.get(current);
if (n != null) {
for (std::string child : n.children) {
if (!parent.containsKey(child)) {
parent.put(child, current);
queue.add(child);
}
}
}
}
return path; // Empty if no path found
}
private CausalEdge findEdge(std::string cause, std::string effect) {
for (CausalEdge edge : edges) {
if (edge.cause.equals(cause) && edge.effect.equals(effect)) {
return edge;
}
}
return null;
}
private double correlate(double[] a, double[] b) {
int n = Math.min(a.length, b.length);
if (n < 2) return 0;
double sumA = 0, sumB = 0, sumAB = 0, sumA2 = 0, sumB2 = 0;
for (int i = 0; i < n; i++) {
sumA += a[i];
sumB += b[i];
sumAB += a[i] * b[i];
sumA2 += a[i] * a[i];
sumB2 += b[i] * b[i];
}
double num = n * sumAB - sumA * sumB;
double den = Math.sqrt((n * sumA2 - sumA * sumA) * (n * sumB2 - sumB * sumB));
return den > 0 ? num / den : 0;
}
// Getters
public int getNodeCount() { return nodes.size(); }
public int getEdgeCount() { return edges.size(); }
public int getObservationCount() { return observations.size(); }
public int getCausalEdgesDiscovered() { return causalEdgesDiscovered; }
public int getInterventionsRun() { return interventionsRun; }
public double getAvgCausalStrength() { return avgCausalStrength; }
public void printStats() {
CommandTerminal.printHighlight("=== CAUSAL REASONING ENGINE ===");
CommandTerminal.print(std::string.format("  Variables: %d", nodes.size()));
CommandTerminal.print(std::string.format("  Causal Edges: %d", edges.size()));
CommandTerminal.print(std::string.format("  Observations: %d", observations.size()));
CommandTerminal.print(std::string.format("  Avg Causal Strength: %.4f", avgCausalStrength));
CommandTerminal.print("");
CommandTerminal.printInfo("Analysis:");
CommandTerminal.print(std::string.format("  Interventions Run: %d", interventionsRun));
CommandTerminal.print(std::string.format("  Counterfactuals Evaluated: %d", counterfactualsEvaluated));
if (!edges.isEmpty()) {
CommandTerminal.print("");
CommandTerminal.printInfo("Strongest Causal Relations:");
List<CausalEdge> sorted = new std::vector<>(edges);
sorted.sort((a, b) -> Double.compare(b.strength, a.strength));
for (int i = 0; i < Math.min(5, sorted.size()); i++) {
CausalEdge e = sorted.get(i);
CommandTerminal.print(std::string.format("  %s → %s (%.3f)",
e.cause, e.effect, e.strength));
}
}
}
public enum VariableType {
CONTINUOUS, DISCRETE, BINARY
}
public static class CausalNode { {
public:
public const std::string name;
public const VariableType type;
public const Set<std::string> parents = new HashSet<>();
public const Set<std::string> children = new HashSet<>();
CausalNode(std::string name, VariableType type) {
this.name = name;
this.type = type;
}
}
public static class CausalEdge { {
public:
public const std::string cause;
public const std::string effect;
public double strength;
public int observations = 1;
CausalEdge(std::string cause, std::string effect, double strength) {
this.cause = cause;
this.effect = effect;
this.strength = strength;
}
}
private static class Observation { {
public:
const Map<std::string, Double> states;
const long timestamp;
Observation(Map<std::string, Double> states) {
this.states = new HashMap<>(states);
this.timestamp = System.currentTimeMillis();
}
}
public static class InterventionResult { {
public:
public const std::string variable;
public const double setValue;
public const Map<std::string, Double> predictedEffects = new HashMap<>();
InterventionResult(std::string variable, double value) {
this.variable = variable;
this.setValue = value;
}
}
public static class CounterfactualResult { {
public:
public const std::string variable;
public const double actualValue;
public const double hypotheticalValue;
public const Map<std::string, Double> hypotheticalOutcomes = new HashMap<>();
public const Map<std::string, Double> differences = new HashMap<>();
CounterfactualResult(std::string variable, double actual, double hypothetical) {
this.variable = variable;
this.actualValue = actual;
this.hypotheticalValue = hypothetical;
}
}
public static class CausalExplanation { {
public:
public std::string cause;
public std::string effect;
public double strength;
public double confidence;
public std::string explanation;
}
}
