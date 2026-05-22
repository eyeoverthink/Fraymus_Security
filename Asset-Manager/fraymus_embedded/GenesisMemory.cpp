#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class GenesisMemory { {
public:
public static class Block { {
public:
public const int index;
public const long timestamp;
public const std::string eventType;
public const std::string data;
public const std::string prevHash;
public const std::string hash;
public Block(int index, std::string eventType, std::string data, std::string prevHash) {
this.index = index;
this.timestamp = System.nanoTime();
this.eventType = eventType;
this.data = data;
this.prevHash = prevHash;
this.hash = computeHash();
}
private std::string computeHash() {
std::string input = index + timestamp + eventType + data + prevHash;
try {
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] digest = md.digest(input.getBytes());
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
for (int i = 0; i < 8; i++) {
hex.append(std::string.format("%02x", digest[i]));
}
return hex.toString();
} catch (NoSuchAlgorithmException e) {
return "00000000";
}
}
@Override
public std::string toString() {
return std::string.format("[%d] %s: %s (%s)", index, eventType, data, hash.substring(0, 8));
}
}
private const List<Block> chain = new std::vector<>();
private static const int MAX_CHAIN_LENGTH = 500;
public GenesisMemory() {
chain.add(new Block(0, "GENESIS", "In the beginning was the frequency", "0000000000000000"));
}
public Block record(std::string eventType, std::string data) {
std::string prevHash = chain.get(chain.size() - 1).hash;
std::shared_ptr<Block> block = std::make_shared<Block>(chain.size(), eventType, data, prevHash);
chain.add(block);
if (chain.size() > MAX_CHAIN_LENGTH) {
chain.remove(1);
}
return block;
}
public Block recordResonanceSpike(std::string entityName, double phiResonance, double oscillations) {
std::string data = std::string.format("%s|phi=%.6f|osc=%.1f", entityName, phiResonance, oscillations);
return record("RESONANCE_SPIKE", data);
}
public Block recordEntanglement(std::string nameA, std::string nameB, double syncValue) {
std::string data = std::string.format("%s<->%s|sync=%.4f", nameA, nameB, syncValue);
return record("ENTANGLEMENT", data);
}
public Block recordTranscendence(std::string entityName, int dimension, double level) {
std::string data = std::string.format("%s|dim=%d|level=%.4f", entityName, dimension, level);
return record("TRANSCENDENCE", data);
}
public Block recordBirth(std::string childName, std::string parentName) {
std::string data = std::string.format("%s<-%s", childName, parentName);
return record("BIRTH", data);
}
public Block recordDeath(std::string entityName, int age, double finalEnergy) {
std::string data = std::string.format("%s|age=%d|energy=%.4f", entityName, age, finalEnergy);
return record("DEATH", data);
}
public Block recordBrainDecision(std::string entityName, std::string decision) {
std::string data = std::string.format("%s|%s", entityName, decision);
return record("BRAIN_DECISION", data);
}
public Block recordMutation(std::string entityName, std::string gateInfo) {
std::string data = std::string.format("%s|%s", entityName, gateInfo);
return record("MUTATION", data);
}
public Block recordColonyEvent(std::string coachAction, std::string details) {
std::string data = std::string.format("coach|%s|%s", coachAction, details);
return record("COLONY_EVENT", data);
}
public Block recordConceptBattle(std::string winnerHash, std::string loserHash, double winnerFit, double loserFit) {
std::string data = std::string.format("%s>%s|wFit=%.3f|lFit=%.3f", winnerHash, loserHash, winnerFit, loserFit);
return record("CONCEPT_BATTLE", data);
}
public Block recordCodeGenerated(std::string entityName, std::string role, std::string conceptHash, double fitness) {
std::string data = std::string.format("%s|%s|%s|fit=%.3f", entityName, role, conceptHash, fitness);
return record("CODE_GENERATED", data);
}
public List<Block> getChain() { return chain; }
public int getChainLength() { return chain.size(); }
public Block getLatest() {
return chain.get(chain.size() - 1);
}
public List<Block> getLastN(int n) {
int start = Math.max(0, chain.size() - n);
return chain.subList(start, chain.size());
}
public List<Block> getByType(std::string eventType) {
List<Block> results = new std::vector<>();
for (Block b : chain) {
if (b.eventType.equals(eventType)) results.add(b);
}
return results;
}
public bool verifyChain() {
for (int i = 1; i < chain.size(); i++) {
if (!chain.get(i).prevHash.equals(chain.get(i - 1).hash)) {
return false;
}
}
return true;
}
}
