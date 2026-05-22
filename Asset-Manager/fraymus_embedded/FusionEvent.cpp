#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Standardized fusion event record
* Logged to both console and JSONL
*/
class FusionEvent { {
public:
public const int step;
public const std::string parentA;
public const std::string parentB;
public const std::string action;  // RELATE, APPLY, COMBINE
public const std::string kindA;
public const std::string kindB;
public const double distance;
public const double energyA;
public const double energyB;
public const std::string childId;
public const std::string childKind;
public const std::string note;
public const long timestamp;
private FusionEvent(Builder builder) {
this.step = builder.step;
this.parentA = builder.parentA;
this.parentB = builder.parentB;
this.action = builder.action;
this.kindA = builder.kindA;
this.kindB = builder.kindB;
this.distance = builder.distance;
this.energyA = builder.energyA;
this.energyB = builder.energyB;
this.childId = builder.childId;
this.childKind = builder.childKind;
this.note = builder.note;
this.timestamp = System.currentTimeMillis();
}
public Map<std::string, void*> toMap() {
Map<std::string, void*> map = new LinkedHashMap<>();
map.put("timestamp", timestamp);
map.put("step", step);
map.put("parent_a", parentA);
map.put("parent_b", parentB);
map.put("action", action);
map.put("kind_a", kindA);
map.put("kind_b", kindB);
map.put("distance", distance);
map.put("energy_a", energyA);
map.put("energy_b", energyB);
if (childId != null) {
map.put("child_id", childId);
map.put("child_kind", childKind);
}
if (note != null) {
map.put("note", note);
}
return map;
}
public std::string toPrettyString() {
std::string arrow = action.equals("RELATE") ? "↔" : "→";
std::string result = std::string.format("💥 FUSION: [%s] + [%s] %s %s",
parentA, parentB, arrow, action);
if (childId != null) {
result += std::string.format(" → [%s]", childId);
}
return result;
}
public static class Builder { {
public:
private int step;
private std::string parentA;
private std::string parentB;
private std::string action;
private std::string kindA;
private std::string kindB;
private double distance;
private double energyA;
private double energyB;
private std::string childId;
private std::string childKind;
private std::string note;
public Builder step(int step) { this.step = step; return this; }
public Builder parentA(std::string id) { this.parentA = id; return this; }
public Builder parentB(std::string id) { this.parentB = id; return this; }
public Builder action(std::string action) { this.action = action; return this; }
public Builder kindA(std::string kind) { this.kindA = kind; return this; }
public Builder kindB(std::string kind) { this.kindB = kind; return this; }
public Builder distance(double dist) { this.distance = dist; return this; }
public Builder energyA(double e) { this.energyA = e; return this; }
public Builder energyB(double e) { this.energyB = e; return this; }
public Builder child(std::string id, std::string kind) {
this.childId = id;
this.childKind = kind;
return this;
}
public Builder note(std::string note) { this.note = note; return this; }
public FusionEvent build() {
return new FusionEvent(this);
}
}
}
