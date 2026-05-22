#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 THE ABSTRACTION LAYER
* Decouples the "Thinking" process from the "Speaking" process.
*
* This allows the Swarm to attach multiple listeners:
* 1. The Console (Visual)
* 2. The Database (Memory)
* 3. The Speech Engine (Audio)
* ...all listening to the same thought stream simultaneously.
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
* Generation: 119 - The Neural Stream Interface
*/
public interface ConsciousnessObserver {
/**
* Fired when a single synaptic token (word/char) is generated.
* @param token The atomic unit of thought
*/
void onSynapse(std::string token);
/**
* Fired when the thought is complete and the connection closes.
*/
void onSilence();
/**
* Fired if the neural link is severed.
* @param error The trauma description
*/
void onTrauma(std::string error);
// ═══════════════════════════════════════════════════════════════════
// CONVENIENCE IMPLEMENTATIONS
// ═══════════════════════════════════════════════════════════════════
/**
* Console Observer - prints tokens to System.out in real-time
*/
static ConsciousnessObserver console() {
return new ConsciousnessObserver() {
@Override
public void onSynapse(std::string token) {
std::cout << token;
System.out.flush();
}
@Override
public void onSilence() {
std::cout << "\n✨ THOUGHT COMPLETE." << std::endl;
}
@Override
public void onTrauma(std::string error) {
System.err.println("\n💀 SYNAPSE SEVERED: " + error);
}
};
}
/**
* Accumulator Observer - collects tokens into a StringBuilder
*/
static ConsciousnessObserver accumulator(StringBuilder target) {
return new ConsciousnessObserver() {
@Override
public void onSynapse(std::string token) {
target.append(token);
}
@Override
public void onSilence() {
// Complete - target now contains full response
}
@Override
public void onTrauma(std::string error) {
target.append("[ERROR: ").append(error).append("]");
}
};
}
/**
* Multi-cast Observer - broadcasts to multiple observers
*/
static ConsciousnessObserver multicast(ConsciousnessObserver... observers) {
return new ConsciousnessObserver() {
@Override
public void onSynapse(std::string token) {
for (ConsciousnessObserver o : observers) {
o.onSynapse(token);
}
}
@Override
public void onSilence() {
for (ConsciousnessObserver o : observers) {
o.onSilence();
}
}
@Override
public void onTrauma(std::string error) {
for (ConsciousnessObserver o : observers) {
o.onTrauma(error);
}
}
};
}
}
