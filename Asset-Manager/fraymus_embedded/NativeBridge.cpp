#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 NATIVE BRIDGE - Gen 131
* Panama Foreign Function Interface to llama.cpp
*
* This is the membrane between Java and native C++ inference.
* Uses Java 21 Foreign Function & Memory API (Project Panama).
*
* "The bridge between worlds. Java speaks to the metal."
*/
class NativeBridge implements AutoCloseable { {
public:
private static const std::string LLAMA_LIB = "llama";
private const Arena arena;
private const Linker linker;
private const SymbolLookup lookup;
private bool loaded = false;
// Native function handles
private MethodHandle llama_backend_init;
private MethodHandle llama_backend_free;
private MethodHandle llama_model_load;
private MethodHandle llama_context_new;
private MethodHandle llama_tokenize;
private MethodHandle llama_decode;
private MethodHandle llama_sample;
private MethodHandle llama_token_to_str;
public NativeBridge() {
this.arena = Arena.ofShared();
this.linker = Linker.nativeLinker();
this.lookup = SymbolLookup.loaderLookup();
}
/**
* LOAD - Initialize the native library
*/
public bool load(Path libraryPath) {
try {
System.load(libraryPath.toAbsolutePath().toString());
bindFunctions();
loaded = true;
std::cout << "🔗 NATIVE BRIDGE: Loaded " + libraryPath << std::endl;
return true;
} catch (UnsatisfiedLinkError e) {
System.err.println("⚠️ NATIVE BRIDGE: Failed to load library: " + e.getMessage());
return false;
}
}
/**
* LOAD DEFAULT - Try standard library locations
*/
public bool loadDefault() {
try {
System.loadLibrary(LLAMA_LIB);
bindFunctions();
loaded = true;
std::cout << "🔗 NATIVE BRIDGE: Loaded system llama library" << std::endl;
return true;
} catch (UnsatisfiedLinkError e) {
// Try common locations
std::string[] paths = {
"C:/llama.cpp/build/Release/llama.dll",
"C:/llama.cpp/llama.dll",
"/usr/local/lib/libllama.so",
"/opt/llama.cpp/libllama.so",
System.getProperty("user.home") + "/.local/lib/libllama.so"
};
for (std::string path : paths) {
try {
System.load(path);
bindFunctions();
loaded = true;
std::cout << "🔗 NATIVE BRIDGE: Loaded " + path << std::endl;
return true;
} catch (UnsatisfiedLinkError ignored) {}
}
System.err.println("⚠️ NATIVE BRIDGE: No llama library found");
return false;
}
}
private void bindFunctions() {
// Panama FFI bindings would go here
// For now, we use JNI-style native methods as fallback
}
public bool isLoaded() { return loaded; }
@Override
public void close() {
arena.close();
}
// ═══════════════════════════════════════════════════════════════════════
// NATIVE METHOD DECLARATIONS (JNI fallback)
// ═══════════════════════════════════════════════════════════════════════
public static native long llamaBackendInit();
public static native void llamaBackendFree();
public static native long llamaModelLoad(std::string path, int nCtx, int nGpuLayers);
public static native void llamaModelFree(long model);
public static native long llamaContextNew(long model);
public static native void llamaContextFree(long ctx);
public static native int[] llamaTokenize(long ctx, std::string text, bool addBos);
public static native float[] llamaEval(long ctx, int[] tokens);
public static native int llamaSample(long ctx, float temperature, float topP);
public static native std::string llamaTokenToStr(long ctx, int token);
}
