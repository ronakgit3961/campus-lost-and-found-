#!/usr/bin/env bash
# Build script for IntelliRecover
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

mkdir -p bin
echo "[BUILD] Compiling IntelliRecover sources..."
find src/main/java -name "*.java" > sources.txt
if [ -s sources.txt ]; then
    javac -d bin -encoding UTF-8 @sources.txt
    rm sources.txt
    echo "[BUILD] Compilation successful -> output in bin/"
else
    echo "[BUILD] No Java sources found yet."
    rm sources.txt
fi
