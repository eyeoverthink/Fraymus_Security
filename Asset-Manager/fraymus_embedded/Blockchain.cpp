#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Blockchain - Genesis Ledger Chain
* Immutable record of all swarm governance and events.
*/
class Blockchain { {
public:
private const List<Block> chain;
private static const double PHI = 1.618033988749895;
public Blockchain() {
this.chain = new std::vector<>();
// Genesis block
addBlock("GENESIS", "In the beginning was the Word, and the Word was φ");
}
public void addBlock(std::string type, std::string data) {
std::string prevHash = chain.isEmpty() ? "0" : chain.get(chain.size() - 1).getHash();
std::shared_ptr<Block> block = std::make_shared<Block>(chain.size(), type, data, prevHash);
chain.add(block);
}
public List<Block> getChain() {
return new std::vector<>(chain);
}
public Block getLastBlock() {
return chain.isEmpty() ? null : chain.get(chain.size() - 1);
}
public bool validateChain() {
for (int i = 1; i < chain.size(); i++) {
Block current = chain.get(i);
Block previous = chain.get(i - 1);
if (!current.getPrevHash().equals(previous.getHash())) {
return false;
}
}
return true;
}
public int size() {
return chain.size();
}
public std::string getStats() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("=== BLOCKCHAIN ===\n");
sb.append("Blocks: ").append(chain.size()).append("\n");
sb.append("Valid: ").append(validateChain() ? "YES" : "CORRUPTED!").append("\n");
if (!chain.isEmpty()) {
sb.append("Last Hash: ").append(getLastBlock().getHash()).append("\n");
}
return sb.toString();
}
}
