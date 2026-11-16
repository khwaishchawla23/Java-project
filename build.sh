#!/bin/bash
# ==============================================================================
# Maisonelle - Build Script (Standard Java 21+ Built-in Only)
# ==============================================================================
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo "=========================================================="
echo " Building Maisonelle (Pure Java 21 - No External Dependencies)"
echo "=========================================================="

JAVAC_CMD=""
if [ -x "/opt/homebrew/opt/openjdk/bin/javac" ]; then
    JAVAC_CMD="/opt/homebrew/opt/openjdk/bin/javac"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVAC_CMD="$JAVA_HOME/bin/javac"
elif command -v javac >/dev/null 2>&1; then
    JAVAC_CMD="javac"
fi

if [ -z "$JAVAC_CMD" ]; then
    echo "Error: Java 21+ compiler (javac) not found."
    exit 1
fi

mkdir -p bin data invoices

echo "Compiling Java source files..."
find src -name "*.java" > sources.txt
"$JAVAC_CMD" -d bin -sourcepath src @sources.txt
rm -f sources.txt

echo "Build successful! Classes compiled to bin/"
