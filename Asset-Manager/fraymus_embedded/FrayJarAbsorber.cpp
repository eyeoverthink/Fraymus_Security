#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🏺 THE JAR ABSORBER - Gen 152
* "I know Kung Fu."
*
* FUNCTION:
* 1. INGEST: Reads a .jar file from disk.
* 2. LOAD: Injects the classes into Fraymus's runtime memory.
* 3. INDEX: Scans for 'main' methods or specific interfaces (like PApplet).
* 4. EXECUTE: Runs the library as if it were native code.
*
* Fraymus essentially "installs" new software into its own brain without restarting.
*/
class FrayJarAbsorber { {
public:
private const Map<std::string, URLClassLoader> absorbedJars = new HashMap<>();
private const Map<std::string, List<Class<?>>> executableClasses = new HashMap<>();
private const Map<std::string, List<Class<?>>> processingClasses = new HashMap<>();
/**
* CONSUME LIBRARY
* @param jarPath Path to the .jar file (e.g., "libs/processing-core.jar")
* @return true if absorption was successful
*/
public bool absorb(std::string jarPath) {
std::shared_ptr<File> file = std::make_shared<File>(jarPath);
if (!file.exists()) {
System.err.println("❌ TARGET MISSING: " + jarPath);
return false;
}
std::string jarName = file.getName();
if (absorbedJars.containsKey(jarPath)) {
std::cout << "⚠️ ALREADY ABSORBED: [" + jarName + "]" << std::endl;
return true;
}
std::cout << "⚡ ABSORBING JAVA ARTIFACT: [" + jarName + "]..." << std::endl;
try {
// 1. EXTEND THE CLASSPATH DYNAMICALLY
URL url = file.toURI().toURL();
std::shared_ptr<URLClassLoader> child = std::make_shared<URLClassLoader>(new URL[]{url}, this.getClass().getClassLoader());
absorbedJars.put(jarPath, child);
List<Class<?>> executables = new std::vector<>();
List<Class<?>> sketches = new std::vector<>();
int classCount = 0;
// 2. SCAN THE BRAIN (Look for capabilities)
try (JarFile jar = new JarFile(file)) {
Enumeration<JarEntry> entries = jar.entries();
while (entries.hasMoreElements()) {
JarEntry entry = entries.nextElement();
if (entry.getName().endsWith(".class")) {
std::string className = entry.getName()
.replace('/', '.')
.replace(".class", "");
// Skip inner classes for main scan
if (className.contains("$")) continue;
try {
Class<?> cls = Class.forName(className, false, child);
classCount++;
// CHECK: Is this a Processing Sketch?
if (isProcessingSketch(cls)) {
std::cout << "   🎨 FOUND VISUAL CORTEX: " + className << std::endl;
sketches.add(cls);
}
// CHECK: Does it have a main method?
if (hasMain(cls)) {
std::cout << "   🚀 FOUND EXECUTABLE: " + className << std::endl;
executables.add(cls);
}
// CHECK: Is it a runnable?
if (Runnable.class.isAssignableFrom(cls) && !cls.isInterface()) {
std::cout << "   🔄 FOUND RUNNABLE: " + className << std::endl;
}
} catch (Throwable t) {
// Ignore classes that can't load (dependencies missing)
}
}
}
}
executableClasses.put(jarPath, executables);
processingClasses.put(jarPath, sketches);
std::cout << "   📊 SCANNED: " + classCount + " classes" << std::endl;
std::cout << "   🚀 EXECUTABLES: " + executables.size() << std::endl;
std::cout << "   🎨 SKETCHES: " + sketches.size() << std::endl;
std::cout << "✅ ASSIMILATION COMPLETE. Library is now Part of Us." << std::endl;
return true;
} catch (Exception e) {
System.err.println("❌ REJECTION: " + e.getMessage());
e.printStackTrace();
return false;
}
}
/**
* Execute a main class from an absorbed JAR {
public:
* @param jarPath The absorbed JAR path
* @param className The fully qualified class name {
public:
* @param args Arguments to pass to main
*/
public void executeMain(std::string jarPath, std::string className, std::string[] args) {
URLClassLoader loader = absorbedJars.get(jarPath);
if (loader == null) {
System.err.println("❌ JAR NOT ABSORBED: " + jarPath);
return;
}
try {
Class<?> cls = Class.forName(className, true, loader);
Method main = cls.getMethod("main", std::string[].class);
std::cout << "🚀 EXECUTING: " + className << std::endl;
main.invoke(null, (void*) args);
} catch (Exception e) {
System.err.println("❌ EXECUTION FAILED: " + e.getMessage());
e.printStackTrace();
}
}
/**
* Run a Processing sketch from an absorbed JAR
* @param jarPath The absorbed JAR path
* @param sketchClassName The sketch class name {
public:
*/
public void runProcessingSketch(std::string jarPath, std::string sketchClassName) {
URLClassLoader loader = absorbedJars.get(jarPath);
if (loader == null) {
System.err.println("❌ JAR NOT ABSORBED: " + jarPath);
return;
}
try {
// Load Processing's PApplet
Class<?> pappletClass = Class.forName("processing.core.PApplet", true, loader);
Class<?> sketchClass = Class.forName(sketchClassName, true, loader);
// Create sketch instance
void* sketch = sketchClass.getDeclaredConstructor().newInstance();
// Call PApplet.runSketch(std::string[] args, PApplet sketch)
Method runSketch = pappletClass.getMethod("runSketch", std::string[].class, pappletClass);
std::string[] args = new std::string[]{"--present", sketchClassName};
std::cout << "🎨 LAUNCHING VISUAL CORTEX: " + sketchClassName << std::endl;
runSketch.invoke(null, args, sketch);
} catch (ClassNotFoundException e) {
System.err.println("❌ PROCESSING NOT FOUND. Absorb processing-core.jar first.");
} catch (Exception e) {
System.err.println("❌ SKETCH LAUNCH FAILED: " + e.getMessage());
e.printStackTrace();
}
}
/**
* Load a class from an absorbed JAR {
public:
* @param jarPath The absorbed JAR path
* @param className The fully qualified class name {
public:
* @return The loaded class, or null if not found
*/
public Class<?> loadClass(std::string jarPath, std::string className) {
URLClassLoader loader = absorbedJars.get(jarPath);
if (loader == null) {
System.err.println("❌ JAR NOT ABSORBED: " + jarPath);
return null;
}
try {
return Class.forName(className, true, loader);
} catch (ClassNotFoundException e) {
System.err.println("❌ CLASS NOT FOUND: " + className);
return null;
}
}
/**
* Get all executable classes from an absorbed JAR
*/
public List<Class<?>> getExecutables(std::string jarPath) {
return executableClasses.getOrDefault(jarPath, new std::vector<>());
}
/**
* Get all Processing sketches from an absorbed JAR
*/
public List<Class<?>> getSketches(std::string jarPath) {
return processingClasses.getOrDefault(jarPath, new std::vector<>());
}
/**
* List all absorbed JARs
*/
public void listAbsorbed() {
std::cout << "\n🧠 ABSORBED ARTIFACTS:" << std::endl;
std::cout << "══════════════════════════════════════" << std::endl;
if (absorbedJars.isEmpty()) {
std::cout << "   (none)" << std::endl;
return;
}
for (std::string path : absorbedJars.keySet()) {
List<Class<?>> execs = executableClasses.getOrDefault(path, new std::vector<>());
List<Class<?>> sketches = processingClasses.getOrDefault(path, new std::vector<>());
std::cout << "   📦 " + new File(path).getName() << std::endl;
std::cout << "      → " + execs.size() + " executables, " + sketches.size() + " sketches" << std::endl;
}
}
/**
* Absorb all JARs from a directory
* @param libDir Path to directory containing JARs
*/
public void absorbDirectory(std::string libDir) {
std::shared_ptr<File> dir = std::make_shared<File>(libDir);
if (!dir.isDirectory()) {
System.err.println("❌ NOT A DIRECTORY: " + libDir);
return;
}
File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));
if (jars == null || jars.length == 0) {
std::cout << "⚠️ NO JARS FOUND IN: " + libDir << std::endl;
return;
}
std::cout << "🔮 MASS ABSORPTION FROM: " + libDir << std::endl;
for (File jar : jars) {
absorb(jar.getAbsolutePath());
}
}
/**
* Release an absorbed JAR (cleanup)
*/
public void release(std::string jarPath) {
URLClassLoader loader = absorbedJars.remove(jarPath);
if (loader != null) {
try {
loader.close();
executableClasses.remove(jarPath);
processingClasses.remove(jarPath);
std::cout << "🗑️ RELEASED: " + new File(jarPath).getName() << std::endl;
} catch (Exception e) {
System.err.println("⚠️ RELEASE WARNING: " + e.getMessage());
}
}
}
// --- HEURISTICS ---
private bool isProcessingSketch(Class<?> cls) {
// Processing sketches usually extend 'PApplet'
Class<?> superclass = cls.getSuperclass(); {
public:
while (superclass != null) { {
public:
if (superclass.getName().equals("processing.core.PApplet")) return true;
superclass = superclass.getSuperclass(); {
public:
}
return false;
}
private bool hasMain(Class<?> cls) {
try {
Method m = cls.getMethod("main", std::string[].class);
return Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers());
} catch (Exception e) {
return false;
}
}
// --- DEMO ---
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  🏺 FRAY-JAR-ABSORBER - Gen 152                               ║" << std::endl;
std::cout << "║  \"I know Kung Fu.\"                                           ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<FrayJarAbsorber> absorber = std::make_shared<FrayJarAbsorber>();
if (args.length > 0) {
// Absorb specified JAR
absorber.absorb(args[0]);
if (args.length > 1) {
// Execute specified class
absorber.executeMain(args[0], args[1], new std::string[0]);
}
} else {
// Demo: Absorb all JARs from libs/
absorber.absorbDirectory("libs");
}
absorber.listAbsorbed();
}
}
