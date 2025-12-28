package com.meaning.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.meaning.app.kernel.QuantizationEngine
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@LargeTest
@RunWith(AndroidJUnit4::class)
class QuantizationBenchmark {
    
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    private fun generateRandomVectors(count: Int, dimensions: Int): List<FloatArray> {
        return List(count) {
            FloatArray(dimensions) { Random.nextFloat() * 2 - 1 }
        }
    }
    
    @Test
    fun benchmarkQuantizationToINT8() {
        val vectors = generateRandomVectors(1000, 128)
        
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None()
        ) {
            vectors.forEach { vector ->
                QuantizationEngine.quantizeToINT8(vector)
            }
        }
    }
    
    @Test
    fun benchmarkNeonSimilarity() {
        val vectors = generateRandomVectors(100, 128)
        val quantizedVectors = vectors.map { QuantizationEngine.quantizeToINT8(it) }
        
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None()
        ) {
            val query = quantizedVectors.first()
            quantizedVectors.forEach { vector ->
                QuantizationEngine.calculateSimilarity(query, vector)
            }
        }
    }
    
    @Test
    fun benchmarkVectorSearch() {
        val vectors = generateRandomVectors(10000, 128)
        val queryVector = vectors.first()
        
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None()
        ) {
            val quantizedQuery = QuantizationEngine.quantizeToINT8(queryVector)
            val results = vectors
                .map { QuantizationEngine.quantizeToINT8(it) }
                .map { vector -> QuantizationEngine.calculateSimilarity(quantizedQuery, vector) }
                .sortedDescending()
                .take(10)
        }
    }
}
