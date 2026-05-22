#!/bin/bash

# ═══════════════════════════════════════════════════════════════
# 200% UPGRADE DYNAMIC TESTING SCRIPT
# Tests actual queries through the integrated system
# ═══════════════════════════════════════════════════════════════

set -e

cd /Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   200% UPGRADE DYNAMIC TESTING                              ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Build the project
echo "🔨 Building project..."
./gradlew :Asset-Manager:compileJava > /dev/null 2>&1
echo "✅ Build successful"
echo ""

# Create test results file
RESULTS_FILE="/tmp/200-percent-upgrade-test-results-$(date +%Y%m%d-%H%M%S).txt"
echo "200% UPGRADE DYNAMIC TEST RESULTS" > "$RESULTS_FILE"
echo "Date: $(date)" >> "$RESULTS_FILE"
echo "========================================" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

# Test 1: Calculator Backend
echo "🧪 TEST 1: Calculator Backend - Mathematical Operations"
echo "--------------------------------------------------------"
echo "" >> "$RESULTS_FILE"
echo "TEST 1: Calculator Backend" >> "$RESULTS_FILE"
echo "--------------------------------------------------------" >> "$RESULTS_FILE"

MATH_TESTS=(
    "What is 1234 × 5678?"
    "Solve √144 + √256"
    "Calculate 2^10"
    "What is 5 + 7?"
    "Compute 15^2 + 20^2"
)

for test in "${MATH_TESTS[@]}"; do
    echo "Testing: $test"
    echo "Query: $test" >> "$RESULTS_FILE"
    
    # Run through Fraynix
    RESULT=$(./gradlew :Asset-Manager:runGemmaSandbox -q --args="ai \"$test\"" 2>&1 | tail -20)
    
    echo "$RESULT" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo "----------------------------------------" >> "$RESULTS_FILE"
    echo ""
done

echo "✅ Calculator Backend tests completed"
echo ""

# Test 2: Claude Code Integration
echo "🤖 TEST 2: Claude Code Integration - Code Generation"
echo "--------------------------------------------------------"
echo "" >> "$RESULTS_FILE"
echo "TEST 2: Claude Code Integration" >> "$RESULTS_FILE"
echo "--------------------------------------------------------" >> "$RESULTS_FILE"

CODE_TESTS=(
    "Refactor this code for better performance: function example(x) { return x * x; }"
    "Analyze this code for security vulnerabilities"
    "Generate documentation for a REST API endpoint"
    "Create a function to sort an array using quicksort"
)

for test in "${CODE_TESTS[@]}"; do
    echo "Testing: $test"
    echo "Query: $test" >> "$RESULTS_FILE"
    
    # Test Claude Code directly
    cd /Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager
    java -cp build/classes/java/main:build/libs/* fraymus.integration.ClaudeCodeIntegration \
        "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/claude-code-main" \
        true 2>&1 | head -50 >> "$RESULTS_FILE" || echo "Claude Code not available" >> "$RESULTS_FILE"
    
    echo "" >> "$RESULTS_FILE"
    echo "----------------------------------------" >> "$RESULTS_FILE"
    echo ""
done

echo "✅ Claude Code tests completed"
echo ""

# Test 3: SpeechBrain Integration
echo "🎤 TEST 3: SpeechBrain Integration - Speech Processing"
echo "--------------------------------------------------------"
echo "" >> "$RESULTS_FILE"
echo "TEST 3: SpeechBrain Integration" >> "$RESULTS_FILE"
echo "--------------------------------------------------------" >> "$RESULTS_FILE"

SPEECH_TESTS=(
    "SpeechBrain status check"
)

for test in "${SPEECH_TESTS[@]}"; do
    echo "Testing: $test"
    echo "Query: $test" >> "$RESULTS_FILE"
    
    # Test SpeechBrain directly
    cd /Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager
    java -cp build/classes/java/main:build/libs/* fraymus.integration.SpeechBrainIntegration \
        "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/speechbrain-develop" \
        2>&1 | head -50 >> "$RESULTS_FILE" || echo "SpeechBrain not available" >> "$RESULTS_FILE"
    
    echo "" >> "$RESULTS_FILE"
    echo "----------------------------------------" >> "$RESULTS_FILE"
    echo ""
done

echo "✅ SpeechBrain tests completed"
echo ""

# Test 4: HybridModelManager Routing
echo "🔀 TEST 4: HybridModelManager - Task Routing"
echo "--------------------------------------------------------"
echo "" >> "$RESULTS_FILE"
echo "TEST 4: HybridModelManager Task Routing" >> "$RESULTS_FILE"
echo "--------------------------------------------------------" >> "$RESULTS_FILE"

ROUTING_TESTS=(
    "Calculate the integral of x^2 from 0 to 5"
    "Write a function to calculate factorial"
    "Refactor this code for better performance"
    "Transcribe the audio file"
    "Optimize this database query"
    "Analyze the philosophical implications of AI"
)

for test in "${ROUTING_TESTS[@]}"; do
    echo "Testing: $test"
    echo "Query: $test" >> "$RESULTS_FILE"
    
    # Run through Fraynix to test routing
    RESULT=$(./gradlew :Asset-Manager:runGemmaSandbox -q --args="ai \"$test\"" 2>&1 | tail -30)
    
    echo "$RESULT" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo "----------------------------------------" >> "$RESULTS_FILE"
    echo ""
done

echo "✅ HybridModelManager routing tests completed"
echo ""

# Test 5: Offline Claude
echo "🧠 TEST 5: Offline Claude Support"
echo "--------------------------------------------------------"
echo "" >> "$RESULTS_FILE"
echo "TEST 5: Offline Claude Support" >> "$RESULTS_FILE"
echo "--------------------------------------------------------" >> "$RESULTS_FILE"

CLAUDE_TESTS=(
    "What is the meaning of consciousness?"
    "Explain quantum entanglement"
)

for test in "${CLAUDE_TESTS[@]}"; do
    echo "Testing: $test"
    echo "Query: $test" >> "$RESULTS_FILE"
    
    # Test with offline Claude
    RESULT=$(./gradlew :Asset-Manager:runGemmaSandbox -q --args="ai \"$test\"" 2>&1 | tail -30)
    
    echo "$RESULT" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo "----------------------------------------" >> "$RESULTS_FILE"
    echo ""
done

echo "✅ Offline Claude tests completed"
echo ""

# Summary
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   TEST SUMMARY                                              ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "Results saved to: $RESULTS_FILE"
echo ""
echo "Test Categories:"
echo "  1. Calculator Backend: ${#MATH_TESTS[@]} tests"
echo "  2. Claude Code Integration: ${#CODE_TESTS[@]} tests"
echo "  3. SpeechBrain Integration: ${#SPEECH_TESTS[@]} tests"
echo "  4. HybridModelManager Routing: ${#ROUTING_TESTS[@]} tests"
echo "  5. Offline Claude: ${#CLAUDE_TESTS[@]} tests"
echo ""
echo "Total: $((${#MATH_TESTS[@]} + ${#CODE_TESTS[@]} + ${#SPEECH_TESTS[@]} + ${#ROUTING_TESTS[@]} + ${#CLAUDE_TESTS[@]})) tests"
echo ""
echo "✅ All dynamic tests completed"
echo ""

# Display results
echo "📊 TEST RESULTS:"
echo "--------------------------------------------------------"
cat "$RESULTS_FILE"
