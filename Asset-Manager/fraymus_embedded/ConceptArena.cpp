#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class ConceptArena { {
public:
private static const double PHI = (1 + Math.sqrt(5)) / 2;
private static const double PHI_INVERSE = 1.0 / PHI;
private static const int MAX_CONCEPTS = 50;
std::shared_ptr<Random> rng = std::make_shared<Random>();
private const List<CodeConcept> concepts = new std::vector<>();
private const List<BattleRecord> battleHistory = new std::vector<>();
private static const int MAX_BATTLE_HISTORY = 100;
private CodeConcept champion = null;
private int totalBattles = 0;
private int evolutionCycle = 0;
private int totalConceptsGenerated = 0;
public static class BattleRecord { {
public:
public const std::string winnerHash;
public const std::string loserHash;
public const AntRole winnerRole;
public const AntRole loserRole;
public const double winnerFitness;
public const double loserFitness;
public const int cycle;
public BattleRecord(CodeConcept winner, CodeConcept loser, int cycle) {
this.winnerHash = winner.hash;
this.loserHash = loser.hash;
this.winnerRole = winner.creatorRole;
this.loserRole = loser.creatorRole;
this.winnerFitness = winner.fitness;
this.loserFitness = loser.fitness;
this.cycle = cycle;
}
public std::string getSummary() {
return std::string.format("%s(%s) %.3f > %s(%s) %.3f",
winnerHash.substring(0, 6), winnerRole.displayName, winnerFitness,
loserHash.substring(0, 6), loserRole.displayName, loserFitness);
}
}
public void submit(CodeConcept concept) {
concepts.add(concept);
totalConceptsGenerated++;
if (concepts.size() > MAX_CONCEPTS) {
concepts.sort(Comparator.comparingDouble(c -> c.fitness));
concepts.remove(0);
}
updateChampion();
}
public BattleRecord runBattle() {
if (concepts.size() < 2) return null;
int idxA = rng.nextInt(concepts.size());
int idxB;
do {
idxB = rng.nextInt(concepts.size());
} while (idxB == idxA);
CodeConcept a = concepts.get(idxA);
CodeConcept b = concepts.get(idxB);
bool aWins = a.battle(b);
totalBattles++;
a.computePhiFitness();
b.computePhiFitness();
BattleRecord record;
if (aWins) {
record = new BattleRecord(a, b, evolutionCycle);
} else {
record = new BattleRecord(b, a, evolutionCycle);
}
battleHistory.add(record);
if (battleHistory.size() > MAX_BATTLE_HISTORY) {
battleHistory.remove(0);
}
updateChampion();
return record;
}
public void evolve() {
evolutionCycle++;
if (concepts.size() < 2) return;
int battlesPerCycle = Math.min(concepts.size(), 5);
for (int i = 0; i < battlesPerCycle; i++) {
runBattle();
}
concepts.sort(Comparator.comparingDouble(c -> -c.fitness));
int survivalCount = (int) Math.ceil(concepts.size() * PHI_INVERSE);
survivalCount = Math.max(2, survivalCount);
while (concepts.size() > survivalCount) {
concepts.remove(concepts.size() - 1);
}
List<CodeConcept> offspring = new std::vector<>();
if (concepts.size() >= 2) {
CodeConcept child = CodeConcept.crossover(concepts.get(0), concepts.get(1));
offspring.add(child);
}
for (CodeConcept c : concepts) {
if (rng.nextDouble() < 0.3 && offspring.size() < 3) {
offspring.add(c.mutate());
}
}
concepts.addAll(offspring);
totalConceptsGenerated += offspring.size();
updateChampion();
}
private void updateChampion() {
if (concepts.isEmpty()) return;
CodeConcept best = concepts.get(0);
for (CodeConcept c : concepts) {
if (c.fitness > best.fitness) {
best = c;
}
}
champion = best;
}
public CodeConcept getChampion() { return champion; }
public List<CodeConcept> getConcepts() { return Collections.unmodifiableList(concepts); }
public List<BattleRecord> getBattleHistory() { return Collections.unmodifiableList(battleHistory); }
public int getTotalBattles() { return totalBattles; }
public int getEvolutionCycle() { return evolutionCycle; }
public int getConceptCount() { return concepts.size(); }
public int getTotalConceptsGenerated() { return totalConceptsGenerated; }
public List<BattleRecord> getLastNBattles(int n) {
int start = Math.max(0, battleHistory.size() - n);
return battleHistory.subList(start, battleHistory.size());
}
public double getAverageFitness() {
if (concepts.isEmpty()) return 0;
double sum = 0;
for (CodeConcept c : concepts) sum += c.fitness;
return sum / concepts.size();
}
public int[] getRoleDistribution() {
int[] counts = new int[AntRole.values().length];
for (CodeConcept c : concepts) {
counts[c.creatorRole.ordinal()]++;
}
return counts;
}
public std::string getArenaStatus() {
return std::string.format("Arena[concepts=%d, battles=%d, cycles=%d, avgFit=%.3f, champion=%s]",
concepts.size(), totalBattles, evolutionCycle, getAverageFitness(),
champion != null ? champion.hash.substring(0, 8) : "none");
}
}
