cmake_minimum_required(VERSION 3.18.1)
project("meaning-kernel" VERSION 1.1.0 LANGUAGES CXX)

# Set C++20 standard
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Compiler flags for all builds
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Werror -pedantic")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -Werror")

# Platform-specific settings
if(ANDROID)
    # Language standards
    set(CMAKE_CXX_STANDARD 20)
    set(CMAKE_C_STANDARD 11)
    
    # NEON optimization for ARM
    if(ANDROID_ABI STREQUAL "armeabi-v7a")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon -mfloat-abi=softfp -march=armv7-a")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfpu=neon -mfloat-abi=softfp -march=armv7-a")
        add_definitions(-DHAVE_NEON=1 -DHAVE_ARMV7=1)
        message(STATUS "Building for ARMv7 with NEON")
        
    elseif(ANDROID_ABI STREQUAL "arm64-v8a")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a -mtune=cortex-a75")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8-a -mtune=cortex-a75")
        add_definitions(-DHAVE_NEON=1 -DHAVE_ARM64=1 -DHAVE_NEON_AARCH64=1)
        message(STATUS "Building for ARM64 with NEON")
        
    elseif(ANDROID_ABI STREQUAL "x86")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse4.2 -mpopcnt")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse4.2 -mpopcnt")
        add_definitions(-DHAVE_SSE=1 -DHAVE_X86=1)
        message(STATUS "Building for x86 with SSE4.2")
        
    elseif(ANDROID_ABI STREQUAL "x86_64")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse4.2 -mpopcnt -mavx2 -mfma")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse4.2 -mpopcnt -mavx2 -mfma")
        add_definitions(-DHAVE_SSE=1 -DHAVE_AVX2=1 -DHAVE_X86_64=1)
        message(STATUS "Building for x86_64 with AVX2")
        
    else()
        message(WARNING "Unknown ABI: ${ANDROID_ABI}")
    endif()
    
    # Position Independent Code (required for Android)
    set(CMAKE_POSITION_INDEPENDENT_CODE TRUE)
    
    # Build type specific flags
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -O0 -g -DDEBUG -UNDEBUG")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG -flto=thin")
    set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "${CMAKE_CXX_FLAGS_RELWITHDEBINFO} -O2 -g -DNDEBUG")
    
    # Link Time Optimization for release
    set(CMAKE_INTERPROCEDURAL_OPTIMIZATION_RELEASE TRUE)
    
    # Android specific definitions
    add_definitions(-DANDROID -D__ANDROID_API__=${ANDROID_PLATFORM})
endif()

# Source files
set(SOURCES
    meaning-kernel.cpp
)

# Header files
set(HEADERS
    # Public headers would go here
)

# Create shared library
add_library(
    meaning-kernel
    SHARED
    ${SOURCES}
    ${HEADERS}
)

# Include directories
target_include_directories(meaning-kernel PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Compiler feature detection
target_compile_features(meaning-kernel PRIVATE cxx_std_20)

# Set library version
set_target_properties(meaning-kernel PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
    OUTPUT_NAME "meaning-kernel"
)

# Link libraries
find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(meaning-kernel PRIVATE
    ${
