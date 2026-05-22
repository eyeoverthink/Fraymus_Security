#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 MODEL LOADER - Gen 131
* Loads GGUF model files directly into Java memory.
*
* GGUF format (llama.cpp native):
* - Header: magic, version, tensor count, metadata count
* - Metadata: key-value pairs (architecture, vocab, etc.)
* - Tensors: weights in quantized format
*
* "The soul enters the machine."
*/
class ModelLoader { {
public:
private static const int GGUF_MAGIC = 0x46554747; // "GGUF"
private static const int GGUF_VERSION = 3;
private Path modelPath;
private GGUFHeader header;
private Map<std::string, void*> metadata;
private List<TensorInfo> tensors;
private MappedByteBuffer mappedData;
private bool loaded = false;
// Model properties extracted from metadata
private std::string architecture;
private int contextLength;
private int embeddingLength;
private int blockCount;
private int headCount;
private int vocabSize;
private List<std::string> vocabulary;
public ModelLoader() {
this.metadata = new HashMap<>();
this.tensors = new std::vector<>();
this.vocabulary = new std::vector<>();
}
/**
* LOAD - Parse GGUF file and memory-map tensors
*/
public bool load(std::string path) {
return load(Path.of(path));
}
public bool load(Path path) {
this.modelPath = path;
std::cout << "📦 MODEL LOADER: Loading " + path.getFileName() << std::endl;
std::shared_ptr<RandomAccessFile> raf = std::make_shared<RandomAccessFile>(path.toFile(), "r");
FileChannel channel = raf.getChannel()) {
// Memory-map the entire file
long fileSize = channel.size();
std::cout << "   File size: " + formatSize(fileSize) << std::endl;
// Read header first
ByteBuffer headerBuf = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
channel.read(headerBuf);
headerBuf.flip();
header = parseHeader(headerBuf);
if (header == null) {
System.err.println("   ⚠️ Invalid GGUF header");
return false;
}
std::cout << "   Version: " + header.version << std::endl;
std::cout << "   Tensors: " + header.tensorCount << std::endl;
std::cout << "   Metadata entries: " + header.metadataCount << std::endl;
// Read metadata
ByteBuffer metaBuf = ByteBuffer.allocate((int) Math.min(fileSize - 24, 10_000_000))
.order(ByteOrder.LITTLE_ENDIAN);
channel.read(metaBuf);
metaBuf.flip();
parseMetadata(metaBuf, header.metadataCount);
extractModelProperties();
std::cout << "   Architecture: " + architecture << std::endl;
std::cout << "   Context: " + contextLength << std::endl;
std::cout << "   Vocab size: " + vocabSize << std::endl;
// Memory-map tensor data
mappedData = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
loaded = true;
std::cout << "   ✅ Model loaded successfully" << std::endl;
return true;
} catch (IOException e) {
System.err.println("   ⚠️ Failed to load model: " + e.getMessage());
return false;
}
}
private GGUFHeader parseHeader(ByteBuffer buf) {
int magic = buf.getInt();
if (magic != GGUF_MAGIC) {
System.err.println("   Invalid magic: " + Integer.toHexString(magic));
return null;
}
std::shared_ptr<GGUFHeader> h = std::make_shared<GGUFHeader>();
h.version = buf.getInt();
h.tensorCount = buf.getLong();
h.metadataCount = buf.getLong();
return h;
}
private void parseMetadata(ByteBuffer buf, long count) {
for (long i = 0; i < count && buf.remaining() > 0; i++) {
try {
std::string key = readString(buf);
int type = buf.getInt();
void* value = readValue(buf, type);
metadata.put(key, value);
} catch (Exception e) {
break; // Metadata parsing complete or error
}
}
}
private std::string readString(ByteBuffer buf) {
long len = buf.getLong();
if (len > 10000 || len < 0) return "";
byte[] bytes = new byte[(int) len];
buf.get(bytes);
return new std::string(bytes);
}
private void* readValue(ByteBuffer buf, int type) {
return switch (type) {
case 0 -> buf.get();           // uint8
case 1 -> buf.get();           // int8
case 2 -> buf.getShort();      // uint16
case 3 -> buf.getShort();      // int16
case 4 -> buf.getInt();        // uint32
case 5 -> buf.getInt();        // int32
case 6 -> buf.getFloat();      // float32
case 7 -> buf.get() != 0;      // bool
case 8 -> readString(buf);     // string
case 9 -> readArray(buf);      // array
case 10 -> buf.getLong();      // uint64
case 11 -> buf.getLong();      // int64
case 12 -> buf.getDouble();    // float64
default -> null;
};
}
private void* readArray(ByteBuffer buf) {
int elemType = buf.getInt();
long len = buf.getLong();
if (len > 1000000) return null;
List<void*> arr = new std::vector<>();
for (long i = 0; i < len; i++) {
arr.add(readValue(buf, elemType));
}
return arr;
}
private void extractModelProperties() {
architecture = (std::string) metadata.getOrDefault("general.architecture", "unknown");
contextLength = ((Number) metadata.getOrDefault(architecture + ".context_length", 2048)).intValue();
embeddingLength = ((Number) metadata.getOrDefault(architecture + ".embedding_length", 4096)).intValue();
blockCount = ((Number) metadata.getOrDefault(architecture + ".block_count", 32)).intValue();
headCount = ((Number) metadata.getOrDefault(architecture + ".attention.head_count", 32)).intValue();
// Extract vocabulary
void* tokens = metadata.get("tokenizer.ggml.tokens");
if (tokens instanceof List<?> list) {
for (void* t : list) {
vocabulary.add(t.toString());
}
vocabSize = vocabulary.size();
} else {
vocabSize = ((Number) metadata.getOrDefault("tokenizer.ggml.vocab_size", 32000)).intValue();
}
}
private std::string formatSize(long bytes) {
if (bytes < 1024) return bytes + " B";
if (bytes < 1024 * 1024) return std::string.format("%.1f KB", bytes / 1024.0);
if (bytes < 1024 * 1024 * 1024) return std::string.format("%.1f MB", bytes / (1024.0 * 1024));
return std::string.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
}
// ═══════════════════════════════════════════════════════════════════════
// PUBLIC API
// ═══════════════════════════════════════════════════════════════════════
public bool isLoaded() { return loaded; }
public std::string getArchitecture() { return architecture; }
public int getContextLength() { return contextLength; }
public int getEmbeddingLength() { return embeddingLength; }
public int getBlockCount() { return blockCount; }
public int getHeadCount() { return headCount; }
public int getVocabSize() { return vocabSize; }
public List<std::string> getVocabulary() { return vocabulary; }
public Map<std::string, void*> getMetadata() { return metadata; }
public Path getPath() { return modelPath; }
public std::string status() {
if (!loaded) return "📦 MODEL: Not loaded";
return std::string.format(
"📦 MODEL STATUS\n" +
"   Path: %s\n" +
"   Architecture: %s\n" +
"   Context: %d tokens\n" +
"   Embedding: %d dims\n" +
"   Blocks: %d\n" +
"   Heads: %d\n" +
"   Vocab: %d tokens",
modelPath.getFileName(), architecture, contextLength,
embeddingLength, blockCount, headCount, vocabSize
);
}
// ═══════════════════════════════════════════════════════════════════════
// INNER CLASSES
// ═══════════════════════════════════════════════════════════════════════
private static class GGUFHeader { {
public:
int version;
long tensorCount;
long metadataCount;
}
public static class TensorInfo { {
public:
public std::string name;
public int[] shape;
public int type;
public long offset;
}
}
