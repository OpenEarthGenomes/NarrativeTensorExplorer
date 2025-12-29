#include <jni.h>
#include <arm_neon.h>
#include <cmath>
#include <vector>

extern "C" JNIEXPORT jfloat JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_calculateVectorDistance(
    JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* data1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* data2 = env->GetByteArrayElements(v2, nullptr);
    jsize len = env->GetArrayLength(v1);

    float32x4_t sum_vec = vdupq_n_f32(0.0f);
    
    // Biztonságos NEON ciklus
    for (int i = 0; i <= len - 4; i += 4) {
        int8x8_t r1 = vld1_s8(reinterpret_cast<const int8_t*>(data1 + i));
        int8x8_t r2 = vld1_s8(reinterpret_cast<const int8_t*>(data2 + i));
        
        int16x8_t diff = vsubl_s8(r1, r2);
        float32x4_t diff_f = vcvtq_f32_s32(vmovl_s16(vget_low_s16(diff)));
        sum_vec = vmlaq_f32(sum_vec, diff_f, diff_f);
    }

    float sum = vgetq_lane_f32(sum_vec, 0) + vgetq_lane_f32(sum_vec, 1) + 
                vgetq_lane_f32(sum_vec, 2) + vgetq_lane_f32(sum_vec, 3);

    env->ReleaseByteArrayElements(v1, data1, JNI_ABORT);
    env->ReleaseByteArrayElements(v2, data2, JNI_ABORT);

    return std::sqrt(sum);
}

// ... a többi függvény marad az eredeti struktúrában
