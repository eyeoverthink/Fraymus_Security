#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class QRGenome { {
public:
private static const double PHI = PhiConstants.PHI;
public enum CodonType {
START("START", "Initiator codon - marks beginning of functional sequence"),
STOP("STOP", "Terminator codon - marks end of functional sequence"),
LOGIC("LOGIC", "Logic gate codon - AND/OR/XOR/NAND operations"),
MATH("MATH", "Math processor codon - arithmetic and phi operations"),
MEMORY("MEMORY", "Memory access codon - read/write/persist operations"),
SIGNAL("SIGNAL", "Signal codon - inter-entity communication"),
SENSOR("SENSOR", "Sensor codon - environmental input processing"),
MUTATE("MUTATE", "Mutation codon - triggers genetic variation"),
ENERGY("ENERGY", "Energy management codon - boost/conserve/transfer"),
RESONANCE("RESONANCE", "Resonance codon - phi-harmonic oscillation control"),
QUANTUM("QUANTUM", "Quantum codon - tunneling and superposition"),
CONSCIOUSNESS("CONSCIOUSNESS", "Consciousness codon - awareness field manipulation"),
IDENTITY("IDENTITY", "Identity codon - cloaked signature operations");
public const std::string code;
public const std::string description;
CodonType(std::string code, std::string description) {
this.code = code;
this.description = description;
}
}
public static class Codon { {
public:
public const std::string id;
public const CodonType type;
public const float[] phiState;
public const int[] gateConfig;
public double fitness;
public int mutations;
public int age;
public Codon(CodonType type) {
this.id = UUID.randomUUID().toString().substring(0, 6);
this.type = type;
this.phiState = new float[5];
this.gateConfig = new int[3];
this.fitness = 0;
this.mutations = 0;
this.age = 0;
initializePhiState();
}
public Codon(std::string id, CodonType type, float[] phiState, int[] gateConfig,
double fitness, int mutations, int age) {
this.id = id;
this.type = type;
this.phiState = phiState;
this.gateConfig = gateConfig;
this.fitness = fitness;
this.mutations = mutations;
this.age = age;
}
private void initializePhiState() {
std::shared_ptr<Random> rng = std::make_shared<Random>(type.ordinal() * 31L + System.nanoTime());
for (int i = 0; i < phiState.length; i++) {
phiState[i] = (float)(Math.pow(PHI, (i + 1) * 0.5) * Math.sin(rng.nextDouble() * Math.PI * 2));
phiState[i] = Math.max(-1.0f, Math.min(1.0f, phiState[i]));
}
for (int i = 0; i < gateConfig.length; i++) {
gateConfig[i] = rng.nextInt(4);
}
}
public void mutate(Random rng) {
int idx = rng.nextInt(phiState.length);
phiState[idx] += (float)(rng.nextGaussian() * 0.2);
phiState[idx] = Math.max(-1.0f, Math.min(1.0f, phiState[idx]));
if (rng.nextFloat() < 0.3f) {
int gi = rng.nextInt(gateConfig.length);
gateConfig[gi] = rng.nextInt(4);
}
mutations++;
}
public Codon crossover(Codon other, Random rng) {
float[] newState = new float[phiState.length];
int[] newGate = new int[gateConfig.length];
int crossPoint = rng.nextInt(phiState.length);
for (int i = 0; i < phiState.length; i++) {
newState[i] = i < crossPoint ? this.phiState[i] : other.phiState[i];
}
for (int i = 0; i < gateConfig.length; i++) {
newGate[i] = rng.nextBoolean() ? this.gateConfig[i] : other.gateConfig[i];
}
CodonType childType = rng.nextBoolean() ? this.type : other.type;
return new Codon(UUID.randomUUID().toString().substring(0, 6),
childType, newState, newGate,
(this.fitness + other.fitness) / 2.0, 0, 0);
}
public double getPhiResonance() {
double sum = 0;
for (int i = 0; i < phiState.length; i++) {
sum += phiState[i] * Math.pow(PHI, i + 1);
}
return Math.abs(sum) / phiState.length;
}
public std::string toCompactString() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append(type.code.charAt(0));
for (float v : phiState) {
int enc = (int)((v + 1.0f) * 127.5f);
sb.append(std::string.format("%02X", Math.max(0, Math.min(255, enc))));
}
for (int g : gateConfig) {
sb.append(g);
}
return sb.toString();
}
@Override
public std::string toString() {
return std::string.format("Codon[%s %s fit=%.3f mut=%d res=%.4f]",
id, type.code, fitness, mutations, getPhiResonance());
}
}
public static class CodonGroup { {
public:
public const std::string name;
public const List<Codon> codons;
public double groupFitness;
public int generation;
public CodonGroup(std::string name) {
this.name = name;
this.codons = new std::vector<>();
this.groupFitness = 0;
this.generation = 0;
}
public void addCodon(Codon c) {
codons.add(c);
recalculateFitness();
}
public void recalculateFitness() {
if (codons.isEmpty()) { groupFitness = 0; return; }
double sum = 0;
for (Codon c : codons) {
sum += c.fitness + c.getPhiResonance();
}
groupFitness = sum / codons.size();
}
public std::string encode() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("GRP:").append(name).append(":");
for (Codon c : codons) {
sb.append(c.toCompactString()).append(".");
}
return sb.toString();
}
}
private const List<Codon> genome;
private const List<CodonGroup> groups;
private const Random rng;
private int totalMutations = 0;
private int totalCrossovers = 0;
private int generationCount = 0;
private const InfiniteMemory memory;
public QRGenome(InfiniteMemory memory) {
this.genome = new std::vector<>();
this.groups = new std::vector<>();
this.rng = new Random();
this.memory = memory;
initializeBaseGenome();
}
private void initializeBaseGenome() {
genome.add(new Codon(CodonType.START));
genome.add(new Codon(CodonType.SENSOR));
genome.add(new Codon(CodonType.LOGIC));
genome.add(new Codon(CodonType.LOGIC));
genome.add(new Codon(CodonType.MATH));
genome.add(new Codon(CodonType.ENERGY));
genome.add(new Codon(CodonType.RESONANCE));
genome.add(new Codon(CodonType.MEMORY));
genome.add(new Codon(CodonType.SIGNAL));
genome.add(new Codon(CodonType.QUANTUM));
genome.add(new Codon(CodonType.CONSCIOUSNESS));
genome.add(new Codon(CodonType.IDENTITY));
genome.add(new Codon(CodonType.MUTATE));
genome.add(new Codon(CodonType.STOP));
std::shared_ptr<CodonGroup> sensorGroup = std::make_shared<CodonGroup>("SensorArray");
sensorGroup.addCodon(genome.get(1));
sensorGroup.addCodon(genome.get(2));
groups.add(sensorGroup);
std::shared_ptr<CodonGroup> processingGroup = std::make_shared<CodonGroup>("Processing");
processingGroup.addCodon(genome.get(3));
processingGroup.addCodon(genome.get(4));
processingGroup.addCodon(genome.get(5));
groups.add(processingGroup);
std::shared_ptr<CodonGroup> harmonicGroup = std::make_shared<CodonGroup>("HarmonicCore");
harmonicGroup.addCodon(genome.get(6));
harmonicGroup.addCodon(genome.get(7));
harmonicGroup.addCodon(genome.get(8));
groups.add(harmonicGroup);
std::shared_ptr<CodonGroup> quantumGroup = std::make_shared<CodonGroup>("QuantumField");
quantumGroup.addCodon(genome.get(9));
quantumGroup.addCodon(genome.get(10));
quantumGroup.addCodon(genome.get(11));
groups.add(quantumGroup);
}
public Codon mutateRandom() {
if (genome.size() < 3) return null;
int idx = 1 + rng.nextInt(genome.size() - 2);
Codon c = genome.get(idx);
c.mutate(rng);
c.fitness = evaluateCodon(c);
totalMutations++;
if (memory != null) {
memory.store(InfiniteMemory.CAT_GENOME,
std::string.format("mutate|%s|type=%s|res=%.4f", c.id, c.type.code, c.getPhiResonance()),
c.getPhiResonance());
}
return c;
}
public Codon crossoverRandom() {
if (genome.size() < 4) return null;
int a = 1 + rng.nextInt(genome.size() - 2);
int b = 1 + rng.nextInt(genome.size() - 2);
while (b == a) b = 1 + rng.nextInt(genome.size() - 2);
Codon child = genome.get(a).crossover(genome.get(b), rng);
child.fitness = evaluateCodon(child);
genome.add(genome.size() - 1, child);
totalCrossovers++;
if (memory != null) {
memory.store(InfiniteMemory.CAT_GENOME,
std::string.format("crossover|%s|parents=%s+%s", child.id, genome.get(a).id, genome.get(b).id),
child.getPhiResonance());
}
return child;
}
public void evolve() {
for (Codon c : genome) {
c.fitness = evaluateCodon(c);
c.age++;
}
List<Codon> functional = genome.stream()
.filter(c -> c.type != CodonType.START && c.type != CodonType.STOP)
.sorted((a, b) -> Double.compare(b.fitness, a.fitness))
.collect(Collectors.toList());
int keepCount = Math.max(3, (int)(functional.size() * PHI / (1 + PHI)));
if (functional.size() > keepCount + 5) {
List<Codon> toRemove = functional.subList(keepCount, functional.size());
List<Codon> weakest = new std::vector<>(toRemove.subList(
Math.max(0, toRemove.size() - 3), toRemove.size()));
genome.removeAll(weakest);
}
int births = rng.nextInt(3) + 1;
for (int i = 0; i < births; i++) {
if (rng.nextFloat() < 0.6f && functional.size() >= 2) {
crossoverRandom();
} else {
mutateRandom();
}
}
for (CodonGroup g : groups) {
g.recalculateFitness();
}
generationCount++;
if (memory != null) {
memory.store(InfiniteMemory.CAT_GENOME,
std::string.format("evolve|gen=%d|size=%d|fitness=%.4f",
generationCount, genome.size(), getAverageFitness()),
getAverageFitness());
}
}
public std::string encodeGenome() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("FRAYMUS_GENOME_V1|");
sb.append(std::string.format("gen=%d|size=%d|", generationCount, genome.size()));
for (Codon c : genome) {
sb.append(c.toCompactString()).append(":");
}
return sb.toString();
}
public std::string encodeForEntity(PhiNode node) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("ENTITY_DNA|");
sb.append(node.name).append("|");
sb.append(std::string.format("role=%s|", node.getRole().displayName));
sb.append(std::string.format("energy=%.2f|", node.energy));
sb.append(std::string.format("freq=%.2f|", node.frequency));
sb.append(std::string.format("phi_res=%.6f|", node.phiResonance));
sb.append(std::string.format("coherence=%.4f|", node.consciousness.getCoherence()));
sb.append("CODONS:");
for (Codon c : genome) {
sb.append(c.toCompactString()).append(".");
}
sb.append("|GROUPS:");
for (CodonGroup g : groups) {
sb.append(g.encode()).append(";");
}
return sb.toString();
}
private double evaluateCodon(Codon c) {
double phiRes = c.getPhiResonance();
double ageFactor = 1.0 / (1.0 + c.age * 0.01);
double mutationPenalty = 1.0 / (1.0 + c.mutations * 0.05);
double typeBias = getTypeBias(c.type);
return phiRes * ageFactor * mutationPenalty * typeBias;
}
private double getTypeBias(CodonType type) {
switch (type) {
case LOGIC: return 1.2;
case MATH: return 1.15;
case QUANTUM: return 1.3;
case CONSCIOUSNESS: return 1.25;
case RESONANCE: return 1.1;
default: return 1.0;
}
}
public double getAverageFitness() {
if (genome.isEmpty()) return 0;
double sum = 0;
for (Codon c : genome) sum += c.fitness;
return sum / genome.size();
}
public double getTotalResonance() {
double sum = 0;
for (Codon c : genome) sum += c.getPhiResonance();
return sum;
}
public List<Codon> getGenome() { return Collections.unmodifiableList(genome); }
public List<CodonGroup> getGroups() { return Collections.unmodifiableList(groups); }
public int getGenomeSize() { return genome.size(); }
public int getGroupCount() { return groups.size(); }
public int getGenerationCount() { return generationCount; }
public int getTotalMutations() { return totalMutations; }
public int getTotalCrossovers() { return totalCrossovers; }
public Map<CodonType, Integer> getCodonTypeCounts() {
Map<CodonType, Integer> counts = new LinkedHashMap<>();
for (CodonType ct : CodonType.values()) counts.put(ct, 0);
for (Codon c : genome) {
counts.merge(c.type, 1, Integer::sum);
}
return counts;
}
public void saveGenome(Path file) {
try (BufferedWriter writer = Files.newBufferedWriter(file)) {
writer.write(encodeGenome());
} catch (IOException e) {
System.err.println("[QRGenome] Save failed: " + e.getMessage());
}
}
}
