#!/bin/bash
# Native library build script for multiple ABIs

set -e

echo "🔨 Building Native Kernel Library"
echo "================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if NDK is available
if [ -z "$ANDROID_NDK_HOME" ]; then
    if [ -z "$ANDROID_NDK_ROOT" ]; then
        echo -e "${RED}❌ ANDROID_NDK_HOME or ANDROID_NDK_ROOT not set${NC}"
        exit 1
    else
        export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
    fi
fi

echo -e "${GREEN}✓ NDK found at: $ANDROID_NDK_HOME${NC}"

# Build directory
BUILD_DIR="build-native"
mkdir -p $BUILD_DIR
cd $BUILD_DIR

# ABIs to build for
ABIS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")
CMAKE_ARGS=(
    "-DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake"
    "-DANDROID_PLATFORM=android-24"
    "-DANDROID_STL=c++_shared"
    "-DCMAKE_BUILD_TYPE=Release"
    "-DCMAKE_CXX_STANDARD=17"
    "-DCMAKE_CXX_STANDARD_REQUIRED=ON"
)

# Build for each ABI
for ABI in "${ABIS[@]}"; do
    echo -e "\n${YELLOW}🏗️  Building for $ABI${NC}"
    
    # Create ABI-specific build directory
    ABI_BUILD_DIR="build-$ABI"
    mkdir -p $ABI_BUILD_DIR
    cd $ABI_BUILD_DIR
    
    # Configure CMake
    cmake ../.. \
        "${CMAKE_ARGS[@]}" \
        -DANDROID_ABI="$ABI"
    
    # Build
    cmake --build . --config Release --target meaning-kernel -j $(nproc)
    
    # Copy output to libs directory
    mkdir -p ../../app/src/main/jniLibs/$ABI
    cp libmeaning-kernel.so ../../app/src/main/jniLibs/$ABI/
    
    echo -e "${GREEN}✓ Built for $ABI${NC}"
    
    # Return to build directory
    cd ..
done

# Create combined JNI directory structure
echo -e "\n${YELLOW}📦 Creating JNI directory structure${NC}"
cd ..
JNI_DIR="app/src/main/jniLibs"
mkdir -p $JNI_DIR

# Generate version info
VERSION_FILE="$JNI_DIR/version.txt"
echo "Build Date: $(date)" > $VERSION_FILE
echo "NDK Version: $(basename $ANDROID_NDK_HOME)" >> $VERSION_FILE
echo "CMake Version: $(cmake --version | head -1)" >> $VERSION_FILE
echo "ABIs: ${ABIS[@]}" >> $VERSION_FILE

echo -e "\n${GREEN}✅ Native build completed!${NC}"
echo "Libraries are in: $JNI_DIR"
ls -la $JNI_DIR
