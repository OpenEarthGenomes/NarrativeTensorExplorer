#!/bin/bash

# Színek a terminálhoz
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

echo -e "${GREEN}=== Meaning Archive APK Analízis ===${NC}"

# 1. Ellenőrizzük, létezik-e az APK
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Hiba: Az APK nem található! Futtasd előbb a build_all.bat-ot vagy ./gradlew assembleDebug-ot.${NC}"
    exit 1
fi

echo -e "APK mérete: $(du -h "$APK_PATH" | cut -f1)"

# 2. Native Library (.so) ellenőrzése
echo -e "\n${GREEN}[1/3] Native könyvtárak ellenőrzése (JNI):${NC}"
unzip -l "$APK_PATH" | grep "libmeaning-kernel.so"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}OK: A meaning-kernel beágyazva.${NC}"
else
    echo -e "${RED}HIBA: A .so fájl hiányzik az APK-ból!${NC}"
fi

# 3. Adatbázis sémák és assetek
echo -e "\n${GREEN}[2/3] Adatbázis és erőforrások ellenőrzése:${NC}"
unzip -l "$APK_PATH" | grep -E "assets/|res/raw/"

# 4. Manifest és Csomagnév ellenőrzése
echo -e "\n${GREEN}[3/3] Manifest ellenőrzése (Csomagnév):${NC}"
# Ehhez szükséges az 'aapt' eszköz, ami az Android SDK része
if command -v aapt &> /dev/null; then
    aapt dump badging "$APK_PATH" | grep "package: name="
else
    echo "Info: 'aapt' nem található, a csomagnév részletes ellenőrzése kihagyva."
fi

echo -e "\n${GREEN}Analízis kész!${NC}"
