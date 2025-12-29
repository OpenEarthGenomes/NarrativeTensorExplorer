package com.meaning.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    // Coroutine Scope a háttérben futó feladatokhoz, hogy ne akassza meg a 120Hz-es UI-t
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Állapotjelzők a felhasználói felülethez
        var statusMessage by mutableStateOf("Rendszer inicializálása...")
        var isReady by mutableStateOf(false)
        var isError by mutableStateOf(false)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Narrative Tensor Explorer",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isError) MaterialTheme.colorScheme.error 
                                    else if (isReady) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.secondary
                        )
                        
                        if (!isReady && !isError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        // A motor betöltése Dispatchers.IO-n (háttérszálon)
        activityScope.launch {
            statusMessage = "C++ Kernel betöltése (Exynos 1380 fix)..."
            
            val result = withContext(Dispatchers.IO) {
                try {
                    // Itt történik a natív könyvtár betöltése
                    System.loadLibrary("meaning_kernel")
                    "Siker: A motor aktív!"
                } catch (e: Throwable) {
                    Log.e("MeaningApp", "Betöltési hiba", e)
                    isError = true
                    "Hiba a betöltésnél: ${e.localizedMessage}"
                }
            }
            
            delay(800) // Pici késleltetés, hogy a szemünk is lássa a folyamatot
            statusMessage = result
            if (!isError) isReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Megállítjuk a háttérszálat, ha bezárják az appot
    }
}
