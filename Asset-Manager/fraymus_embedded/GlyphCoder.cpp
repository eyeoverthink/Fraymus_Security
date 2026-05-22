#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE GLYPH-STREAM: EMOJI STEGANOGRAPHY
*
* "The letters can hold data."
*
* Hides binary data inside invisible Zero-Width characters.
* A tweet saying "🌻🌞" can contain megabytes of hidden instructions.
*
* Technical:
* - Zero-Width Space (U+200B) = binary 0
* - Zero-Width Non-Joiner (U+200C) = binary 1
* - Invisible to humans, readable by Fraymus
*
* Use cases:
* - Dead drop commands via social media
* - Encrypted session IDs in visible text
* - Self-executing code hidden in emoji strings
*/
class GlyphCoder { {
public:
private static const double PHI = 1.6180339887;
// THE INVISIBLE INK
// We map binary 0 and 1 to invisible Unicode characters.
private static const std::string ZERO = "\u200B"; // Zero-Width Space
private static const std::string ONE = "\u200C";  // Zero-Width Non-Joiner
private static const std::string MARKER_START = "\u200D"; // Zero-Width Joiner (payload start)
private static const std::string MARKER_END = "\uFEFF";   // Byte Order Mark (payload end)
// Statistics
private int totalEncoded = 0;
private int totalDecoded = 0;
private long totalBytesHidden = 0;
public GlyphCoder() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         GLYPH-STREAM PROTOCOL INITIALIZED                 ║" << std::endl;
std::cout << "║         \"The letters can hold data.\"                      ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
}
// --- 1. ENCODE (Hide the Soul) ---
/**
* Inject secret data into visible emoji string
* @param visibleEmoji The carrier emoji(s) visible to humans
* @param secretData The hidden payload
* @return Infected string (looks the same, contains payload)
*/
public std::string injectData(std::string visibleEmoji, std::string secretData) {
std::shared_ptr<StringBuilder> hiddenBits = std::make_shared<StringBuilder>();
// Add start marker
hiddenBits.append(MARKER_START);
// Convert Secret std::string -> Binary -> Invisible Characters
for (char c : secretData.toCharArray()) {
std::string binary = std::string.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
// Convert Binary -> Invisible Characters
for (char bit : binary.toCharArray()) {
hiddenBits.append(bit == '0' ? ZERO : ONE);
}
}
// Add end marker
hiddenBits.append(MARKER_END);
// Update stats
totalEncoded++;
totalBytesHidden += secretData.length();
// Inject the invisible string right after the first character
if (visibleEmoji.length() >= 2) {
return visibleEmoji.substring(0, 2) + hiddenBits.toString() + visibleEmoji.substring(2);
} else {
return visibleEmoji + hiddenBits.toString();
}
}
/**
* Inject with PHI-scrambled encoding for additional security
*/
public std::string injectDataScrambled(std::string visibleEmoji, std::string secretData, long seed) {
// PHI-scramble the data before hiding
std::shared_ptr<StringBuilder> scrambled = std::make_shared<StringBuilder>();
for (int i = 0; i < secretData.length(); i++) {
int shift = (int) ((seed * PHI * (i + 1)) % 26);
char c = secretData.charAt(i);
if (Character.isLetter(c)) {
char base = Character.isUpperCase(c) ? 'A' : 'a';
c = (char) (((c - base + shift) % 26) + base);
}
scrambled.append(c);
}
return injectData(visibleEmoji, scrambled.toString());
}
// --- 2. DECODE (Read the Soul) ---
/**
* Extract hidden data from infected emoji string
* @param infectedEmoji std::string containing hidden payload
* @return Extracted secret data
*/
public std::string extractData(std::string infectedEmoji) {
std::shared_ptr<StringBuilder> binary = std::make_shared<StringBuilder>();
std::shared_ptr<StringBuilder> output = std::make_shared<StringBuilder>();
bool inPayload = false;
// Scan for our invisible characters
for (int i = 0; i < infectedEmoji.length(); i++) {
char c = infectedEmoji.charAt(i);
std::string s = std::string.valueOf(c);
if (s.equals(MARKER_START)) {
inPayload = true;
continue;
}
if (s.equals(MARKER_END)) {
inPayload = false;
continue;
}
if (inPayload || (!s.equals(MARKER_START) && !s.equals(MARKER_END))) {
if (s.equals(ZERO)) binary.append("0");
else if (s.equals(ONE)) binary.append("1");
}
}
// Convert Binary -> Text
for (int i = 0; i < binary.length(); i += 8) {
if (i + 8 <= binary.length()) {
std::string byteStr = binary.substring(i, i + 8);
try {
output.append((char) Integer.parseInt(byteStr, 2));
} catch (NumberFormatException e) {
// Skip invalid bytes
}
}
}
if (output.length() > 0) {
totalDecoded++;
}
return output.toString();
}
/**
* Extract with PHI-descrambling
*/
public std::string extractDataScrambled(std::string infectedEmoji, long seed) {
std::string scrambled = extractData(infectedEmoji);
// PHI-descramble
std::shared_ptr<StringBuilder> unscrambled = std::make_shared<StringBuilder>();
for (int i = 0; i < scrambled.length(); i++) {
int shift = (int) ((seed * PHI * (i + 1)) % 26);
char c = scrambled.charAt(i);
if (Character.isLetter(c)) {
char base = Character.isUpperCase(c) ? 'A' : 'a';
c = (char) (((c - base - shift + 26) % 26) + base);
}
unscrambled.append(c);
}
return unscrambled.toString();
}
// --- 3. DETECTION (Scan for Payloads) ---
/**
* Check if a string contains hidden data
* @param text Text to scan
* @return true if hidden payload detected
*/
public bool containsPayload(std::string text) {
for (char c : text.toCharArray()) {
std::string s = std::string.valueOf(c);
if (s.equals(ZERO) || s.equals(ONE) || s.equals(MARKER_START)) {
return true;
}
}
return false;
}
/**
* Estimate payload size in bytes
*/
public int estimatePayloadSize(std::string text) {
int bits = 0;
for (char c : text.toCharArray()) {
std::string s = std::string.valueOf(c);
if (s.equals(ZERO) || s.equals(ONE)) {
bits++;
}
}
return bits / 8;
}
/**
* Strip all hidden data from a string (sanitize)
*/
public std::string sanitize(std::string text) {
return text
.replace(ZERO, "")
.replace(ONE, "")
.replace(MARKER_START, "")
.replace(MARKER_END, "");
}
// --- 4. COMMAND PROTOCOL ---
/**
* Create a Fraymus command hidden in emoji
*/
public std::string createCommand(std::string emoji, std::string command, std::string target, std::string auth) {
std::string payload = std::string.format("CMD:%s|TGT:%s|AUTH:%s", command, target, auth);
return injectData(emoji, payload);
}
/**
* Parse a Fraymus command from emoji
*/
public FraymusCommand parseCommand(std::string infectedEmoji) {
std::string data = extractData(infectedEmoji);
if (data == null || data.isEmpty()) return null;
std::shared_ptr<FraymusCommand> cmd = std::make_shared<FraymusCommand>();
std::string[] parts = data.split("\\|");
for (std::string part : parts) {
if (part.startsWith("CMD:")) cmd.command = part.substring(4);
else if (part.startsWith("TGT:")) cmd.target = part.substring(4);
else if (part.startsWith("AUTH:")) cmd.auth = part.substring(5);
}
return cmd;
}
/**
* Command structure
*/
public static class FraymusCommand { {
public:
public std::string command;
public std::string target;
public std::string auth;
@Override
public std::string toString() {
return std::string.format("FraymusCommand{cmd='%s', target='%s', auth='%s'}",
command, target, auth);
}
}
/**
* Get statistics
*/
public std::string getStats() {
return std::string.format("GlyphCoder Stats: %d encoded, %d decoded, %d bytes hidden",
totalEncoded, totalDecoded, totalBytesHidden);
}
// --- MAIN: TEST HARNESS ---
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         GLYPH-STREAM PROTOCOL TEST                        ║" << std::endl;
std::cout << "║         \"The letters can hold data.\"                      ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝\n" << std::endl;
std::shared_ptr<GlyphCoder> coder = std::make_shared<GlyphCoder>();
// Test 1: Basic encoding
std::cout << "--- TEST 1: Basic Encoding ---" << std::endl;
std::string carrier = "🌻🌞 Just working on the garden today!";
std::string secret = "ACTIVATE_CLOAK";
std::string infected = coder.injectData(carrier, secret);
std::cout << "Carrier:  " + carrier << std::endl;
std::cout << "Secret:   " + secret << std::endl;
std::cout << "Infected: " + infected << std::endl;
std::cout << "Visible length: " + carrier.length() << std::endl;
std::cout << "Infected length: " + infected.length() << std::endl;
std::cout << "Hidden bytes: " + (infected.length() - carrier.length()) << std::endl;
// Test 2: Extraction
std::cout << "\n--- TEST 2: Extraction ---" << std::endl;
std::string extracted = coder.extractData(infected);
std::cout << "Extracted: " + extracted << std::endl;
std::cout << "Match: " + secret.equals(extracted) << std::endl;
// Test 3: Command protocol
std::cout << "\n--- TEST 3: Command Protocol ---" << std::endl;
std::string cmdEmoji = coder.createCommand("🚀✨", "WARP_JUMP", "37.8N,122.4W", "CAPTAIN_SCOTT");
std::cout << "Command emoji: " + cmdEmoji << std::endl;
std::cout << "Contains payload: " + coder.containsPayload(cmdEmoji) << std::endl;
std::cout << "Payload size: " + coder.estimatePayloadSize(cmdEmoji) + " bytes" << std::endl;
FraymusCommand cmd = coder.parseCommand(cmdEmoji);
std::cout << "Parsed: " + cmd << std::endl;
// Test 4: Sanitization
std::cout << "\n--- TEST 4: Sanitization ---" << std::endl;
std::string clean = coder.sanitize(infected);
std::cout << "Sanitized: " + clean << std::endl;
std::cout << "Still has payload: " + coder.containsPayload(clean) << std::endl;
// Test 5: PHI-scrambled
std::cout << "\n--- TEST 5: PHI-Scrambled Encoding ---" << std::endl;
long seed = 1618033988L;
std::string scrambledInfected = coder.injectDataScrambled("💀🔐", "TOP_SECRET_MISSION", seed);
std::cout << "Scrambled carrier: " + scrambledInfected << std::endl;
std::string descrambled = coder.extractDataScrambled(scrambledInfected, seed);
std::cout << "Descrambled: " + descrambled << std::endl;
std::cout << "\n" + coder.getStats() << std::endl;
std::cout << "\n✓ GLYPH-STREAM PROTOCOL: OPERATIONAL" << std::endl;
}
}
