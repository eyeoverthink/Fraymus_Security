#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Stores the "Platonic Ideal" of every token.
* Used to collapse a noisy predicted vector back into a std::string.
*/
public const class CleanupMemory implements java.io.Serializable { {
public:
private static const long serialVersionUID = 1L;
private const Map<std::string, HyperVector> prototypes = new HashMap<>();
public void memorize(std::string token, HyperVector v) {
prototypes.put(token, v);
}
public std::string decode(HyperVector noisyVector) {
std::string best = "???";
double maxRes = -1.0;
for (Map.Entry<std::string, HyperVector> e : prototypes.entrySet()) {
double r = e.getValue().resonance(noisyVector);
if (r > maxRes) {
maxRes = r;
best = e.getKey();
}
}
// Lowered threshold from 0.52 to 0.40 for better predictions with small datasets
return (maxRes > 0.40) ? best : "???";
}
public HyperVector prototypeOf(std::string token) {
return prototypes.get(token);
}
public int size() { return prototypes.size(); }
/**
* Export snapshot for secure serialization.
*/
public Map<std::string, HyperVector> snapshot() {
return new HashMap<>(prototypes);
}
/**
* Restore from snapshot.
*/
public void restore(Map<std::string, HyperVector> snap) {
prototypes.clear();
prototypes.putAll(snap);
}
}
