#include <jni.h>
#include <arm_neon.h>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "MeaningKernel"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

// Ez a függvény közvetlenül a MainActivity-hez kapcsolódik tesztelésre
JNIEXPORT jstring JNICALL
Java_com_meaning_app_MainActivity_checkKernelStatus(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Kernel Kapcsolat: OK (16KB Page Size aktív)");
}

// Tenzor számítási függvények (QuantizationEngine-hez)
JNIEXPORT jfloat JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_calculateVectorDistance(
    JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* data1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* data2 = env->GetByteArrayElements(v2, nullptr);
    jsize len = env->GetArrayLength(v1);

    float32x4_t sum_vec = vdupq_n_f32(0.0f);
    for (int i = 0; i <= len - 4; i += 4) {
        int8x8_t r1 = vld1_s8(reinterpret_cast<const int8_t*>(data1 + i));
        int8x8_t r2 = vld1_s8(reinterpret_cast<const int8_t*>(data2 + i));
        int16x8_t diff = vsubl_s8(r1, r2);
        int16x4_t low_diff = vget_low_s16(diff);
        float32x4_t diff_f = vcvtq_f32_s32(vmovl_s16(low_diff));
        sum_vec = vmlaq_f32(sum_vec, diff_f, diff_f);
    }

    float sum = vgetq_lane_f32(sum_vec, 0) + vgetq_lane_f32(sum_vec, 1) + 
                vgetq_lane_f32(sum_vec, 2) + vgetq_lane_f32(sum_vec, 3);

    env->ReleaseByteArrayElements(v1, data1, JNI_ABORT);
    env->ReleaseByteArrayElements(v2, data2, JNI_ABORT);
    return std::sqrt(sum);
}

} // extern "C"
