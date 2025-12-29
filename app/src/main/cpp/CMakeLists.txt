cmake_minimum_required(VERSION 3.22.1)

# A projekt neve (meg kell egyeznie a Gradle-ben megadottal)
project("meaning_kernel")

# A natív könyvtár létrehozása a forrásfájlból
add_library(
    meaning_kernel
    SHARED
    meaning_kernel.cpp
)

# Android naplózó könyvtár keresése (hogy tudjunk Log-ot írni)
find_library(
    log-lib
    log
)

# KRITIKUS: Memória-igazítás beállítása 16KB-ra az Android 16-hoz
set_target_properties(meaning_kernel PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")

# Összekapcsolás a szükséges könyvtárakkal
target_link_libraries(
    meaning_kernel
    ${log-lib}
    android
)

# C++ modern szabvány és optimalizáció
target_compile_options(meaning_kernel PRIVATE -std=c++20 -O2 -mfpu=neon)
