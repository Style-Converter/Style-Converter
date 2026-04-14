#!/bin/bash
#
# Android SDUI Testing Script
# Automates: copy IR → build → install → launch → screenshot → logcat
#
# Usage: ./test-android.sh [input_json]
#   input_json: Optional path to input JSON (default: examples/all-css-properties.json)
#
# Output:
#   - testing/screenshots/*.png (multiple screenshots, each < 2000px)
#   - testing/screenshots/logcat.txt
#

set -e

# Configuration
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
INPUT_JSON="${1:-examples/all-css-properties.json}"
OUTPUT_DIR="$PROJECT_ROOT/out"
ANDROID_DIR="$PROJECT_ROOT/testing/Android"
ASSETS_DIR="$ANDROID_DIR/app/src/main/assets"
SCREENSHOTS_DIR="$PROJECT_ROOT/testing/screenshots"
PACKAGE_NAME="com.styleconverter.test"
ACTIVITY_NAME="$PACKAGE_NAME.MainActivity"

# Screenshot settings
MAX_SCREENSHOTS=50  # Maximum screenshots to capture (increase for large views)
SCROLL_AMOUNT=1500  # pixels to scroll each time
SCROLL_PAUSE=0.5    # seconds to wait after scroll
RENDER_WAIT=6       # seconds to wait for initial render

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Find adb in common locations
find_adb() {
    if command -v adb &> /dev/null; then
        echo "adb"
        return
    fi

    # Common Android SDK locations
    local ADB_PATHS=(
        "$HOME/Library/Android/sdk/platform-tools/adb"
        "$HOME/Android/Sdk/platform-tools/adb"
        "/usr/local/share/android-sdk/platform-tools/adb"
        "$ANDROID_HOME/platform-tools/adb"
        "$ANDROID_SDK_ROOT/platform-tools/adb"
    )

    for path in "${ADB_PATHS[@]}"; do
        if [ -x "$path" ]; then
            echo "$path"
            return
        fi
    done

    echo ""
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    ADB=$(find_adb)
    if [ -z "$ADB" ]; then
        log_error "adb not found. Please install Android SDK platform-tools."
        exit 1
    fi
    log_info "Using adb: $ADB"

    # Check for connected device
    DEVICE_COUNT=$("$ADB" devices | grep -v "List" | grep -c "device$" || true)
    if [ "$DEVICE_COUNT" -eq 0 ]; then
        log_error "No Android device/emulator connected. Please connect a device or start an emulator."
        exit 1
    fi

    log_success "Found $DEVICE_COUNT device(s)"
}

# Step 1: Run Style Converter
run_converter() {
    log_info "Running Style Converter on $INPUT_JSON..."

    if [ ! -f "$PROJECT_ROOT/$INPUT_JSON" ]; then
        log_error "Input file not found: $INPUT_JSON"
        exit 1
    fi

    cd "$PROJECT_ROOT"
    ./gradlew run --args="convert --from css --to compose -i $INPUT_JSON -o out" --quiet

    if [ ! -f "$OUTPUT_DIR/tmpOutput.json" ]; then
        log_error "tmpOutput.json not generated"
        exit 1
    fi

    log_success "Generated tmpOutput.json"
}

# Step 2: Copy IR to Android assets
copy_to_assets() {
    log_info "Copying tmpOutput.json to Android assets..."

    mkdir -p "$ASSETS_DIR"
    cp "$OUTPUT_DIR/tmpOutput.json" "$ASSETS_DIR/tmpOutput.json"

    log_success "Copied to $ASSETS_DIR/tmpOutput.json"
}

# Step 3: Build and install Android app
build_and_install() {
    log_info "Building and installing Android app..."

    cd "$ANDROID_DIR"

    # Use Java 21 for Android build (Java 25 not compatible with Gradle 8.x)
    local JAVA21_HOME=""
    if [ -d "$HOME/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
        JAVA21_HOME="$HOME/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    elif [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
        JAVA21_HOME="/usr/lib/jvm/java-21-openjdk"
    fi

    if [ -n "$JAVA21_HOME" ]; then
        log_info "Using Java 21 from $JAVA21_HOME"
        JAVA_HOME="$JAVA21_HOME" ./gradlew installDebug --quiet
    else
        log_warn "Java 21 not found, using default Java (may fail with Java 25+)"
        ./gradlew installDebug --quiet
    fi

    log_success "App installed"
}

# Step 4: Launch the app
launch_app() {
    log_info "Launching app..."

    # Force stop first to ensure clean state
    "$ADB" shell am force-stop "$PACKAGE_NAME" 2>/dev/null || true

    # Clear logcat
    "$ADB" logcat -c

    # Launch activity
    "$ADB" shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"

    # Wait for app to render
    log_info "Waiting ${RENDER_WAIT}s for render..."
    sleep "$RENDER_WAIT"

    log_success "App launched"
}

# Step 5: Clean old screenshots
clean_screenshots() {
    log_info "Cleaning old screenshots..."

    rm -rf "$SCREENSHOTS_DIR"
    mkdir -p "$SCREENSHOTS_DIR"

    log_success "Screenshots directory cleaned"
}

# Step 6: Take screenshots
take_screenshots() {
    log_info "Taking screenshots..."

    # Get screen dimensions
    SCREEN_SIZE=$("$ADB" shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1)
    SCREEN_WIDTH=$(echo "$SCREEN_SIZE" | cut -d'x' -f1)
    SCREEN_HEIGHT=$(echo "$SCREEN_SIZE" | cut -d'x' -f2)

    log_info "Screen size: ${SCREEN_WIDTH}x${SCREEN_HEIGHT}"

    # Take initial screenshot
    SCREENSHOT_NUM=1
    PREV_HASH=""

    while [ $SCREENSHOT_NUM -le $MAX_SCREENSHOTS ]; do
        SCREENSHOT_FILE="screenshot_$(printf '%02d' $SCREENSHOT_NUM).png"
        DEVICE_PATH="/sdcard/screenshot.png"

        # Take screenshot
        "$ADB" shell screencap -p "$DEVICE_PATH"
        "$ADB" pull "$DEVICE_PATH" "$SCREENSHOTS_DIR/$SCREENSHOT_FILE" > /dev/null 2>&1
        "$ADB" shell rm "$DEVICE_PATH"

        # CRITICAL: Resize if any dimension >= 2000px (API limit)
        resize_if_needed "$SCREENSHOTS_DIR/$SCREENSHOT_FILE"

        # Check if we've reached the end (same screenshot as before)
        CURRENT_HASH=$(md5 -q "$SCREENSHOTS_DIR/$SCREENSHOT_FILE" 2>/dev/null || md5sum "$SCREENSHOTS_DIR/$SCREENSHOT_FILE" | cut -d' ' -f1)

        if [ "$CURRENT_HASH" = "$PREV_HASH" ]; then
            log_info "Reached end of content (screenshot $SCREENSHOT_NUM same as previous)"
            rm "$SCREENSHOTS_DIR/$SCREENSHOT_FILE"
            break
        fi

        PREV_HASH="$CURRENT_HASH"
        log_info "Captured $SCREENSHOT_FILE"

        # Scroll down for next screenshot
        if [ $SCREENSHOT_NUM -lt $MAX_SCREENSHOTS ]; then
            # Swipe up to scroll down (from center-bottom to center-top)
            START_Y=$((SCREEN_HEIGHT * 3 / 4))
            END_Y=$((SCREEN_HEIGHT / 4))
            CENTER_X=$((SCREEN_WIDTH / 2))

            "$ADB" shell input swipe "$CENTER_X" "$START_Y" "$CENTER_X" "$END_Y" 300
            sleep "$SCROLL_PAUSE"
        fi

        SCREENSHOT_NUM=$((SCREENSHOT_NUM + 1))
    done

    TOTAL=$((SCREENSHOT_NUM - 1))
    log_success "Captured $TOTAL screenshot(s)"
}

# Resize image if any dimension >= 2000px
# Uses sips (macOS) or ImageMagick (convert) as fallback
resize_if_needed() {
    local FILE="$1"
    local MAX_DIM=1999  # Must be < 2000

    if [ ! -f "$FILE" ]; then
        return
    fi

    # Get current dimensions
    if command -v sips &> /dev/null; then
        # macOS
        local WIDTH=$(sips -g pixelWidth "$FILE" | tail -1 | awk '{print $2}')
        local HEIGHT=$(sips -g pixelHeight "$FILE" | tail -1 | awk '{print $2}')

        if [ "$WIDTH" -ge 2000 ] || [ "$HEIGHT" -ge 2000 ]; then
            log_warn "Resizing $FILE (${WIDTH}x${HEIGHT} -> max ${MAX_DIM}px)"

            # Calculate new dimensions maintaining aspect ratio
            if [ "$WIDTH" -ge "$HEIGHT" ]; then
                sips --resampleWidth "$MAX_DIM" "$FILE" --out "$FILE" > /dev/null 2>&1
            else
                sips --resampleHeight "$MAX_DIM" "$FILE" --out "$FILE" > /dev/null 2>&1
            fi
        fi
    elif command -v convert &> /dev/null; then
        # ImageMagick
        local DIMS=$(identify -format '%wx%h' "$FILE")
        local WIDTH=$(echo "$DIMS" | cut -d'x' -f1)
        local HEIGHT=$(echo "$DIMS" | cut -d'x' -f2)

        if [ "$WIDTH" -ge 2000 ] || [ "$HEIGHT" -ge 2000 ]; then
            log_warn "Resizing $FILE (${WIDTH}x${HEIGHT} -> max ${MAX_DIM}px)"
            convert "$FILE" -resize "${MAX_DIM}x${MAX_DIM}>" "$FILE"
        fi
    else
        # No resize tool available, check and warn
        log_warn "No image resize tool found (sips/convert). Cannot guarantee < 2000px."
    fi
}

# Step 7: Capture logcat
capture_logcat() {
    log_info "Capturing logcat..."

    # Get logcat with filter for our app
    "$ADB" logcat -d -v time "*:W" | grep -E "(styleconverter|Exception|Error|SDUI|IRProperty)" > "$SCREENSHOTS_DIR/logcat.txt" 2>/dev/null || true

    # Also get full logcat for the app
    "$ADB" logcat -d -v time --pid=$("$ADB" shell pidof "$PACKAGE_NAME" 2>/dev/null || echo "0") > "$SCREENSHOTS_DIR/logcat_full.txt" 2>/dev/null || true

    LINES=$(wc -l < "$SCREENSHOTS_DIR/logcat.txt" | tr -d ' ')
    log_success "Captured logcat ($LINES lines)"
}

# Step 8: Summary
print_summary() {
    echo ""
    echo "=========================================="
    log_success "Testing complete!"
    echo "=========================================="
    echo ""
    echo "Screenshots:"
    ls -la "$SCREENSHOTS_DIR"/*.png 2>/dev/null || echo "  (none)"
    echo ""
    echo "Logs:"
    ls -la "$SCREENSHOTS_DIR"/*.txt 2>/dev/null || echo "  (none)"
    echo ""
    echo "Directory: $SCREENSHOTS_DIR"
    echo ""
}

# Main execution
main() {
    echo ""
    echo "=========================================="
    echo "  Android SDUI Testing Script"
    echo "=========================================="
    echo ""

    check_prerequisites
    run_converter
    copy_to_assets
    build_and_install
    launch_app
    clean_screenshots
    take_screenshots
    capture_logcat
    print_summary
}

main "$@"
