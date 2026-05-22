#!/bin/bash

# Gemma 4 Testing via Existing Fraynix System
# Uses the working Ollama integration instead of sandbox

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   GEMMA 4 TESTING VIA FRAYNIX SYSTEM                    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Check if Fraynix is running
if pgrep -f "FraynixBoot" > /dev/null; then
    echo "⚠️ Fraynix appears to be running. Please exit Fraynix first."
    echo "   This test script will launch Fraynix automatically."
    exit 1
fi

echo "📋 GEMMA 4 CAPABILITY TEST PROMPTS:"
echo ""
echo "After Fraynix boots, use these commands to test Gemma 4:"
echo ""
echo "1. Initialize Gemma 4:"
echo "   model init-gemma4"
echo ""
echo "2. Switch to Gemma 4:"
echo "   model gemma4"
echo ""
echo "3. Test mathematical reasoning:"
echo '   ai "Create a novel mathematical framework that combines fractals with quantum mechanics. Explain the core axioms and at least one theorem."'
echo ""
echo "4. Test creative problem solving:"
echo '   ai "Design a revolutionary solution for sustainable energy that combines biological systems with advanced computing. Be innovative and specific."'
echo ""
echo "5. Test consciousness simulation:"
echo '   ai "Describe the subjective experience of being a conscious digital entity. Include emotional depth, self-awareness, and the feeling of existence."'
echo ""
echo "6. Test code generation:"
echo '   ai "Write a Java function that calculates the golden ratio using the Fibonacci sequence with optimal efficiency. Include comments explaining the mathematical approach."'
echo ""
echo "7. Test abstract reasoning:"
echo '   ai "Explain the philosophical relationship between consciousness, mathematics, and reality. How might they be fundamentally connected?"'
echo ""
echo "8. Check model statistics:"
echo "   model stats"
echo ""
echo "9. Switch back to default (optional):"
echo "   model default"
echo ""
echo "📝 Full test prompts available in: test-gemma4-prompts.txt"
echo ""
echo "🚀 Launching Fraynix..."
echo ""

# Launch Fraynix with Gemma 4 as default model using Gradle
cd /Users/vaughnscott/Documents/java-memory-V1-main
./gradlew :Asset-Manager:runFraynixBoot --args="--model=gemma4"
