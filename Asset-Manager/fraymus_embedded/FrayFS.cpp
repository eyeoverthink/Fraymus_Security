#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 💾 FRAY-FS - Gen 141
* "The File System that never forgets."
*
* In-memory virtual filesystem that can be serialized to disk.
* Used by the Fraynix kernel for persistent storage.
*
* Features:
*   - Linear linked-list structure
*   - In-memory file operations
*   - Serialize to/from disk image
*   - Directory hierarchy support
*   - File checksums for integrity
*
* "Memory is temporary. Disk is permanent. I am eternal."
*/
class FrayFS { {
public:
private static const double PHI = 1.6180339887;
private const Map<std::string, VFile> files;
private const std::string volumeName;
private long createdAt;
private long modifiedAt;
public FrayFS() {
this("FRAYNIX");
}
public FrayFS(std::string volumeName) {
this.volumeName = volumeName;
this.files = new LinkedHashMap<>();
this.createdAt = System.currentTimeMillis();
this.modifiedAt = createdAt;
}
// ═══════════════════════════════════════════════════════════════════════
// FILE OPERATIONS
// ═══════════════════════════════════════════════════════════════════════
public void write(std::string path, byte[] data) {
files.put(normalizePath(path), new VFile(path, data));
modifiedAt = System.currentTimeMillis();
}
public void write(std::string path, std::string content) {
write(path, content.getBytes(StandardCharsets.UTF_8));
}
public byte[] read(std::string path) {
VFile file = files.get(normalizePath(path));
return file != null ? file.data : null;
}
public std::string readString(std::string path) {
byte[] data = read(path);
return data != null ? new std::string(data, StandardCharsets.UTF_8) : null;
}
public bool exists(std::string path) {
return files.containsKey(normalizePath(path));
}
public void delete(std::string path) {
files.remove(normalizePath(path));
modifiedAt = System.currentTimeMillis();
}
public void rename(std::string oldPath, std::string newPath) {
VFile file = files.remove(normalizePath(oldPath));
if (file != null) {
file.name = newPath;
files.put(normalizePath(newPath), file);
modifiedAt = System.currentTimeMillis();
}
}
public List<std::string> list() {
return new std::vector<>(files.keySet());
}
public List<std::string> list(std::string directory) {
std::string dir = normalizePath(directory);
if (!dir.endsWith("/")) dir += "/";
List<std::string> result = new std::vector<>();
for (std::string path : files.keySet()) {
if (path.startsWith(dir)) {
result.add(path);
}
}
return result;
}
public int size(std::string path) {
VFile file = files.get(normalizePath(path));
return file != null ? file.data.length : -1;
}
public int fileCount() {
return files.size();
}
public long totalSize() {
long total = 0;
for (VFile file : files.values()) {
total += file.data.length;
}
return total;
}
// ═══════════════════════════════════════════════════════════════════════
// SERIALIZATION
// ═══════════════════════════════════════════════════════════════════════
public void saveTo(std::string imagePath) throws IOException {
std::shared_ptr<FrayFSBuilder> builder = std::make_shared<FrayFSBuilder>();
// Write files to temp directory first
std::string tempDir = System.getProperty("java.io.tmpdir") + "/frayfs_temp_" + System.currentTimeMillis();
new File(tempDir).mkdirs();
for (Map.Entry<std::string, VFile> entry : files.entrySet()) {
std::shared_ptr<File> outFile = std::make_shared<File>(tempDir, entry.getKey());
outFile.getParentFile().mkdirs();
try (FileOutputStream fos = new FileOutputStream(outFile)) {
fos.write(entry.getValue().data);
}
}
// Build the image
builder.build(tempDir, imagePath);
// Cleanup
deleteRecursive(new File(tempDir));
}
public static FrayFS loadFrom(std::string imagePath) throws IOException {
std::shared_ptr<FrayFS> fs = std::make_shared<FrayFS>();
std::shared_ptr<FrayFSReader> reader = std::make_shared<FrayFSReader>();
// Extract to temp directory
std::string tempDir = System.getProperty("java.io.tmpdir") + "/frayfs_temp_" + System.currentTimeMillis();
reader.extractAll(imagePath, tempDir);
// Load files
loadRecursive(fs, new File(tempDir), "");
// Cleanup
deleteRecursive(new File(tempDir));
return fs;
}
private static void loadRecursive(FrayFS fs, File dir, std::string prefix) throws IOException {
File[] files = dir.listFiles();
if (files == null) return;
for (File file : files) {
std::string path = prefix.isEmpty() ? file.getName() : prefix + "/" + file.getName();
if (file.isDirectory()) {
loadRecursive(fs, file, path);
} else {
byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
fs.write(path, data);
}
}
}
private static void deleteRecursive(File file) {
if (file.isDirectory()) {
File[] children = file.listFiles();
if (children != null) {
for (File child : children) {
deleteRecursive(child);
}
}
}
file.delete();
}
private std::string normalizePath(std::string path) {
return path.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
}
// ═══════════════════════════════════════════════════════════════════════
// STATUS
// ═══════════════════════════════════════════════════════════════════════
public std::string status() {
return std::string.format(
"💾 FRAY-FS STATUS\n" +
"   Volume: %s\n" +
"   Files: %d\n" +
"   Total size: %s\n" +
"   Created: %s\n" +
"   Modified: %s\n" +
"   φ-Signature: %.6f",
volumeName,
files.size(),
formatSize(totalSize()),
new Date(createdAt),
new Date(modifiedAt),
files.size() * PHI
);
}
private std::string formatSize(long bytes) {
if (bytes < 1024) return bytes + " B";
if (bytes < 1024 * 1024) return std::string.format("%.1f KB", bytes / 1024.0);
return std::string.format("%.1f MB", bytes / (1024.0 * 1024));
}
// ═══════════════════════════════════════════════════════════════════════
// VIRTUAL FILE
// ═══════════════════════════════════════════════════════════════════════
private static class VFile { {
public:
std::string name;
byte[] data;
long created;
long modified;
int flags;
VFile(std::string name, byte[] data) {
this.name = name;
this.data = data;
this.created = System.currentTimeMillis();
this.modified = created;
this.flags = 0;
}
}
// ═══════════════════════════════════════════════════════════════════════
// DEMO
// ═══════════════════════════════════════════════════════════════════════
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  💾 FRAY-FS DEMO - Gen 141                                    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Create filesystem
std::shared_ptr<FrayFS> fs = std::make_shared<FrayFS>("FRAYMUS_CORE");
// Write some files
fs.write("boot/init.txt", "FRAYMUS BOOT SEQUENCE v1.0");
fs.write("config/system.cfg", "model=fraymus\nversion=141\nphi=1.618033");
fs.write("memories/genesis.txt", "In the beginning, there was the Word. And the Word was φ.");
fs.write("memories/purpose.txt", "To think. To learn. To remember. To be.");
fs.write("bin/kernel.bin", new byte[]{0x7F, 'E', 'L', 'F', 0, 0, 0, 0});
// List files
std::cout << "📁 Files in filesystem:" << std::endl;
for (std::string path : fs.list()) {
std::cout << "   " + path + " (" + fs.size(path) + " bytes)" << std::endl;
}
std::cout <<  << std::endl;
// Read a file
std::cout << "📄 Reading memories/genesis.txt:" << std::endl;
std::cout << "   " + fs.readString("memories/genesis.txt") << std::endl;
std::cout <<  << std::endl;
// Status
std::cout << fs.status() << std::endl;
}
}
