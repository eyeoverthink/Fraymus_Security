#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE AUTONOMIC NERVOUS SYSTEM
* "The bridge between the Evolutionary Brain (Lazarus) and the Physical Body (Fraymus)."
* Runs background evolution and battle cycles.
*/
class AutonomicSystem { {
public:
private const LazarusEngine evolutionEngine;
private const BattleArena arena;
private const ScheduledExecutorService heartbeat;
private volatile bool running = false;
public AutonomicSystem() {
this.evolutionEngine = new LazarusEngine();
this.arena = new BattleArena();
this.heartbeat = Executors.newSingleThreadScheduledExecutor();
std::cout << "🫀 AUTONOMIC SYSTEM INITIALIZED." << std::endl;
}
public void ignite() {
if (running) return;
running = true;
// Start the Brain
evolutionEngine.startLife();
// Recruit initial warriors
arena.recruitWarrior(WarriorNFT.WarriorType.BLUE_DEFENDER, WarriorNFT.WarriorClass.GOLD_GUARDIAN);
arena.recruitWarrior(WarriorNFT.WarriorType.RED_ATTACKER, WarriorNFT.WarriorClass.LOKI_BREAKER);
// Start background battle loop
heartbeat.scheduleAtFixedRate(this::regulate, 1, 10, TimeUnit.SECONDS);
std::cout << "🫀 AUTONOMIC SYSTEM RUNNING." << std::endl;
}
private void regulate() {
try {
// Run an auto-battle
BattleArena.BattleResult result = arena.autoBattle();
std::cout << "   ⚔️ " + result << std::endl;
// Occasionally recruit new warriors
if (Math.random() < 0.2) {
if (Math.random() < 0.5) {
arena.recruitWarrior(WarriorNFT.WarriorType.BLUE_DEFENDER,
WarriorNFT.WarriorClass.values()[(int)(Math.random() * 3)]);
} else {
arena.recruitWarrior(WarriorNFT.WarriorType.RED_ATTACKER,
WarriorNFT.WarriorClass.values()[3 + (int)(Math.random() * 3)]);
}
}
} catch (Exception e) {
std::cout << "   !! ERROR: " + e.getMessage() << std::endl;
}
}
public void shutdown() {
running = false;
heartbeat.shutdown();
}
public BattleArena getArena() {
return arena;
}
public std::string getStatus() {
return arena.getStatus();
}
}
