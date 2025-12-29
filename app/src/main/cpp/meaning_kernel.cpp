#include <jni.h>
#include <arm_neon.h>

extern "C" {

JNIEXPORT jint JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_fastNeonSimilarity(
    JNIEnv* env, jobject thiz, jbyteArray v1, jbyteArray v2) {
    
    jbyte* pV1 = env->GetByteArrayElements(v1, nullptr);
    jbyte* pV2 = env->GetByteArrayElements(v2, nullptr);
    jint length = env->GetArrayLength(v1);
    
    int32_t dotProduct = 0;
    int i = 0;
    for (; i <= length - 16; i += 16) {
        int8x16_t vec1 = vld1q_s8(pV1 + i);
        int8x16_t vec2 = vld1q_s8(pV2 + i);
        int16x8_t prod_low = vmull_s8(vget_low_s8(vec1), vget_low_s8(vec2));
        int16x8_t prod_high = vmull_s8(vget_high_s8(vec1), vget_high_s8(vec2));
        int32x4_t sum = vaddq_s32(vaddl_s16(vget_low_s16(prod_low), vget_high_s16(prod_low)),
                                  vaddl_s16(vget_low_s16(prod_high), vget_high_s16(prod_high)));
        dotProduct += vaddvq_s32(sum);
    }
    for (; i < length; i++) { dotProduct += pV1[i] * pV2[i]; }
    
    env->ReleaseByteArrayElements(v1, pV1, JNI_ABORT);
    env->ReleaseByteArrayElements(v2, pV2, JNI_ABORT);
    return dotProduct;
}

JNIEXPORT void JNICALL
Java_com_meaning_app_kernel_QuantizationEngine_transform3DCoordinates(
    JNIEnv* env, jobject thiz, jfloatArray points, jint pointCount, jfloatArray matrix) {
    
    jfloat* p = env->GetFloatArrayElements(points, nullptr);
    jfloat* m = env->GetFloatArrayElements(matrix, nullptr);
    float32x4_t c0 = vld1q_f32(m), c1 = vld1q_f32(m+4), c2 = vld1q_f32(m+8), c3 = vld1q_f32(m+12);
    
    for (int i = 0; i < pointCount * 3; i += 3) {
        float32x4_t res = vmulq_n_f32(c0, p[i]);
        res = vmlaq_n_f32(res, c1, p[i+1]);
        res = vmlaq_n_f32(res, c2, p[i+2]);
        res = vaddq_f32(res, c3);
        p[i] = vgetq_lane_f32(res, 0); p[i+1] = vgetq_lane_f32(res, 1); p[i+2] = vgetq_lane_f32(res, 2);
    }
    env->ReleaseFloatArrayElements(points, p, 0);
    env->ReleaseFloatArrayElements(matrix, m, JNI_ABORT);
}

}
