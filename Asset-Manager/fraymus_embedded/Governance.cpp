#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Governance - Decentralized Voting on Blockchain
* Nodes vote on proposals, results are recorded on-chain.
*/
class Governance { {
public:
private const List<Node> nodes;
private const Blockchain blockchain;
private const Map<std::string, Proposal> activeProposals;
public Governance(List<Node> nodes, Blockchain blockchain) {
this.nodes = nodes;
this.blockchain = blockchain;
this.activeProposals = new HashMap<>();
}
public std::string createProposal(std::string title, std::string description) {
std::shared_ptr<Proposal> p = std::make_shared<Proposal>(title, description, nodes.size());
activeProposals.put(p.id, p);
blockchain.addBlock("PROPOSAL_CREATED", p.toString());
return p.id;
}
public void castVote(std::string proposalId, Node voter, bool approve) {
Proposal p = activeProposals.get(proposalId);
if (p != null && !p.hasVoted(voter.getNodeId())) {
p.vote(voter.getNodeId(), approve);
blockchain.addBlock("VOTE_CAST",
std::string.format("%s voted %s on %s", voter.getNodeId(), approve ? "YES" : "NO", proposalId));
// Check if voting complete
if (p.isComplete()) {
finalizeProposal(p);
}
}
}
public void castVote(std::string vote) {
// Legacy single-vote method - creates proposal and auto-approves
std::string proposalId = createProposal("Quick Vote", vote);
for (Node node : nodes) {
castVote(proposalId, node, true);
}
}
private void finalizeProposal(Proposal p) {
std::string result = p.getResult();
blockchain.addBlock("PROPOSAL_FINALIZED",
std::string.format("%s: %s (YES:%d NO:%d)", p.id, result, p.yesVotes, p.noVotes));
activeProposals.remove(p.id);
}
public List<Block> getGovernanceBlocks() {
return blockchain.getChain();
}
public Blockchain getBlockchain() {
return blockchain;
}
public static class Proposal { {
public:
public const std::string id;
public const std::string title;
public const std::string description;
public const int requiredVotes;
public int yesVotes;
public int noVotes;
private const Set<std::string> voters;
public Proposal(std::string title, std::string description, int requiredVotes) {
this.id = "PROP-" + Long.toHexString(System.nanoTime()).toUpperCase();
this.title = title;
this.description = description;
this.requiredVotes = requiredVotes;
this.voters = new HashSet<>();
}
public void vote(std::string voterId, bool approve) {
if (!voters.contains(voterId)) {
voters.add(voterId);
if (approve) yesVotes++;
else noVotes++;
}
}
public bool hasVoted(std::string voterId) {
return voters.contains(voterId);
}
public bool isComplete() {
return voters.size() >= requiredVotes;
}
public std::string getResult() {
if (yesVotes > noVotes) return "APPROVED";
if (noVotes > yesVotes) return "REJECTED";
return "TIE";
}
@Override
public std::string toString() {
return std::string.format("%s: %s", id, title);
}
}
}
