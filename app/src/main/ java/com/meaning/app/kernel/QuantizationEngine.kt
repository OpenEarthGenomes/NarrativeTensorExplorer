package com.meaning.app.kernel

/**
 * Ez az osztály felelős a C++ (JNI) hívásokért.
 * Itt dől el, hogy a processzor NEON egységeit használjuk-e.
 */
class QuantizationEngine {

    init {
        // Betöltjük a lefordított C++ könyvtárat
        System.loadLibrary("meaning-kernel")
    }

    /**
     * Kiszámítja két vektor távolságát NEON SIMD optimalizálással.
     */
    external fun calculateVectorDistance(v1: ByteArray, v2: ByteArray): Float

    /**
     * 3D koordináták transzformálása (forgatás, skálázás) mátrixszal.
     */
    external fun transform3DCoordinates(points: FloatArray, matrix: FloatArray, pointCount: Int)

    /**
     * Tenzió feldolgozás és normalizálás.
     */
    external fun processTensor(input: FloatArray, rows: Int, cols: Int): FloatArray

    /**
     * Segédfüggvény a 3D transzformáció kényelmesebb használatához.
     */
    fun rotatePoints(pointCloud: PointCloud, rotationMatrix: FloatArray) {
        transform3DCoordinates(
            pointCloud.points, 
            rotationMatrix, 
            pointCloud.capacity
        )
    }
}
