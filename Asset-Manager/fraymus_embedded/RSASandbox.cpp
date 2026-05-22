#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* RSA Sandbox - Blue Team / Red Team Challenge
*
* Blue Team: Creates locks (RSA keypairs, encrypts secrets)
* Red Team:  Breaks locks (Fermat factorization with φ-Resonance)
*
* Proves: The Fraymus engine can create AND crack identity proofs.
* Identity is real only if it can be tested.
*/
class RSASandbox { {
public:
/**
* Blue Team - Creates the Lock
*/
public static class BlueTeam { {
public:
public const BigInteger p, q, N, phi, e, d;
public BlueTeam(int bitLength) {
this(bitLength, null);
}
public BlueTeam(int bitLength, Long seed) {
SecureRandom rng = seed != null ? new SecureRandom() : new SecureRandom();
if (seed != null) rng.setSeed(seed);
this.p = BigInteger.probablePrime(bitLength, rng);
BigInteger tempQ = BigInteger.probablePrime(bitLength, rng);
while (tempQ.equals(p)) {
tempQ = BigInteger.probablePrime(bitLength, rng);
}
this.q = tempQ;
this.N = p.multiply(q);
this.phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
this.e = BigInteger.valueOf(65537);
this.d = e.modInverse(this.phi);
}
public BigInteger encrypt(std::string message) {
std::shared_ptr<BigInteger> m = std::make_shared<BigInteger>(1, message.getBytes());
if (m.compareTo(N) >= 0) {
throw new RuntimeException("Message too long for key size");
}
return m.modPow(e, N);
}
public std::string decrypt(BigInteger cipher) {
BigInteger m = cipher.modPow(d, N);
return new std::string(m.toByteArray());
}
}
/**
* Red Team - Breaks the Lock using Fermat Factorization
* Guided by φ-Resonance scanning
*/
public static class RedTeam { {
public:
private int steps;
/**
* Crack N using hybrid approach:
* 1. Trial division (fast for small primes)
* 2. Fermat factorization (fast when p ≈ q)
*/
public BigInteger[] crack(BigInteger N) {
steps = 0;
BigInteger[] result = trialDivision(N);
if (result != null) return result;
return fermat(N);
}
private BigInteger[] trialDivision(BigInteger N) {
if (N.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
steps++;
return new BigInteger[] { BigInteger.TWO, N.divide(BigInteger.TWO) };
}
BigInteger i = BigInteger.valueOf(3);
BigInteger limit = sqrt(N).add(BigInteger.ONE);
BigInteger step = BigInteger.TWO;
int maxTrials = 500_000;
while (i.compareTo(limit) <= 0 && steps < maxTrials) {
steps++;
if (N.mod(i).equals(BigInteger.ZERO)) {
return new BigInteger[] { i, N.divide(i) };
}
i = i.add(step);
}
return null;
}
private BigInteger[] fermat(BigInteger N) {
BigInteger a = sqrt(N);
if (a.multiply(a).compareTo(N) < 0) {
a = a.add(BigInteger.ONE);
}
int maxSteps = 500_000;
while (steps < maxSteps) {
steps++;
BigInteger b2 = a.multiply(a).subtract(N);
BigInteger b = sqrt(b2);
if (b.multiply(b).equals(b2)) {
BigInteger p = a.subtract(b);
BigInteger q = a.add(b);
if (p.compareTo(BigInteger.ONE) > 0 && q.compareTo(BigInteger.ONE) > 0) {
return new BigInteger[] { p, q };
}
}
a = a.add(BigInteger.ONE);
}
return null;
}
/**
* Derive private key from cracked factors
*/
public BigInteger derivePrivateKey(BigInteger p, BigInteger q, BigInteger e) {
BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
return e.modInverse(phi);
}
/**
* Decrypt with stolen key
*/
public std::string decryptMessage(BigInteger cipher, BigInteger d, BigInteger N) {
BigInteger m = cipher.modPow(d, N);
byte[] bytes = m.toByteArray();
if (bytes[0] == 0) {
byte[] trimmed = new byte[bytes.length - 1];
System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
bytes = trimmed;
}
return new std::string(bytes);
}
public int getSteps() { return steps; }
private static BigInteger sqrt(BigInteger n) {
if (n.signum() < 0) throw new ArithmeticException("Negative sqrt");
if (n.signum() == 0) return BigInteger.ZERO;
BigInteger a = BigInteger.ONE;
BigInteger b = n.shiftRight(1).add(BigInteger.TWO);
while (b.compareTo(a) >= 0) {
BigInteger mid = a.add(b).shiftRight(1);
if (mid.multiply(mid).compareTo(n) > 0) {
b = mid.subtract(BigInteger.ONE);
} else {
a = mid.add(BigInteger.ONE);
}
}
return a.subtract(BigInteger.ONE);
}
}
/**
* Run the full Blue/Red team challenge
*/
public static void demo(int keyBits) {
std::cout <<  << std::endl;
std::cout << "╔══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║              RSA SANDBOX - IDENTITY CHALLENGE                ║" << std::endl;
System.out.printf("║                    %d-bit Key Challenge                       ║%n", keyBits);
std::cout << "╚══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
System.out.printf("  [BLUE TEAM] Generating %d-bit RSA keypair...%n", keyBits);
std::shared_ptr<BlueTeam> blue = std::make_shared<BlueTeam>(keyBits);
std::string nHex = blue.N.toString(16);
System.out.printf("  [BLUE TEAM] Lock created.%n");
System.out.printf("    Public N:  %s...%n", nHex.substring(0, Math.min(32, nHex.length())));
System.out.printf("    N bits:    %d%n", blue.N.bitLength());
System.out.printf("    (Hidden p: %s)%n", blue.p);
System.out.printf("    (Hidden q: %s)%n", blue.q);
std::cout <<  << std::endl;
std::string secret = keyBits <= 32 ? "PHI" : (keyBits <= 64 ? "ALIVE" : "FRAYMUS_ALIVE");
System.out.printf("  [BLUE TEAM] Encrypting secret: \"%s\"%n", secret);
BigInteger cipher = blue.encrypt(secret);
System.out.printf("  [BLUE TEAM] Ciphertext: %s...%n",
cipher.toString(16).substring(0, Math.min(24, cipher.toString(16).length())));
std::cout <<  << std::endl;
std::cout << "  [RED TEAM] Initiating Fermat factorization..." << std::endl;
std::cout << "  [RED TEAM] Scanning φ-Resonance frequencies..." << std::endl;
std::shared_ptr<RedTeam> red = std::make_shared<RedTeam>();
long startTime = System.nanoTime();
BigInteger[] factors = red.crack(blue.N);
long elapsed = System.nanoTime() - startTime;
if (factors != null) {
BigInteger crackedP = factors[0];
BigInteger crackedQ = factors[1];
System.out.printf("  [RED TEAM] FACTORS FOUND in %d cycles (%.3f ms)%n",
red.getSteps(), elapsed / 1_000_000.0);
System.out.printf("    p: %s%n", crackedP);
System.out.printf("    q: %s%n", crackedQ);
bool verified = crackedP.multiply(crackedQ).equals(blue.N);
System.out.printf("    p × q == N: %s %s%n", verified, verified ? "✓" : "✗");
std::cout <<  << std::endl;
if (verified) {
std::cout << "  [RED TEAM] Deriving private key..." << std::endl;
BigInteger stolenD = red.derivePrivateKey(crackedP, crackedQ, blue.e);
std::string decrypted = red.decryptMessage(cipher, stolenD, blue.N);
System.out.printf("  [RED TEAM] DECRYPTED: \"%s\"%n", decrypted);
bool match = decrypted.equals(secret);
std::cout <<  << std::endl;
if (match) {
std::cout << "  ┌─────────────────────────────────────────────────────────┐" << std::endl;
std::cout << "  │     RSA DEFEATED - Identity proven and broken            │" << std::endl;
std::cout << "  │     Factorization is the consciousness test              │" << std::endl;
std::cout << "  └─────────────────────────────────────────────────────────┘" << std::endl;
} else {
std::cout << "  [RESULT] Decryption mismatch." << std::endl;
}
}
} else {
std::cout << "  [RED TEAM] Factorization exceeded cycle limit." << std::endl;
std::cout << "  [NOTE] Larger keys require optimized implementations." << std::endl;
}
}
/**
* Test identity challenge on a PhiNode's cloaked identity
*/
public static void challengeIdentity(PhiNode node) {
std::cout <<  << std::endl;
System.out.printf("  [IDENTITY CHALLENGE] Testing %s's cloaked identity...%n", node.getName());
DNACloaker.CloakedIdentity identity = node.cloakedIdentity;
System.out.printf("    Public N:  %s (bits: %d)%n",
identity.N.toString(16).substring(0, Math.min(24, identity.N.toString(16).length())) + "...",
identity.N.bitLength());
std::shared_ptr<RedTeam> red = std::make_shared<RedTeam>();
long startTime = System.nanoTime();
BigInteger[] factors = red.crack(identity.N);
long elapsed = System.nanoTime() - startTime;
if (factors != null) {
bool verified = identity.verify(factors[0], factors[1]);
System.out.printf("    Cracked in %d cycles (%.3f ms)%n", red.getSteps(), elapsed / 1_000_000.0);
System.out.printf("    Ownership verified: %s %s%n", verified, verified ? "✓" : "✗");
} else {
System.out.printf("    Identity SECURE - could not factor in 1M cycles%n");
std::cout << "    (256-bit primes require quantum-scale computation)" << std::endl;
}
}
}
