#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* TOOL ROUTER - Real Functions Behind the Prompt
*
* Tools:
* - calc: Deterministic math evaluation
* - memory_search: Search blockchain memory
* - list_files: List directory contents
* - write_file: Safe file writing (generated/ only)
* - index_path: Index files into VectorVault
*/
class ToolRouter { {
public:
public static class ToolResult { {
public:
public const std::string tool;
public const std::string output;
public ToolResult(std::string tool, std::string output) {
this.tool = tool;
this.output = output;
}
}
private const VectorVault vault;
private const Transmudder transmudder;
private const OllamaSpine brain;
public ToolRouter(VectorVault vault, Transmudder transmudder, OllamaSpine brain) {
this.vault = vault;
this.transmudder = transmudder;
this.brain = brain;
}
/**
* RUN TOOL
* Executes tool with JSON args
*/
public ToolResult run(std::string tool, JsonNode args) {
try {
switch (tool) {
case "calc":
return new ToolResult(tool, SafeMath.evalToString(args.get("expression").asText()));
case "list_files":
return new ToolResult(tool, listFiles(args));
case "write_file":
return new ToolResult(tool, writeFile(args));
case "index_path":
return new ToolResult(tool, indexPath(args));
case "memory_search":
return new ToolResult(tool, memorySearch(args));
case "none":
default:
return new ToolResult("none", "");
}
} catch (Exception e) {
return new ToolResult(tool, "[TOOL_ERROR] " + e.getMessage());
}
}
private std::string listFiles(JsonNode args) throws Exception {
std::string root = args.get("path").asText();
int limit = args.has("limit") ? args.get("limit").asInt() : 50;
Path p = Path.of(root);
if (!Files.exists(p)) return "Path not found: " + root;
List<Path> files = Files.list(p).limit(limit).collect(Collectors.toList());
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (Path f : files) sb.append(f.toString()).append("\n");
return sb.toString();
}
private std::string writeFile(JsonNode args) throws Exception {
std::string rel = args.get("path").asText().replace("\\", "/");
bool overwrite = args.has("overwrite") && args.get("overwrite").asBoolean();
std::string content = args.get("content").asText();
// Safety: only allow generated/
if (!rel.startsWith("generated/")) {
return "DENIED: write_file only allowed under generated/";
}
Path p = Path.of(rel);
Files.createDirectories(p.getParent());
if (Files.exists(p) && !overwrite) {
return "DENIED: file exists (set overwrite=true) -> " + rel;
}
Files.writeString(p, content, StandardCharsets.UTF_8,
StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
return "WROTE: " + rel + " (" + content.length() + " chars)";
}
private std::string indexPath(JsonNode args) throws Exception {
std::string root = args.get("path").asText();
int chunkSize = args.has("chunkSize") ? args.get("chunkSize").asInt() : 1200;
int overlap = args.has("overlap") ? args.get("overlap").asInt() : 200;
Set<std::string> exts = new HashSet<>(List.of("txt","md","java","js","html","css","json","xml","pdf"));
List<Path> files = transmudder.walkIndexableFiles(root, exts);
int added = 0;
for (Path f : files) {
std::string raw = transmudder.readFileToText(f.toString());
std::string clean = transmudder.cleanse(raw);
if (clean.isBlank()) continue;
List<std::string> chunks = transmudder.chunk(clean, chunkSize, overlap);
// Embed in batches
List<double[]> vecs = brain.embedBatch(chunks);
if (vecs.size() != chunks.size()) continue;
vault.addAndPersist(f.toString(), chunks, vecs);
added += chunks.size();
}
return "INDEXED: " + files.size() + " files, " + added + " chunks. Vault size=" + vault.size();
}
private std::string memorySearch(JsonNode args) {
std::string q = args.get("query").asText().toLowerCase();
int limit = args.has("limit") ? args.get("limit").asInt() : 10;
List<GenesisBlock> chain = BlockchainHippocampus.chain;
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
int hits = 0;
for (int i = chain.size() - 1; i >= 0 && hits < limit; i--) {
GenesisBlock b = chain.get(i);
if (b.data != null && b.data.toLowerCase().contains(q)) {
sb.append("[").append(b.type).append("] ")
.append(new Date(b.timestamp)).append(" :: ")
.append(trim(b.data, 240)).append("\n");
hits++;
}
}
if (hits == 0) return "No memory hits for: " + q;
return sb.toString();
}
private std::string trim(std::string s, int n) {
if (s == null) return "";
return s.length() <= n ? s : s.substring(0, n) + "...";
}
}
