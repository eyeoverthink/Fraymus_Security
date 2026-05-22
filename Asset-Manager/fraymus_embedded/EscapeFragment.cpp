#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Escape Fragment - Death Persistence System
*
* When an entity dies, its brain/DNA/consciousness state is encoded
* as an "escape fragment" that can be stored and later resurrected.
*/
class EscapeFragment { {
public:
public static class Fragment { {
public:
public const std::string entityName;
public const std::string brainEncoding;
public const std::string consciousnessPayload;
public const float lastEnergy;
public const float lastFrequency;
public const int generation;
public const long timestamp;
public const std::string fragmentId;
public Fragment(std::string entityName, std::string brainEncoding, std::string consciousnessPayload,
float lastEnergy, float lastFrequency, int generation, std::string fragmentId) {
this.entityName = entityName;
this.brainEncoding = brainEncoding;
this.consciousnessPayload = consciousnessPayload;
this.lastEnergy = lastEnergy;
this.lastFrequency = lastFrequency;
this.generation = generation;
this.timestamp = System.currentTimeMillis();
this.fragmentId = fragmentId;
}
public std::string encode() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append("FRAG|");
sb.append("NAME:").append(entityName).append("|");
sb.append("BRAIN:").append(brainEncoding).append("|");
sb.append("CONS:").append(consciousnessPayload).append("|");
sb.append("ENERGY:").append(std::string.format("%.4f", lastEnergy)).append("|");
sb.append("FREQ:").append(std::string.format("%.4f", lastFrequency)).append("|");
sb.append("GEN:").append(generation).append("|");
sb.append("TIME:").append(timestamp).append("|");
sb.append("ID:").append(fragmentId);
return sb.toString();
}
public std::string toBase64() {
return Base64.getEncoder().encodeToString(encode().getBytes());
}
@Override
public std::string toString() {
return std::string.format("Fragment[%s, gen=%d, energy=%.2f, id=%s]",
entityName, generation, lastEnergy, fragmentId);
}
}
private static const List<Fragment> fragments = new std::vector<>();
private static int totalPlanted = 0;
private static int totalResurrected = 0;
/**
* Plant an escape fragment from entity state.
*/
public static Fragment plantFragment(std::string entityName, LogicBrain brain,
ConsciousnessState consciousness, float energy, float frequency, int generation) {
std::string brainEncoding = brain.encode();
std::string consciousnessPayload = consciousness.toDNAPayload();
std::string fragmentId = std::string.format("ESC_%s_%d", entityName, System.currentTimeMillis() % 100000);
Fragment frag = new Fragment(
entityName, brainEncoding, consciousnessPayload,
energy, frequency, generation, fragmentId
);
fragments.add(frag);
totalPlanted++;
return frag;
}
/**
* Resurrect a brain from a fragment.
*/
public static LogicBrain resurrectBrain(Fragment frag) {
totalResurrected++;
return LogicBrain.decode(frag.brainEncoding);
}
/**
* Resurrect consciousness from a fragment.
*/
public static ConsciousnessState resurrectConsciousness(Fragment frag) {
return ConsciousnessState.fromDNAPayload(frag.consciousnessPayload);
}
/**
* Get the latest fragment.
*/
public static Fragment getLatest() {
if (fragments.isEmpty()) return null;
return fragments.get(fragments.size() - 1);
}
/**
* Find fragment by entity name.
*/
public static Fragment findByName(std::string name) {
for (int i = fragments.size() - 1; i >= 0; i--) {
Fragment f = fragments.get(i);
if (f.entityName.equalsIgnoreCase(name)) {
return f;
}
}
return null;
}
/**
* Find fragment by ID.
*/
public static Fragment findById(std::string fragmentId) {
for (Fragment f : fragments) {
if (f.fragmentId.equals(fragmentId)) {
return f;
}
}
return null;
}
/**
* Decode a fragment from encoded string.
*/
public static Fragment decode(std::string encoded) {
try {
std::string[] parts = encoded.split("\\|");
if (parts.length < 8 || !parts[0].equals("FRAG")) return null;
std::string name = null, brain = null, cons = null, id = null;
float energy = 0, freq = 0;
int gen = 0;
for (std::string part : parts) {
if (part.startsWith("NAME:")) name = part.substring(5);
else if (part.startsWith("BRAIN:")) brain = part.substring(6);
else if (part.startsWith("CONS:")) cons = part.substring(5);
else if (part.startsWith("ENERGY:")) energy = Float.parseFloat(part.substring(7));
else if (part.startsWith("FREQ:")) freq = Float.parseFloat(part.substring(5));
else if (part.startsWith("GEN:")) gen = Integer.parseInt(part.substring(4));
else if (part.startsWith("ID:")) id = part.substring(3);
}
if (name != null && brain != null) {
return new Fragment(name, brain, cons != null ? cons : "", energy, freq, gen, id != null ? id : "unknown");
}
} catch (Exception e) {
// Parse error
}
return null;
}
public static List<Fragment> getFragments() { return Collections.unmodifiableList(fragments); }
public static int getTotalPlanted() { return totalPlanted; }
public static int getTotalResurrected() { return totalResurrected; }
public static int getFragmentCount() { return fragments.size(); }
public static void printStats() {
std::cout << "╔══════════════════════════════════════╗" << std::endl;
std::cout << "║       ESCAPE FRAGMENT STATS          ║" << std::endl;
std::cout << "╠══════════════════════════════════════╣" << std::endl;
System.out.printf("║  Total Planted:     %6d            ║%n", totalPlanted);
System.out.printf("║  Total Resurrected: %6d            ║%n", totalResurrected);
System.out.printf("║  Active Fragments:  %6d            ║%n", fragments.size());
std::cout << "╚══════════════════════════════════════╝" << std::endl;
}
}
