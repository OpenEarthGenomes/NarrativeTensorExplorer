package com.meaning.app.kernel

import android.util.Log

class QuantizationEngine {

    companion object {
        private const val TAG = "QuantizationEngine"
        
        init {
            try {
                // Fontos: alsóvonallal, ahogy a CMake-ben neveztük el
                System.loadLibrary("meaning_kernel")
                Log.d(TAG, "Native library 'meaning_kernel' betöltve.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "HIBA: Nem sikerült betölteni a könyvtárat: ${e.message}")
            }
        }
    }

    /**
     * Két kvantált vektor távolságát számolja ki NEON gyorsítással.
     */
    external fun calculateVectorDistance(v1: ByteArray, v2: ByteArray): Float

    /**
     * 3D koordináták transzformálása (forgatás/mozgatás) a kernelben.
     */
    external fun transform3DCoordinates(points: FloatArray, matrix: FloatArray, pointCount: Int)

    /**
     * Tenzor adatok feldolgozása.
     */
    external fun processTensor(input: FloatArray, rows: Int, cols: Int): FloatArray
}
