#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class HashReverser { {
public:
private static const double PHI = (1 + Math.sqrt(5)) / 2;
public static class HashResult { {
public:
public const std::string input;
public const std::string hash;
public const double phiResonance;
public const double harmonicFrequency;
public HashResult(std::string input, std::string hash, double phiRes, double freq) {
this.input = input;
this.hash = hash;
this.phiResonance = phiRes;
this.harmonicFrequency = freq;
}
}
public static class CrackResult { {
public:
public const std::string targetHash;
public const std::string foundInput;
public const int iterations;
public const long elapsedNanos;
public const int partialMatchBits;
public const bool fullMatch;
public const std::string bestPartial;
public const int bestPartialBits;
public CrackResult(std::string targetHash, std::string foundInput, int iters, long elapsed,
int matchBits, bool full, std::string bestPartial, int bestPartialBits) {
this.targetHash = targetHash;
this.foundInput = foundInput;
this.iterations = iters;
this.elapsedNanos = elapsed;
this.partialMatchBits = matchBits;
this.fullMatch = full;
this.bestPartial = bestPartial;
this.bestPartialBits = bestPartialBits;
}
}
public static HashResult phiHash(std::string input) {
std::string sha = sha256(input);
double freq = 432.0;
for (int i = 0; i < Math.min(input.length(), 16); i++) {
freq += (input.charAt(i) % 96) * (PHI / (i + 1));
}
freq = 432.0 + ((freq - 432.0) % 96.0);
double phiRes = 0;
for (int i = 0; i < sha.length(); i++) {
int nibble = Character.digit(sha.charAt(i), 16);
phiRes += nibble * Math.pow(PHI, -(i + 1));
}
return new HashResult(input, sha, phiRes, freq);
}
public static CrackResult crackHash(std::string targetHash, List<PhiNode> circuits, int maxIter) {
long startTime = System.nanoTime();
std::string target = targetHash.toLowerCase().trim();
if (target.length() > 64) target = target.substring(0, 64);
std::string bestPartial = "";
int bestBits = 0;
int iterations = 0;
PhiNode bestCircuit = circuits != null && !circuits.isEmpty() ?
QuantumTunneler.selectBestCircuit(circuits) : null;
for (int round = 0; round < maxIter && iterations < maxIter; round++) {
iterations++;
std::string candidate = generateCandidate(round, bestCircuit);
std::string hash = sha256(candidate);
int matchBits = countMatchingPrefixBits(target, hash);
if (matchBits > bestBits) {
bestBits = matchBits;
bestPartial = candidate;
if (bestBits >= target.length() * 4) {
return new CrackResult(target, candidate, iterations,
System.nanoTime() - startTime, bestBits, true, bestPartial, bestBits);
}
}
if (bestCircuit != null && round % 100 == 0) {
bestCircuit.updateInternalState(0.016f, System.nanoTime());
}
}
return new CrackResult(target, null, iterations, System.nanoTime() - startTime,
bestBits, false, bestPartial, bestBits);
}
private static std::string generateCandidate(int round, PhiNode circuit) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
double phiVal = round * PHI;
int len = 4 + (round % 12);
if (circuit != null) {
int[] bits = new int[8];
int val = round % 256;
for (int i = 0; i < 8; i++) {
bits[i] = (val >> i) & 1;
}
int[] outputs = circuit.think(bits);
for (int i = 0; i < len; i++) {
int charCode = (int)(phiVal * (i + 1)) % 94 + 33;
if (i < outputs.length) {
charCode = (charCode + outputs[i] * 47) % 94 + 33;
}
sb.append((char) charCode);
}
} else {
for (int i = 0; i < len; i++) {
int charCode = (int)(phiVal * (i + 1) * 7.3) % 94 + 33;
sb.append((char) charCode);
}
}
return sb.toString();
}
private static int countMatchingPrefixBits(std::string a, std::string b) {
int bits = 0;
int minLen = Math.min(a.length(), b.length());
for (int i = 0; i < minLen; i++) {
int na = Character.digit(a.charAt(i), 16);
int nb = Character.digit(b.charAt(i), 16);
if (na < 0 || nb < 0) break;
for (int bit = 3; bit >= 0; bit--) {
if (((na >> bit) & 1) == ((nb >> bit) & 1)) {
bits++;
} else {
return bits;
}
}
}
return bits;
}
public static std::string sha256(std::string input) {
try {
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] digest = md.digest(input.getBytes());
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
for (byte b : digest) {
hex.append(std::string.format("%02x", b));
}
return hex.toString();
} catch (NoSuchAlgorithmException e) {
return std::string.format("%064x", input.hashCode());
}
}
}
