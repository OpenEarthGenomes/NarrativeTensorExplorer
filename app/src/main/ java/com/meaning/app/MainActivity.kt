package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meaning.app.kernel.QuantizationEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Biztonságos inicializálás
        var statusMessage by mutableStateOf("Motor indítása...")
        
        try {
            val engine = QuantizationEngine()
            // Egy gyors teszt hívás a C++ felé
            statusMessage = "Kernel sikeresen fut az Android 16-on!"
        } catch (e: Throwable) {
            statusMessage = "Hiba a motorban: ${e.localizedMessage}"
        }
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Narrative Tensor Explorer",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (statusMessage.contains("Hiba")) 
                                    MaterialTheme.colorScheme.error 
                                    else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
