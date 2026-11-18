#!/bin/bash
# ==============================================================================
# Maisonelle - Terminal Application Launcher
# Tagline: A little luxury, thoughtfully curated.
# ==============================================================================
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

JAVA_CMD=""
if [ -x "/opt/homebrew/opt/openjdk/bin/java" ]; then
    JAVA_CMD="/opt/homebrew/opt/openjdk/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif compgen -G "$HOME/.vscode/extensions/redhat.java-*/jre/*/bin/java" > /dev/null; then
    for j in $HOME/.vscode/extensions/redhat.java-*/jre/*/bin/java; do
        if [ -x "$j" ]; then
            JAVA_CMD="$j"
            break
        fi
    done
elif compgen -G "/Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java" > /dev/null; then
    for j in /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java; do
        if [ -x "$j" ]; then
            JAVA_CMD="$j"
            break
        fi
    done
elif command -v java >/dev/null 2>&1; then
    JAVA_CMD="java"
fi

if [ -z "$JAVA_CMD" ]; then
    echo "Error: Java 21+ runtime not found."
    exit 1
fi

# Compile if bin is empty
if [ ! -f "bin/com/maisonelle/Main.class" ]; then
    echo "Compiling application sources..."
    ./build.sh
fi

echo "Launching Maisonelle Terminal Edition..."
exec "$JAVA_CMD" -cp "bin" com.maisonelle.Main "$@"
