#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Test URLAbsorber integration
*/
class TestWebAbsorption { {
public:
public static void main(std::string[] args) {
std::cout << "🌐 Testing URLAbsorber Integration" << std::endl;
std::cout << "=====================================\n" << std::endl;
std::shared_ptr<AkashicRecord> akashic = std::make_shared<AkashicRecord>();
std::shared_ptr<URLAbsorber> webAbsorber = std::make_shared<URLAbsorber>(akashic);
// Test with a simple, reliable URL (HTTP to avoid SSL issues)
std::string testUrl = "http://example.com";
std::cout << "Absorbing: " + testUrl << std::endl;
webAbsorber.absorb(testUrl);
std::cout << "\n📊 AkashicRecord Status:" << std::endl;
std::cout << "Total blocks: " + akashic.getPersistedBlockCount() << std::endl;
std::cout << "Categories: " + akashic.getCategoryCount() << std::endl;
// Test query
std::cout << "\n🔍 Testing query for 'example':" << std::endl;
var results = akashic.query("example");
std::cout << "Found " + results.size() + " matching blocks" << std::endl;
if (!results.isEmpty()) {
std::cout << "\nFirst result:" << std::endl;
std::cout << results.get(0).content << std::endl;
}
// Test with Apache Arrow Wikipedia (will fail SSL, but that's expected)
std::cout << "\n\n🌐 Testing Apache Arrow Wikipedia (HTTPS - will fail SSL):" << std::endl;
std::string arrowUrl = "https://en.wikipedia.org/wiki/Apache_Arrow";
std::cout << "Absorbing: " + arrowUrl << std::endl;
webAbsorber.absorb(arrowUrl);
std::cout << "\n📊 Final AkashicRecord Status:" << std::endl;
std::cout << "Total blocks: " + akashic.getPersistedBlockCount() << std::endl;
std::cout << "Categories: " + akashic.getCategoryCount() << std::endl;
// Test query for Apache Arrow
std::cout << "\n🔍 Testing query for 'Apache Arrow':" << std::endl;
var arrowResults = akashic.query("Apache Arrow");
std::cout << "Found " + arrowResults.size() + " matching blocks" << std::endl;
// Show first few absorbed blocks
if (!arrowResults.isEmpty()) {
std::cout << "\n📄 Sample absorbed content:" << std::endl;
for (int i = 0; i < Math.min(3, arrowResults.size()); i++) {
std::cout << "\n--- Block " + (i+1) + " ---" << std::endl;
std::cout << arrowResults.get(i).category + ": " + arrowResults.get(i).content.substring(0, Math.min(200, arrowResults.get(i).content.length())) << std::endl;
}
}
}
}
