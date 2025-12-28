package com.meaning.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@LargeTest
@RunWith(AndroidJUnit4::class)
class ComposeUIBenchmark {
    
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun benchmarkAppStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(FrameTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None()
        ) {
            pressHome()
            startActivityAndWait()
            
            // Várunk, hogy a UI teljesen betöltődjön
            device.wait(Until.hasObject(By.res("com.meaning.app:id/main_container")), 5000)
        }
    }
    
    @Test
    fun benchmark3DMapNavigation() {
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.None()
        ) {
            // Indítás
            startActivityAndWait()
            
            // 3D tér navigáció szimulálása
            repeat(10) {
                // Pinch zoom
                val centerX = device.displayWidth / 2
                val centerY = device.displayHeight / 2
                device.performTwoPointerGesture(
                    PointF(centerX - 100, centerY),
                    PointF(centerX + 100, centerY),
                    PointF(centerX - 50, centerY),
                    PointF(centerX + 50, centerY),
                    1000
                )
                
                // Drag rotation
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight / 2,
                    device.displayWidth / 2 + 200,
                    device.displayHeight / 2,
                    10
                )
            }
        }
    }
    
    @Test
    fun benchmarkSearchFlow() {
        benchmarkRule.measureRepeated(
            packageName = "com.meaning.app",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.None()
        ) {
            startActivityAndWait()
            
            // Keresés indítása
            val searchButton = device.findObject(By.res("com.meaning.app:id/search_button"))
            searchButton.click()
            
            // Keresési kifejezés beírása
            val searchInput = device.findObject(By.res("com.meaning.app:id/search_input"))
            searchInput.text = "tenger"
            
            // Enter
            device.pressEnter()
            
            // Várakozás eredményekre
            device.wait(Until.hasObject(By.res("com.meaning.app:id/search_results")), 3000)
            
            // Eredmények görgetése
            val resultsContainer = device.findObject(By.res("com.meaning.app:id/results_container"))
            resultsContainer.fling(Direction.DOWN)
        }
    }
}
