#include <jni.h>
#include <arm_neon.h>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "MeaningKernel"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jfloat JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_calculateVectorDistance(
    JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* data1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* data2 = env->GetByteArrayElements(v2, nullptr);
    jsize len = env->GetArrayLength(v1);

    // NEON regiszter inicializálása nullákkal
    float32x4_t sum_vec = vdupq_n_f32(0.0f);
    
    // 4 elem feldolgozása egyszerre
    for (int i = 0; i <= len - 4; i += 4) {
        int8x8_t r1 = vld1_s8(reinterpret_cast<const int8_t*>(data1 + i));
        int8x8_t r2 = vld1_s8(reinterpret_cast<const int8_t*>(data2 + i));
        
        int16x8_t diff = vsubl_s8(r1, r2);
        int16x4_t low_diff = vget_low_s16(diff);
        float32x4_t diff_f = vcvtq_f32_s32(vmovl_s16(low_diff));
        sum_vec = vmlaq_f32(sum_vec, diff_f, diff_f);
    }

    // Eredmények összegzése a regiszterből
    float sum = vgetq_lane_f32(sum_vec, 0) + vgetq_lane_f32(sum_vec, 1) + 
                vgetq_lane_f32(sum_vec, 2) + vgetq_lane_f32(sum_vec, 3);

    env->ReleaseByteArrayElements(v1, data1, JNI_ABORT);
    env->ReleaseByteArrayElements(v2, data2, JNI_ABORT);

    return std::sqrt(sum);
}

extern "C" JNIEXPORT void JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_transform3DCoordinates(
    JNIEnv* env, jobject thiz, jfloatArray points, jfloatArray matrix, jint pointCount) {
    
    jfloat* p = env->GetFloatArrayElements(points, nullptr);
    jfloat* m = env->GetFloatArrayElements(matrix, nullptr);

    for (int i = 0; i < pointCount * 3; i += 3) {
        float x = p[i];
        float y = p[i+1];
        float z = p[i+2];

        p[i]   = x * m[0] + y * m[1] + z * m[2] + m[3];
        p[i+1] = x * m[4] + y * m[5] + z * m[6] + m[7];
        p[i+2] = x * m[8] + y * m[9] + z * m[10] + m[11];
    }

    env->ReleaseFloatArrayElements(points, p, 0);
    env->ReleaseFloatArrayElements(matrix, m, JNI_ABORT);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_processTensor(
    JNIEnv* env, jobject thiz, jfloatArray input, jint rows, jint cols) {
    
    jfloat* data = env->GetFloatArrayElements(input, nullptr);
    jfloatArray result = env->NewFloatArray(rows);
    std::vector<float> rowSums(rows, 0.0f);

    for (int i = 0; i < rows; ++i) {
        float row_sum = 0.0f;
        for (int j = 0; j < cols; ++j) {
            row_sum += data[i * cols + j];
        }
        rowSums[i] = row_sum;
    }

    env->SetFloatArrayRegion(result, 0, rows, rowSums.data());
    env->ReleaseFloatArrayElements(input, data, JNI_ABORT);
    return result;
}
