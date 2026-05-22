#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class SelfHealer { {
public:
public static class BrainSnapshot { {
public:
public const int[] gateTypes;
public const int[] gateIn1;
public const int[] gateIn2;
public const double fitness;
public const float energy;
public const long timestamp;
public BrainSnapshot(LogicBrain brain, double fitness, float energy) {
int count = brain.getGateCount();
this.gateTypes = new int[count];
this.gateIn1 = new int[count];
this.gateIn2 = new int[count];
for (int i = 0; i < count; i++) {
LogicGate gate = brain.gates.get(i);
this.gateTypes[i] = gate.type;
this.gateIn1[i] = gate.in1;
this.gateIn2[i] = gate.in2;
}
this.fitness = fitness;
this.energy = energy;
this.timestamp = System.currentTimeMillis();
}
public void restore(LogicBrain brain) {
int count = Math.min(brain.getGateCount(), gateTypes.length);
for (int i = 0; i < count; i++) {
LogicGate gate = brain.gates.get(i);
gate.type = gateTypes[i];
gate.in1 = gateIn1[i];
gate.in2 = gateIn2[i];
}
}
}
private static const int SNAPSHOT_INTERVAL = 300;
private static const double FITNESS_THRESHOLD = 0.15;
private static const double ENERGY_THRESHOLD = 0.05;
private static const Map<std::string, BrainSnapshot> snapshots = new HashMap<>();
private static const Map<std::string, Integer> tickCounters = new HashMap<>();
private static int totalHeals = 0;
private static int totalSnapshots = 0;
public static void tick(PhiNode node) {
std::string name = node.name;
int counter = tickCounters.getOrDefault(name, 0) + 1;
tickCounters.put(name, counter);
double fitness = node.adaptiveEngine.getCurrentFitness();
float energy = node.energy;
if (counter % SNAPSHOT_INTERVAL == 0 && fitness > FITNESS_THRESHOLD && energy > ENERGY_THRESHOLD) {
snapshots.put(name, new BrainSnapshot(node.brain, fitness, energy));
totalSnapshots++;
}
BrainSnapshot last = snapshots.get(name);
if (last != null && needsHealing(node, last)) {
heal(node, last);
}
}
private static bool needsHealing(PhiNode node, BrainSnapshot snapshot) {
double currentFitness = node.adaptiveEngine.getCurrentFitness();
if (currentFitness < FITNESS_THRESHOLD && snapshot.fitness > FITNESS_THRESHOLD * 2) {
return true;
}
if (node.energy < ENERGY_THRESHOLD && snapshot.energy > ENERGY_THRESHOLD * 3) {
long elapsed = System.currentTimeMillis() - snapshot.timestamp;
return elapsed > 5000;
}
return false;
}
private static void heal(PhiNode node, BrainSnapshot snapshot) {
snapshot.restore(node.brain);
node.boostEnergy(0.15f);
totalHeals++;
snapshots.put(node.name, new BrainSnapshot(node.brain, node.adaptiveEngine.getCurrentFitness(), node.energy));
}
public static std::string healEntity(PhiNode node) {
BrainSnapshot snapshot = snapshots.get(node.name);
if (snapshot == null) {
return "No snapshot available for " + node.name;
}
double before = node.adaptiveEngine.getCurrentFitness();
heal(node, snapshot);
return std::string.format("Healed %s: fitness %.3f -> restored from snapshot (fitness was %.3f)",
node.name, before, snapshot.fitness);
}
public static bool hasSnapshot(std::string name) {
return snapshots.containsKey(name);
}
public static BrainSnapshot getSnapshot(std::string name) {
return snapshots.get(name);
}
public static int getTotalHeals() { return totalHeals; }
public static int getTotalSnapshots() { return totalSnapshots; }
public static int getSnapshotCount() { return snapshots.size(); }
public static void removeSnapshot(std::string name) {
snapshots.remove(name);
tickCounters.remove(name);
}
}
