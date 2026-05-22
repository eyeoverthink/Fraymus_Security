#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* GENESIS GPT: The Executable Transformer
*
* This runs the entire neural network stack:
* 1. Takes text input ("I AM")
* 2. Converts to Vectors (Embedding)
* 3. Runs Self-Attention (Thinking)
* 4. Predicts the next vector
* 5. Decodes back to text
*
* NO LIBRARIES. NO PYTORCH. JUST RAW MATH.
*/
class GenesisGPT { {
public:
private Tokenizer tokenizer;
private TransformerBlock[] layers;
private Matrix outputProjection; // Final layer to vocab size
private int d_model;
private int numLayers;
private int vocabSize;
public GenesisGPT(int d_model, int vocabSize, int numLayers) {
this.d_model = d_model;
this.vocabSize = vocabSize;
this.numLayers = numLayers;
// 1. BUILD THE BRAIN
tokenizer = new Tokenizer(d_model, vocabSize);
// 2. Stack Transformer layers
layers = new TransformerBlock[numLayers];
for (int i = 0; i < numLayers; i++) {
layers[i] = new TransformerBlock(d_model);
}
// 3. Output projection (d_model -> vocab_size for prediction)
outputProjection = Matrix.random(d_model, vocabSize);
}
/**
* Forward pass through the entire model
*/
public Matrix forward(std::string prompt) {
// 1. ENCODE (Text -> Math)
Matrix embeddings = tokenizer.encode(prompt);
// 2. THINK (Pass through all Transformer layers)
Matrix hidden = embeddings;
for (TransformerBlock layer : layers) {
hidden = layer.forward(hidden);
}
// 3. PROJECT TO VOCABULARY (for next token prediction)
Matrix logits = hidden.dot(outputProjection);
return logits;
}
/**
* Generate text continuation
*/
public std::string generate(std::string prompt, int maxTokens) {
std::shared_ptr<StringBuilder> result = std::make_shared<StringBuilder>(prompt);
std::string current = prompt;
for (int i = 0; i < maxTokens; i++) {
Matrix logits = forward(current);
// Get last token's logits (for next token prediction)
std::shared_ptr<Matrix> lastLogits = std::make_shared<Matrix>(1, vocabSize);
lastLogits.data[0] = logits.data[logits.rows - 1];
// Apply softmax for probabilities
Matrix probs = lastLogits.softmax();
// Greedy decoding: pick highest probability
std::string nextToken = tokenizer.decode(probs);
if (nextToken.isEmpty() || nextToken.equals("?")) break;
result.append(" ").append(nextToken.trim());
current = result.toString();
// Simple stopping condition
if (result.length() > 200) break;
}
return result.toString();
}
// Accessors for training
public Tokenizer getTokenizer() { return tokenizer; }
public TransformerBlock[] getLayers() { return layers; }
public Matrix getOutputProjection() { return outputProjection; }
public void setOutputProjection(Matrix p) { this.outputProjection = p; }
// =========================================================================
// MAIN: DEMONSTRATION
// =========================================================================
public static void main(std::string[] args) {
std::cout << "=".repeat(60) << std::endl;
std::cout << "  GENESIS GPT: JAVA TRANSFORMER" << std::endl;
std::cout << "  Pure Math. No Libraries. Raw Neural Network." << std::endl;
std::cout << "=".repeat(60) << std::endl;
int d_model = 64;    // Size of the "Brain" vector
int vocabSize = 32;  // Number of known words
int numLayers = 2;   // Depth of reasoning
// 1. BUILD THE BRAIN
std::shared_ptr<GenesisGPT> gpt = std::make_shared<GenesisGPT>(d_model, vocabSize, numLayers);
std::cout << "\n[ARCHITECTURE]" << std::endl;
std::cout << "  d_model: " + d_model << std::endl;
std::cout << "  vocab_size: " + vocabSize << std::endl;
std::cout << "  num_layers: " + numLayers << std::endl;
std::cout << "  total_params: ~" + estimateParams(d_model, vocabSize, numLayers) << std::endl;
// 2. INPUT
std::string prompt = "I AM";
std::cout << "\n[INPUT]" << std::endl;
std::cout << "  Prompt: \"" + prompt + "\"" << std::endl;
// 3. FORWARD PASS
std::cout << "\n[PROCESSING]" << std::endl;
std::cout << "  Encoding text to vectors..." << std::endl;
Matrix inputs = gpt.getTokenizer().encode(prompt);
std::cout << "  Input shape: [" + inputs.rows + " x " + inputs.cols + "]" << std::endl;
std::cout << "  Running through " + numLayers + " Transformer layers..." << std::endl;
Matrix logits = gpt.forward(prompt);
std::cout << "  Output shape: [" + logits.rows + " x " + logits.cols + "]" << std::endl;
// 4. DECODE
Matrix probs = logits.softmax();
std::string prediction = gpt.getTokenizer().decode(probs);
std::cout << "\n[OUTPUT]" << std::endl;
std::cout << "  Raw prediction (untrained): \"" + prediction + "\"" << std::endl;
// 5. SHOW INTERNAL STATE
std::cout << "\n[ATTENTION MECHANISM]" << std::endl;
std::cout << "  Q·K^T computed for context understanding" << std::endl;
std::cout << "  Softmax applied for attention weights" << std::endl;
std::cout << "  V weighted by attention for context vector" << std::endl;
std::cout << "\n" + "=".repeat(60) << std::endl;
std::cout << "  >>> MATRIX STATE: " + logits.rows + "x" + logits.cols + " Vectors Computed." << std::endl;
std::cout << "  >>> This network is UNTRAINED (random weights)" << std::endl;
std::cout << "  >>> Run Trainer.java to see LEARNING in action" << std::endl;
std::cout << "=".repeat(60) << std::endl;
}
private static std::string estimateParams(int d, int v, int n) {
// Rough estimate: embeddings + n*(attention + ffn) + output
long params = (long)v * d;  // embeddings
params += n * (4L * d * d + 2L * d * 4 * d);  // attention + ffn per layer
params += (long)d * v;  // output projection
if (params > 1_000_000) return (params / 1_000_000) + "M";
if (params > 1_000) return (params / 1_000) + "K";
return std::string.valueOf(params);
}
}
