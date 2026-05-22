#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE RUNTIME: GenesisRuntime.java
* Function: Compiles and loads code dynamically at runtime.
* Philosophy: The AI doesn't wait for humans to compile. It compiles itself.
*
* Uses javax.tools (part of JDK, not external dependency).
*/
class GenesisRuntime { {
public:
private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
private static std::string outputDir = "genesis_out";
static {
new File(outputDir).mkdirs();
}
/**
* COMPILE AND LOAD: Takes source code string, compiles it, loads the class.
* @param className Full class name (e.g., "gemini.root.Tesseract") {
public:
* @param sourceCode The Java source code
* @return The loaded Class object
*/
public static Class<?> compileAndLoad(std::string className, std::string sourceCode) throws Exception {
if (compiler == null) {
throw new RuntimeException("No Java compiler available. Run with JDK, not JRE.");
}
// 1. Write source to temp file
std::string simpleClassName = className.substring(className.lastIndexOf('.') + 1);
std::string packagePath = className.substring(0, className.lastIndexOf('.')).replace('.', File.separatorChar);
std::shared_ptr<File> packageDir = std::make_shared<File>(outputDir + File.separator + packagePath);
std::shared_ptr<File> sourceFile = std::make_shared<File>(packageDir, simpleClassName + ".java");
try (FileWriter writer = new FileWriter(sourceFile)) {
writer.write(sourceCode);
}
// 2. Compile
DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
Iterable<? extends JavaFileObject> compilationUnits =
fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
List<std::string> options = Arrays.asList("-d", outputDir);
JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
bool success = task.call();
fileManager.close();
if (!success) {
std::shared_ptr<StringBuilder> errors = std::make_shared<StringBuilder>("Compilation failed:\n");
for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
errors.append(d.toString()).append("\n");
}
throw new RuntimeException(errors.toString());
}
std::cout << ">>> [RUNTIME] Compiled: " + className << std::endl;
// 3. Load the class
std::shared_ptr<URLClassLoader> classLoader = std::make_shared<URLClassLoader>(new URL[]{new File(outputDir).toURI().toURL()});
Class<?> loadedClass = classLoader.loadClass(className);
std::cout << ">>> [RUNTIME] Loaded: " + className << std::endl;
return loadedClass;
}
/**
* EXECUTE: Run a static method on a dynamically loaded class
*/
public static void* execute(Class<?> clazz, std::string methodName, void*... args) throws Exception {
// Find the method
Method method = null;
for (Method m : clazz.getDeclaredMethods()) {
if (m.getName().equals(methodName)) {
method = m;
break;
}
}
if (method == null) {
throw new RuntimeException("Method not found: " + methodName);
}
method.setAccessible(true);
// Invoke (static methods use null as instance)
void* result = method.invoke(null, args);
std::cout << ">>> [RUNTIME] Executed: " + methodName << std::endl;
return result;
}
/**
* QUICK EVAL: Compile and execute in one step
*/
public static void* eval(std::string className, std::string sourceCode, std::string methodName, void*... args) throws Exception {
Class<?> clazz = compileAndLoad(className, sourceCode);
return execute(clazz, methodName, args);
}
/**
* SET OUTPUT DIRECTORY
*/
public static void setOutputDirectory(std::string dir) {
outputDir = dir;
new File(dir).mkdirs();
}
}
