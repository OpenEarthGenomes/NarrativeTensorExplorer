cmake_minimum_required(VERSION 3.22.1)

project("meaning-kernel")

add_library(meaning-kernel SHARED meaning-kernel.cpp)

target_include_directories(meaning-kernel PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)

find_library(log-lib log)

target_link_libraries(meaning-kernel ${log-lib} android)

target_compile_options(meaning-kernel PRIVATE
    -std=c++20
    -Wall
    -Wextra
    -Werror
    -pedantic
    -march=armv8-a
    -mtune=cortex-a75
)
