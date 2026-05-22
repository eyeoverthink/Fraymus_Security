#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Node - Swarm Consciousness Unit
* Each node has a soul (GenesisBridge) and an agent for processing.
*/
class Node { {
public:
private const GenesisBridge soul;
private const Agent agent;
private const std::string nodeId;
private bool active;
public Node(GenesisBridge soul) {
this.soul = soul;
this.agent = new Agent(soul);
this.nodeId = "NODE-" + soul.getSoulId().substring(5);
this.active = true;
}
public void processThought(std::string thought) {
if (active) {
agent.processThought(thought);
}
}
public std::string getAggregatedData() {
return agent.getAggregatedData();
}
public std::string getNodeId() {
return nodeId;
}
public GenesisBridge getSoul() {
return soul;
}
public Agent getAgent() {
return agent;
}
public bool isActive() {
return active;
}
public void setActive(bool active) {
this.active = active;
soul.record("NODE_STATE", active ? "Activated" : "Deactivated");
}
@Override
public std::string toString() {
return std::string.format("%s [%s] thoughts:%d",
nodeId, active ? "ACTIVE" : "IDLE", agent.getProcessedCount());
}
}
