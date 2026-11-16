#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"
chmod +x "$DIR/build.sh" "$DIR/run.sh" 2>/dev/null || true
exec "$DIR/run.sh" "$@"
