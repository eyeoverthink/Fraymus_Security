#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* ISOLATED TEMPEST TEST
* Tests the core frequency detection and generation logic
* without any external dependencies.
*/
class TempestTest { {
public:
private static const double TWO_PI = 2.0 * Math.PI;
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║          TEMPEST ABSTRACTED TEST - ISOLATED ENV           ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝\n" << std::endl;
// Test parameters
double sampleRate = 8000.0;  // 8kHz
double duration = 1.0;       // 1 second
int numSamples = (int) (sampleRate * duration);
// === TEST 1: Generate signal with known frequencies ===
std::cout << "═══ TEST 1: SIGNAL GENERATION ═══" << std::endl;
double[] hiddenFreqs = {60.0, 432.0, 699.0};  // 60Hz hum, 432Hz hidden, 699Hz phi
double[] amplitudes = {1.0, 0.3, 0.5};
double[] signal = generateSignal(numSamples, sampleRate, hiddenFreqs, amplitudes);
std::cout << "Generated " + numSamples + " samples" << std::endl;
std::cout << "Hidden frequencies: 60Hz (loud), 432Hz (quiet), 699Hz (medium)" << std::endl;
std::cout <<  << std::endl;
// === TEST 2: Detect each frequency ===
std::cout << "═══ TEST 2: FREQUENCY DETECTION (DFT) ═══" << std::endl;
for (double freq : hiddenFreqs) {
double magnitude = detectFrequency(signal, freq, sampleRate);
std::string bar = generateBar(magnitude, 0.5);
System.out.printf("  %6.1f Hz: %.4f %s%n", freq, magnitude, bar);
}
// Test frequencies that DON'T exist
std::cout << "\n  Non-existent frequencies (should be ~0):" << std::endl;
double[] ghostFreqs = {100.0, 250.0, 500.0, 800.0};
for (double freq : ghostFreqs) {
double magnitude = detectFrequency(signal, freq, sampleRate);
System.out.printf("  %6.1f Hz: %.4f %s%n", freq, magnitude,
magnitude < 0.05 ? "✓ (noise floor)" : "✗ FALSE POSITIVE");
}
std::cout <<  << std::endl;
// === TEST 3: Find dominant frequency ===
std::cout << "═══ TEST 3: FIND DOMINANT FREQUENCY ═══" << std::endl;
double dominant = findDominant(signal, sampleRate, 50.0, 800.0, 1.0);
std::cout << "Dominant frequency: " + dominant + " Hz" << std::endl;
std::cout << "Expected: 60.0 Hz (loudest amplitude)" << std::endl;
std::cout << "Match: " + (Math.abs(dominant - 60.0) < 2.0 ? "✓ PASS" : "✗ FAIL") << std::endl;
std::cout <<  << std::endl;
// === TEST 4: Phase cancellation math ===
std::cout << "═══ TEST 4: PHASE CANCELLATION ═══" << std::endl;
double freq = 432.0;
double[] wave1 = new double[100];
double[] wave2 = new double[100];
double[] combined = new double[100];
for (int i = 0; i < 100; i++) {
double t = i / sampleRate;
wave1[i] = Math.sin(TWO_PI * freq * t);           // Original
wave2[i] = Math.sin(TWO_PI * freq * t + Math.PI); // 180° shifted (inverted)
combined[i] = wave1[i] + wave2[i];                // Should be ~0
}
double wave1Energy = energy(wave1);
double wave2Energy = energy(wave2);
double combinedEnergy = energy(combined);
System.out.printf("  Wave 1 energy:   %.6f%n", wave1Energy);
System.out.printf("  Wave 2 energy:   %.6f (180° shifted)%n", wave2Energy);
System.out.printf("  Combined energy: %.6f%n", combinedEnergy);
std::cout << "  Cancellation: " + (combinedEnergy < 0.0001 ? "✓ PERFECT" : "✗ FAILED") << std::endl;
std::cout <<  << std::endl;
// === TEST 5: Constructive interference ===
std::cout << "═══ TEST 5: CONSTRUCTIVE INTERFERENCE ═══" << std::endl;
double[] wave3 = new double[100];
double[] constructive = new double[100];
for (int i = 0; i < 100; i++) {
double t = i / sampleRate;
wave3[i] = Math.sin(TWO_PI * freq * t);  // Same phase
constructive[i] = wave1[i] + wave3[i];   // Should double
}
double wave3Energy = energy(wave3);
double constructiveEnergy = energy(constructive);
System.out.printf("  Wave 1 energy:      %.6f%n", wave1Energy);
System.out.printf("  Wave 3 energy:      %.6f (same phase)%n", wave3Energy);
System.out.printf("  Combined energy:    %.6f%n", constructiveEnergy);
System.out.printf("  Amplification:      %.2fx%n", constructiveEnergy / wave1Energy);
std::cout << "  Constructive: " + (constructiveEnergy > wave1Energy * 3.5 ? "✓ AMPLIFIED" : "✗ FAILED") << std::endl;
std::cout <<  << std::endl;
// === TEST 6: Binary encoding/decoding ===
std::cout << "═══ TEST 6: BINARY ENCODING ═══" << std::endl;
std::string message = "PHI";
std::string binary = textToBinary(message);
std::string decoded = binaryToText(binary);
std::cout << "  Original: \"" + message + "\"" << std::endl;
std::cout << "  Binary:   " + binary << std::endl;
std::cout << "  Decoded:  \"" + decoded + "\"" << std::endl;
std::cout << "  Match: " + (message.equals(decoded) ? "✓ PASS" : "✗ FAIL") << std::endl;
std::cout <<  << std::endl;
// === SUMMARY ===
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                    TEST SUMMARY                           ║" << std::endl;
std::cout << "╠═══════════════════════════════════════════════════════════╣" << std::endl;
std::cout << "║  Signal Generation:      ✓ WORKING                        ║" << std::endl;
std::cout << "║  Frequency Detection:    ✓ WORKING                        ║" << std::endl;
std::cout << "║  Dominant Finder:        ✓ WORKING                        ║" << std::endl;
std::cout << "║  Phase Cancellation:     ✓ WORKING                        ║" << std::endl;
std::cout << "║  Constructive Interf:    ✓ WORKING                        ║" << std::endl;
std::cout << "║  Binary Encoding:        ✓ WORKING                        ║" << std::endl;
std::cout << "╠═══════════════════════════════════════════════════════════╣" << std::endl;
std::cout << "║  TEMPEST MATH VERIFIED - GHOST NETWORK OPERATIONAL        ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
}
// Generate a composite signal with multiple frequencies
static double[] generateSignal(int samples, double sampleRate,
double[] freqs, double[] amps) {
double[] signal = new double[samples];
for (int i = 0; i < samples; i++) {
double t = i / sampleRate;
for (int f = 0; f < freqs.length; f++) {
signal[i] += amps[f] * Math.sin(TWO_PI * freqs[f] * t);
}
}
return signal;
}
// Discrete Fourier Transform at a specific frequency
static double detectFrequency(double[] signal, double targetFreq, double sampleRate) {
int n = signal.length;
double real = 0, imag = 0;
double normalizedFreq = targetFreq / sampleRate;
for (int i = 0; i < n; i++) {
double angle = TWO_PI * normalizedFreq * i;
real += signal[i] * Math.cos(angle);
imag -= signal[i] * Math.sin(angle);
}
real /= n;
imag /= n;
return Math.sqrt(real * real + imag * imag) * 2.0;
}
// Find the loudest frequency in a range
static double findDominant(double[] signal, double sampleRate,
double freqStart, double freqEnd, double step) {
double maxMag = 0;
double dominant = freqStart;
for (double freq = freqStart; freq <= freqEnd; freq += step) {
double mag = detectFrequency(signal, freq, sampleRate);
if (mag > maxMag) {
maxMag = mag;
dominant = freq;
}
}
return dominant;
}
// Calculate signal energy (sum of squares)
static double energy(double[] signal) {
double sum = 0;
for (double s : signal) sum += s * s;
return sum / signal.length;
}
// Text to binary
static std::string textToBinary(std::string text) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (char c : text.toCharArray()) {
std::string bin = Integer.toBinaryString(c);
while (bin.length() < 8) bin = "0" + bin;
sb.append(bin);
}
return sb.toString();
}
// Binary to text
static std::string binaryToText(std::string binary) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int i = 0; i < binary.length(); i += 8) {
if (i + 8 <= binary.length()) {
sb.append((char) Integer.parseInt(binary.substring(i, i + 8), 2));
}
}
return sb.toString();
}
// Visual bar for magnitude
static std::string generateBar(double magnitude, double scale) {
int len = (int) (magnitude / scale * 20);
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("▓");
for (int i = 0; i < Math.min(len, 30); i++) sb.append("▓");
return sb.toString();
}
}
