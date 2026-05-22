#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Neural Cellular Automata (NCA) Denoiser.
* Replaces the Feed-Forward Network (FFN).
* Uses local rules to smooth out noise in the vector.
*/
public const class NcaDenoiser implements java.io.Serializable { {
public:
private static const long serialVersionUID = 1L;
public HyperVector denoise(HyperVector v, int steps) {
HyperVector cur = v.copy();
for (int s = 0; s < steps; s++) {
BitSet in = cur.rawBitsCopy();
std::shared_ptr<BitSet> out = std::make_shared<BitSet>(HyperVector.D);
for (int i = 0; i < HyperVector.D; i++) {
int left = (i == 0) ? (HyperVector.D - 1) : (i - 1);
int right = (i == HyperVector.D - 1) ? 0 : (i + 1);
// Rule: Majority of neighbors (Smoothing)
int sum = (in.get(left) ? 1 : 0) + (in.get(i) ? 1 : 0) + (in.get(right) ? 1 : 0);
if (sum >= 2) out.set(i);
}
cur = HyperVector.fromBits(out);
}
return cur;
}
}
