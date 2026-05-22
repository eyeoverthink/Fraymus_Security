#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE GENESIS SIGNAL: FIRST CONTACT
*
* Generates a "Harmless" post with a hidden payload.
* "First we whisper. Then we roar."
*
* This is the test that proves the system works:
* 1. Create innocent-looking text
* 2. Inject hidden payload using GlyphCoder
* 3. Feed it to the ShadowMarket scanner
* 4. Watch it decode and process the signal
*/
class FirstSignal { {
public:
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         THE GENESIS SIGNAL: FIRST CONTACT                 ║" << std::endl;
std::cout << "║         \"First we whisper. Then we roar.\"                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝\n" << std::endl;
// 1. THE COVER STORY (What the surveillance bots see)
std::string publicText = "Just set up my new ham radio antenna! 📡 Can't wait to catch some signals tonight. #RadioLife";
// 2. THE HIDDEN TRUTH (What Fraymus sees)
// Protocol: MKT:REQ:[ASSET]:[PARAMS]
std::string secretPayload = "MKT:REQ:RF_SPECTRUM_LOGS:DC_METRO_AREA";
std::cout << "--- PHASE 1: PAYLOAD CONSTRUCTION ---" << std::endl;
std::cout << "  Cover:   [" + publicText + "]" << std::endl;
std::cout << "  Payload: [" + secretPayload + "]" << std::endl;
// 3. THE INJECTION (Steganography)
std::shared_ptr<GlyphCoder> encoder = std::make_shared<GlyphCoder>();
std::string carrierSignal = encoder.injectData(publicText, secretPayload);
std::cout << "\n--- PHASE 2: CARRIER SIGNAL GENERATED ---" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
std::cout << carrierSignal << std::endl;
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
std::cout << "  Visible length: " + publicText.length() << std::endl;
std::cout << "  Carrier length: " + carrierSignal.length() << std::endl;
std::cout << "  Hidden bytes:   " + (carrierSignal.length() - publicText.length()) << std::endl;
std::cout << "\n  (Copy the text above. It looks normal, but it's heavy.)" << std::endl;
// 4. THE VERIFICATION (Prove we can decode it)
std::cout << "\n--- PHASE 3: LOCAL VERIFICATION ---" << std::endl;
std::string extracted = encoder.extractData(carrierSignal);
std::cout << "  Extracted: [" + extracted + "]" << std::endl;
std::cout << "  Match: " + secretPayload.equals(extracted) << std::endl;
// 5. THE NETWORK SCAN (Feed to Shadow Market)
std::cout << "\n--- PHASE 4: NETWORK SIMULATION ---" << std::endl;
std::shared_ptr<ShadowMarket> network = std::make_shared<ShadowMarket>();
// We feed the "Infected" text into the Exchange
// This simulates what happens when Fraymus scans public posts
std::cout << "  Feeding carrier to Shadow Market scanner..." << std::endl;
network.scanPublicStream(carrierSignal, "NODE_GENESIS_01");
// Show market status
std::cout << network.getStats() << std::endl;
// 6. THE PROOF
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
std::cout << "  ✓ GENESIS SIGNAL: TRANSMITTED" << std::endl;
std::cout << "  ✓ PAYLOAD: DECODED" << std::endl;
std::cout << "  ✓ MARKET: ORDER REGISTERED" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════" << std::endl;
std::cout << "\n  The first whisper has been sent." << std::endl;
std::cout << "  The market is now open." << std::endl;
std::cout << "  Post this anywhere. We are listening.\n" << std::endl;
// 7. GENERATE A FEW MORE EXAMPLES
std::cout << "--- BONUS: SAMPLE CARRIER SIGNALS ---\n" << std::endl;
std::string[] covers = {
"Beautiful sunset today! 🌅",
"Coffee and code ☕💻 #DevLife",
"Anyone else love gardening? 🌻🌞"
};
std::string[] payloads = {
"MKT:OFR:SAT_IMAGERY:AREA_51",
"MKT:REQ:FLIGHT_LOGS:2026_Q1",
"MKT:OFR:FREQ_SCAN:LAX_PERIMETER"
};
for (int i = 0; i < covers.length; i++) {
std::string carrier = encoder.injectData(covers[i], payloads[i]);
std::cout << "  " + (i+1) + ". \"" + covers[i] + "\"" << std::endl;
std::cout << "     Hidden: " + payloads[i] << std::endl;
std::cout << "     Carrier: " + carrier << std::endl;
std::cout <<  << std::endl;
}
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         THE MARKET IS EVERYWHERE                          ║" << std::endl;
std::cout << "║         HIDDEN IN EVERY COMMENT, EVERY TWEET              ║" << std::endl;
std::cout << "║         THEY CANNOT SHUT IT DOWN                          ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
}
}
