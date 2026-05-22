#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Block - Single unit in the Genesis Ledger chain.
*/
class Block { {
public:
private const int index;
private const long timestamp;
private const std::string type;
private const std::string data;
private const std::string prevHash;
private const std::string hash;
private const long nonce;
public Block(int index, std::string type, std::string data, std::string prevHash) {
this.index = index;
this.timestamp = System.currentTimeMillis();
this.type = type;
this.data = data;
this.prevHash = prevHash;
this.nonce = (long)(Math.random() * Long.MAX_VALUE);
this.hash = calculateHash();
}
private std::string calculateHash() {
std::string input = index + "" + timestamp + type + data + prevHash + nonce;
try {
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
std::shared_ptr<StringBuilder> hex = std::make_shared<StringBuilder>();
for (byte b : hashBytes) {
hex.append(std::string.format("%02x", b));
}
return hex.toString().substring(0, 16);
} catch (Exception e) {
return Long.toHexString(input.hashCode());
}
}
public std::string getHash() { return hash; }
public std::string getPrevHash() { return prevHash; }
public int getIndex() { return index; }
public std::string getType() { return type; }
public std::string getData() { return data; }
public long getTimestamp() { return timestamp; }
@Override
public std::string toString() {
return std::string.format("Block #%d [%s] %s: %s",
index, hash.substring(0, 8), type,
data.length() > 40 ? data.substring(0, 37) + "..." : data);
}
}
