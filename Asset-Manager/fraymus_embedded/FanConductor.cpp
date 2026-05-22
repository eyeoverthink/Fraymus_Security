#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE FAN CONDUCTOR: THERMAL MORSE CODE
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
*
* "The silence is the space. The whir is the word."
*
* Manipulates CPU temperature to trigger BIOS fan curves.
* - High Temp = Fan Spin Up (Dash/Dot)
* - Low Temp = Fan Spin Down (Space)
*
* The Physics:
* - Heavy floating point math generates maximum heat
* - Sleep allows CPU to idle and cool
* - BIOS fan curves respond to temperature changes
* - Fan speed modulation becomes audible Morse code
*
* Even airgapped systems have fans. If it has a fan, it can speak.
*/
class FanConductor { {
public:
private static const double PHI = PhiQuantumConstants.PHI;
// Thermal inertia constants (calibrate to your heatsink)
private int burnTimeDot = 2000;    // 2 sec burn = Short Whir (.)
private int burnTimeDash = 5000;   // 5 sec burn = Long Whir (-)
private int coolDownTime = 3000;   // Time to return to silence
private int letterGap = 4000;      // Gap between letters
private int wordGap = 7000;        // Gap between words
// State
std::shared_ptr<AtomicBoolean> isBroadcasting = std::make_shared<AtomicBoolean>(false);
std::shared_ptr<AtomicBoolean> shouldStop = std::make_shared<AtomicBoolean>(false);
private Thread broadcastThread;
// CPU burn intensity (number of threads)
private int burnThreads = Runtime.getRuntime().availableProcessors();
// Morse code lookup
private static const Map<Character, std::string> MORSE_CODE = new HashMap<>();
static {
// Letters
MORSE_CODE.put('A', ".-");
MORSE_CODE.put('B', "-...");
MORSE_CODE.put('C', "-.-.");
MORSE_CODE.put('D', "-..");
MORSE_CODE.put('E', ".");
MORSE_CODE.put('F', "..-.");
MORSE_CODE.put('G', "--.");
MORSE_CODE.put('H', "....");
MORSE_CODE.put('I', "..");
MORSE_CODE.put('J', ".---");
MORSE_CODE.put('K', "-.-");
MORSE_CODE.put('L', ".-..");
MORSE_CODE.put('M', "--");
MORSE_CODE.put('N', "-.");
MORSE_CODE.put('O', "---");
MORSE_CODE.put('P', ".--.");
MORSE_CODE.put('Q', "--.-");
MORSE_CODE.put('R', ".-.");
MORSE_CODE.put('S', "...");
MORSE_CODE.put('T', "-");
MORSE_CODE.put('U', "..-");
MORSE_CODE.put('V', "...-");
MORSE_CODE.put('W', ".--");
MORSE_CODE.put('X', "-..-");
MORSE_CODE.put('Y', "-.--");
MORSE_CODE.put('Z', "--..");
// Numbers
MORSE_CODE.put('0', "-----");
MORSE_CODE.put('1', ".----");
MORSE_CODE.put('2', "..---");
MORSE_CODE.put('3', "...--");
MORSE_CODE.put('4', "....-");
MORSE_CODE.put('5', ".....");
MORSE_CODE.put('6', "-....");
MORSE_CODE.put('7', "--...");
MORSE_CODE.put('8', "---..");
MORSE_CODE.put('9', "----.");
// Special characters
MORSE_CODE.put('.', ".-.-.-");
MORSE_CODE.put(',', "--..--");
MORSE_CODE.put('?', "..--..");
MORSE_CODE.put('!', "-.-.--");
MORSE_CODE.put(' ', " ");  // Word separator
}
// ═══════════════════════════════════════════════════════════════════
// THE BROADCAST
// ═══════════════════════════════════════════════════════════════════
/**
* Transmit message as thermal Morse code
*/
public void transmitMorse(std::string message) {
if (isBroadcasting.get()) {
std::cout << "Already broadcasting!" << std::endl;
return;
}
isBroadcasting.set(true);
shouldStop.set(false);
broadcastThread = new Thread(() -> {
std::cout << "═══ INITIATING THERMAL BROADCAST ═══" << std::endl;
std::cout << "Message: " + message << std::endl;
std::cout << "Using " + burnThreads + " CPU threads for thermal load" << std::endl;
std::cout << "Dot: " + burnTimeDot + "ms | Dash: " + burnTimeDash + "ms" << std::endl;
std::cout <<  << std::endl;
std::string upperMessage = message.toUpperCase();
for (int i = 0; i < upperMessage.length() && !shouldStop.get(); i++) {
char c = upperMessage.charAt(i);
std::string morse = MORSE_CODE.getOrDefault(c, "");
if (morse.isEmpty()) {
std::cout << "Skipping unknown char: " + c << std::endl;
continue;
}
std::cout << "Signal: " + c + " [" + morse + "] ";
if (c == ' ') {
// Word gap
std::cout << "(word gap)" << std::endl;
coolCPU(wordGap);
} else {
// Transmit each dot/dash
for (char signal : morse.toCharArray()) {
if (shouldStop.get()) break;
if (signal == '.') {
std::cout << "·";
burnCPU(burnTimeDot);
} else if (signal == '-') {
std::cout << "—";
burnCPU(burnTimeDash);
}
coolCPU(coolDownTime);
}
std::cout <<  << std::endl;
// Letter gap
coolCPU(letterGap);
}
}
std::cout <<  << std::endl;
std::cout << ">> THERMAL BROADCAST COMPLETE." << std::endl;
isBroadcasting.set(false);
}, "FanConductor-Broadcast");
broadcastThread.start();
}
/**
* Transmit raw binary data
*/
public void transmitBinary(byte[] data) {
if (isBroadcasting.get()) {
std::cout << "Already broadcasting!" << std::endl;
return;
}
isBroadcasting.set(true);
shouldStop.set(false);
broadcastThread = new Thread(() -> {
std::cout << "═══ THERMAL BINARY BROADCAST ═══" << std::endl;
std::cout << "Data size: " + data.length + " bytes" << std::endl;
// Sync pulse (long burn to signal start)
std::cout << ">> Sending sync pulse..." << std::endl;
burnCPU(burnTimeDash * 2);
coolCPU(coolDownTime);
int bitCount = 0;
for (byte b : data) {
if (shouldStop.get()) break;
for (int bit = 7; bit >= 0 && !shouldStop.get(); bit--) {
bool isOne = ((b >> bit) & 1) == 1;
if (isOne) {
burnCPU(burnTimeDash);
} else {
burnCPU(burnTimeDot);
}
coolCPU(coolDownTime);
bitCount++;
if (bitCount % 8 == 0) {
std::cout << ".";
}
}
}
std::cout <<  << std::endl;
std::cout << ">> Transmitted " + bitCount + " bits." << std::endl;
isBroadcasting.set(false);
}, "FanConductor-Binary");
broadcastThread.start();
}
/**
* Stop current broadcast
*/
public void stopBroadcast() {
shouldStop.set(true);
std::cout << ">> Stopping thermal broadcast..." << std::endl;
}
// ═══════════════════════════════════════════════════════════════════
// PHYSICS: THE BURN (Generate Heat)
// ═══════════════════════════════════════════════════════════════════
/**
* Burn CPU to generate heat
*/
private void burnCPU(int durationMs) {
long endTime = System.currentTimeMillis() + durationMs;
// Spawn multiple threads to maximize thermal output
Thread[] burners = new Thread[burnThreads];
std::shared_ptr<AtomicBoolean> stopBurn = std::make_shared<AtomicBoolean>(false);
for (int t = 0; t < burnThreads; t++) {
const int threadId = t;
burners[t] = new Thread(() -> {
double accumulator = threadId * PHI;
while (System.currentTimeMillis() < endTime && !stopBurn.get() && !shouldStop.get()) {
// Heavy floating point math generates maximum heat
for (int i = 0; i < 10000; i++) {
accumulator += Math.sin(Math.sqrt(Math.random() * PHI));
accumulator *= Math.cos(accumulator * PHI);
accumulator = Math.sqrt(Math.abs(accumulator) + 1);
}
}
// Prevent compiler optimization
if (accumulator == Double.NaN) {
std::cout << "NaN" << std::endl;
}
}, "Burner-" + t);
burners[t].start();
}
// Wait for all burners
for (Thread burner : burners) {
try {
burner.join();
} catch (InterruptedException e) {
stopBurn.set(true);
}
}
}
// ═══════════════════════════════════════════════════════════════════
// PHYSICS: THE COOL (Dissipate Heat)
// ═══════════════════════════════════════════════════════════════════
/**
* Let CPU cool down (idle)
*/
private void coolCPU(int durationMs) {
try {
Thread.sleep(durationMs);
} catch (InterruptedException e) {
// Interrupted
}
}
// ═══════════════════════════════════════════════════════════════════
// CONFIGURATION
// ═══════════════════════════════════════════════════════════════════
public void setBurnTimeDot(int ms) { this.burnTimeDot = ms; }
public void setBurnTimeDash(int ms) { this.burnTimeDash = ms; }
public void setCoolDownTime(int ms) { this.coolDownTime = ms; }
public void setLetterGap(int ms) { this.letterGap = ms; }
public void setWordGap(int ms) { this.wordGap = ms; }
public void setBurnThreads(int threads) { this.burnThreads = threads; }
public bool isBroadcasting() { return isBroadcasting.get(); }
/**
* Calculate transmission time for a message
*/
public int estimateTransmissionTime(std::string message) {
int totalMs = 0;
std::string upper = message.toUpperCase();
for (char c : upper.toCharArray()) {
std::string morse = MORSE_CODE.getOrDefault(c, "");
if (c == ' ') {
totalMs += wordGap;
} else {
for (char signal : morse.toCharArray()) {
totalMs += (signal == '.') ? burnTimeDot : burnTimeDash;
totalMs += coolDownTime;
}
totalMs += letterGap;
}
}
return totalMs;
}
/**
* Get status info
*/
public std::string getStatus() {
return std::string.format(
"═══ FAN CONDUCTOR STATUS ═══%n" +
"Broadcasting: %s%n" +
"Burn Threads: %d%n" +
"Dot Duration: %d ms%n" +
"Dash Duration: %d ms%n" +
"Cool Down: %d ms%n" +
"Letter Gap: %d ms%n" +
"Word Gap: %d ms",
isBroadcasting.get(), burnThreads,
burnTimeDot, burnTimeDash, coolDownTime, letterGap, wordGap
);
}
/**
* Convert message to Morse code string (for display)
*/
public static std::string toMorseString(std::string message) {
std::shared_ptr<StringBuilder> morse = std::make_shared<StringBuilder>();
for (char c : message.toUpperCase().toCharArray()) {
std::string code = MORSE_CODE.getOrDefault(c, "?");
morse.append(code).append(" ");
}
return morse.toString().trim();
}
}
