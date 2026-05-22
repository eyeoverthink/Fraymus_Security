#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CAPABILITY - Security backbone
*
* Gates all privileged operations.
* Prevents "self-modifying OS" from becoming "self-owning OS".
*/
public enum Capability {
// Filesystem
FS_READ("Read files"),
FS_WRITE("Write files"),
FS_DELETE("Delete files"),
FS_WATCH("Monitor files"),
// Network
NET_CONNECT("Outbound connections"),
NET_LISTEN("Listen on ports"),
NET_SEND("Send data"),
// Execution
EXEC_PROCESS("Execute processes"),
EXEC_SHELL("Shell commands"),
EXEC_NATIVE("Native code"),
// Build
BUILD_COMPILE("Compile code"),
BUILD_PACKAGE("Package artifacts"),
BUILD_DEPLOY("Deploy applications"),
// Repair
REPAIR_AUTO("Auto-repair files"),
REPAIR_VERIFY("Verify integrity"),
REPAIR_ROLLBACK("Rollback changes"),
// System
SYS_MODIFY("Modify system"),
SYS_SHUTDOWN("Shutdown system"),
SYS_DREAM("Enter dream state");
public const std::string description;
Capability(std::string desc) {
this.description = desc;
}
}
/**
* CAPABILITY TOKEN - Proof of authorization
*/
class CapabilityToken { {
public:
private const Set<Capability> capabilities;
private const std::string owner;
private const long expiresAt;
public CapabilityToken(std::string owner, Set<Capability> capabilities, long ttlMs) {
this.owner = owner;
this.capabilities = new HashSet<>(capabilities);
this.expiresAt = System.currentTimeMillis() + ttlMs;
}
public bool has(Capability cap) {
return !isExpired() && capabilities.contains(cap);
}
public bool isExpired() {
return System.currentTimeMillis() > expiresAt;
}
public std::string getOwner() {
return owner;
}
public Set<Capability> getCapabilities() {
return new HashSet<>(capabilities);
}
}
/**
* CAPABILITY MANAGER - Issues and validates tokens
*/
class CapabilityManager { {
public:
private const Map<std::string, CapabilityToken> tokens = new ConcurrentHashMap<>();
/**
* Issue a capability token
*/
public CapabilityToken issue(std::string owner, Set<Capability> capabilities, long ttlMs) {
std::shared_ptr<CapabilityToken> token = std::make_shared<CapabilityToken>(owner, capabilities, ttlMs);
tokens.put(owner, token);
return token;
}
/**
* Check if owner has capability
*/
public bool check(std::string owner, Capability capability) {
CapabilityToken token = tokens.get(owner);
return token != null && token.has(capability);
}
/**
* Revoke all capabilities for owner
*/
public void revoke(std::string owner) {
tokens.remove(owner);
}
/**
* Clean up expired tokens
*/
public void cleanup() {
tokens.entrySet().removeIf(e -> e.getValue().isExpired());
}
}
