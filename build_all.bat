@echo off
echo [Meaning Archive] Building Project...
call gradlew assembleDebug
echo Done! APK: app\build\outputs\apk\debug\app-debug.apk
pause

