package com.meaning.app.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.NarrativeKernel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class NarrativeKernelBenchmark {
    
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    private lateinit var database: NarrativeDatabase
    private lateinit var kernel: NarrativeKernel
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = NarrativeDatabase.getInstance(context)
        kernel = NarrativeKernel(database.dao())
        
        // Inicializáljuk a tesztadatokkal
        runBlocking(Dispatchers.IO) {
            // Adatok betöltése, ha üres az adatbázis
            if (database.dao().getCount() == 0L) {
                // Tesztadatok generálása
            }
        }
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun benchmarkFindNearest() = runBlocking {
        val queryVector = FloatArray(128) { Random.nextFloat() * 2 - 1 }
        
        benchmarkRule.measureRepeated {
            kernel.findNearest(queryVector, k = 10, minSimilarity = 0.5f)
        }
    }
    
    @Test
    fun benchmarkGenerate3DMap() = runBlocking {
        benchmarkRule.measureRepeated {
            kernel.generate3DMap(maxPoints = 100)
        }
    }
    
    @Test
    fun benchmarkRealTimeStream() = runBlocking {
        benchmarkRule.measureRepeated {
            var count = 0
            kernel.observeNarrativeSpace().collect {
                if (++count >= 10) {
                    return@collect
                }
            }
        }
    }
}
