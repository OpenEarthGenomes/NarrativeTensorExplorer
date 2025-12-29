package com.meaning.app.kernel

class QuantizationEngine {
    init {
        try {
            System.loadLibrary("meaning_kernel")
        } catch (e: UnsatisfiedLinkError) {
            // Logolás, ha mégis hiba lenne
        }
    }

    external fun calculateVectorDistance(v1: ByteArray, v2: ByteArray): Float
    // ... további deklarációk
}
