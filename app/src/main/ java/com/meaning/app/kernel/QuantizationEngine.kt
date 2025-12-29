package com.meaning.app.kernel
class QuantizationEngine {
    companion object {
        init { System.loadLibrary("meaning_kernel") }
    }
    external fun fastNeonSimilarity(v1: ByteArray, v2: ByteArray): Int
    external fun transform3DCoordinates(points: FloatArray, pointCount: Int, matrix: FloatArray)
}

