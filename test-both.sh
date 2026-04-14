#!/bin/bash
#
# Unified Testing Script - Launches both Web and Android testing environments
#
# Usage: ./test-both.sh [input_json]
#   input_json: Optional path to input JSON (default: examples/visual-test.json)
#
# What it does:
#   1. Runs the Style Converter to generate IR from input
#   2. Copies IR to both web and android assets
#   3. Starts the web dev server (localhost:3000)
#   4. Configures the Android emulator to 390x844 @ 160dpi (matching web)
#   5. Builds, installs, and launches the Android app
#
# Both environments display at 390x844 with identical dark theme UI.
#

set -e

# ── Configuration ─────────────────────────────────────────────────────────────
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
INPUT_JSON="${1:-examples/visual-test.json}"
OUTPUT_DIR="$PROJECT_ROOT/out"
ANDROID_DIR="$PROJECT_ROOT/testing/Android"
ASSETS_DIR="$ANDROID_DIR/app/src/main/assets"
WEB_DIR="$PROJECT_ROOT/testing/web"
PACKAGE_NAME="com.styleconverter.test"
ACTIVITY_NAME="$PACKAGE_NAME.MainActivity"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log()  { echo -e "${GREEN}[test-both]${NC} $1"; }
warn() { echo -e "${YELLOW}[test-both]${NC} $1"; }
err()  { echo -e "${RED}[test-both]${NC} $1"; }
step() { echo -e "\n${BLUE}━━━ $1 ━━━${NC}"; }

# ── Find tools ────────────────────────────────────────────────────────────────

# Find adb
ADB=""
for candidate in \
    "$HOME/Library/Android/sdk/platform-tools/adb" \
    "$ANDROID_HOME/platform-tools/adb" \
    "$(which adb 2>/dev/null)"; do
    if [ -x "$candidate" ]; then
        ADB="$candidate"
        break
    fi
done

# Find Java 21
if /usr/libexec/java_home -v 21 &>/dev/null; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 21)
elif [ -n "$JAVA_HOME" ]; then
    :
else
    err "Java 21 not found. Install it or set JAVA_HOME."
    exit 1
fi

# ── Step 1: Convert CSS to IR ─────────────────────────────────────────────────
step "Step 1: Converting $INPUT_JSON → IR"

if [ ! -f "$INPUT_JSON" ]; then
    err "Input file not found: $INPUT_JSON"
    exit 1
fi

cd "$PROJECT_ROOT"
./gradlew run --args="convert --from css --to compose -i $INPUT_JSON -o out" --quiet
log "Generated out/tmpOutput.json"

# ── Step 2: Copy IR to both environments ──────────────────────────────────────
step "Step 2: Syncing IR data to web + android"

cp "$OUTPUT_DIR/tmpOutput.json" "$WEB_DIR/public/ir-components.json"
log "→ testing/web/public/ir-components.json"

mkdir -p "$ASSETS_DIR"
cp "$OUTPUT_DIR/tmpOutput.json" "$ASSETS_DIR/tmpOutput.json"
log "→ testing/Android/app/src/main/assets/tmpOutput.json"

COMPONENT_COUNT=$(grep -c '"id"' "$OUTPUT_DIR/tmpOutput.json" || echo "?")
log "$COMPONENT_COUNT components in IR"

# ── Step 3: Start web dev server ──────────────────────────────────────────────
step "Step 3: Starting web dev server"

# Kill any existing vite server on port 3000
lsof -ti:3000 2>/dev/null | xargs kill -9 2>/dev/null || true

cd "$WEB_DIR"

# Check if node_modules exist
if [ ! -d "node_modules" ]; then
    log "Installing npm dependencies..."
    npm install --silent
fi

npx vite --port 3000 &>/dev/null &
WEB_PID=$!
log "Web server starting (PID $WEB_PID)..."

# Wait for it to be ready
for i in $(seq 1 15); do
    if curl -s http://localhost:3000 &>/dev/null; then
        log "Web server ready at http://localhost:3000"
        break
    fi
    sleep 1
done

cd "$PROJECT_ROOT"

# ── Step 4: Build and launch Android ──────────────────────────────────────────
step "Step 4: Building and launching Android app"

if [ -z "$ADB" ]; then
    warn "adb not found — skipping Android. Web is running at http://localhost:3000"
    echo ""
    log "Press Ctrl+C to stop the web server."
    wait $WEB_PID
    exit 0
fi

# Check for connected device/emulator
DEVICE_COUNT=$("$ADB" devices | grep -c "device$" || true)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    warn "No Android device/emulator connected."
    warn "Start an emulator first, then re-run this script."
    warn "Web is running at http://localhost:3000"
    echo ""
    log "Press Ctrl+C to stop the web server."
    wait $WEB_PID
    exit 0
fi

# Configure emulator to match web viewport: 390x844 @ 1x (160dpi)
log "Configuring emulator to 390x844dp @ 160dpi..."
"$ADB" shell wm size 390x844
"$ADB" shell wm density 160

# Build the APK
log "Building Android APK..."
cd "$ANDROID_DIR"
JAVA_HOME="$JAVA_HOME" ./gradlew installDebug --quiet 2>&1
log "APK installed"

# Launch the app
"$ADB" shell am force-stop "$PACKAGE_NAME" 2>/dev/null || true
"$ADB" shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME" 2>&1
log "App launched"

cd "$PROJECT_ROOT"

# ── Done ──────────────────────────────────────────────────────────────────────
step "Both environments running"

echo ""
echo -e "  ${GREEN}Web:${NC}     http://localhost:3000  (390×844 viewport)"
echo -e "  ${GREEN}Android:${NC} emulator (390×844dp @ 160dpi)"
echo ""
echo -e "  Both display the same ${COMPONENT_COUNT} components from: ${INPUT_JSON}"
echo ""
echo -e "  ${YELLOW}To reset emulator display:${NC}"
echo -e "    $ADB shell wm size reset"
echo -e "    $ADB shell wm density reset"
echo ""
echo -e "  Press ${RED}Ctrl+C${NC} to stop the web server."

# Cleanup on exit
cleanup() {
    echo ""
    log "Shutting down web server (PID $WEB_PID)..."
    kill $WEB_PID 2>/dev/null || true
    log "Done."
}
trap cleanup EXIT

wait $WEB_PID
