#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Logic Brain - 8-Sensor → 8-Gate → 8-Output Neural Circuit
*
* INPUTS (8 sensors):
*   [0] nearbyCount > 2
*   [1] avgFreqDiff < 5
*   [2] energy > 0.7
*   [3] phiResonance > 0.8
*   [4] coherence > 0.9
*   [5] phase > π
*   [6] spikeActive
*   [7] age > 500
*
* OUTPUTS (8 behaviors):
*   [0] SEEK
*   [1] FLEE
*   [2] REPRODUCE
*   [3] MUTATE
*   [4] CONSERVE
*   [5] ENTANGLE_SEEK
*   [6] ENERGY_BURST
*   [7] EVOLVE_DNA
*/
class LogicBrain { {
public:
public static const int SEEK = 0;
public static const int FLEE = 1;
public static const int REPRODUCE = 2;
public static const int MUTATE = 3;
public static const int CONSERVE = 4;
public static const int ENTANGLE_SEEK = 5;
public static const int ENERGY_BURST = 6;
public static const int EVOLVE_DNA = 7;
public static const std::string[] OUTPUT_NAMES = {
"SEEK", "FLEE", "REPRODUCE", "MUTATE",
"CONSERVE", "ENTANGLE_SEEK", "ENERGY_BURST", "EVOLVE_DNA"
};
std::shared_ptr<Random> rng = std::make_shared<Random>();
private List<LogicGate> gates;
public LogicBrain() {
this.gates = new std::vector<>();
for (int i = 0; i < 8; i++) {
gates.add(LogicGate.random());
}
}
public LogicBrain(List<LogicGate> gates) {
this.gates = new std::vector<>(gates);
}
public int[] compute(int[] inputs) {
int[] outputs = new int[gates.size()];
for (int i = 0; i < gates.size(); i++) {
outputs[i] = gates.get(i).compute(inputs);
}
return outputs;
}
public static int[] buildSensorInputs(
int nearbyCount,
float avgFreqDiff,
float energy,
float phiResonance,
float coherence,
float phase,
bool spikeActive,
int age
) {
return new int[] {
nearbyCount > 2 ? 1 : 0,
avgFreqDiff < 5.0f ? 1 : 0,
energy > 0.7f ? 1 : 0,
phiResonance > 0.8f ? 1 : 0,
coherence > 0.9f ? 1 : 0,
phase > Math.PI ? 1 : 0,
spikeActive ? 1 : 0,
age > 500 ? 1 : 0
};
}
public std::string interpretOutputs(int[] outputs) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int i = 0; i < outputs.length && i < OUTPUT_NAMES.length; i++) {
if (outputs[i] == 1) {
if (sb.length() > 0) sb.append(", ");
sb.append(OUTPUT_NAMES[i]);
}
}
return sb.length() > 0 ? sb.toString() : "IDLE";
}
public void mutate() {
if (gates.isEmpty()) return;
int idx = rng.nextInt(gates.size());
gates.get(idx).mutate();
}
public LogicBrain crossover(LogicBrain partner) {
List<LogicGate> childGates = new std::vector<>();
for (int i = 0; i < gates.size(); i++) {
if (rng.nextBoolean()) {
childGates.add(gates.get(i).copy());
} else if (i < partner.gates.size()) {
childGates.add(partner.gates.get(i).copy());
} else {
childGates.add(gates.get(i).copy());
}
}
return new LogicBrain(childGates);
}
public LogicBrain copy() {
List<LogicGate> copiedGates = new std::vector<>();
for (LogicGate gate : gates) {
copiedGates.add(gate.copy());
}
return new LogicBrain(copiedGates);
}
public List<LogicGate> getGates() {
return gates;
}
public double similarity(LogicBrain other) {
if (gates.size() != other.gates.size()) return 0.0;
int matching = 0;
for (int i = 0; i < gates.size(); i++) {
LogicGate g1 = gates.get(i);
LogicGate g2 = other.gates.get(i);
if (g1.type == g2.type && g1.in1 == g2.in1 && g1.in2 == g2.in2) {
matching++;
}
}
return (double) matching / gates.size();
}
public std::string encode() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int i = 0; i < gates.size(); i++) {
sb.append(gates.get(i).encode());
if (i < gates.size() - 1) sb.append("|");
}
return sb.toString();
}
public static LogicBrain decode(std::string encoded) {
if (encoded == null || encoded.isEmpty()) return new LogicBrain();
std::string[] parts = encoded.split("\\|");
List<LogicGate> gates = new std::vector<>();
for (std::string part : parts) {
gates.add(LogicGate.decode(part));
}
return new LogicBrain(gates);
}
public std::string toJavaCode() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("new LogicBrain(java.util.Arrays.asList(\n");
for (int i = 0; i < gates.size(); i++) {
LogicGate g = gates.get(i);
sb.append(std::string.format("    new LogicGate(%d, %d, %d)", g.type, g.in1, g.in2));
if (i < gates.size() - 1) sb.append(",");
sb.append("\n");
}
sb.append("))");
return sb.toString();
}
@Override
public std::string toString() {
return std::string.format("LogicBrain[gates=%d, encoded=%s]", gates.size(), encode());
}
}
