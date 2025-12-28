
@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo [Meaning Archive] - PROJEKT NEV ES CSOMAG ELLENORZO
echo ======================================================

set EXPECTED_PACKAGE=com.meaning.app
set EXPECTED_PROJECT=NarrativeTensorExplorer

echo 1. Ellenorzes: settings.gradle.kts...
findstr /C:"rootProject.name = \"%EXPECTED_PROJECT%\"" settings.gradle.kts >nul
if %errorlevel% neq 0 (
    echo [!] HIBA: A projekt neve nem "%EXPECTED_PROJECT%" a settings.gradle.kts-ben!
) else (
    echo [OK] Projekt nev rendben.
)

echo 2. Ellenorzes: build.gradle.kts (namespace)...
findstr /C:"namespace = \"%EXPECTED_PACKAGE%\"" app\build.gradle.kts >nul
if %errorlevel% neq 0 (
    echo [!] HIBA: A namespace nem "%EXPECTED_PACKAGE%" az app\build.gradle.kts-ben!
) else (
    echo [OK] Namespace rendben.
)

echo 3. Ellenorzes: Mappa struktura...
if exist "app\src\main\java\com\meaning\app" (
    echo [OK] Mappa struktura rendben.
) else (
    echo [!] HIBA: A mappak nem a com\meaning\app utvonalon vannak!
)

echo 4. Ellenorzes: JNI Kernel (C++)...
findstr /C:"Java_com_meaning_app" app\src\main\cpp\meaning-kernel.cpp >nul
if %errorlevel% neq 0 (
    echo [!] HIBA: A C++ JNI fuggvenyek nem a "%EXPECTED_PACKAGE%" nevet hasznaljak!
) else (
    echo [OK] JNI nevek rendben.
)

echo ------------------------------------------------------
echo Ellenorzes kesz! Ha lattal [!] jelzest, javitsd a fajlt!
pause
