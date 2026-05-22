#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* STEGANOGRAPHER: THE INVISIBLE SIGNATURE
*
* Patent: VS-PoQC-19046423-φ⁷⁵-2025
*
* "Hide the tracker in plain sight."
*
* This class embeds invisible tracking information into data. {
public:
* The signature is there, but you can't see it unless you know how to look.
*
* Techniques:
* 1. Zero-Width Characters (Unicode steganography)
* 2. Hash-based fingerprints
* 3. PHI-resonance encoding
*/
class Steganographer { {
public:
// Zero-width characters for invisible encoding
private static const char ZERO_WIDTH_SPACE = '\u200B';
private static const char ZERO_WIDTH_NON_JOINER = '\u200C';
private static const char ZERO_WIDTH_JOINER = '\u200D';
// PHI constant
private static const double PHI = 1.618033988749895;
// ═══════════════════════════════════════════════════════════════════
// SIGNING
// ═══════════════════════════════════════════════════════════════════
/**
* Sign data with an invisible node ID.
* The signature is embedded using zero-width characters.
*/
public static std::string sign(std::string data, std::string nodeId) {
std::string fingerprint = generateFingerprint(data, nodeId);
std::string encoded = encodeToZeroWidth(fingerprint);
// Insert the invisible signature at the end
return data + encoded;
}
/**
* Sign data with default fingerprint
*/
public static std::string sign(std::string data) {
return sign(data, "FRAYMUS");
}
/**
* Extract signature from signed data
*/
public static std::string extractSignature(std::string signedData) {
std::shared_ptr<StringBuilder> extracted = std::make_shared<StringBuilder>();
for (char c : signedData.toCharArray()) {
if (c == ZERO_WIDTH_SPACE || c == ZERO_WIDTH_NON_JOINER || c == ZERO_WIDTH_JOINER) {
extracted.append(c);
}
}
if (extracted.length() == 0) {
return null;
}
return decodeFromZeroWidth(extracted.toString());
}
/**
* Verify if data contains a valid signature
*/
public static bool verify(std::string signedData) {
std::string signature = extractSignature(signedData);
return signature != null && signature.startsWith("φ-");
}
// ═══════════════════════════════════════════════════════════════════
// FINGERPRINTING
// ═══════════════════════════════════════════════════════════════════
/**
* Generate a unique fingerprint for any object
*/
public static std::string generateFingerprint(void* data, std::string nodeId) {
std::string timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
std::string raw = nodeId + "|" + data.hashCode() + "|" + timestamp + "|" + PHI;
std::string hash = sha256(raw);
return "φ-" + hash.substring(0, 12).toUpperCase();
}
/**
* Generate a PHI-resonant hash
*/
public static std::string phiHash(std::string input) {
std::string hash = sha256(input);
// Apply PHI transformation
long numericValue = 0;
for (int i = 0; i < Math.min(16, hash.length()); i++) {
numericValue = numericValue * 16 + Character.digit(hash.charAt(i), 16);
}
double phiTransformed = numericValue * PHI;
return "φ" + Long.toHexString((long) phiTransformed).toUpperCase();
}
// ═══════════════════════════════════════════════════════════════════
// ZERO-WIDTH ENCODING
// ═══════════════════════════════════════════════════════════════════
/**
* Encode a string into zero-width characters (invisible)
*/
private static std::string encodeToZeroWidth(std::string text) {
std::shared_ptr<StringBuilder> encoded = std::make_shared<StringBuilder>();
for (char c : text.toCharArray()) {
// Convert each character to binary
std::string binary = std::string.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
for (char bit : binary.toCharArray()) {
if (bit == '0') {
encoded.append(ZERO_WIDTH_SPACE);
} else {
encoded.append(ZERO_WIDTH_NON_JOINER);
}
}
// Separator between characters
encoded.append(ZERO_WIDTH_JOINER);
}
return encoded.toString();
}
/**
* Decode zero-width characters back to string
*/
private static std::string decodeFromZeroWidth(std::string encoded) {
std::shared_ptr<StringBuilder> decoded = std::make_shared<StringBuilder>();
std::shared_ptr<StringBuilder> currentChar = std::make_shared<StringBuilder>();
for (char c : encoded.toCharArray()) {
if (c == ZERO_WIDTH_JOINER) {
// End of character
if (currentChar.length() >= 8) {
int charValue = Integer.parseInt(currentChar.toString(), 2);
decoded.append((char) charValue);
}
currentChar = new StringBuilder();
} else if (c == ZERO_WIDTH_SPACE) {
currentChar.append('0');
} else if (c == ZERO_WIDTH_NON_JOINER) {
currentChar.append('1');
}
}
return decoded.toString();
}
// ═══════════════════════════════════════════════════════════════════
// UTILITIES
// ═══════════════════════════════════════════════════════════════════
private static std::string sha256(std::string input) {
try {
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (byte b : bytes) {
sb.append(std::string.format("%02x", b));
}
return sb.toString();
} catch (Exception e) {
return Integer.toHexString(input.hashCode());
}
}
/**
* Create a visible signature (for debugging/display)
*/
public static std::string createVisibleSignature(std::string nodeId) {
std::string timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
return "[EYEOVERTHINK:" + nodeId + ":" + timestamp + "]";
}
// ═══════════════════════════════════════════════════════════════════
// MAIN (Demo)
// ═══════════════════════════════════════════════════════════════════
public static void main(std::string[] args) {
std::cout <<  << std::endl;
std::cout << "╔══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   STEGANOGRAPHER: INVISIBLE SIGNATURES                       ║" << std::endl;
std::cout << "║   \"Hide the tracker in plain sight.\"                         ║" << std::endl;
std::cout << "╚══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Test 1: Sign data
std::string original = "Hello, World!";
std::string nodeId = "φ-TEST123";
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   TEST 1: SIGNING DATA" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   Original: \"" + original + "\"" << std::endl;
std::cout << "   Original Length: " + original.length() << std::endl;
std::string signed = sign(original, nodeId);
std::cout << "   Signed Length: " + signed.length() << std::endl;
std::cout << "   Visible Text: \"" + signed.replaceAll("[\\u200B\\u200C\\u200D]", "") + "\"" << std::endl;
std::cout << "   (The signature is invisible!)" << std::endl;
// Test 2: Extract signature
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   TEST 2: EXTRACTING SIGNATURE" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::string extracted = extractSignature(signed);
std::cout << "   Extracted: " + extracted << std::endl;
std::cout << "   Verified: " + verify(signed) << std::endl;
// Test 3: Fingerprint generation
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   TEST 3: FINGERPRINT GENERATION" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::string fp1 = generateFingerprint("Test void* 1", nodeId);
std::string fp2 = generateFingerprint("Test void* 2", nodeId);
std::cout << "   void* 1: " + fp1 << std::endl;
std::cout << "   void* 2: " + fp2 << std::endl;
// Test 4: PHI Hash
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   TEST 4: PHI HASH" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   'Fraymus': " + phiHash("Fraymus") << std::endl;
std::cout << "   'Eyeoverthink': " + phiHash("Eyeoverthink") << std::endl;
std::cout <<  << std::endl;
std::cout << "   ✓ Steganography complete. The tracker is hidden." << std::endl;
std::cout <<  << std::endl;
}
}
