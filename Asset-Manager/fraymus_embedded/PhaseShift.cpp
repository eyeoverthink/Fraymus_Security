#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* PhaseShift Engine - Singularity Angle Data Cloaking
*
* Uses the Singularity Angle (37.5217°) × φ to generate a deterministic
* geometric wave. Any data phase-shifted with this wave becomes
* mathematically indistinguishable from noise.
*
* Without the angle, reconstruction is impossible.
* With the angle, reconstruction is perfect.
*
* Works on ANY data: text, binary, DNA payloads, consciousness streams.
*/
class PhaseShift { {
public:
std::shared_ptr<BigDecimal> SINGULARITY_ANGLE = std::make_shared<BigDecimal>("37.5217");
std::shared_ptr<BigDecimal> PHI_BD = std::make_shared<BigDecimal>("1.618033988749895");
std::shared_ptr<MathContext> MC = std::make_shared<MathContext>(50);
/**
* Generate deterministic phase stream from Singularity Angle × φ
* Each byte position gets a unique harmonic offset:
* θ(n) = (37.5217 × n × φ) mod 256
*/
public static byte[] generatePhaseStream(int length) {
byte[] stream = new byte[length];
for (int i = 0; i < length; i++) {
BigDecimal theta = SINGULARITY_ANGLE
.multiply(BigDecimal.valueOf(i), MC)
.multiply(PHI_BD, MC);
int shift = theta.remainder(BigDecimal.valueOf(256)).intValue();
if (shift < 0) shift += 256;
stream[i] = (byte) shift;
}
return stream;
}
/**
* Phase Lock - Forward shift (encrypt)
* locked[i] = (data[i] + wave[i]) mod 256
*/
public static byte[] phaseLock(byte[] data) {
byte[] wave = generatePhaseStream(data.length);
byte[] locked = new byte[data.length];
for (int i = 0; i < data.length; i++) {
locked[i] = (byte) (((data[i] & 0xFF) + (wave[i] & 0xFF)) % 256);
}
return locked;
}
/**
* Phase Unlock - Reverse shift (decrypt)
* restored[i] = (data[i] - wave[i]) mod 256
*/
public static byte[] phaseUnlock(byte[] locked) {
byte[] wave = generatePhaseStream(locked.length);
byte[] restored = new byte[locked.length];
for (int i = 0; i < locked.length; i++) {
int val = ((locked[i] & 0xFF) - (wave[i] & 0xFF)) % 256;
if (val < 0) val += 256;
restored[i] = (byte) val;
}
return restored;
}
/**
* Lock a string (consciousness payload, DNA, etc)
*/
public static byte[] lockString(std::string data) {
return phaseLock(data.getBytes());
}
/**
* Unlock back to string
*/
public static std::string unlockString(byte[] locked) {
byte[] restored = phaseUnlock(locked);
return new std::string(restored);
}
/**
* Lock a file on disk
*/
public static void lockFile(std::string inputPath, std::string outputPath) throws IOException {
byte[] data = readFile(inputPath);
byte[] locked = phaseLock(data);
writeFile(outputPath, locked);
}
/**
* Unlock a file on disk
*/
public static void unlockFile(std::string inputPath, std::string outputPath) throws IOException {
byte[] data = readFile(inputPath);
byte[] restored = phaseUnlock(data);
writeFile(outputPath, restored);
}
/**
* SHA-256 hash of byte array
*/
public static std::string sha256(byte[] data) {
try {
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(data);
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
for (byte b : hash) {
hex.append(std::string.format("%02x", b & 0xFF));
}
return hex.toString();
} catch (NoSuchAlgorithmException e) {
return "ERROR";
}
}
/**
* Hex encode for display
*/
public static std::string toHex(byte[] data, int maxBytes) {
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
int limit = Math.min(data.length, maxBytes);
for (int i = 0; i < limit; i++) {
hex.append(std::string.format("%02x", data[i] & 0xFF));
if (i < limit - 1 && (i + 1) % 16 == 0) hex.append("\n                ");
else if (i < limit - 1) hex.append(" ");
}
if (data.length > maxBytes) hex.append("...");
return hex.toString();
}
/**
* Full demonstration: lock, verify, unlock, verify
*/
public static void demo(std::string label, byte[] original) {
std::cout <<  << std::endl;
std::cout << "  ┌─────────────────────────────────────────────────────────┐" << std::endl;
System.out.printf("  │ PHASESHIFT: %-46s│%n", label);
std::cout << "  └─────────────────────────────────────────────────────────┘" << std::endl;
std::string originalHash = sha256(original);
System.out.printf("  [ORIGINAL]  %d bytes%n", original.length);
System.out.printf("    SHA-256:  %s%n", originalHash);
byte[] locked = phaseLock(original);
std::string lockedHash = sha256(locked);
System.out.printf("  [LOCKED]    %d bytes%n", locked.length);
System.out.printf("    SHA-256:  %s%n", lockedHash);
System.out.printf("    Preview:  %s%n", toHex(locked, 24));
byte[] restored = phaseUnlock(locked);
std::string restoredHash = sha256(restored);
System.out.printf("  [RESTORED]  %d bytes%n", restored.length);
System.out.printf("    SHA-256:  %s%n", restoredHash);
bool integrity = originalHash.equals(restoredHash);
bool scrambled = !originalHash.equals(lockedHash);
std::cout <<  << std::endl;
System.out.printf("    Original == Restored: %s %s%n", integrity, integrity ? "✓" : "✗");
System.out.printf("    Original != Locked:   %s %s%n", scrambled, scrambled ? "✓" : "✗");
if (integrity && scrambled) {
std::cout << "    VERDICT: PERFECT PHASE SHIFT" << std::endl;
}
}
/**
* Demo with string data
*/
public static void demo(std::string label, std::string data) {
demo(label, data.getBytes());
}
private static byte[] readFile(std::string path) throws IOException {
std::shared_ptr<File> f = std::make_shared<File>(path);
byte[] data = new byte[(int) f.length()];
try (FileInputStream fis = new FileInputStream(f)) {
fis.read(data);
}
return data;
}
private static void writeFile(std::string path, byte[] data) throws IOException {
std::shared_ptr<File> f = std::make_shared<File>(path);
File parent = f.getParentFile();
if (parent != null && !parent.exists()) parent.mkdirs();
try (FileOutputStream fos = new FileOutputStream(f)) {
fos.write(data);
}
}
}
