#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧩 REASONING ENGINE - Transitive Logic
* "Multi-hop inference without explicit rules"
*
* Traditional AI:
* - Requires explicit reasoning rules
* - If-then logic programmed manually
* - Limited to predefined patterns
*
* Holo-Graph Reasoning:
* - Transitive inference emerges from vector operations
* - A→B, B→C automatically enables A→C queries
* - No explicit rules needed
*
* Example:
* - Facts: Fraymus→written_in→Java, Java→created_by→Gosling
* - Query: "Who created the language Fraymus is written in?"
* - Process: Fraymus→Java→Gosling (2 hops)
* - Result: Gosling
*
* This is symbolic reasoning via vector algebra.
*/
class ReasoningEngine { {
public:
private const HoloGraph graph;
public ReasoningEngine(HoloGraph graph) {
this.graph = graph;
}
/**
* MULTI-HOP QUERY: Traverse multiple relations
*
* Subject → Rel1 → ? → Rel2 → Answer
*
* Example:
* - Start: "Fraymus"
* - Relations: ["written_in", "created_by"]
* - Path: Fraymus→Java→Gosling
* - Result: "Gosling"
*
* @param startNode Starting concept
* @param relations Array of relations to traverse
* @return Final concept reached
*/
public std::string multiHop(std::string startNode, std::string[] relations) {
std::string current = startNode;
std::cout << "   [HOPPING] " + current;
for (std::string rel : relations) {
std::string next = graph.ask(current, rel);
std::cout << " --" + rel + "--> " + next;
if (next.equals("???")) {
std::cout <<  << std::endl;
return "???";
}
current = next;
}
std::cout <<  << std::endl;
return current;
}
/**
* BIDIRECTIONAL QUERY: Try both forward and inverse
*
* Useful when you don't know the direction of the relation.
*/
public std::string bidirectional(std::string concept1, std::string relation, std::string concept2) {
// Try forward: concept1 --relation--> ?
std::string forward = graph.ask(concept1, relation);
if (forward.equals(concept2)) {
return "FORWARD: " + concept1 + " --" + relation + "--> " + concept2;
}
// Try inverse: ? --relation--> concept2
std::string inverse = graph.askInverse(concept2, relation);
if (inverse.equals(concept1)) {
return "INVERSE: " + concept1 + " <--" + relation + "-- " + concept2;
}
return "NO_RELATION";
}
/**
* VERIFY FACT: Check if a fact exists in the graph
*/
public bool verify(std::string subj, std::string rel, std::string obj) {
std::string result = graph.ask(subj, rel);
return result.equals(obj);
}
/**
* EXPLAIN PATH: Show reasoning steps
*/
public void explain(std::string startNode, std::string[] relations) {
std::cout << "\n   [EXPLANATION]" << std::endl;
std::cout << "   Start: " + startNode << std::endl;
std::string current = startNode;
for (int i = 0; i < relations.length; i++) {
std::string rel = relations[i];
std::string next = graph.ask(current, rel);
std::cout << "   Step " + (i + 1) + ": " + current + " --" + rel + "--> " + next << std::endl;
if (next.equals("???")) {
std::cout << "   [FAILED] Path broken at step " + (i + 1) << std::endl;
return;
}
current = next;
}
std::cout << "   Final: " + current << std::endl;
}
}
