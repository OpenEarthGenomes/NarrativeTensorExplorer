cmake_minimum_required(VERSION 3.22.1)
project("meaning_kernel")

# Itt javítottuk: meaning_kernel.cpp (alulvonással)
add_library(meaning_kernel SHARED 
    meaning_kernel.cpp 
)

find_library(log-lib log)
target_link_libraries(meaning_kernel ${log-lib})
