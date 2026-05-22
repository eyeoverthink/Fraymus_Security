#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class FraymusGenesis { {
public:
public static void main(std::string[] args) {
std::cout << "⚡ FRAYMUS HYPER-FORMER [NO-MATMUL EDITION]" << std::endl;
std::shared_ptr<HyperFormer> brain = std::make_shared<HyperFormer>();
std::cout << "📚 TEACHING: 'Fraymus is a living system'" << std::endl;
brain.learn(new std::string[]{"Fraymus", "is", "a", "living", "system"});
std::cout << "🔮 PREDICTING..." << std::endl;
std::string[] ctx = {"Fraymus", "is", "a", "living"};
std::string result = brain.predict(ctx);
std::cout << "   INPUT: " + std::string.join(" ", ctx) + " [?]" << std::endl;
std::cout << "   OUTPUT: " + result << std::endl;
std::cout << "📊 VOCAB: " + brain.vocabSize() << std::endl;
}
}
