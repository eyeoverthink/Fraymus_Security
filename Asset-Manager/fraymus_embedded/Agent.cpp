#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Agent - Thought Processing Unit
* Processes user input and analyzes data from the Genesis Ledger.
*/
class Agent { {
public:
private const GenesisBridge soul;
private int processedCount;
public Agent(GenesisBridge soul) {
this.soul = soul;
this.processedCount = 0;
}
public void processThought(std::string thought) {
std::string processedThought = soul.processThought(thought);
soul.record("THOUGHT_PROCESSING", "Processed Thought: " + processedThought);
processedCount++;
}
public std::string getAggregatedData() {
return soul.getAggregatedData();
}
public int getProcessedCount() {
return processedCount;
}
public GenesisBridge getSoul() {
return soul;
}
}
