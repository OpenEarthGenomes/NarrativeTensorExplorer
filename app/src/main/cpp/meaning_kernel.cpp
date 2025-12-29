#include <jni.h>
#include <arm_neon.h>
#include <android/log.h>

#define LOG_TAG "MeaningKernel"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

// 1. KVANTÁLT HASONLÓSÁG - NEON SIMD (Fixált név!)
JNIEXPORT jint JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_fastNeonSimilarity(
    JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* pV1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* pV2 = env->GetByteArrayElements(v2, nullptr);
    jint length = env->GetArrayLength(v1);
    
    int32_t dotProduct = 0;
    int i = 0;
    
    // NEON SIMD - 16 byte egyszerre
    for (; i <= length - 16; i += 16) {
        int8x16_t vec1 = vld1q_s8(pV1 + i);
        int8x16_t vec2 = vld1q_s8(pV2 + i);
        
        int16x8_t prod_low = vmull_s8(vget_low_s8(vec1), vget_low_s8(vec2));
        int16x8_t prod_high = vmull_s8(vget_high_s8(vec1), vget_high_s8(vec2));
        
        int32x4_t sum_low = vaddl_s16(vget_low_s16(prod_low), vget_high_s16(prod_low));
        int32x4_t sum_high = vaddl_s16(vget_low_s16(prod_high), vget_high_s16(prod_high));
        
        int32x4_t final_sum = vaddq_s32(sum_low, sum_high);
        dotProduct += vaddvq_s32(final_sum);
    }
    
    for (; i < length; i++) {
        dotProduct += pV1[i] * pV2[i];
    }
    
    env->ReleaseByteArrayElements(v1, pV1, JNI_ABORT);
    env->ReleaseByteArrayElements(v2, pV2, JNI_ABORT);
    
    return dotProduct;
}

// 2. 3D KOORDINÁTA TRANSZFORMÁCIÓ (Fixált név!)
JNIEXPORT void JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_transform3DCoordinates(
    JNIEnv* env, jobject thiz,
    jfloatArray points, jint pointCount,
    jfloatArray matrix) {
    
    jfloat* pointsPtr = env->GetFloatArrayElements(points, nullptr);
    jfloat* matrixPtr = env->GetFloatArrayElements(matrix, nullptr);
    
    float32x4_t col0 = vld1q_f32(matrixPtr);
    float32x4_t col1 = vld1q_f32(matrixPtr + 4);
    float32x4_t col2 = vld1q_f32(matrixPtr + 8);
    float32x4_t col3 = vld1q_f32(matrixPtr + 12);
    
    for (int i = 0; i < pointCount * 3; i += 3) {
        float32x4_t point = {pointsPtr[i], pointsPtr[i+1], pointsPtr[i+2], 1.0f};
        
        float32x4_t result;
        result = vmulq_n_f32(col0, vgetq_lane_f32(point, 0));
        result = vmlaq_n_f32(result, col1, vgetq_lane_f32(point, 1));
        result = vmlaq_n_f32(result, col2, vgetq_lane_f32(point, 2));
        result = vaddq_f32(result, col3);
        
        pointsPtr[i] = vgetq_lane_f32(result, 0);
        pointsPtr[i+1] = vgetq_lane_f32(result, 1);
        pointsPtr[i+2] = vgetq_lane_f32(result, 2);
    }
    
    env->ReleaseFloatArrayElements(points, pointsPtr, 0);
    env->ReleaseFloatArrayElements(matrix, matrixPtr, JNI_ABORT);
}

// 3. KVANTÁLÁS TÖMEGESEN (Fixált név!)
JNIEXPORT void JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_quantizeBatch(
    JNIEnv* env, jobject thiz,
    jfloatArray input, jbyteArray output, jint count) {
    
    jfloat* inputPtr = env->GetFloatArrayElements(input, nullptr);
    jbyte* outputPtr = env->GetByteArrayElements(output, nullptr);
    
    const float scale = 127.0f;
    
    for (int i = 0; i < count; i += 16) {
        float32x4_t vec1 = vld1q_f32(inputPtr + i);
        float32x4_t vec2 = vld1q_f32(inputPtr + i + 4);
        float32x4_t vec3 = vld1q_f32(inputPtr + i + 8);
        float32x4_t vec4 = vld1q_f32(inputPtr + i + 12);
        
        vec1 = vmulq_n_f32(vec1, scale);
        vec2 = vmulq_n_f32(vec2, scale);
        vec3 = vmulq_n_f32(vec3, scale);
        vec4 = vmulq_n_f32(vec4, scale);
        
        int32x4_t int1 = vcvtq_s32_f32(vec1);
        int32x4_t int2 = vcvtq_s32_f32(vec2);
        int32x4_t int3 = vcvtq_s32_f32(vec3);
        int32x4_t int4 = vcvtq_s32_f32(vec4);
        
        int16x8_t short1 = vcombine_s16(vmovn_s32(int1), vmovn_s32(int2));
        int16x8_t short2 = vcombine_s16(vmovn_s32(int3), vmovn_s32(int4));
        int8x16_t byteVec = vcombine_s8(vmovn_s16(short1), vmovn_s16(short2));
        
        vst1q_s8((int8_t*)(outputPtr + i), byteVec);
    }
    
    env->ReleaseFloatArrayElements(input, inputPtr, JNI_ABORT);
    env->ReleaseByteArrayElements(output, outputPtr, 0);
}

}
