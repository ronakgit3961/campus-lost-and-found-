#!/usr/bin/env bash
# Test execution script for IntelliRecover
set -e

if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"
    elif [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "[TEST] Building project first..."
./build.sh

echo "[TEST] Compiling tests..."
find tests -name "*.java" > test_sources.txt
if [ -s test_sources.txt ]; then
    javac -cp bin -d bin -encoding UTF-8 @test_sources.txt
    rm test_sources.txt
    echo "[TEST] Running test suite..."
    java -ea -cp bin com.intellirecover.TestRunner
else
    echo "[TEST] No tests found yet."
    rm test_sources.txt
fi
