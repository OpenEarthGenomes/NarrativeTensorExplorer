package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("Narrative Tensor: Motor indítása...", modifier = Modifier.padding(20.dp))
                }
            }
        }

        // Külön szálra rakjuk a betöltést, hogy ne akassza meg a rendszert
        CoroutineScope(Dispatchers.IO).launch {
            try {
                System.loadLibrary("meaning_kernel")
                // Ha sikerül, ide tehetünk majd egy logot
            } catch (e: Throwable) {
                // Ha hiba van, elkapjuk, így nem omlik össze az app indításkor
            }
        }
    }
}
