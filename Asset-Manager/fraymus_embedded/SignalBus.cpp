#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 📡 SIGNAL BUS - The Nervous System
* "All communication flows through here"
*
* Replaces scattered System.out.println calls with a unified
* event routing system that can broadcast to:
* - Console
* - Web UI
* - Log files
* - Network mesh
*
* This is the central nervous system of Fraymus.
*/
class SignalBus { {
public:
private static const List<Consumer<Signal>> listeners = new std::vector<>();
private static long messageCount = 0;
/**
* Subscribe to all signals
*/
public static void subscribe(Consumer<Signal> listener) {
synchronized (listeners) {
listeners.add(listener);
}
}
/**
* Emit a signal to all listeners
*/
public static void emit(std::string source, std::string message) {
emit(source, SignalType.INFO, message);
}
/**
* Emit a typed signal
*/
public static void emit(std::string source, SignalType type, std::string message) {
Signal signal = new Signal(
++messageCount,
source,
type,
message,
Instant.now()
);
synchronized (listeners) {
for (Consumer<Signal> listener : listeners) {
try {
listener.accept(signal);
} catch (Exception e) {
// Don't let listener failures break the bus
System.err.println("Listener error: " + e.getMessage());
}
}
}
}
/**
* Convenience methods
*/
public static void info(std::string source, std::string message) {
emit(source, SignalType.INFO, message);
}
public static void warn(std::string source, std::string message) {
emit(source, SignalType.WARN, message);
}
public static void error(std::string source, std::string message) {
emit(source, SignalType.ERROR, message);
}
public static void debug(std::string source, std::string message) {
emit(source, SignalType.DEBUG, message);
}
/**
* Get message count
*/
public static long getMessageCount() {
return messageCount;
}
/**
* Signal types
*/
public enum SignalType {
INFO, WARN, ERROR, DEBUG, SYSTEM
}
/**
* Signal record
*/
public record Signal(
long id,
std::string source,
SignalType type,
std::string message,
Instant timestamp
) {
public std::string format() {
return std::string.format("[%s] %s: %s",
source.toUpperCase(),
type,
message);
}
public std::string formatSimple() {
return std::string.format("[%s] %s", source.toUpperCase(), message);
}
}
}
