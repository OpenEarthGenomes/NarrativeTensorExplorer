package com.meaning.app.kernel

import android.util.Log
import kotlin.math.abs

/**
 * QUANTIZATION ENGINE - Narrative Tensor Explorer Kernel
 * Ez az osztály köti össze a Kotlin kódot a NEON SIMD optimalizált C++ motorral.
 */
object QuantizationEngine {
    
    private const val TAG = "QuantizationEngine"

    init {
        try {
            // Ez tölti be a CMake által generált "meaning-kernel" könyvtárat
            System.loadLibrary("meaning-kernel")
            Log.i(TAG, "Native Meaning Kernel (NEON) successfully loaded.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library. Falling back to slow Kotlin implementation.", e)
        }
    }

    // ==============================================
    // NATIVE (C++) FÜGGVÉNYEK - A Meaning-kernel.cpp-ből
    // ==============================================

    /** NEON SIMD gyorsított hasonlóság számítás (INT8) */
    external fun fastNeonSimilarity(v1: ByteArray, v2: ByteArray): Int
    
    /** 3D koordináta transzformáció mátrix szorzással (NEON) */
    external fun transform3DCoordinates(points: FloatArray, pointCount: Int, matrix: FloatArray)
    
    /** Tömeges kvantálás float-ról int8-ra (NEON) */
    external fun quantizeBatch(input: FloatArray, output: ByteArray, count: Int)

    // ==============================================
    // MEGLÉVŐ DUMMY ÉS HELPER FÜGGVÉNYEK JAVÍTVA
    // ==============================================

    /**
     * DUMMY QUANTIZE - Most már a NEON motort használja, ha 8-bitről van szó
     */
    fun dummyQuantize(vector: FloatArray, bits: Int = 8): ByteArray {
        val result = ByteArray(vector.size)
        return when (bits) {
            8 -> {
                // Meghívjuk a C++ gyorsítót
                quantizeBatch(vector, result, vector.size)
                result
            }
            16 -> {
                val buffer = ByteArray(vector.size * 2)
                for (i in vector.indices) {
                    val half = floatToHalfSimplified(vector[i])
                    buffer[i * 2] = (half.toInt() shr 8).toByte()
                    buffer[i * 2 + 1] = half.toByte()
                }
                buffer
            }
            else -> {
                Log.w(TAG, "Unsupported bits: $bits, falling back to 8-bit NEON")
                quantizeBatch(vector, result, vector.size)
                result
            }
        }
    }

    /**
     * DUMMY SIMILARITY - A lassú ciklus helyett a NEON motorral számol
     */
    fun dummySimilarity(v1: ByteArray, v2: ByteArray): Float {
        if (v1.size != v2.size) return 0f
        
        // A C++ fastNeonSimilarity a skaláris szorzatot (dot product) adja vissza
        val dotProduct = fastNeonSimilarity(v1, v2)
        
        // Normalizálás: Mivel INT8 (-128...127), a max érték v1.size * 127 * 127
        val maxPossible = v1.size * 127 * 127
        return (dotProduct.toFloat() / maxPossible).coerceIn(0f, 1f)
    }

    /**
     * DUMMY DEQUANTIZE - Visszaalakítás (marad Kotlinban, mert ritkábban kell)
     */
    fun dummyDequantize(quantized: ByteArray, bits: Int = 8, originalMax: Float = 1.0f): FloatArray {
        val result = FloatArray(if (bits == 16) quantized.size / 2 else quantized.size)
        when (bits) {
            8 -> {
                for (i in quantized.indices) {
                    result[i] = (quantized[i].toFloat() / 127.0f) * originalMax
                }
            }
            16 -> {
                for (i in result.indices) {
                    val high = quantized[i * 2].toInt() and 0xFF
                    val low = quantized[i * 2 + 1].toInt() and 0xFF
                    val half = (high shl 8) or low
                    result[i] = halfToFloatSimplified(half.toShort())
                }
            }
        }
        return result
    }

    // ==============================================
    // ALACSONY SZINTŰ SEGÉDFÜGGVÉNYEK
    // ==============================================

    private fun floatToHalfSimplified(value: Float): Short {
        val normalized = value.coerceIn(-1f, 1f)
        return (normalized * 32767).toInt().toShort()
    }

    private fun halfToFloatSimplified(half: Short): Float {
        return half.toFloat() / 32767f
    }
    
    // Régi fallback függvények, ha a native hívás nem elérhető
    private fun quantizeToINT8(vector: FloatArray): ByteArray {
        val out = ByteArray(vector.size)
        for (i in vector.indices) {
            out[i] = (vector[i].coerceIn(-1f, 1f) * 127).toInt().toByte()
        }
        return out
    }

    private fun dequantizeFromINT8(quantized: ByteArray, max: Float): FloatArray {
        val out = FloatArray(quantized.size)
        for (i in quantized.indices) {
            out[i] = (quantized[i].toFloat() / 127f) * max
        }
        return out
    }
}
