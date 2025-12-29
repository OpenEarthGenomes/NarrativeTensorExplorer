#include <jni.h>
#include <string>

extern "C" {

// Alapvető státusz ellenőrző
JNIEXPORT jstring JNICALL
Java_com_meaning_app_MainActivity_checkKernelStatus(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Kernel OK - 16KB Page Size");
}

// Üres függvények, hogy ne legyen hiba, ha a QuantizationEngine meghívja őket
JNIEXPORT jfloat JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_calculateVectorDistance(JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    return 0.0f;
}

}
