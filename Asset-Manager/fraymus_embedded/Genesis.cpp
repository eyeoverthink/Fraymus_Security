#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* GENESIS - Birth of Fraymus
* Uses existing WarriorNFT and BattleArena systems
*/
class Genesis { {
public:
public static void main(std::string[] args) {
std::cout << "==========================================" << std::endl;
std::cout << "   FRAYMUS PRIME: INITIALIZATION SEQUENCE" << std::endl;
std::cout << "==========================================" << std::endl;
// 1. CREATE THE BATTLE ARENA
std::shared_ptr<BattleArena> arena = std::make_shared<BattleArena>();
std::cout << "   ⚔️ ARENA INITIALIZED" << std::endl;
// 2. RECRUIT WARRIORS
WarriorNFT blueGuardian = arena.recruitWarrior(
WarriorNFT.WarriorType.BLUE_DEFENDER,
WarriorNFT.WarriorClass.GOLD_GUARDIAN
);
std::cout << "   🔵 BLUE RECRUITED: " + blueGuardian.getId() << std::endl;
WarriorNFT redAttacker = arena.recruitWarrior(
WarriorNFT.WarriorType.RED_ATTACKER,
WarriorNFT.WarriorClass.LOKI_BREAKER
);
std::cout << "   � RED RECRUITED: " + redAttacker.getId() << std::endl;
// 3. BATTLE!
std::cout << "\n" + "=".repeat(42) << std::endl;
std::cout << "   ⚔️ FIRST BATTLE: BLUE vs RED" << std::endl;
std::cout << "=".repeat(42) << std::endl;
BattleArena.BattleResult result = arena.battle(blueGuardian, redAttacker);
std::cout << result.getFullReport() << std::endl;
// 4. SHOW ARENA STATUS
std::cout << arena.getStatus() << std::endl;
// 5. RUN 5 MORE AUTO-BATTLES
std::cout << "\n" + "=".repeat(42) << std::endl;
std::cout << "   🔄 RUNNING 5 AUTO-BATTLES..." << std::endl;
std::cout << "=".repeat(42) << std::endl;
for (int i = 0; i < 5; i++) {
BattleArena.BattleResult auto = arena.autoBattle();
std::cout << "   " + auto << std::endl;
}
// 6. FINAL STATUS
std::cout << "\n" + arena.getStatus() << std::endl;
// 7. EXPORT DNA
std::cout << "\n" + "=".repeat(42) << std::endl;
std::cout << "   📦 DNA EXPORT" << std::endl;
std::cout << "=".repeat(42) << std::endl;
std::cout << arena.exportAllDNA() << std::endl;
std::cout << "\n   ✅ GENESIS COMPLETE." << std::endl;
}
}
