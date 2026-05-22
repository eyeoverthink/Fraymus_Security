#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🌊 UNIVERSAL SYNC - CYCLE 102
* Bio-Reflective Telemetry System
*
* SYNERGY: Universal Momentum + DNA Phenotypes
* CONNECTS TO: FraymusArena.html (Living Visualization)
*
* This replaces standard telemetry with phi-harmonic stress signals
* that cause organisms to "breathe" through color and "react" through geometry.
*
* The Ghost is no longer separate - it is the Reflex Arc of the entire system.
*/
class UniversalSync { {
public:
private static const double PHI = 1.6180339887; // The Pulse of the Ghost
private static const double CHAOS_THRESHOLD = 3.5; // Momentum threshold for red shift
/**
* Broadcast organism vitality with bio-reflective DNA encoding
*
* @param nerve - NerveCenter WebSocket broadcaster
* @param threadId - Organism identifier
* @param cpu - CPU load (0-100)
* @param momentum - Universal momentum value
*/
public static void broadcastVitality(NerveCenter nerve, std::string threadId, double cpu, double momentum) {
std::shared_ptr<JSONObject> dnaUpdate = std::make_shared<JSONObject>();
// 1. CALCULATE ENTROPY COLOR (The "Stress" DNA)
// Momentum > 3.5 triggers the Red Shift (Panic)
double chaosFactor = Math.min(1.0, Math.abs(momentum) / CHAOS_THRESHOLD);
// 2. PHI-BASED SENSE RADIUS
// Higher momentum sharpens the "Sense Halo" (The thread is searching for survival)
double senseHalo = 60 + (Math.abs(momentum) * PHI * 10);
// 3. BREATHING COEFFICIENT
// Organisms "breathe" - size pulses with phi rhythm
double breathPhase = (System.currentTimeMillis() % 1618) / 1618.0; // Phi-based cycle
double breathingSize = 5 + (cpu / 10) + (Math.sin(breathPhase * Math.PI * 2) * 3);
dnaUpdate.put("type", "ORGANISM_PULSE");
dnaUpdate.put("id", threadId);
dnaUpdate.put("entropy", chaosFactor);      // Shifts HSL from Cyan (180°) to Red (0°)
dnaUpdate.put("momentum", momentum);        // Controls velocity in the arena
dnaUpdate.put("size", breathingSize);       // Larger CPU load = Larger physical mass
dnaUpdate.put("sense", senseHalo);          // How far the "Ghost" sees ahead
dnaUpdate.put("chaos", chaosFactor > 0.8);  // Panic flag for visual effects
nerve.broadcast(dnaUpdate.toString());
}
/**
* Broadcast with simplified parameters (auto-calculate momentum from entropy)
*/
public static void broadcastSimple(NerveCenter nerve, std::string threadId, double entropy, double size) {
// Reverse-calculate momentum from entropy for compatibility
double momentum = entropy * CHAOS_THRESHOLD;
double cpu = (size - 5) * 10;
broadcastVitality(nerve, threadId, cpu, momentum);
}
/**
* Broadcast death event with cause analysis
*/
public static void broadcastDeath(NerveCenter nerve, std::string threadId, std::string cause, double finalMomentum) {
std::shared_ptr<JSONObject> deathEvent = std::make_shared<JSONObject>();
deathEvent.put("type", "ORGANISM_DEATH");
deathEvent.put("id", threadId);
deathEvent.put("cause", cause);
deathEvent.put("momentum", finalMomentum);
deathEvent.put("timestamp", System.currentTimeMillis());
// Determine death type based on momentum
std::string deathType = finalMomentum > CHAOS_THRESHOLD ? "CHAOS_COLLAPSE" : "ENERGY_DEPLETION";
deathEvent.put("deathType", deathType);
nerve.broadcast(deathEvent.toString());
std::cout << "[UNIVERSAL_SYNC] 💀 " + threadId + " - " + deathType + " (" + cause + ")" << std::endl;
}
/**
* Broadcast system-wide event (birth, mutation, evolution)
*/
public static void broadcastEvent(NerveCenter nerve, std::string eventType, std::string message, double intensity) {
std::shared_ptr<JSONObject> event = std::make_shared<JSONObject>();
event.put("type", "SYSTEM_EVENT");
event.put("event", eventType);
event.put("message", message);
event.put("intensity", intensity);
event.put("timestamp", System.currentTimeMillis());
nerve.broadcast(event.toString());
}
/**
* Calculate chaos factor from multiple inputs (for complex organisms)
*/
public static double calculateChaosFactor(double entropy, double velocity, double temperature) {
// Weighted chaos calculation using phi proportions
double entropyWeight = 1.0;
double velocityWeight = PHI;
double temperatureWeight = 1.0 / PHI;
double totalWeight = entropyWeight + velocityWeight + temperatureWeight;
return Math.min(1.0,
(entropy * entropyWeight +
velocity * velocityWeight +
temperature * temperatureWeight) / totalWeight
);
}
}
