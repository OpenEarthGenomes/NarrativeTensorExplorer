cmake_minimum_required(VERSION 3.22.1)

project("meaning_kernel")

set(CMAKE_CXX_STANDARD 17)

add_library(
    meaning_kernel
    SHARED
    meaning-kernel.cpp
)

find_library(log-lib log)
find_library(android-lib android)

if(ANDROID_ABI STREQUAL "arm64-v8a")
    target_compile_options(meaning_kernel PRIVATE -march=armv8-a -O3)
    add_definitions(-DHAVE_NEON=1)
endif()

target_link_libraries(
    meaning_kernel
    ${log-lib}
    ${android-lib}
)
