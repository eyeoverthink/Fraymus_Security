#!/bin/bash

# Direct Fraynix Boot Script - Bypasses Gradle console issues
echo "Starting Fraynix Boot with proper console..."

# Set Java options for proper console handling
JAVA_OPTS="-Xmx2g -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"

# Build if needed
if [ ! -d "build/classes/java/main" ] || [ "src/main/java" -nt "build/classes/java/main" ]; then
    echo "Building Fraynix..."
    ./gradlew compileJava -q
fi

# Get classpath from Gradle
CLASSPATH=$(./gradlew printClasspath -q 2>/dev/null | tail -1)

# Run directly with Java for proper console interaction
echo "Launching Fraynix Boot..."
java $JAVA_OPTS -cp "$CLASSPATH" fraymus.FraynixBoot
