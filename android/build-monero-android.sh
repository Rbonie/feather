#!/bin/bash
# Build script for Monero Core Android libraries
# This script compiles the Monero wallet library for Android architectures

set -e

# Configuration
MONERO_VERSION="0.18.3.1"
ANDROID_NDK_HOME="${ANDROID_NDK_HOME:-$HOME/android-ndk}"
BUILD_DIR="$(pwd)/build-monero-android"
OUTPUT_DIR="$(pwd)/app/src/main/jniLibs"

# Architectures to build
ARCHS=("arm64-v8a" "armeabi-v7a" "x86_64")

# Check prerequisites
echo "=== Feather Wallet - Monero Core Android Build Script ==="
echo ""

if [ ! -d "$ANDROID_NDK_HOME" ]; then
    echo "ERROR: Android NDK not found at $ANDROID_NDK_HOME"
    echo "Please set ANDROID_NDK_HOME environment variable or install NDK"
    exit 1
fi

echo "NDK Path: $ANDROID_NDK_HOME"
echo "Monero Version: $MONERO_VERSION"
echo "Build Directory: $BUILD_DIR"
echo "Output Directory: $OUTPUT_DIR"
echo ""

# Create directories
mkdir -p "$BUILD_DIR"
mkdir -p "$OUTPUT_DIR"

# Function to build for a specific architecture
build_arch() {
    local ARCH=$1
    local API_LEVEL=21
    
    echo "=========================================="
    echo "Building for $ARCH (API $API_LEVEL)"
    echo "=========================================="
    
    # Set toolchain based on architecture
    case $ARCH in
        arm64-v8a)
            HOST="aarch64-linux-android"
            TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64"
            ;;
        armeabi-v7a)
            HOST="armv7a-linux-androideabi"
            TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64"
            ;;
        x86_64)
            HOST="x86_64-linux-android"
            TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64"
            ;;
        *)
            echo "Unknown architecture: $ARCH"
            return 1
            ;;
    esac
    
    # Create build directory for this architecture
    local ARCH_BUILD_DIR="$BUILD_DIR/$ARCH"
    mkdir -p "$ARCH_BUILD_DIR"
    
    # Set compiler flags
    export CC="$TOOLCHAIN/bin/$HOST$API_LEVEL-clang"
    export CXX="$TOOLCHAIN/bin/$HOST$API_LEVEL-clang++"
    export AR="$TOOLCHAIN/bin/llvm-ar"
    export RANLIB="$TOOLCHAIN/bin/llvm-ranlib"
    export STRIP="$TOOLCHAIN/bin/llvm-strip"
    
    export CFLAGS="-target $HOST$API_LEVEL -fPIC"
    export CXXFLAGS="-target $HOST$API_LEVEL -fPIC -std=c++17"
    export LDFLAGS="-target $HOST$API_LEVEL -pie"
    
    # Clone Monero if not already cloned
    if [ ! -d "$ARCH_BUILD_DIR/monero" ]; then
        echo "Cloning Monero repository..."
        git clone --branch release-v0.18 --depth 1 \
            https://github.com/monero-project/monero.git \
            "$ARCH_BUILD_DIR/monero"
    fi
    
    cd "$ARCH_BUILD_DIR/monero"
    
    # Create build directory
    mkdir -p build
    cd build
    
    # Configure with CMake
    cmake .. \
        -DCMAKE_SYSTEM_NAME=Android \
        -DCMAKE_SYSTEM_VERSION=$API_LEVEL \
        -DCMAKE_ANDROID_ARCH_ABI=$ARCH \
        -DCMAKE_ANDROID_NDK=$ANDROID_NDK_HOME \
        -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
        -DCMAKE_BUILD_TYPE=Release \
        -DBUILD_SHARED_LIBS=ON \
        -DBUILD_TESTS=OFF \
        -DBUILD_EXAMPLES=OFF \
        -DWITH_GUI=OFF \
        -DWITH_DEVICE_LEDGER=OFF \
        -DWITH_DEVICE_TREZOR=OFF \
        -DWITH_RANDOMX=ON \
        -DWITH_ASM=ON \
        -DSTATIC_ASSERT=ON \
        -DARCH=$ARCH \
        -DCMAKE_INSTALL_PREFIX="$ARCH_BUILD_DIR/install"
    
    # Build
    echo "Building Monero libraries..."
    make -j$(nproc) wallet
    
    # Install
    echo "Installing libraries..."
    make install
    
    # Copy libraries to output directory
    mkdir -p "$OUTPUT_DIR/$ARCH"
    
    if [ -f "$ARCH_BUILD_DIR/install/lib/libwallet2_api.so" ]; then
        cp "$ARCH_BUILD_DIR/install/lib/libwallet2_api.so" "$OUTPUT_DIR/$ARCH/"
        echo "✓ Copied libwallet2_api.so to $OUTPUT_DIR/$ARCH/"
    fi
    
    if [ -f "$ARCH_BUILD_DIR/install/lib/libmonero_wallet.so" ]; then
        cp "$ARCH_BUILD_DIR/install/lib/libmonero_wallet.so" "$OUTPUT_DIR/$ARCH/"
        echo "✓ Copied libmonero_wallet.so to $OUTPUT_DIR/$ARCH/"
    fi
    
    # Copy any other .so files
    find "$ARCH_BUILD_DIR/install/lib" -name "*.so" -exec cp {} "$OUTPUT_DIR/$ARCH/" \;
    
    cd ../..
    
    echo "✓ Build completed for $ARCH"
    echo ""
}

# Build for each architecture
for ARCH in "${ARCHS[@]}"; do
    build_arch "$ARCH" || {
        echo "⚠ Failed to build for $ARCH, continuing with next architecture..."
    }
done

# Summary
echo "=========================================="
echo "Build Summary"
echo "=========================================="
echo ""
echo "Output directory: $OUTPUT_DIR"
echo ""
echo "Built libraries:"
find "$OUTPUT_DIR" -name "*.so" -type f | sort
echo ""
echo "Next steps:"
echo "1. Verify the built libraries work on target devices"
echo "2. Update CMakeLists.txt to link against these libraries"
echo "3. Test the JNI integration"
echo ""
echo "Build completed!"
