#!/bin/bash

# Gemma 4 Sandbox Test Runner
# Isolated testing environment for Gemma 4 capabilities
# SAFE: Does not touch core Fraynix system

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   GEMMA 4 SANDBOX TEST RUNNER                            ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Check if Ollama is running
echo "🔍 Checking Ollama status..."
if ! curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "❌ Ollama is not running. Please start with: ollama serve"
    exit 1
fi
echo "✅ Ollama is running"
echo ""

# Check if Gemma 4 is installed
echo "🔍 Checking Gemma 4 installation..."
if ! ollama list | grep -q "gemma4"; then
    echo "⚠️ Gemma 4 is not installed. Installing now..."
    ollama pull gemma4
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install Gemma 4"
        exit 1
    fi
fi
echo "✅ Gemma 4 is installed"
echo ""

# Compile and run using javac with manual classpath
echo "🔧 Compiling sandbox with javac..."
cd /Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager

# Find Jackson JARs in Gradle cache
JACKSON_DATABIND=$(find ~/.gradle/caches -name 'jackson-databind*.jar' 2>/dev/null | head -1)
JACKSON_CORE=$(find ~/.gradle/caches -name 'jackson-core*.jar' 2>/dev/null | head -1)
JACKSON_ANNOTATIONS=$(find ~/.gradle/caches -name 'jackson-annotations*.jar' 2>/dev/null | head -1)
JSON_JAR=$(find ~/.gradle/caches -name 'json*.jar' 2>/dev/null | head -1)

if [ -z "$JACKSON_DATABIND" ] || [ -z "$JACKSON_CORE" ] || [ -z "$JACKSON_ANNOTATIONS" ]; then
    echo "❌ Jackson JARs not found in Gradle cache"
    echo "📦 Installing required dependencies..."
    # Create lib directory and download JARs
    mkdir -p lib
    curl -L -o lib/jackson-databind.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
    curl -L -o lib/jackson-core.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
    curl -L -o lib/jackson-annotations.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
    curl -L -o lib/json.jar https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar
    
    JACKSON_DATABIND="lib/jackson-databind.jar"
    JACKSON_CORE="lib/jackson-core.jar"
    JACKSON_ANNOTATIONS="lib/jackson-annotations.jar"
    JSON_JAR="lib/json.jar"
fi

# Create build directory
mkdir -p build/sandbox

# Build classpath
CLASSPATH="$JACKSON_DATABIND:$JACKSON_CORE:$JACKSON_ANNOTATIONS:$JSON_JAR:src/main/java"

# Compile
echo "📦 Compiling GemmaSandbox.java..."
javac -cp "$CLASSPATH" -d build/sandbox src/main/java/fraymus/sandbox/GemmaSandbox.java \
    src/main/java/fraymus/llm/ModelInterface.java \
    src/main/java/fraymus/llm/OllamaModelAdapter.java \
    src/main/java/fraymus/llm/Gemma4Model.java \
    src/main/java/fraymus/llm/ModelManager.java \
    src/main/java/fraymus/ollama/OllamaSpine.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo ""

echo "🚀 Starting Gemma 4 Sandbox..."
echo ""

# Run
java -cp "$CLASSPATH:build/sandbox" fraymus.sandbox.GemmaSandbox
