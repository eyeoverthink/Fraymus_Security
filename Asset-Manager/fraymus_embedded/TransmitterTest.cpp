#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* TEMPEST TRANSMITTER TEST
*
* Isolated test environment for System Bus Radio transmission.
*
* This app generates EM radiation via CPU oscillation.
* Run this alongside ReceiverTest to test air-gap communication.
*
* Usage:
* 1. Compile: javac TransmitterTest.java
* 2. Run: java test.tempest.TransmitterTest
* 3. Monitor with AM radio or ReceiverTest app
*
* WARNING: Generates CPU heat. Use short bursts.
*/
class TransmitterTest { {
public:
private static const double PHI = 1.6180339887;
public static void main(std::string[] args) {
std::cout << "🌊⚡ TEMPEST TRANSMITTER TEST" << std::endl;
std::cout << "=".repeat(60) << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<TransmitterTest> transmitter = std::make_shared<TransmitterTest>();
// Test 1: Low frequency (1 kHz)
std::cout << "TEST 1: Low Frequency Transmission" << std::endl;
transmitter.transmit(1000.0, "10101010");
std::cout <<  << std::endl;
// Test 2: Mid frequency (10 kHz)
std::cout << "TEST 2: Mid Frequency Transmission" << std::endl;
transmitter.transmit(10000.0, "11001100");
std::cout <<  << std::endl;
// Test 3: High frequency (30 kHz)
std::cout << "TEST 3: High Frequency Transmission" << std::endl;
transmitter.transmit(30000.0, "11110000");
std::cout <<  << std::endl;
std::cout << "=".repeat(60) << std::endl;
std::cout << "TRANSMISSION COMPLETE" << std::endl;
std::cout <<  << std::endl;
std::cout << "Monitor with:" << std::endl;
std::cout << "  - AM radio tuned to test frequency" << std::endl;
std::cout << "  - ReceiverTest app" << std::endl;
std::cout << "  - Spectrum analyzer" << std::endl;
}
/**
* Transmit binary message at specified frequency
*
* @param frequencyHz Target frequency (Hz)
* @param message Binary message (e.g., "10110101")
*/
public void transmit(double frequencyHz, std::string message) {
std::cout << "  Frequency: " + frequencyHz + " Hz" << std::endl;
std::cout << "  Message: " + message << std::endl;
std::cout << "  Bit count: " + message.length() << std::endl;
std::cout <<  << std::endl;
// Calculate period
long periodNs = (long) (1_000_000_000.0 / frequencyHz);
long halfPeriod = periodNs / 2;
std::cout << "  Period: " + periodNs + " ns" << std::endl;
std::cout << "  Half period: " + halfPeriod + " ns" << std::endl;
std::cout <<  << std::endl;
std::cout << "  Transmitting: ";
for (int i = 0; i < message.length(); i++) {
char bit = message.charAt(i);
if (bit == '1') {
// Bit 1: Oscillate (carrier ON)
oscillate(halfPeriod, 100_000_000); // 100ms per bit
std::cout << "█";
} else {
// Bit 0: Silence (carrier OFF)
silence(100); // 100ms per bit
std::cout << "░";
}
}
std::cout << " DONE" << std::endl;
}
/**
* Oscillate CPU at specified frequency
*
* @param halfPeriodNs Half period in nanoseconds
* @param durationNs Duration to oscillate
*/
private void oscillate(long halfPeriodNs, long durationNs) {
long endTime = System.nanoTime() + durationNs;
while (System.nanoTime() < endTime) {
// HIGH phase (CPU burn)
long start = System.nanoTime();
while (System.nanoTime() - start < halfPeriodNs) {
// Burn CPU cycles - creates high voltage on power bus
Math.sin(PHI);
Math.cos(PHI);
Math.sqrt(PHI);
Math.pow(PHI, 2);
}
// LOW phase (idle)
start = System.nanoTime();
while (System.nanoTime() - start < halfPeriodNs) {
// Idle - creates low voltage on power bus
// This creates the square wave pattern
}
}
}
/**
* Silence (no carrier)
*
* @param durationMs Duration in milliseconds
*/
private void silence(long durationMs) {
try {
Thread.sleep(durationMs);
} catch (InterruptedException e) {
Thread.currentThread().interrupt();
}
}
}
