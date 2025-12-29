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
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    // Native híd deklarációja
    private external fun checkKernelStatus(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var statusMessage by mutableStateOf("Rendszer inicializálása...")
        var isReady by mutableStateOf(false)
        var isError by mutableStateOf(false)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
                        Text(text = "Narrative Tensor Explorer", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        if (!isReady && !isError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        activityScope.launch {
            statusMessage = "C++ Kernel betöltése..."
            val result = withContext(Dispatchers.IO) {
                try {
                    System.loadLibrary("meaning_kernel")
                    checkKernelStatus() 
                } catch (e: Throwable) {
                    Log.e("MeaningApp", "Hiba", e)
                    "Betöltési hiba: ${e.message}"
                }
            }
            delay(1000)
            statusMessage = result
            if (result.contains("OK")) isReady = true else isError = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
