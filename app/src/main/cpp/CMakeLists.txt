cmake_minimum_required(VERSION 3.22.1)

# A projekt neve (ez fog megjelenni a build logokban)
project("meaning-kernel")

# Itt adjuk hozzá a C++ forrásfájlt
add_library(
    meaning-kernel 
    SHARED 
    meaning-kernel.cpp
)

# Megkeressük az Android logoláshoz szükséges könyvtárat
find_library(log-lib log)

# Összekötjük a kódunkat a szükséges rendszerkönyvtárakkal
target_link_libraries(
    meaning-kernel 
    ${log-lib} 
    android
)

# Alapvető beállítások: C++20 szabvány és optimalizálás
target_compile_options(meaning-kernel PRIVATE
    -std=c++20
    -O2
)
