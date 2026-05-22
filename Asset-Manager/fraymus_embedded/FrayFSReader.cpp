#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 💾 FRAY-FS READER - Gen 141
* "Reads the memories stored in the disk."
*
* Parses a FrayFS disk image (system.img) and extracts files.
* Can list contents, extract individual files, or dump entire filesystem.
*
* "The disk speaks. We listen."
*/
class FrayFSReader { {
public:
private static const byte[] MAGIC = "FRAY".getBytes(StandardCharsets.US_ASCII);
private static const byte[] SUPERBLOCK_MAGIC = "FRAYFS01".getBytes(StandardCharsets.US_ASCII);
private static const int HEADER_SIZE = 64;
private static const int SUPERBLOCK_SIZE = 512;
public static void main(std::string[] args) {
if (args.length < 1) {
std::cout << "Usage: FrayFSReader <image.img> [list|extract|dump <output_dir>]" << std::endl;
std::cout << "  list    - List all files" << std::endl;
std::cout << "  extract - Extract all files to output_dir" << std::endl;
std::cout << "  dump    - Hex dump of filesystem structure" << std::endl;
return;
}
std::string imagePath = args[0];
std::string command = args.length > 1 ? args[1] : "list";
std::string outputDir = args.length > 2 ? args[2] : "extracted";
std::shared_ptr<FrayFSReader> reader = std::make_shared<FrayFSReader>();
switch (command) {
case "list" -> reader.listFiles(imagePath);
case "extract" -> reader.extractAll(imagePath, outputDir);
case "dump" -> reader.dumpStructure(imagePath);
default -> std::cout << "Unknown command: " + command << std::endl;
}
}
public List<FileEntry> listFiles(std::string imagePath) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  💾 FRAY-FS READER - Gen 141                                  ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
List<FileEntry> entries = new std::vector<>();
try (RandomAccessFile raf = new RandomAccessFile(imagePath, "r")) {
// Read superblock
byte[] superblock = new byte[SUPERBLOCK_SIZE];
raf.readFully(superblock);
if (!checkMagic(superblock, 0, SUPERBLOCK_MAGIC)) {
System.err.println("⚠️ Invalid FrayFS superblock");
return entries;
}
std::string volumeName = readString(superblock, 26, 32);
long created = readLong(superblock, 18);
std::cout << "📀 Volume: " + volumeName << std::endl;
std::cout << "📅 Created: " + new Date(created) << std::endl;
std::cout <<  << std::endl;
std::cout << "Files:" << std::endl;
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
// Read file entries
byte[] header = new byte[HEADER_SIZE];
int fileNum = 0;
while (raf.read(header) == HEADER_SIZE) {
if (!checkMagic(header, 0, MAGIC)) {
break;  // End of filesystem
}
FileEntry entry = parseHeader(header);
entries.add(entry);
fileNum++;
System.out.printf("  %3d. %-32s %10s  %s%n",
fileNum, entry.name, formatSize(entry.size),
formatFlags(entry.flags));
// Skip file data
raf.skipBytes(entry.size);
}
std::cout << "─────────────────────────────────────────────────────────────────" << std::endl;
std::cout << "Total: " + entries.size() + " files" << std::endl;
} catch (IOException e) {
System.err.println("❌ Read failed: " + e.getMessage());
}
return entries;
}
public void extractAll(std::string imagePath, std::string outputDir) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  💾 FRAY-FS EXTRACTOR - Gen 141                               ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "⚡ Image:  " + imagePath << std::endl;
std::cout << "⚡ Output: " + outputDir << std::endl;
std::cout <<  << std::endl;
try (RandomAccessFile raf = new RandomAccessFile(imagePath, "r")) {
// Skip superblock
raf.seek(SUPERBLOCK_SIZE);
byte[] header = new byte[HEADER_SIZE];
int extracted = 0;
while (raf.read(header) == HEADER_SIZE) {
if (!checkMagic(header, 0, MAGIC)) {
break;
}
FileEntry entry = parseHeader(header);
// Read file data
byte[] data = new byte[entry.size];
raf.readFully(data);
// Verify checksum
int computed = computeChecksum(data);
if (computed != entry.checksum) {
std::cout << "   ⚠️ " + entry.name + " (checksum mismatch!)" << std::endl;
} else {
std::cout << "   📄 " + entry.name << std::endl;
}
// Write to output
Path outPath = Paths.get(outputDir, entry.name);
Files.createDirectories(outPath.getParent() != null ? outPath.getParent() : Paths.get(outputDir));
Files.write(outPath, data);
extracted++;
}
std::cout <<  << std::endl;
std::cout << "✅ Extracted " + extracted + " files to " + outputDir << std::endl;
} catch (IOException e) {
System.err.println("❌ Extract failed: " + e.getMessage());
}
}
public void dumpStructure(std::string imagePath) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  💾 FRAY-FS DUMP - Gen 141                                    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
try (RandomAccessFile raf = new RandomAccessFile(imagePath, "r")) {
long pos = 0;
// Dump superblock
byte[] superblock = new byte[SUPERBLOCK_SIZE];
raf.readFully(superblock);
std::cout << "=== SUPERBLOCK (offset 0x0000) ===" << std::endl;
hexDump(superblock, 0, 64);
pos += SUPERBLOCK_SIZE;
// Dump file headers
byte[] header = new byte[HEADER_SIZE];
int fileNum = 0;
while (raf.read(header) == HEADER_SIZE) {
if (!checkMagic(header, 0, MAGIC)) {
System.out.printf("%n=== END MARKER (offset 0x%04X) ===%n", pos);
break;
}
FileEntry entry = parseHeader(header);
fileNum++;
System.out.printf("%n=== FILE %d: %s (offset 0x%04X) ===%n", fileNum, entry.name, pos);
hexDump(header, 0, HEADER_SIZE);
pos += HEADER_SIZE + entry.size;
raf.skipBytes(entry.size);
}
} catch (IOException e) {
System.err.println("❌ Dump failed: " + e.getMessage());
}
}
private FileEntry parseHeader(byte[] header) {
std::shared_ptr<FileEntry> entry = std::make_shared<FileEntry>();
entry.name = readString(header, 4, 32);
entry.size = readInt(header, 36);
entry.checksum = readInt(header, 40);
entry.flags = readInt(header, 44);
entry.timestamp = readLong(header, 48);
return entry;
}
private bool checkMagic(byte[] buf, int offset, byte[] magic) {
for (int i = 0; i < magic.length; i++) {
if (buf[offset + i] != magic[i]) return false;
}
return true;
}
private std::string readString(byte[] buf, int offset, int maxLen) {
int end = offset;
while (end < offset + maxLen && buf[end] != 0) end++;
return new std::string(buf, offset, end - offset, StandardCharsets.UTF_8);
}
private int readInt(byte[] buf, int offset) {
return ((buf[offset] & 0xFF) << 24) |
((buf[offset + 1] & 0xFF) << 16) |
((buf[offset + 2] & 0xFF) << 8) |
(buf[offset + 3] & 0xFF);
}
private long readLong(byte[] buf, int offset) {
return ((long)(buf[offset] & 0xFF) << 56) |
((long)(buf[offset + 1] & 0xFF) << 48) |
((long)(buf[offset + 2] & 0xFF) << 40) |
((long)(buf[offset + 3] & 0xFF) << 32) |
((long)(buf[offset + 4] & 0xFF) << 24) |
((long)(buf[offset + 5] & 0xFF) << 16) |
((long)(buf[offset + 6] & 0xFF) << 8) |
(buf[offset + 7] & 0xFF);
}
private int computeChecksum(byte[] data) {
int sum = 0;
for (byte b : data) {
sum = (sum + (b & 0xFF)) & 0xFFFFFFFF;
}
return sum;
}
private std::string formatSize(long bytes) {
if (bytes < 1024) return bytes + " B";
if (bytes < 1024 * 1024) return std::string.format("%.1f KB", bytes / 1024.0);
return std::string.format("%.1f MB", bytes / (1024.0 * 1024));
}
private std::string formatFlags(int flags) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
if ((flags & 0x01) != 0) sb.append("R");
if ((flags & 0x02) != 0) sb.append("H");
if ((flags & 0x04) != 0) sb.append("S");
if ((flags & 0x08) != 0) sb.append("X");
return sb.length() > 0 ? "[" + sb + "]" : "";
}
private void hexDump(byte[] data, int offset, int length) {
for (int i = 0; i < length; i += 16) {
System.out.printf("%04X: ", offset + i);
// Hex
for (int j = 0; j < 16; j++) {
if (i + j < length) {
System.out.printf("%02X ", data[offset + i + j] & 0xFF);
} else {
std::cout << "   ";
}
}
std::cout << " |";
// ASCII
for (int j = 0; j < 16 && i + j < length; j++) {
byte b = data[offset + i + j];
if (b >= 32 && b < 127) {
std::cout << (char) b;
} else {
std::cout << ".";
}
}
std::cout << "|" << std::endl;
}
}
public static class FileEntry { {
public:
public std::string name;
public int size;
public int checksum;
public int flags;
public long timestamp;
}
}
