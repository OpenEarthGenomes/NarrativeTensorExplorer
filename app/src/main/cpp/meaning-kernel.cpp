#include <jni.h>
#include <arm_neon.h>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "MeaningKernel"

extern "C" JNIEXPORT jfloat JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_calculateVectorDistance(
    JNIEnv* env, [[maybe_unused]] jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* data1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* data2 = env->GetByteArrayElements(v2, nullptr);
    jsize len = env->GetArrayLength(v1);

    float32x4_t sum_vec = vdupq_n_f32(0.0f);
    
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

extern "C" JNIEXPORT void JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_transform3DCoordinates(
    JNIEnv* env, [[maybe_unused]] jobject thiz,
    jfloatArray points, jfloatArray matrix, jint pointCount) {
    
    jfloat* p = env->GetFloatArrayElements(points, nullptr);
    jfloat* m = env->GetFloatArrayElements(matrix, nullptr);

    float32x4_t m0 = vld1q_f32(m);     // Mátrix 1. sor
    float32x4_t m1 = vld1q_f32(m + 4); // Mátrix 2. sor
    float32x4_t m2 = vld1q_f32(m + 8); // Mátrix 3. sor

    for (int i = 0; i < pointCount * 3; i += 3) {
        // Vektor betöltése [x, y, z, 1.0]
        float32x4_t v = {p[i], p[i+1], p[i+2], 1.0f};
        
        // Kompatibilis mátrix szorzás vdotq_f32 nélkül
        float32x4_t r0 = vmulq_f32(v, m0);
        float32x4_t r1 = vmulq_f32(v, m1);
        float32x4_t r2 = vmulq_f32(v, m2);

        p[i]   = vgetq_lane_f32(r0, 0) + vgetq_lane_f32(r0, 1) + vgetq_lane_f32(r0, 2) + vgetq_lane_f32(r0, 3);
        p[i+1] = vgetq_lane_f32(r1, 0) + vgetq_lane_f32(r1, 1) + vgetq_lane_f32(r1, 2) + vgetq_lane_f32(r1, 3);
        p[i+2] = vgetq_lane_f32(r2, 0) + vgetq_lane_f32(r2, 1) + vgetq_lane_f32(r2, 2) + vgetq_lane_f32(r2, 3);
    }

    env->ReleaseFloatArrayElements(points, p, 0);
    env->ReleaseFloatArrayElements(matrix, m, JNI_ABORT);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_processTensor(
    JNIEnv* env, [[maybe_unused]] jobject thiz,
    jfloatArray input, jint rows, jint cols) {
    
    jfloat* data = env->GetFloatArrayElements(input, nullptr);
    jfloatArray result = env->NewFloatArray(rows);
    std::vector<float> rowSums(rows, 0.0f);

    for (int i = 0; i < rows; ++i) {
        float32x4_t sum_v = vdupq_n_f32(0.0f);
        for (int j = 0; j <= cols - 4; j += 4) {
            sum_v = vaddq_f32(sum_v, vld1q_f32(data + i * cols + j));
        }
        rowSums[i] = vgetq_lane_f32(sum_v, 0) + vgetq_lane_f32(sum_v, 1) + 
                     vgetq_lane_f32(sum_v, 2) + vgetq_lane_f32(sum_v, 3);
    }

    env->SetFloatArrayRegion(result, 0, rows, rowSums.data());
    env->ReleaseFloatArrayElements(input, data, JNI_ABORT);
    return result;
}
