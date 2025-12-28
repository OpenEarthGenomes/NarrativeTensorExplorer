package com.meaning.app.db

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 4)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    this.forEach { buffer.putFloat(it) }
    return buffer.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    val result = FloatArray(this.size / 4)
    for (i in result.indices) {
        result[i] = buffer.float
    }
    return result
}

fun FloatArray.normalize(): FloatArray {
    val max = this.maxOrNull() ?: 1f
    val min = this.minOrNull() ?: 0f
    val range = max - min
    return if (range == 0f) this else FloatArray(this.size) { (this[it] - min) / range }
}

fun FloatArray.dot(other: FloatArray): Float {
    require(this.size == other.size) { "Vektorok mérete nem egyezik" }
    var sum = 0f
    for (i in indices) {
        sum += this[i] * other[i]
    }
    return sum
}

fun FloatArray.magnitude(): Float {
    return sqrt(this.fold(0f) { acc, value -> acc + value * value })
}

fun FloatArray.cosineSimilarity(other: FloatArray): Float {
    val dot = this.dot(other)
    val mag1 = this.magnitude()
    val mag2 = other.magnitude()
    return if (mag1 > 0 && mag2 > 0) dot / (mag1 * mag2) else 0f
}

fun List<FloatArray>.mean(): FloatArray {
    if (isEmpty()) return FloatArray(0)
    val result = FloatArray(this[0].size)
    for (vector in this) {
        for (i in vector.indices) {
            result[i] += vector[i]
        }
    }
    for (i in result.indices) {
        result[i] /= this.size
    }
    return result
}
