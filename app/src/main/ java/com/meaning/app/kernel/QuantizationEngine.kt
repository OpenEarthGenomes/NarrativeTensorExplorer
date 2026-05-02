package com.meaning.app.kernel

class QuantizationEngine {
    companion object {
        init {
            System.loadLibrary("meaning_kernel")
        }
        
        // JNI hívások a C++ felé
        @JvmStatic
        external fun fastNeonSimilarity(v1: ByteArray, v2: ByteArray): Int
        
        @JvmStatic
        external fun transform3DCoordinates(points: FloatArray, pointCount: Int, matrix: FloatArray)
    }
}
