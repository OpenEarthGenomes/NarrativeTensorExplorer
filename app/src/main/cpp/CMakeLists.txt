cmake_minimum_required(VERSION 3.22.1)
project("meaning-kernel")

add_library(meaning-kernel SHARED meaning-kernel.cpp)

find_library(log-lib log)

target_link_libraries(meaning-kernel ${log-lib})

