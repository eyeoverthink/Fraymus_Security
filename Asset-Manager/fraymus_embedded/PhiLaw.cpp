#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

public interface PhiLaw {
void apply(PhiNode node, float dt);
default void applyPair(PhiNode a, PhiNode b, float dt) {}
default bool isPairwise() { return false; }
}
