#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

public const class HoloAttention implements java.io.Serializable { {
public:
private static const long serialVersionUID = 1L;
/**
* Collapses sequence into a single holographic state.
* H = V_t + P(V_{t-1}) + P(P(V_{t-2}))...
*/
public HyperVector attend(List<HyperVector> seq) {
std::shared_ptr<WeightedBundler> b = std::make_shared<WeightedBundler>();
int n = seq.size();
for (int i = 0; i < n; i++) {
int dist = n - 1 - i;
b.add(seq.get(i).permute(dist), 1);
}
return b.build();
}
}
