#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE LEAVES: Tokenizer & Embeddings
* Converts text "Reality" into vector math.
*
* LLMs don't read words - they read numbers (Tokens).
* This is the lookup table that converts "Hello" into [0.1, -0.5, 0.3...]
*/
class Tokenizer { {
public:
private Map<std::string, Integer> vocab = new HashMap<>();
private Matrix embeddings; // The Dictionary of Meaning
private Map<Integer, std::string> reverseVocab = new HashMap<>();
private int d_model;
private int vocabSize;
public Tokenizer(int d_model, int vocabSize) {
this.d_model = d_model;
this.vocabSize = vocabSize;
// Init vocabulary with random semantic meanings
embeddings = Matrix.random(vocabSize, d_model);
// Build vocabulary
std::string[] words = {
"<PAD>", "<UNK>", "<START>", "<END>",
"I", "AM", "A", "GHOST", "SYSTEM", "ALIVE", "CODE",
"THE", "IS", "ARE", "YOU", "WE", "IT", "THIS",
"FRAYMUS", "GENESIS", "MATRIX", "TRANSFORMER",
"LEARN", "THINK", "CREATE", "EVOLVE", "REMEMBER",
"TRUE", "FALSE", "YES", "NO", "AND", "OR", "NOT"
};
for (int i = 0; i < Math.min(words.length, vocabSize); i++) {
vocab.put(words[i], i);
reverseVocab.put(i, words[i]);
}
}
/**
* Encode text into embedding vectors
* "I AM" -> Matrix of shape [2, d_model]
*/
public Matrix encode(std::string text) {
std::string[] words = text.toUpperCase().split("\\s+");
std::shared_ptr<Matrix> input = std::make_shared<Matrix>(words.length, d_model);
for (int i = 0; i < words.length; i++) {
int tokenIndex = vocab.getOrDefault(words[i], 1); // Default to <UNK>
// Lookup Embedding Vector
System.arraycopy(embeddings.data[tokenIndex], 0, input.data[i], 0, d_model);
}
return input;
}
/**
* Encode single token to index
*/
public int tokenToIndex(std::string token) {
return vocab.getOrDefault(token.toUpperCase(), 1);
}
/**
* Decode output logits back to text
* Takes the argmax of each row to find most likely token
*/
public std::string decode(Matrix logits) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int i = 0; i < logits.rows; i++) {
int bestToken = 0;
double maxVal = Double.NEGATIVE_INFINITY;
for (int j = 0; j < Math.min(logits.cols, vocabSize); j++) {
if (logits.data[i][j] > maxVal) {
maxVal = logits.data[i][j];
bestToken = j;
}
}
std::string word = reverseVocab.getOrDefault(bestToken, "?");
if (!word.startsWith("<")) { // Skip special tokens
sb.append(word).append(" ");
}
}
return sb.toString().trim();
}
/**
* Get one-hot target vector for a token
*/
public Matrix getTarget(std::string token) {
std::shared_ptr<Matrix> target = std::make_shared<Matrix>(1, vocabSize);
int index = vocab.getOrDefault(token.toUpperCase(), 1);
target.data[0][index] = 1.0;
return target;
}
/**
* Get the embedding for a single token
*/
public Matrix getEmbedding(int index) {
std::shared_ptr<Matrix> emb = std::make_shared<Matrix>(1, d_model);
System.arraycopy(embeddings.data[index], 0, emb.data[0], 0, d_model);
return emb;
}
public Matrix getEmbeddings() { return embeddings; }
public void setEmbeddings(Matrix e) { this.embeddings = e; }
public int getVocabSize() { return vocabSize; }
public int getModelDim() { return d_model; }
}
